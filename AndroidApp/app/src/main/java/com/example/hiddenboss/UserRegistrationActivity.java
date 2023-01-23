package com.example.hiddenboss;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

// Activity where users can register new accounts. Is started from LoginActivity in cases where the user has not yet made an account.
public class UserRegistrationActivity extends AppCompatActivity {
    private RequestQueue registerQueue;
    private static final String tag = "UserRegistrationActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /* When activity is created, set layout, set on click listener for registerButton and
        create a RequestQueue so requests can be sent */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_registration);

        Button registerButton = findViewById(R.id.loginButton);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });
        registerQueue = Volley.newRequestQueue(this);
    }

    private void registerUser(){
        // The string values of the text input by the user is retrieved and used as JSON data.
        TextView usernameText = findViewById(R.id.username);
        TextView passwordText = findViewById(R.id.password);

        final String username = usernameText.getText().toString();
        final String password = passwordText.getText().toString();

        // A JSON string that userData is made from
        String json = String.format("{\'username\': %s, \'password\': %s}", username, password);
        final JSONObject userData;

        try {
            // userData is initialized
            userData = new JSONObject(json);

            final String url = "https://hidden-boss-server.herokuapp.com/users";

            // Sends a request to register a new user
            StringRequest registerRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        // Signal that the user has successfully been registered and then go back to LoginActivity
                        Log.d(tag, "User was successfully registered");
                        finish();
                    }
                    catch (Throwable x){
                        Log.e(tag, "Issue registering: \"" + response + "\"");
                    }
                }
            }, new Response.ErrorListener(){
                @Override
                public void onErrorResponse(VolleyError error){
                    try {
                        Log.e(tag, "Unable to register, username is \"" + userData.getString("username") + " and error is " + error.toString() + "\"");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    error.printStackTrace();
                }
            }) {
                @Override
                public byte[] getBody () {
                    // getBody is necessary in order to send JSON data with a StringRequest
                    return userData.toString().getBytes();
                }
                @Override
                public String getBodyContentType() {
                    // define the data sent with the request as JSON
                    return "application/json; charset=utf-8";
                }
            };
            registerQueue.add(registerRequest);
        } catch (Throwable x) {
            x.printStackTrace();
        }
    }
}
