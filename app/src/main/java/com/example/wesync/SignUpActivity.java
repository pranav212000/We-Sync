package com.example.wesync;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wesync.databinding.ActivityMainBinding;
import com.example.wesync.databinding.ActivitySignUpBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "SignUpActivity";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ActivitySignUpBinding binding;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        firebaseAuth = FirebaseAuth.getInstance();


        //if getCurrentUser does not returns null
        if (firebaseAuth.getCurrentUser() != null) {
            //that means user is already logged in
            //so close this activity

            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();

        }

        progressDialog = new ProgressDialog(this);
        //attaching listener to button
        binding.signUp.setOnClickListener(this);
        binding.signIn.setOnClickListener(this);
    }

    private void registerUser() {

        //getting email and password from edit texts
        String email = binding.email.getText().toString().trim();
        String password = binding.password.getText().toString().trim();
        String name = binding.name.getText().toString().trim();
        SharedPreferences preferences = getSharedPreferences(Constants.LOGIN, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.USERNAME, name);
        editor.apply();

        //checking if email and passwords are empty
        if (TextUtils.isEmpty(email)) {
            binding.email.setError("Please enter email");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            binding.password.setError("Please enter password");
            return;
        }

        //if the email and password are not empty
        //displaying a progress dialog

        progressDialog.setMessage("Registering Please Wait...");
        progressDialog.show();

        //creating a new user
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //checking if success
                        if (task.isSuccessful()) {
//                            Map<String, String> user = new HashMap<>();
                            String email = binding.email.getText().toString().trim();
                            String name = binding.name.getText().toString().trim();
//                            user.put(Constants.EMAIL, email);
//                            user.put(Constants.NAME, name);

                            User user = new User(email, name);
                            /*db.collection("users")
                                    .add(user)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            System.out.println("Added");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            System.out.println("Failure");
                                        }
                                    });*/
                            CollectionReference users = db.collection("users");
                            users.document(email).set(user);

                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            finish();

                        } else {
                            Log.e(TAG, "onComplete: task unsuccessful, error : ", task.getException());
                            //display some message here
                            Toast.makeText(SignUpActivity.this, "Registration Error", Toast.LENGTH_LONG).show();
                        }
                        progressDialog.dismiss();
                    }
                });

    }

    @Override
    public void onClick(View view) {
        if (binding.signUp.equals(view)) {
            registerUser();
        } else if (binding.signIn.equals(view)) {
            startActivity(new Intent(this, LoginActivity.class));
        }
    }
}