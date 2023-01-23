package com.example.hiddenboss;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//RegistrationActivity is the Activity where users can view information about and register themselves for a tournament
public class RegistrationActivity extends BaseActivity {
    //Global variables
    private boolean isRegistered;
    private RequestQueue infoQueue;
    private static final String Tag = "RegistrationActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //The layout is created, toolbar is setup and initScreenInfo is called
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        infoQueue = Volley.newRequestQueue(this);
        setupToolbar();
        initScreenInfo();
    }



    public void initScreenInfo(){
        //initScreenInfo uses the data from the intent and volley request to fill all objects in view with the appropriate information
        Intent intent = getIntent();

        ImageView gameImg = findViewById(R.id.gameImageRegistration);
        String imgUrl = intent.getStringExtra("imgUrl");
        final String tourneyId = intent.getStringExtra("id");
        final String gameId = intent.getStringExtra("game_id");
        // Using Glide, the image URL passed from the past activity with intent is loaded onto the ImageView
        Glide.with(this)
                .asBitmap()
                .load(imgUrl)
                .into(gameImg);

        TextView title = findViewById(R.id.titleRegistration);
        title.setText(intent.getStringExtra("title"));

        TextView startEnd = findViewById(R.id.startEndReg);
        startEnd.setText(intent.getStringExtra("startEnd"));

        TextView location = findViewById(R.id.locationReg);
        location.setText(intent.getStringExtra("location"));

        TextView host = findViewById(R.id.hostReg);
        host.setText(intent.getStringExtra("host"));

        TextView description = findViewById(R.id.description);
        description.setText(intent.getStringExtra("description"));

        TextView likes = findViewById(R.id.likesReg);
        likes.setText(intent.getStringExtra("likesString"));

        Button comments = findViewById(R.id.commentsButton);
        comments.setOnClickListener(new View.OnClickListener() {
            //When this button is pressed the comment section for the tournament is showed
            @Override
            public void onClick(View v) {
                // When clicked, load a fragment displaying user-posted comments that have been made on tournament
                Bundle args = new Bundle();
                args.putString("tourney_id", tourneyId);
                Fragment commentFragment = new CommentFragment();
                commentFragment.setArguments(args);
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.tourneyLayout, commentFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        Button gameInfo = findViewById(R.id.gameInfoButton);
        gameInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //When this button is pressed more detailed information about the tournament's game is shown
                Bundle args = new Bundle();
                args.putString("game_id", gameId);
                Fragment gameInfoFragment = new GameInfoFragment();
                gameInfoFragment.setArguments(args);
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.tourneyLayout, gameInfoFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        final Button registrationButton = findViewById(R.id.registerButton);
        // Create a request that checks if the user is registered to the tournament currently being viewed
        String url = "https://hidden-boss-server.herokuapp.com/tourneys/" + tourneyId + "/participants";
        StringRequest checkIfRegistered = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Checks to see whether or not the user is included in the list of users registered for the tournament
                if (response.contains(preferences.getString("userName","Missing user"))){
                    // If user is included, update activity to indicate that user is registered
                    updateRegistered(true);
                }
                else{
                    // If user isn't included, update activity to indicate that user isn't registered
                    updateRegistered(false);
                    }
                Log.d(Tag, "Currently registered: " + String.valueOf(isRegistered));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse.statusCode == 404){
                    /* Error code 404 is used when no participants were found for the tournament. In this case,
                       users should still be able to register, so just update the activity to indicate that user is registered. */
                    updateRegistered(false);
                }
                else{
                    Log.e(Tag, "Volley error with status code " + String.valueOf(error.networkResponse.statusCode));
                }
            }
        });
        infoQueue.add(checkIfRegistered);
    }

    
    public void updateRegistered(boolean userRegistered){
        /* Changes the appearance and behaviour of the register button (registrationButton).
        If registered, the register button's function is to deregister user from the tournament.
        If instead not registered, button's function is to register user for the tournament. */

        Intent intent = getIntent();
        final RequestQueue infoQueue = Volley.newRequestQueue(this);
        final String tourneyId = intent.getStringExtra("id");

        final Button registrationButton = findViewById(R.id.registerButton);
        if (userRegistered){
            // If user is registered, change appearance and behaviour of registrationButton to enable deregistering from tournament
            isRegistered = true;
            registrationButton.setText(R.string.deregister);
            registrationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Dynamically create different URLs depending on the game by using tourneyId as part of the URL string
                    String url = "https://hidden-boss-server.herokuapp.com/tourneys/" + tourneyId + "/register";
                    final StringRequest removeParticipant = new StringRequest(Request.Method.DELETE, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            /* On successful deregistering, call updateRegsitered to change
                            registrationButton's appearance and behaviour once again */
                            updateRegistered(!isRegistered);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(Tag, "Volley error with status code " + String.valueOf(error.networkResponse.statusCode));
                        }
                    }){
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            /* Deregistering from a tournament is an action that requires user authentication.
                            Thus, authentication needs to be sent in the request header. */
                            super.getHeaders();
                            Map<String, String> headers = new HashMap<>();
                            headers.put("Authorization", "Bearer " +  preferences.getString("token","Missing token"));
                            return headers;
                        }
                    };
                    infoQueue.add(removeParticipant);
                }
            });
        }
        else{
            // If user isn't registered, change appearance and behaviour of registrationButton to enable registering for tournament
            isRegistered = false;
            registrationButton.setText(R.string.register);
            registrationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Dynamically create different URLs depending on the game by using tourneyId as part of the URL string
                    String url = "https://hidden-boss-server.herokuapp.com/tourneys/" + tourneyId + "/register";
                    final StringRequest registerParticipant = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            /* On successful registering, call updateRegsitered to change
                            registrationButton's appearance and behaviour once again */
                            updateRegistered(!isRegistered);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(Tag, "Volley error with status code " + String.valueOf(error.networkResponse.statusCode));
                        }
                    }){
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            /* Registering for is an action that requires user authentication.
                            Thus, authentication needs to be sent in the request header. */
                            super.getHeaders();
                            Map<String, String> headers = new HashMap<>();
                            headers.put("Authorization", "Bearer " +  preferences.getString("token","Missing token"));
                            return headers;
                        }
                    };
                    infoQueue.add(registerParticipant);
                }
            });
        }
        Log.d(Tag, "Currently registered: " + String.valueOf(isRegistered));
    }

    @Override
    protected void openCreate() {
        //Uses volley to get the necessary information to open the activity for creating a tournament
        String url = "https://hidden-boss-server.herokuapp.com/games";
        JsonObjectRequest fetchGamesFromDB = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray gamesJSON = response.getJSONArray("games");
                            Intent openCreateTournament = new Intent(RegistrationActivity.this, CreateActivity.class);
                            openCreateTournament.putExtra("gamesJSON", gamesJSON.toString());
                            openCreateTournament.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                            startActivity(openCreateTournament);
                            finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error instanceof AuthFailureError){
                    openLogin();}
                else{
                    Log.e(Tag, "Volley error with status code " + String.valueOf(error.networkResponse.statusCode));
                }
            }
        });
        infoQueue.add(fetchGamesFromDB);
    }

    @Override
    protected void openLiked() {
        //Uses volley to get the necessary information to open the activity for the tournaments that the user has liked
        String likedUrl = "https://hidden-boss-server.herokuapp.com/users/" + preferences.getString("userName", "missing") + "/likes";
        JsonObjectRequest getLikedTournaments = new JsonObjectRequest(Request.Method.GET, likedUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray likedArray = response.getJSONArray("tourneys");
                    ArrayList<Tournaments> likedTourneys = new ArrayList<>();
                    Gson gson = new Gson();
                    for (int i = 0; i < likedArray.length(); i++) {
                        JSONObject currentT = likedArray.getJSONObject(i);
                        likedTourneys.add(gson.fromJson(String.valueOf(currentT), Tournaments.class));
                    }
                    Intent liked = new Intent(RegistrationActivity.this, LikesActivity.class);
                    liked.putParcelableArrayListExtra("likedTournaments", likedTourneys);
                    startActivity(liked);
                    finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(Tag, "Volley error with status code " + String.valueOf(error.networkResponse.statusCode));
            }
        });
        infoQueue.add(getLikedTournaments);
    }

    @Override
    protected void openRegister() {
        //Uses volley to get the necessary information to open the registered activity
        String registeredUrl = "https://hidden-boss-server.herokuapp.com/users/" + preferences.getString("userName", "missing") + "/tourneys";
        JsonObjectRequest getRegisteredTournaments = new JsonObjectRequest(registeredUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray registeredArray = response.getJSONArray("tourneys");
                    ArrayList<Tournaments> registeredTourenys = new ArrayList<>();
                    Gson gson = new Gson();
                    for (int i = 0; i < registeredArray.length(); i++) {
                        JSONObject currentT = registeredArray.getJSONObject(i);
                        registeredTourenys.add(gson.fromJson(String.valueOf(currentT), Tournaments.class));
                    }
                    Intent registered = new Intent(RegistrationActivity.this, RegisteredActivity.class);
                    registered.putParcelableArrayListExtra("registeredTournaments", registeredTourenys);
                    startActivity(registered);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(Tag, "Volley error with status code " + String.valueOf(error.networkResponse.statusCode));
            }
        });
        infoQueue.add(getRegisteredTournaments);
    }

    @Override
    protected void openMyTourneys() {
        //Uses volley to get the necessary information to open the activity for the user's own tournaments
        String urlMyTournaments ="https://hidden-boss-server.herokuapp.com/users/" + preferences.getString("userName", "error") + "/hosted_tourneys";

        JsonObjectRequest getMyTournaments = new JsonObjectRequest(Request.Method.GET, urlMyTournaments, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Gson gson = new Gson();
                try {
                    JSONArray myTournamentsJSON = response.getJSONArray("tourneys");
                    final ArrayList<Tournaments> myTourneys = new ArrayList<>();
                    for (int i = 0; i < myTournamentsJSON.length(); i++) {
                        JSONObject myCurrentTournament = myTournamentsJSON.getJSONObject(i);
                        final Tournaments myTournaments=gson.fromJson(String.valueOf(myCurrentTournament), Tournaments.class);
                        myTourneys.add(myTournaments);
                    }
                    Intent myTourneysIntent = new Intent(RegistrationActivity.this, EditTournamentsActivity.class);
                    myTourneysIntent.putParcelableArrayListExtra("myT", myTourneys);
                    myTourneysIntent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                    startActivity(myTourneysIntent);
                    finish();


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(Tag, "Volley error with status code " + String.valueOf(error.networkResponse.statusCode));
            }
        });
        infoQueue.add(getMyTournaments);
    }

    @Override
    protected void openLogin() {
        //Opens the login activity
        final int loginRequestCode = 13;
        Intent i = new Intent(RegistrationActivity.this, LoginActivity.class);
        startActivityForResult(i, loginRequestCode);
    }

}
