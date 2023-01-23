package com.example.hiddenboss;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

//LikesActivity is just an activity that holds the likedAdapter and toolbar functionality
public class LikesActivity extends BaseActivity implements LikedAdapter.login{
    //Tag string for debugging and global variables
    private static final String TAG = "Likes";
    private ArrayList<Tournaments> tournaments;
    RequestQueue createRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //The variables are given values and the layout is created
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_likes);
        createRequest = Volley.newRequestQueue(this);
        setupToolbar();
        Intent intent = getIntent();
        tournaments = Objects.requireNonNull(intent.getExtras()).getParcelableArrayList("likedTournaments");
        RecyclerView recyclerView = findViewById(R.id.myRecyclerView);
        LikedAdapter.login loginActivity = (LikedAdapter.login) this;
        LikedAdapter adapter = new LikedAdapter(this,tournaments, loginActivity);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void openCreate() {
    //Uses volley to get the necessary information to open the activity for creating a tournament
        String url = "https://hidden-boss-server.herokuapp.com/games";
        RequestQueue createRequest = Volley.newRequestQueue(this);
        JsonObjectRequest fetchGamesFromDB = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray gamesJSON = response.getJSONArray("games");
                            Intent openCreateTournament = new Intent(LikesActivity.this, CreateActivity.class);
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
                if(error instanceof AuthFailureError){openLogin();}
            }
        });
        createRequest.add(fetchGamesFromDB);
    }

    @Override
    //Since this is the liked activity openLiked does nothing
    protected void openLiked() {}

    @Override
    protected void openRegister() {
        //Uses volley to get the necessary information to open the activity for the tournaments that the user has been registered for
        RequestQueue registerQueue = Volley.newRequestQueue(this);
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
                    Intent registered = new Intent(LikesActivity.this, RegisteredActivity.class);
                    registered.putParcelableArrayListExtra("registeredTournaments", registeredTourenys);
                    startActivity(registered);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        registerQueue.add(getRegisteredTournaments);
    }

    @Override
    protected void openMyTourneys() {
        //Uses volley to get the necessary information to open the activity for the users own tournaments
        RequestQueue myTournamentQueue = Volley.newRequestQueue(this);
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
                    Intent myTourneysIntent = new Intent(LikesActivity.this, EditTournamentsActivity.class);
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

            }
        });
        myTournamentQueue.add(getMyTournaments);
    }

    @Override
    protected void openLogin() {
        //Opens the activity for login
        final int loginRequestCode = 13;
        tournaments.clear();
        Intent loginIntent = new Intent(LikesActivity.this, LoginActivity.class);
        startActivityForResult(loginIntent, loginRequestCode);
    }

    @Override
    public void openLoginActivity() {
        openLogin();
    }
}
