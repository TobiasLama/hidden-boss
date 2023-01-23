package com.example.hiddenboss;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

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
public class MainActivity extends BaseActivity implements FeedAdapter.listButtonListener{
    //Global variables
    private ArrayList<Tournaments> tournaments = new ArrayList<>();
    private static final int updateFeedCode = 100;
    private static final int loginRequestCode = 13;
    private static final int openRegistrationCode = 25;
    private RequestQueue requestFeed;
    private Gson tournamentGson = new Gson();


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //onActivityResult is used when a new intent is opened with startActivityForResult.
        //The request code is used as the argument in startActivityForResult, it is the code that tracks what request has been completed
        //Result code is sent back and determines the appropriate response. Data is the data that's been sent back
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == updateFeedCode){
            //When the response bear the updateFeedCode the method getJsonTournaments is called
            if (resultCode == RESULT_FIRST_USER) {
                getJsonTournaments();
            }
        } else if (requestCode == loginRequestCode){
            //After the user has logged in getJsonTournaments() is called
            assert data != null;
            getJsonTournaments();
        }
    }

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //The layout is created and toolbar is setup from the BaseActivity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestFeed = Volley.newRequestQueue(this);
        setupToolbar();
        removeBackButton();
        //Checks if the user is logged in using volley, if the user is it will call getJsonTournaments and if not loginOpen is called
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://hidden-boss-server.herokuapp.com/check_token";
        JsonObjectRequest checkToken = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                getJsonTournaments();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                loginOpen();
            }
        });
        queue.add(checkToken);
    }

    //Refreshes the tournaments when the user returns to the feed
    @Override
    protected void onResume() {
        super.onResume();
        tournaments.clear();
        getJsonTournaments();
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
                            Intent openCreateTournament = new Intent(MainActivity.this, CreateActivity.class);
                            openCreateTournament.putExtra("gamesJSON", gamesJSON.toString());
                            startActivityForResult(openCreateTournament, updateFeedCode);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error instanceof AuthFailureError){loginOpen();}
            }
        });
        createRequest.add(fetchGamesFromDB);
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
                    for (int i = 0; i < likedArray.length(); i++) {
                        JSONObject currentT = likedArray.getJSONObject(i);
                        likedTourneys.add(tournamentGson.fromJson(String.valueOf(currentT), Tournaments.class));
                    }
                    Intent liked = new Intent(MainActivity.this, LikesActivity.class);
                    liked.putParcelableArrayListExtra("likedTournaments", likedTourneys);
                    startActivityForResult(liked,updateFeedCode);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        requestFeed.add(getLikedTournaments);
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
                    for (int i = 0; i < registeredArray.length(); i++) {
                        JSONObject currentT = registeredArray.getJSONObject(i);
                        registeredTourenys.add(tournamentGson.fromJson(String.valueOf(currentT), Tournaments.class));
                    }
                    Intent registered = new Intent(MainActivity.this, RegisteredActivity.class);
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
        requestFeed.add(getRegisteredTournaments);
    }

    @Override
    protected void openMyTourneys() {
        //Uses volley to get the necessary information to open the activity for the user's own tournaments
        String urlMyTournaments ="https://hidden-boss-server.herokuapp.com/users/" + preferences.getString("userName", "error") + "/hosted_tourneys";

        JsonObjectRequest getMyTournaments = new JsonObjectRequest(Request.Method.GET, urlMyTournaments, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray myTournamentsJSON = response.getJSONArray("tourneys");
                    final ArrayList<Tournaments> myTourneys = new ArrayList<>();
                    for (int i = 0; i < myTournamentsJSON.length(); i++) {
                        JSONObject myCurrentTournament = myTournamentsJSON.getJSONObject(i);
                        final Tournaments myTournaments=tournamentGson.fromJson(String.valueOf(myCurrentTournament), Tournaments.class);
                        myTourneys.add(myTournaments);
                    }
                    Intent myTourneysIntent = new Intent(MainActivity.this, EditTournamentsActivity.class);
                    myTourneysIntent.putParcelableArrayListExtra("myT", myTourneys);
                    startActivityForResult(myTourneysIntent, updateFeedCode);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        requestFeed.add(getMyTournaments);
    }

    private void getJsonTournaments(){
        //getJsonTournaments uses volley to get the tournaments that will appear on the feed
        final String tournamentUrl = "https://hidden-boss-server.herokuapp.com/tourneys/game";
        JsonObjectRequest feedRequest = new JsonObjectRequest(Request.Method.GET, tournamentUrl, null,
                new Response.Listener<JSONObject>() {
            //Called when request successful
                    @Override
                    public void onResponse(final JSONObject response) {
                        try {
                            //It empties the tournaments arrayList. If it does not then will just add tournaments each time it is called
                            tournaments.clear();
                            JSONArray feedArray = response.getJSONArray("tourneys");
                            //All tournaments from the response is added to tournaments
                            for (int i = 0; i < feedArray.length(); i++) {
                                JSONObject currentTournament = feedArray.getJSONObject(i);
                                final Tournaments currentFeedTournament=tournamentGson.fromJson(String.valueOf(currentTournament), Tournaments.class);
                                tournaments.add(currentFeedTournament);
                            }
                            //createFeed is called to build the actual feed, getJsonTournaments only fills tournaments with the tournament objects
                            createFeed();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            //Called when request failed
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        requestFeed.add(feedRequest);

    }



    @Override
    //Opens a new activity for registration with information from the RecyclerViewAdapter
    public void tournamentInfo(Tournaments tournament) {
        openRegistration(tournament);
    }

    @Override
    public void openLogin() {
        //Opens loginOpen. The reason for why there is two login functions is because one is from the abstract class and one is from an interface created in feedAdapter.
        //The interface is required for the login activity to be opened from the fragment holding the feed
        loginOpen();
    }

    public void openRegistration(Tournaments tournament){
        //Opens a new activity for registration
        final Intent registrationIntent = new Intent(this, RegistrationActivity.class);
        registrationIntent.putExtra("title",tournament.getTitle());
        registrationIntent.putExtra("startEnd",tournament.getStartToEnd());
        registrationIntent.putExtra("location", tournament.getLocation());
        registrationIntent.putExtra("host", "Hosted by " + tournament.getHost_id());
        registrationIntent.putExtra("imgUrl",tournament.getGame().getImg());
        registrationIntent.putExtra("description", tournament.getDescription());
        registrationIntent.putExtra("id", tournament.getId());
        registrationIntent.putExtra("game_id", tournament.getGame_id());
        JsonObjectRequest amountOfLikesRequest = new JsonObjectRequest(Request.Method.GET, "https://hidden-boss-server.herokuapp.com/tourneys/" + tournament.getId()+"/likes", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    registrationIntent.putExtra("likesString", String.valueOf(response.getJSONArray("liked_by").length()) + " Likes");
                    startActivityForResult(registrationIntent, openRegistrationCode);
                } catch (JSONException e) {
                    Toast.makeText(MainActivity.this,e.toString(),Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this,String.valueOf(error.networkResponse.statusCode),Toast.LENGTH_LONG).show();
            }
        });
        requestFeed.add(amountOfLikesRequest);

    }



    public void createFeed() {
        //Uses the Tournament objects stored in tournaments to create a feed
        Bundle feedTournaments = new Bundle();
        feedTournaments.putParcelableArrayList("feedTournaments", tournaments);
        FeedFragment recycleFeedFragment = new FeedFragment();
        FragmentTransaction transaction =
                getSupportFragmentManager().beginTransaction();
        recycleFeedFragment.setArguments(feedTournaments);
        transaction.replace(R.id.feedFrame, recycleFeedFragment);
        transaction.addToBackStack(null);
        transaction.commit();
        }

    public void loginOpen(){
        //Open the login activity
        tournaments.clear();
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivityForResult(loginIntent, loginRequestCode);
    }



}



