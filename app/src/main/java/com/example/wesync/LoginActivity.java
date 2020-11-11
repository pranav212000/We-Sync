package com.example.wesync;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wesync.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {


    public static final int AUTH_TOKEN_REQUEST_CODE = 0x10;
    public static final int AUTH_CODE_REQUEST_CODE = 0x11;
    private static final String TAG = "LoginActivity";

    private String mAccessToken;
    private String mAccessCode;
    private ActivityLoginBinding binding;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private String mRefreshToken;

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
                            final AuthorizationRequest request = getAuthenticationRequest(AuthorizationResponse.Type.CODE);
                            AuthorizationClient.openLoginActivity(LoginActivity.this, AUTH_CODE_REQUEST_CODE, request);

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


    private AuthorizationRequest getAuthenticationRequest(AuthorizationResponse.Type type) {
        return new AuthorizationRequest.Builder(Constants.CLIENT_ID, type, getRedirectUri().toString())
                .setShowDialog(false)
                .setScopes(new String[]{
                        "user-read-email",
                        "user-read-playback-state",
                        "user-modify-playback-state",
                        "user-read-recently-played",
                        "user-read-playback-position",
                        "app-remote-control",
                        "streaming"
                })
//                    .setCampaign("your-campaign-token")
                .build();
    }

    private Uri getRedirectUri() {
        return Uri.parse(Constants.REDIRECT_URI);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: data : " + data);
        final AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, data);
        if (response.getError() != null && !response.getError().isEmpty()) {
//            (response.getError());
            Toast.makeText(this, response.getError(), Toast.LENGTH_SHORT).show();
        }
        if (requestCode == AUTH_TOKEN_REQUEST_CODE) {
            mAccessToken = response.getAccessToken();
            Log.d(TAG, "onActivityResult: token : " + mAccessToken);
            Toast.makeText(this, mAccessToken, Toast.LENGTH_SHORT).show();
            SharedPreferences preferences = this.getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(Constants.ACCESS_TOKEN, response.getAccessToken());
            mAccessToken = response.getAccessToken();
            editor.apply();
        } else if (requestCode == AUTH_CODE_REQUEST_CODE) {
            mAccessCode = response.getCode();
            final SharedPreferences preferences = this.getSharedPreferences("prefs", MODE_PRIVATE);
            final SharedPreferences.Editor editor = preferences.edit();
            editor.putString("code", response.getCode());
            mAccessCode = response.getCode();
            editor.apply();


            HttpUrl url = new HttpUrl.Builder()
                    .scheme("https")
                    .host("accounts.spotify.com")
                    .addPathSegment("api")
                    .addPathSegment("token").build();

            OkHttpClient client = new OkHttpClient();


            RequestBody requestBody = new FormBody.Builder()
                    .addEncoded("grant_type", "authorization_code")
                    .addEncoded("code", mAccessCode)
                    .addEncoded("redirect_uri", Constants.REDIRECT_URI)
                    .build();

            String text = Constants.CLIENT_ID + ':' + Constants.CLIENT_SECRET;

            byte[] temp = new byte[0];
            try {
                temp = text.getBytes("UTF-8");

                String base64 = Base64.encodeToString(temp, Base64.DEFAULT);

                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("ContentType", "application/x-www-form-urlencoded")
                        .addHeader("Authorization", "Basic M2Q3ZmFiYmQxZTAzNDgwYWE5YWM0YzY4ZjBhN2MzNjA6YWUwZGYyZTk1OTFmNDViMTlhODNhOTg0ZTVmZWFiZmM=")
                        .post(requestBody)
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e(TAG, "onFailure: FAILED : ", e);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {

//                        Log.d(TAG, "onResponse: response : " + response.toString());
//                        Log.d(TAG, "onResponse: response body : " + response.body().string());

                        if (response.body() != null) {
                            ResponseBody responseBody = response.body();

                            try {
//                                CAN BE CONSUMED ONLY ONCE (body.string())
                                String responseString = responseBody.string();
                                Log.d(TAG, "onResponse: response String : " + responseString);
                                JSONObject object = new JSONObject(responseString);
                                mAccessToken = object.getString(Constants.ACCESS_TOKEN);
                                mRefreshToken = object.getString(Constants.REFRESH_TOKEN);

                                editor.putString(Constants.REFRESH_TOKEN, mRefreshToken);
                                editor.putString(Constants.ACCESS_TOKEN, mAccessToken);
                                editor.putString(Constants.REFRESH_TIME, Constants.getUTCdatetimeAsString());
                                editor.apply();

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }


            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
    }
}