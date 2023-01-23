package com.example.hiddenboss;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/* Activity were users log in with their account username and password. Whenever the app is started,
 it defaults to this page, and users must log in before they can use any other features. */
public class LoginActivity extends AppCompatActivity {
    private RequestQueue loginQueue;
    // Access token from a successful login attempt. Used in requests that require user authentication
    private String access_token;
    private static final String tag = "LoginActivity";
    // An Editor is needed, as we will modify our global SharedPreferences object after login
    private SharedPreferences.Editor mEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Set two on-click listeners, one for the login button, and one for the registering text
        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginFunc();
            }
        });

        loginQueue = Volley.newRequestQueue(this);
        TextView registerText = findViewById(R.id.registerText);
        registerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openRegister();
            }
        });

        // Get the SharedPreferences file we want to modify and initialize the Editor by calling edit() on the file
        SharedPreferences userInfoPreferences = getSharedPreferences("com.example.hiddenboss", Context.MODE_PRIVATE);
        mEditor = userInfoPreferences.edit();
    }

    private void loginFunc(){
        // The string values of the text input by the user is retrieved and used as JSON data.
        TextView usernameText = findViewById(R.id.username);
        TextView passwordText = findViewById(R.id.password);
        // username must be final in order to be accessed from within onResponse
        final String username = usernameText.getText().toString();
        final String password = passwordText.getText().toString();

        // A JSON string that userData is made from
        String jsonString = String.format("{'username': %s, 'password': %s}", username, password);
        JSONObject userData;

        try {

            userData = new JSONObject(jsonString);
            Log.d("Username value: ", userData.getString("username"));

            String url = "https://hidden-boss-server.herokuapp.com/users/login";
            // Send a request to log in with current user information
            JsonObjectRequest loginRequest = new JsonObjectRequest(Request.Method.POST, url, userData, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        /* On successful login, edit the SharedPreferences file (with current username and token, and signaling that the user is currently logged in)
                           and close itself, getting back to whatever activity started it */
                        access_token = response.getString("token");
                        mEditor.putString("userName",username);
                        mEditor.putString("token",access_token);
                        mEditor.putBoolean("isLoggedIn", true).commit();
                        mEditor.commit();

                        setResult(RESULT_FIRST_USER,null);
                        finish();
                    }
                    catch (Throwable x){
                        Log.e("LoginActivity", "Could not get token from reponse: \"" + response + "\"");
                    }
                }
            }, new Response.ErrorListener(){
                @Override
                public void onErrorResponse(VolleyError error){
                    Log.e(tag, "Unable to log in, error code " + String.valueOf(error.networkResponse.statusCode));
                    error.printStackTrace();
                }
            });
            loginQueue.add(loginRequest);
        } catch (Throwable x) {
            Log.e(tag, "Could not parse malformed JSON: \"" + jsonString + "\"");
        }
    }

    private void openRegister(){
        // Opens UserRegistrationActivity so users can register a new account.
        Intent i = new Intent(this, UserRegistrationActivity.class);
        startActivity(i);
    }

}