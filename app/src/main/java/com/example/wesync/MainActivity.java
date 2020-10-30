package com.example.wesync;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wesync.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    //    TODO replace all instances of progressDialog with
    private ProgressDialog progressDialog;


    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        progressDialog = new ProgressDialog(this);


//        db.collection("users").document(firebaseAuth.getCurrentUser().getEmail()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                DocumentSnapshot document = task.getResult();
//                Log.d(TAG, "Cached document data: " + document.getData());
//                User user = new User(document.getString(Constants.EMAIL), document.getString(Constants.NAME));
//
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//
//            }
//        });
//
//        binding.createRoom.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                // Do something in response to button click
//
//                progressDialog.setMessage("Registering Please Wait...");
//                progressDialog.show();
//
//
//                final String roomId = generateRoomId();
//                String song = "";
//                String host = username;
//                final Map<String, Object> room = new HashMap<>();
//                room.put(Constants.ROOM_ID, roomId);
//                room.put(Constants.SONG, null);
//                room.put(Constants.HOST, username);
//                CollectionReference rooms = db.collection(Constants.ROOMS);
//                rooms.document(roomId).set(room).addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        Intent intent = new Intent(MainActivity.this, RoomActivity.class);
//                        intent.putExtra(Constants.ROOM_ID, roomId);
//                        startActivity(intent);
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(MainActivity.this, "Could not create room, please try again!", Toast.LENGTH_SHORT).show();
//                    }
//                });
//
//            }
//        });


        binding.logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (firebaseAuth.getCurrentUser() != null) {
                    //opening profile activity
                    firebaseAuth.signOut();
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    //close this activity
                    finish();
                }
            }
        });
    }

    public String generateRoomId() {
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "abcdefghijklmnopqrstuvxyz";

        // create StringBuffer size of AlphaNumericString
        StringBuilder sb = new StringBuilder(7);

        for (int i = 0; i < 7; i++) {

            // generate a random number between
            // 0 to AlphaNumericString variable length
            int index
                    = (int) (AlphaNumericString.length()
                    * Math.random());

            // add Character one by one in end of sb
            sb.append(AlphaNumericString
                    .charAt(index));
        }

        return sb.toString();
    }
}
