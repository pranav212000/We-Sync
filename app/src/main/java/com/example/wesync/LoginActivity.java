package com.example.wesync;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wesync.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {


    private static final String TAG = "LoginActivity";

    private ActivityLoginBinding binding;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            //close this activity
            finish();
            //opening profile activity
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }

        progressDialog = new ProgressDialog(this);
        binding.signIn.setOnClickListener(this);
        binding.signUp.setOnClickListener(this);
    }

    //method for user login
    private void userLogin() {
        String email = binding.email.getText().toString().trim();
        String password = binding.password.getText().toString().trim();
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
        //logging in the user
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        //if the task is successfull
                        if (task.isSuccessful()) {
                            //start the profile activity
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            finish();
                        }
                    }
                });
    }

    @Override
    public void onClick(View view) {
        if (binding.signUp.equals(view)) {
            startActivity(new Intent(this, SignUpActivity.class));
            finish();
        } else if (binding.signIn.equals(view)) {
            userLogin();
        }
    }
}