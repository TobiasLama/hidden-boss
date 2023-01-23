package com.example.hiddenboss;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

public class EditTournamentsActivity extends BaseActivity{
    //Global variables used
    private static final String TAG = "Edit";
    private ArrayList<Tournaments> tournaments;
    private RecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Global variables are given values and the layout is created
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_tournaments);
        Intent intent = getIntent();
        tournaments = Objects.requireNonNull(intent.getExtras()).getParcelableArrayList("myT");
        recyclerView = findViewById(R.id.myRecyclerView);
        EditAdapter adapter = new EditAdapter(this,tournaments);
        setupToolbar();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Log.d(TAG, "EditAdapter: finished EditAdapter.");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //onActivityResult runs different codes depending on the request and result code.
        //The request code is the code for the request that is sent and result is for what kind of result it gets back
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 200){
            if (resultCode == RESULT_FIRST_USER){
                //The only result where requesting is for the tournaments created by the user when they go to the view for edit tournaments
                assert data != null;
                ArrayList<Tournaments> updateTournaments = data.getParcelableArrayListExtra("myTourneys");
                EditAdapter adapter = new EditAdapter(this,updateTournaments);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
            }

        }
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
                            Intent openCreateTournament = new Intent(EditTournamentsActivity.this, CreateActivity.class);
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
    //Uses volley to get the necessary information to open the activity for the tournaments that the user has liked
    protected void openLiked() {
        RequestQueue likedRequest = Volley.newRequestQueue(this);
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
                    Intent liked = new Intent(EditTournamentsActivity.this, LikesActivity.class);
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
        likedRequest.add(getLikedTournaments);
    }

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
                    Intent registered = new Intent(EditTournamentsActivity.this, RegisteredActivity.class);
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
    //It does nothing since the user is already on the view for their tournaments
    protected void openMyTourneys() {}

    @Override
    protected void openLogin() {
        //Opens the login view if the user does not have a valid token
        final int loginRequestCode = 13;
        tournaments.clear();
        Intent loginIntent = new Intent(EditTournamentsActivity.this, LoginActivity.class);
        startActivityForResult(loginIntent, loginRequestCode);
    }
}
