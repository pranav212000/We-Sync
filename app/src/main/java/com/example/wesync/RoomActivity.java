package com.example.wesync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wesync.adapters.TracksAdapter;
import com.example.wesync.databinding.ActivityRoomBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
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
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class RoomActivity extends AppCompatActivity {


    private static final String TAG = "RoomActivity";
    private ActivityRoomBinding binding;
    private GridView mGridView;
    private String mAccessToken;
    private ArrayList<Track> mTrackArrayList = new ArrayList<>();
    private SpotifyAppRemote mSpotifyAppRemote;
    private PlayerApi playerApi;
    private boolean isPlaying = false;
    private String currentTrackUri;
    private long duration;
    private int repeat = 0;

    private BottomSheetBehavior mBottomSheetBehavior;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRoomBinding.inflate(getLayoutInflater());
        View root = binding.getRoot();
        setContentView(root);


        SharedPreferences preferences = this.getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE);
        mAccessToken = preferences.getString(Constants.ACCESS_TOKEN, "");
        Intent intent = getIntent();
        String roomId = intent.getStringExtra(Constants.ROOM_ID);
        binding.roomId.setText(roomId);

        View.OnClickListener playPauseListener = view -> {
            if (isPlaying) {
                playerApi.pause().setResultCallback(empty -> {
                    pause();
                }).setErrorCallback(throwable -> Toast.makeText(RoomActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show());

            } else {
                if (currentTrackUri != null && !currentTrackUri.isEmpty())
                    playerApi.resume().setResultCallback(empty -> {
                        play();
                    }).setErrorCallback(new ErrorCallback() {
                        @Override
                        public void onError(Throwable throwable) {
                            Toast.makeText(RoomActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

            }
        };

        binding.bottomSheetPlayPause.setOnClickListener(playPauseListener);
        binding.playPause.setOnClickListener(playPauseListener);
        binding.closeBottomSheet.setOnClickListener(view -> mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED));
        binding.skipNext.setOnClickListener(view -> playerApi.skipNext());

        binding.skipPrevious.setOnClickListener(view -> playerApi.skipPrevious());

        binding.repeat.setOnClickListener(view -> {
            repeat = (repeat + 1) % 3;
            switch (repeat) {
                case Repeat.OFF:
                    playerApi.setRepeat(Repeat.OFF).setResultCallback(empty -> binding.repeat.setImageResource(R.drawable.ic_repeat_off));
                    break;
                case Repeat.ONE:
                    playerApi.setRepeat(Repeat.ONE).setResultCallback(empty -> binding.repeat.setImageResource(R.drawable.ic_repeat_one));
                    break;
                case Repeat.ALL:
                    playerApi.setRepeat(Repeat.ALL).setResultCallback(empty -> binding.repeat.setImageResource(R.drawable.ic_repeat));
                    break;
                default:
            }

        });

        binding.seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (playerApi == null) {
                    Toast.makeText(RoomActivity.this, "Spotify not connected yet!", Toast.LENGTH_SHORT).show();
                } else {
                    if (fromUser) {

                        long currentPosition = duration * progress / 100;

                        playerApi.seekTo(currentPosition);
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

    private void searchTrack(CharSequence charSequence) {
        final SpotifyApi api = new SpotifyApi();
        api.setAccessToken(mAccessToken);

        Log.d(TAG, "onTextChanged: token : " + mAccessToken);
        final SpotifyService spotify = api.getService();


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
                        isPlaying = true;
                        currentTrackUri = track.uri;
                        binding.trackTextView.setText(track.name);
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
                                        pause();
                                    else
                                        play();


                                    Handler handler = new Handler();

                                    final Runnable r = new Runnable() {
                                        public void run() {
                                            playerApi.getPlayerState().setResultCallback(new CallResult.ResultCallback<PlayerState>() {
                                                @Override
                                                public void onResult(PlayerState playerState) {
                                                    Log.d(TAG, "onResult: position : " + playerState.playbackPosition);
                                                    Log.d(TAG, "onResult: position : " + duration);
                                                    Log.d(TAG, "onResult: position : " + (double) playerState.playbackPosition / (double) duration);
                                                    Log.d(TAG, "onResult: current position : " + ((playerState.playbackPosition / duration) * 100));
                                                    binding.seekbar.setProgress((int) ((double) playerState.playbackPosition / (double) duration * 100));
                                                    binding.start.setText(Constants.getTimeFormLong(playerState.playbackPosition));
                                                    binding.end.setText(Constants.getTimeFormLong(duration - playerState.playbackPosition));
                                                }
                                            });
                                            if (!playerState.isPaused)
                                                handler.postDelayed(this, 500);
                                        }
                                    };
                                    handler.postDelayed(r, 1000);
                                }
                            }
                        });
                    }

                    public void onFailure(Throwable throwable) {
                        Log.e("MyActivity", throwable.getMessage(), throwable);
                        Toast.makeText(RoomActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        // Something went wrong when attempting to connect! Handle errors here
                    }
                });

    }


    private void play() {
        isPlaying = true;
        binding.bottomSheetPlayPause.setImageResource(R.drawable.ic_pause);
        binding.playPause.setImageResource(R.drawable.ic_pause_white);
    }

    private void pause() {
        isPlaying = false;
        binding.bottomSheetPlayPause.setImageResource(R.drawable.ic_play);
        binding.playPause.setImageResource(R.drawable.ic_play_white);
    }
}