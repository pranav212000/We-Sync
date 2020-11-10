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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRoomBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);


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

                if (!mAccessToken.isEmpty() && !charSequence.toString().isEmpty()) {
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

                                TracksAdapter adapter = new TracksAdapter(RoomActivity.this, mTrackArrayList);
                                binding.tracksGrid.setAdapter(adapter);
                                binding.tracksGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                                        Toast.makeText(RoomActivity.this, mTrackArrayList.get(position).name, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            Log.e(TAG, "failure: error", error);
                        }
                    });

                }


            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


//        binding.search

    }
}