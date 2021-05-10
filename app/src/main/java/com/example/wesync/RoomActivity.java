package com.example.wesync;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wesync.adapters.TracksAdapter;
import com.example.wesync.databinding.ActivityRoomBinding;
import com.example.wesync.models.Room;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.PlayerApi;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.client.ErrorCallback;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.Artist;
import com.spotify.protocol.types.Empty;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Repeat;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.PlaylistsPager;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class RoomActivity extends AppCompatActivity {


    private static final String TAG = "RoomActivity";

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ActivityRoomBinding binding;
    private GridView mGridView;
    private String mAccessToken;
    private ArrayList<Track> mTrackArrayList = new ArrayList<>();
    private SpotifyAppRemote mSpotifyAppRemote;
    private PlayerApi playerApi;
    //    private boolean isPlaying = false;
    private String currentTrackUri;
    private long duration;
    //    private int repeat = 0;
    private Room mRoom;
    private String currentUser;
    //    private long currentPosition;
    private boolean otherUserUpdateDone = false;

    private BottomSheetBehavior mBottomSheetBehavior;
    private boolean justJoined = true;

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();

        binding = ActivityRoomBinding.inflate(getLayoutInflater());
        View root = binding.getRoot();
        setContentView(root);


        SharedPreferences preferences = this.getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE);
        mAccessToken = preferences.getString(Constants.ACCESS_TOKEN, "");

        View.OnClickListener playPauseListener = view -> {
            Log.d(TAG, "onCreate: on click playpause : room : " + mRoom);
            if (mRoom.isPlaying()) {
                playerApi.pause().setResultCallback(empty -> {
                    pause(true);
                }).setErrorCallback(throwable -> Toast.makeText(RoomActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show());

            } else {
                if (currentTrackUri != null && !currentTrackUri.isEmpty())
                    playerApi.resume().setResultCallback(empty -> {
                        play(true);
                    }).setErrorCallback(new ErrorCallback() {
                        @Override
                        public void onError(Throwable throwable) {
                            Toast.makeText(RoomActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

            }
        };

        binding.share.setOnClickListener(view -> {
            PackageManager pm = RoomActivity.this.getPackageManager();
//                try {
//                    Intent waIntent = new Intent(Intent.ACTION_SEND);
//                    waIntent.setType("text/plain");
//                    String text = "YOUR TEXT HERE";
//                    PackageInfo info = pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
//                    waIntent.setPackage("com.whatsapp");
//                    waIntent.putExtra(Intent.EXTRA_TEXT, text);
//                    startActivity(Intent.createChooser(waIntent, "Share with"));

            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());

            String text = "Join my room on We-Sync. \nRoom ID : " + mRoom.getRoomId();
            Intent shareIntent;
            shareIntent = new
                    Intent(Intent.ACTION_SEND);
            shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            shareIntent.putExtra(Intent.EXTRA_TEXT, text);
            shareIntent.setType("text/*");
            startActivity(Intent.createChooser(shareIntent, "Share with"));
//                } catch (PackageManager.NameNotFoundException e) {
//                    Toast.makeText(RoomActivity.this, "WhatsApp not Installed", Toast.LENGTH_SHORT).show();
//                }

        });

        binding.sync.setOnClickListener(view -> updateDb(0));
        binding.bottomSheetSync.setOnClickListener(view -> updateDb(0));
        binding.bottomSheetPlayPause.setOnClickListener(playPauseListener);
        binding.playPause.setOnClickListener(playPauseListener);
        binding.closeBottomSheet.setOnClickListener(view -> mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED));
        binding.skipNext.setOnClickListener(view -> playerApi.skipNext().setResultCallback(empty -> updateDb(0)));

        binding.skipPrevious.setOnClickListener(view -> playerApi.skipPrevious().setResultCallback(empty -> updateDb(0)));

        binding.repeat.setOnClickListener(view -> {
            mRoom.setRepeat((mRoom.getRepeat() + 1) % 3);
            setRepeat(mRoom.getRepeat());
            updateDb(0);

        });

        binding.seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (playerApi == null) {
                    Toast.makeText(RoomActivity.this, "Spotify not connected yet!", Toast.LENGTH_SHORT).show();
                } else {
                    if (fromUser) {

                        long currentPosition = duration * progress / 100;
                        mRoom.setCurrentPosition(currentPosition);

                        playerApi.seekTo(currentPosition).setResultCallback(new CallResult.ResultCallback<Empty>() {
                            @Override
                            public void onResult(Empty empty) {
                                updateDb(0);
                            }
                        });
                        binding.start.setText(Constants.getTimeFormLong(currentPosition));
                        binding.end.setText(Constants.getTimeFormLong(duration - currentPosition));

                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
//                TODO pause while seeking!
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mBottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet);

        mBottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED)
                    binding.peekView.setVisibility(View.GONE);
                else if (newState == BottomSheetBehavior.STATE_COLLAPSED)
                    binding.peekView.setVisibility(View.VISIBLE);

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });


        binding.search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (!mAccessToken.isEmpty() && !charSequence.toString().isEmpty())
                    searchTrack(charSequence);

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


    }

    @Override
    public void onBackPressed() {
        if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else finish();
    }


    private void setRepeat(int repeat) {
        switch (repeat) {
            case Repeat.OFF:
                playerApi.setRepeat(Repeat.OFF).setResultCallback(empty -> {
//                    Toast.makeText(this, "Repeat off", Toast.LENGTH_SHORT).show();
                    binding.repeat.setImageResource(R.drawable.ic_repeat_off);
                });
                break;
            case Repeat.ONE:
                playerApi.setRepeat(Repeat.ONE).setResultCallback(empty -> {
//                    Toast.makeText(this, "Repeat one", Toast.LENGTH_SHORT).show();
                    binding.repeat.setImageResource(R.drawable.ic_repeat_one);
                });
                break;
            case Repeat.ALL:
                playerApi.setRepeat(Repeat.ALL).setResultCallback(empty -> {
//                    Toast.makeText(this, "Repeat all", Toast.LENGTH_SHORT).show();
                    binding.repeat.setImageResource(R.drawable.ic_repeat);
                });
                break;
            default:
        }
    }

    private void searchTrack(CharSequence charSequence) {
        final SpotifyApi api = new SpotifyApi();
        api.setAccessToken(mAccessToken);

        Log.d(TAG, "onTextChanged: token : " + mAccessToken);
        final SpotifyService spotify = api.getService();


//        spotify.searchPlaylists(charSequence.toString(), new Callback<PlaylistsPager>() {
//            @Override
//            public void success(PlaylistsPager playlistsPager, Response response) {
////                Log.d(TAG, "success: Playlists" + playlistsPager.playlists.items.toString());
////                TOdo remove this is required!
//                Log.d(TAG, "success: href : " + playlistsPager.playlists.items.get(0).external_urls.get("spotify"));
//                playerApi.play(playlistsPager.playlists.items.get(0).external_urls.get("spotify")).setResultCallback(new CallResult.ResultCallback<Empty>() {
//                    @Override
//                    public void onResult(Empty empty) {
//                        Toast.makeText(RoomActivity.this, "Playing", Toast.LENGTH_SHORT).show();
//                    }
//                });
//                Toast.makeText(RoomActivity.this, "GOT PLAYLIST", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void failure(RetrofitError error) {
//                Log.d(TAG, "failure: Playlist" + error.getMessage());
//            }
//        });

        spotify.searchTracks(charSequence.toString(), new Callback<TracksPager>() {
            @Override
            public void success(TracksPager tracksPager, Response response) {

                mTrackArrayList.clear();
                mTrackArrayList.addAll(tracksPager.tracks.items);

                if (mTrackArrayList.size() != 0) {
                    setGridView();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, "failure: error", error);
            }
        });
    }

    private void setGridView() {
        TracksAdapter adapter = new TracksAdapter(RoomActivity.this, mTrackArrayList);
        binding.tracksGrid.setAdapter(adapter);
        binding.tracksGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Toast.makeText(RoomActivity.this, mTrackArrayList.get(position).name, Toast.LENGTH_SHORT).show();
                final Track track = mTrackArrayList.get(position);


                playerApi.play(track.uri).setResultCallback(new CallResult.ResultCallback<Empty>() {
                    @Override
                    public void onResult(Empty empty) {
                        Log.d(TAG, "onResult: Playing");
                        mRoom.setPlaying(true);
                        currentTrackUri = track.uri;
                        binding.trackTextView.setText(track.name);
                        hideKeyboard(RoomActivity.this);
                        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        updateDb(0);
                    }
                }).setErrorCallback(new ErrorCallback() {
                    @Override
                    public void onError(Throwable throwable) {

                    }
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        Toast.makeText(this, "Connecting, please wait!", Toast.LENGTH_SHORT).show();
        Intent intent = getIntent();

        Gson gson = new Gson();
        mRoom = gson.fromJson(intent.getStringExtra(Constants.ROOM), new TypeToken<Room>() {
        }.getType());
        currentUser = intent.getStringExtra(Constants.USERNAME);


        Log.d(TAG, "onStart: room : " + mRoom.toString());
        assert mRoom != null;
        binding.roomId.setText(mRoom.getRoomId());

        ConnectionParams connectionParams =
                new ConnectionParams.Builder(Constants.CLIENT_ID)
                        .setRedirectUri(Constants.REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        SpotifyAppRemote.connect(this, connectionParams,
                new Connector.ConnectionListener() {

                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {

                        Log.d("MainActivity", "Connected! Yay!");
                        Toast.makeText(RoomActivity.this, "CONNECTED", Toast.LENGTH_SHORT).show();
                        mSpotifyAppRemote = spotifyAppRemote;
                        playerApi = mSpotifyAppRemote.getPlayerApi();

                        // Now you can start interacting with App Remote

                        playerApi.subscribeToPlayerState().setEventCallback(new Subscription.EventCallback<PlayerState>() {
                            @Override
                            public void onEvent(PlayerState playerState) {

                                final com.spotify.protocol.types.Track track = playerState.track;
                                if (track != null) {

                                    duration = track.duration;
                                    binding.trackTextView.setText(track.name);
                                    binding.title.setText(track.name);

                                    List<Artist> artists = track.artists;

                                    StringBuilder builder = new StringBuilder();
                                    for (Artist artist : artists) {
                                        builder.append(artist.name);
                                        builder.append(", ");
                                    }

                                    builder.delete(builder.lastIndexOf(","), builder.lastIndexOf(",") + 1);


                                    binding.artists.setText(builder);
                                    binding.artists.setSelected(true);
                                    mSpotifyAppRemote.getImagesApi().getImage(track.imageUri).setResultCallback(bitmap -> binding.trackImage.setImageBitmap(bitmap));


                                    currentTrackUri = playerState.track.uri;
                                    if (playerState.isPaused)
                                        pause(false);
                                    else
                                        play(false);


                                    Handler handler = new Handler();

                                    final Runnable r = new Runnable() {
                                        public void run() {
                                            playerApi.getPlayerState().setResultCallback(new CallResult.ResultCallback<PlayerState>() {
                                                @Override
                                                public void onResult(PlayerState playerState) {
                                                    mRoom.setCurrentPosition(playerState.playbackPosition);
                                                    binding.seekbar.setProgress((int) ((double) playerState.playbackPosition / (double) duration * 100));
                                                    binding.start.setText(Constants.getTimeFormLong(playerState.playbackPosition));
                                                    binding.end.setText(Constants.getTimeFormLong(duration - playerState.playbackPosition));
                                                }
                                            });
                                            if (!playerState.isPaused)
                                                handler.postDelayed(this, 300);
                                        }
                                    };
                                    handler.postDelayed(r, 300);


//                                    if (!justJoined) {
//                                        if (otherUserUpdateDone) {
//                                            updateDb();
//                                            Log.d(TAG, "onEvent: Current user update");
//                                        } else {
//                                            otherUserUpdateDone = true;
//                                            Log.d(TAG, "onEvent: other user update done");
//                                        }
//                                    } else {
//                                        Log.d(TAG, "onEvent: just joined!");
//                                    }

                                }
                            }
                        });

                        db.collection(Constants.ROOMS_COLLECTION)
                                .document(mRoom.getRoomId())
                                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                    @Override
                                    public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                                        if (error != null) {
                                            Log.w(TAG, "Listen failed.", error);

                                        }
                                        if (snapshot != null && snapshot.exists()) {
                                            Log.d(TAG, "Current data: " + snapshot.getData());
//                                            Map<String, Object> body = snapshot.getData();
//                                            assert body != null;
//                                            String jsonRoom = gson.toJson(body.get(Constants.ROOM));
//                                            mRoom = gson.fromJson(jsonRoom, Room.class);
//                                            Timestamp updateTime = gson.fromJson(gson.toJson(body.get(Constants.UPDATE_TIME)), Timestamp.class);

//                                            if (updateTime == null) {
//                                                updateTime = Timestamp.now();
//                                            }
//                                            Log.d(TAG, "onEvent: updated : " + updateTime);
                                            mRoom = snapshot.toObject(Room.class);
                                            if (mRoom != null) {
                                                Log.d(TAG, "onEvent: db changed room : " + mRoom.toString());

                                                if (mRoom.getLastUpdateBy() != null && !mRoom.getLastUpdateBy().equals(currentUser)) {
                                                    if (mRoom.isPlaying())
                                                        if (mRoom.getTrackUri().equals(currentTrackUri))
                                                            playerApi.resume();
                                                        else
                                                            playerApi.play(mRoom.getTrackUri());
                                                    else
                                                        playerApi.pause();
//                                                TODO add the difference between the current time and updated time to current position to them in sync!
                                                    Timestamp currentTime = Timestamp.now();

                                                    Timestamp lastUpdateTime = mRoom.getUpdateTime();

                                                    Log.d(TAG, "onEvent: current time : " + currentTime.toString());
//                                                    Log.d(TAG, "onEvent: last update time : " + lastUpdateTime.toString());
                                                    setRepeat(mRoom.getRepeat());

//                                                    TODO subtract seconds and add to difference with nanoseconds!
                                                    long diff = currentTime.compareTo(lastUpdateTime);
                                                    long nanoSecondsDiff = (currentTime.getNanoseconds() - lastUpdateTime.getNanoseconds()) / 1000000;
                                                    long secondsDiff = (currentTime.getSeconds() - lastUpdateTime.getSeconds()) * 1000;

                                                    if (secondsDiff < 2)
                                                        secondsDiff = 0;
                                                    Log.d(TAG, "onEvent: diff : " + diff);
                                                    Log.d(TAG, "onEvent: nanoseconds difference : " + nanoSecondsDiff);
                                                    Log.d(TAG, "onEvent: nanoseconds difference converted : " + nanoSecondsDiff);
                                                    Log.d(TAG, "onEvent: diff : " + diff);

                                                    Log.d(TAG, "onEvent: Difference : " + (secondsDiff + (nanoSecondsDiff)));
                                                    int progress;
                                                    if (mRoom.isPlaying())
//                                                        progress = (int) ((double) (mRoom.getCurrentPosition() + secondsDiff + (nanoSecondsDiff)) / (double) duration * 100);
                                                        progress = (int) ((double) ((mRoom.getCurrentPosition() + secondsDiff + nanoSecondsDiff) / (double) duration * 100));
                                                    else
                                                        progress = (int) (((double) mRoom.getCurrentPosition() + secondsDiff + nanoSecondsDiff) / (double) duration * 100);
                                                    Log.d(TAG, "onEvent: progress : " + progress);
                                                    binding.seekbar.setProgress(progress);
//                                                    playerApi.seekTo(mRoom.getCurrentPosition() + ((Timestamp.now().getSeconds() - lastUpdateTime.getSeconds()) * 1000) + ((Timestamp.now().getNanoseconds() - lastUpdateTime.getNanoseconds()) / 1000000)).setResultCallback(new CallResult.ResultCallback<Empty>() {
                                                    playerApi.seekTo(mRoom.getCurrentPosition() + secondsDiff + nanoSecondsDiff).setResultCallback(new CallResult.ResultCallback<Empty>() {
                                                        @Override
                                                        public void onResult(Empty empty) {
                                                            if (!mRoom.getLastUpdateBy().equals(currentUser)) {
                                                                otherUserUpdateDone = true;
                                                                justJoined = false;
                                                            }
                                                        }
                                                    });


                                                    otherUserUpdateDone = false;
                                                } else {
                                                    Log.d(TAG, "onEvent: SAME USER DB CHANGE");
                                                }
                                                justJoined = false;
                                            }

                                        } else {
                                            Log.d(TAG, "Current data: null");
                                        }
                                    }
                                });


                    }

                    public void onFailure(Throwable throwable) {
                        Log.e(TAG, throwable.getMessage(), throwable);
                        Toast.makeText(RoomActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        // Something went wrong when attempting to connect! Handle errors here
                    }
                });

    }


    private void play(boolean isFromUser) {
        mRoom.setPlaying(true);
        binding.bottomSheetPlayPause.setImageResource(R.drawable.ic_pause);
        binding.playPause.setImageResource(R.drawable.ic_pause_white);
        if (isFromUser)
            updateDb(0);
    }

    private void pause(boolean isFromUser) {
        mRoom.setPlaying(false);
        binding.bottomSheetPlayPause.setImageResource(R.drawable.ic_play);
        binding.playPause.setImageResource(R.drawable.ic_play_white);
        if (isFromUser)
            updateDb(0);
    }

    private void updateDb(int i) {
        if (!justJoined) {

            if (i == 0 || mRoom.getLastUpdateBy().equals(currentUser)) {

                playerApi.getPlayerState().setResultCallback(playerState -> {
                    mRoom.setTrackUri(currentTrackUri);
                    mRoom.setReSync(true);
                    mRoom.setLastUpdateBy(currentUser);
                    mRoom.setCurrentPosition(playerState.playbackPosition);
                    mRoom.setUpdateTime(Timestamp.now());

                    db.collection(Constants.ROOMS_COLLECTION).document(mRoom.getRoomId()).set(mRoom).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "onSuccess: DB updated");
                            if (i == 0)
                                Toast.makeText(RoomActivity.this, "DB UPDATED", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(e -> {
                        Toast.makeText(RoomActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "onFailure: couldn't update db" + e.getMessage());
                    }).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (i < 5 && mRoom.isPlaying()) {
                                new Handler().postDelayed(() -> updateDb(i + 1), 300);
                            }
                        }
                    });
                });


            }
        } else justJoined = false;
    }
}