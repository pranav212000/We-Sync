package com.example.wesync;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class room_activity extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private EditText editTextName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_activity);
        Button button1 = (Button) findViewById(R.id.create);
        Button button2 = (Button) findViewById(R.id.join);
        SharedPreferences sp1 = this.getSharedPreferences("Login", MODE_PRIVATE);
        final String username = sp1.getString("username", null);
        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
                final CollectionReference rooms = db.collection("rooms");
                String room_id = generateRoomId();
                String song = "";
                String host = username;
                final Map<String, Object> data = new HashMap<>();
                data.put("Room_Id", room_id);
                data.put("Song", null);
                data.put("Username", username);
                DocumentReference docRef = db.collection("users").document("users." + username);


                finish();
                startActivity(new Intent(room_activity.this, Create_Room.class));
            }
        });


    }

    public String generateRoomId()
    {
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "abcdefghijklmnopqrstuvxyz";

        // create StringBuffer size of AlphaNumericString
        StringBuilder sb = new StringBuilder(7);

        for (int i = 0; i < 7; i++) {

            // generate a random number between
            // 0 to AlphaNumericString variable length
            int index
                    = (int)(AlphaNumericString.length()
                    * Math.random());

            // add Character one by one in end of sb
            sb.append(AlphaNumericString
                    .charAt(index));
        }

        return sb.toString();
    }
}
