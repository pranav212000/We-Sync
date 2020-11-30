package com.example.wesync;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wesync.databinding.ActivityMainBinding;
import com.example.wesync.models.Room;
import com.example.wesync.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

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
        getSupportActionBar().hide();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        progressDialog = new ProgressDialog(this);
        getCurrentUser();
        progressDialog.show();

        final SharedPreferences preferences = this.getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE);
        String refreshDateString = preferences.getString(Constants.REFRESH_TIME, "");
        String refreshToken = preferences.getString(Constants.REFRESH_TOKEN, "");

        Log.d(TAG, "onCreate: ref date : " + refreshDateString);
        Log.d(TAG, "onCreate: ref token : " + refreshToken);
        assert refreshToken != null;
        assert refreshDateString != null;
        if (!refreshToken.isEmpty() && !refreshDateString.isEmpty()) {
            Date refreshDate = Constants.getStringDateToDate(refreshDateString);
            Date currentDate = Constants.getUTCdatetimeAsDate();


            long diff = currentDate.getTime() - refreshDate.getTime();
//        int numOfDays = (int) (diff / (1000 * 60 * 60 * 24));
//        int hours = (int) (diff / (1000 * 60 * 60));
//        int minutes = (int) (diff / (1000 * 60));
            int seconds = (int) (diff / (1000));
            Log.d(TAG, "onCreate: seconds : " + seconds);
            if (seconds > 3200) {

                HttpUrl url = new HttpUrl.Builder()
                        .scheme("https")
                        .host("accounts.spotify.com")
                        .addPathSegment("api")
                        .addPathSegment("token").build();

                OkHttpClient client = new OkHttpClient();


                RequestBody requestBody = new FormBody.Builder()
                        .addEncoded("grant_type", "refresh_token")
                        .addEncoded("refresh_token", refreshToken)
                        .build();


                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("ContentType", "application/x-www-form-urlencoded")
                        .addHeader("Authorization", "Basic M2Q3ZmFiYmQxZTAzNDgwYWE5YWM0YzY4ZjBhN2MzNjA6YWUwZGYyZTk1OTFmNDViMTlhODNhOTg0ZTVmZWFiZmM=")
                        .post(requestBody)
                        .build();

                Log.d(TAG, "onCreate: making token request");
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e(TAG, "onFailure: token refresh failure : ", e);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {

                        if (response.body() != null) {
                            ResponseBody responseBody = response.body();

                            try {
//                                CAN BE CONSUMED ONLY ONCE (body.string())
                                String responseString = responseBody.string();
                                Log.d(TAG, "onResponse: response String : " + responseString);
                                JSONObject object = new JSONObject(responseString);
                                String accessToken = object.getString(Constants.ACCESS_TOKEN);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString(Constants.ACCESS_TOKEN, accessToken);
                                editor.putString(Constants.REFRESH_TIME, Constants.getUTCdatetimeAsString());
                                editor.apply();


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }

        }


        binding.createRoom.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                progressDialog.setMessage("Registering Please Wait...");
                progressDialog.show();
                final String roomId = generateRoomId();
                Room room = new Room(roomId, currentUser.getUserName(), "", false, Timestamp.now(), false, 0, new ArrayList<>(), 0, currentUser.getUserName());
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

                                    db.collection(Constants.ROOMS_COLLECTION)
                                            .document(roomId)
                                            .get()
                                            .addOnSuccessListener(documentSnapshot -> {
                                                if (documentSnapshot.exists()) {
                                                    Log.d(TAG, "onSuccess: document data : " + documentSnapshot.getData().toString());
                                                    Room room = documentSnapshot.toObject(Room.class);
                                                    Intent intent = new Intent(MainActivity.this, RoomActivity.class);
                                                    intent.putExtra(Constants.USERNAME, currentUser.getUserName());
                                                    Gson gson = new Gson();
                                                    intent.putExtra(Constants.ROOM, gson.toJson(room));
                                                    startActivity(intent);
                                                }
                                            })
                                            .addOnFailureListener(e -> Log.e(TAG, "onFailure: " + e.getMessage()))
                                            .addOnCompleteListener(task -> progressDialog.dismiss());

                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(MainActivity.this, "Could not join the room", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
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
        db.collection(Constants.ROOMS).document(room.getRoomId()).set(room)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Intent intent = new Intent(MainActivity.this, RoomActivity.class);
                        Gson gson = new Gson();
                        intent.putExtra(Constants.USERNAME, currentUser.getUserName());
                        intent.putExtra(Constants.ROOM, gson.toJson(room));
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
