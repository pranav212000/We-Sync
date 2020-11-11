package com.example.wesync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wesync.adapters.TracksAdapter;
import com.example.wesync.databinding.ActivityRoomBinding;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.PlayerApi;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.client.ErrorCallback;
import com.spotify.protocol.types.Empty;

import java.util.ArrayList;

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
    private String currentTrack;

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

//TODO subscribe to player state and check for externam pause or play and change the icon accordingly
        binding.playPause.setOnClickListener(view -> {
            if (isPlaying) {
                playerApi.pause().setResultCallback(empty -> {
                    binding.playPause.setImageResource(R.drawable.ic_play);
                    isPlaying = false;
                }).setErrorCallback(throwable -> Toast.makeText(RoomActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show());

            } else {
                if (currentTrack != null && !currentTrack.isEmpty())
                    playerApi.resume().setResultCallback(empty -> {
                        binding.playPause.setImageResource(R.drawable.ic_pause);
                        isPlaying = true;
                    }).setErrorCallback(new ErrorCallback() {
                        @Override
                        public void onError(Throwable throwable) {
                            Toast.makeText(RoomActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

            }
        });
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
                        currentTrack = track.uri;
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
                    }

                    public void onFailure(Throwable throwable) {
                        Log.e("MyActivity", throwable.getMessage(), throwable);
                        Toast.makeText(RoomActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        // Something went wrong when attempting to connect! Handle errors here
                    }
                });
    }
}