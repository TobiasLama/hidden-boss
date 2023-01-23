package com.example.hiddenboss;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

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

public class RegisteredActivity extends BaseActivity implements LikedAdapter.login{
    private ArrayList<Tournaments> tournaments;
    private LikedAdapter.login loginCalled;
    RequestQueue infoQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registered);
        infoQueue = Volley.newRequestQueue(this);
        Intent intent = getIntent();
        tournaments = Objects.requireNonNull(intent.getExtras()).getParcelableArrayList("registeredTournaments");
        RecyclerView recyclerView = findViewById(R.id.myRecyclerView);
        loginCalled = (LikedAdapter.login) this;
        LikedAdapter adapter = new LikedAdapter(this,tournaments,loginCalled);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        setupToolbar();
    }

    @Override
    protected void openCreate() {
        String url = "https://hidden-boss-server.herokuapp.com/games";
        JsonObjectRequest fetchGamesFromDB = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray gamesJSON = response.getJSONArray("games");
                            Intent openCreateTournament = new Intent(RegisteredActivity.this, CreateActivity.class);
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
        infoQueue.add(fetchGamesFromDB);
    }

    @Override
    protected void openLiked() {
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
                    Intent liked = new Intent(RegisteredActivity.this, LikesActivity.class);
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

            }
        });
        infoQueue.add(getLikedTournaments);
    }

    @Override
    protected void openRegister() {

    }

    @Override
    protected void openMyTourneys() {
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
                    Intent myTourneysIntent = new Intent(RegisteredActivity.this, EditTournamentsActivity.class);
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
        infoQueue.add(getMyTournaments);
    }

    @Override
    protected void openLogin() {
        final int loginIntentCode = 13;
        Intent i = new Intent(RegisteredActivity.this, LoginActivity.class);
        startActivityForResult(i, loginIntentCode);
    }

    @Override
    public void openLoginActivity() {
        openLogin();
    }
}
