package com.example.wesync;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.wesync.databinding.ActivityMainBinding;
import com.example.wesync.databinding.ActivityRoomBinding;

public class RoomActivity extends AppCompatActivity {

    private ActivityRoomBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRoomBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        Intent intent = getIntent();
        String roomId = intent.getStringExtra(Constants.ROOM_ID);
        binding.roomId.setText(roomId);
    }
}