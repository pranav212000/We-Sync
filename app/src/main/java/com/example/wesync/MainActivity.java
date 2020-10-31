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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    //    TODO replace all instances of progressDialog with
    private ProgressDialog progressDialog;

    private User currentUser;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        progressDialog = new ProgressDialog(this);
        getCurrentUser();
        progressDialog.show();


        binding.createRoom.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                progressDialog.setMessage("Registering Please Wait...");
                progressDialog.show();
                final String roomId = generateRoomId();
                Room room = new Room(roomId, currentUser.userName, "", false, "", new ArrayList<User>());
                addRoom(room);
            }
        });


        binding.joinRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String roomId = binding.roomId.getText().toString().toUpperCase();
                if (roomId.isEmpty()) {
                    binding.roomId.setError("Please enter roomId");
                } else if (roomId.length() != 7) {
                    binding.roomId.setError("Enter valid roomId");
                } else {
                    progressDialog.show();
                    db.collection(Constants.ROOMS_COLLECTION).document(roomId).update(Constants.MEMBERS, FieldValue.arrayUnion(currentUser.getUserName()))
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Intent intent = new Intent(MainActivity.this, RoomActivity.class);
                                    intent.putExtra(Constants.ROOM_ID, roomId);
                                    startActivity(intent);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MainActivity.this, "Could not join the room", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    progressDialog.dismiss();
                                }
                            });
                }
            }
        });


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

    private void getCurrentUser() {
        progressDialog.show();
        db.collection(Constants.USERS_COLLECTION)
                .whereEqualTo(Constants.EMAIL, Objects.requireNonNull(firebaseAuth.getCurrentUser()).getEmail())
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    currentUser = documentSnapshot.toObject(User.class);
                    Toast.makeText(MainActivity.this, currentUser.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "onFailure: ", e);
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressDialog.dismiss();
            }
        });
    }


    public void addRoom(final Room room) {
        db.collection(Constants.ROOMS).document(room.roomId).set(room)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Intent intent = new Intent(MainActivity.this, RoomActivity.class);
                        intent.putExtra(Constants.ROOM_ID, room.roomId);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Could not create room, please try again!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.dismiss();
                    }
                });
    }

    public String generateRoomId() {
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "1234567890";

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
