package com.example.hiddenboss;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

//The create activity is where the user can create their own tournaments if the app supports that fighting game
//and they enter in a title for the tournament, a description, location and when it will start and end

public class CreateActivity extends BaseActivity {
    //All global variables for the activity is defined at the top
    private TextInputLayout inputTitle,inputDescription,inputLocation;
    private TextView startDateView, startTimeView, endDateView, endTimeView;
    private Calendar startDateTime, endDateTime;
    private AutoCompleteTextView enterGame;
    private JSONArray gamesJSON;
    private ArrayList<String> gameNames = new ArrayList<>();
    private InputValidators validators = new InputValidators(this);
    private DialogPickers dialogPickers;
    private Converters converters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        //GamesSTR is all the information about the games in string form
        String gamesSTR = getIntent().getStringExtra("gamesJSON");
        //setupToolbar is a method from the BaseActivity
        setupToolbar();
        //Uses gamesSTR to first convert it to a JSONArray and then uses gson to
        // convert each element to a Games object and add extract the string of the game title and add it to
        // the ArrayList of all titles. This is used for the suggestions when the user chooses game
        try {
            gamesJSON = new JSONArray(gamesSTR);
            for (int i = 0; i < gamesJSON.length(); i++) {
                JSONObject currentGame = gamesJSON.getJSONObject(i);
                Gson gson = new Gson();
                gameNames.add(gson.fromJson(String.valueOf(currentGame),Games.class).getTitle());

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //Initiate all global variables and onClickListeners
        inputTitle = findViewById(R.id.enterTitle);
        inputDescription = findViewById(R.id.enterDescription);
        inputLocation = findViewById(R.id.enterLocation);
        enterGame = findViewById(R.id.enterGame);
        ArrayAdapter<String> suggestionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, gameNames);
        enterGame.setAdapter(suggestionAdapter);
        Button finishButton = findViewById(R.id.finishTournament);
        startDateView = findViewById(R.id.startDate);
        startTimeView = findViewById(R.id.startTime);
        endDateView = findViewById(R.id.endDate);
        endTimeView = findViewById(R.id.endTime);
        startDateTime = Calendar.getInstance();
        endDateTime = Calendar.getInstance();
        dialogPickers = new DialogPickers();
        converters = new Converters();

        //The listeners uses a DialogPickers object
        startDateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogPickers.datePicker(v,startDateView,startDateTime);
            }
        });

        startTimeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogPickers.timePicker(v,startTimeView, startDateTime);
            }
        });

        endDateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogPickers.datePicker(v,endDateView,endDateTime);
            }
        });

        endTimeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogPickers.timePicker(v,endTimeView, endDateTime);
            }
        });
        //Conformation button. If confirm input returns true a new tournament is created and sent to the database through insertNewTournament
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (confirmInput()) {
                    JSONObject newTournament = new JSONObject();
                    try {
                        newTournament.put("description",Objects.requireNonNull(inputDescription.getEditText()).getText().toString());
                        newTournament.put("end_day", endDateTime.get(Calendar.DAY_OF_MONTH));
                        newTournament.put("end_month",endDateTime.get(Calendar.MONTH));
                        newTournament.put("end_time",converters.TimeToString(endDateTime.get(Calendar.HOUR_OF_DAY),endDateTime.get(Calendar.MINUTE)));
                        newTournament.put("end_year",endDateTime.get(Calendar.YEAR));
                        newTournament.put("game_id",getGameId(gamesJSON,Objects.requireNonNull(enterGame).getText().toString()));
                        newTournament.put("location", Objects.requireNonNull(inputLocation.getEditText()).getText().toString());
                        newTournament.put("start_day", startDateTime.get(Calendar.DAY_OF_MONTH));
                        newTournament.put("start_month", startDateTime.get(Calendar.MONTH));
                        newTournament.put("start_time",converters.TimeToString(startDateTime.get(Calendar.HOUR_OF_DAY),endDateTime.get(Calendar.MINUTE)));
                        newTournament.put("start_year", startDateTime.get(Calendar.YEAR));
                        newTournament.put("title", Objects.requireNonNull(inputTitle.getEditText()).getText().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                   insertNewTournament(newTournament);

                }
            }
        });




    }
    //getGameId takes in a JSONArray of games and returns the id of the desired title
    private String getGameId(JSONArray array, String title) throws JSONException {
        for (int i = 0; i < array.length(); i++) {
            JSONObject current = array.getJSONObject(i);
            if (current.optString("title").equals(title)){
                return current.optString("id");
            }
        }
        return null;
    }

    //confirmInput uses methods from validators to check if all the required information has been correctly entered
    public boolean confirmInput(){
        return !(!validators.validateTitle(Objects.requireNonNull(inputTitle.getEditText()).getText().toString().trim(), inputTitle)
                | !validators.validateDescription(Objects.requireNonNull(inputDescription.getEditText()).getText().toString().trim(),inputDescription)
                | !validators.validateLocation(Objects.requireNonNull(inputLocation.getEditText()).getText().toString().trim(),inputLocation)
                | !validators.validateDateTime(startDateView,startTimeView,endDateView,endTimeView,startDateTime,endDateTime)
                | !validators.validateGame(enterGame.getText().toString(),gameNames,enterGame));
    }





    //insertNewTournament uses the Volley library to insert the tournament into the database and move the user back to the main activity
    public void insertNewTournament(final JSONObject tournament){
        RequestQueue insertQueue = Volley.newRequestQueue(this);
        String url = "https://hidden-boss-server.herokuapp.com/tourneys";

        StringRequest postRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //After the request is done it ends this activity and returns to main
                setResult(RESULT_FIRST_USER);
                finish();
                Log.d("Create Tournament", tournament + " posted");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Create Tournament","post failed " + String.valueOf(error.networkResponse.statusCode));
                error.printStackTrace();
            }
        }){
            @Override
            public byte[] getBody() throws AuthFailureError {
                return tournament.toString().getBytes();
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                super.getHeaders();
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " +  preferences.getString("token","Missing token"));
                return headers;
            }
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

        };
        insertQueue.add(postRequest);
    }



    @Override
    //Since this is the create view the abstract method does nothing here
    public void openCreate() {}

    @Override
    //Uses volley to get the necessary information to open the registered activity
    public void openRegister() {
        RequestQueue x = Volley.newRequestQueue(this);
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
                    Intent registered = new Intent(CreateActivity.this, RegisteredActivity.class);
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
        x.add(getRegisteredTournaments);
    }

    @Override
    //Uses volley to get the necessary information to open the activity for the user's own tournaments
    public void openMyTourneys() {
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
                    Intent myTourneysIntent = new Intent(CreateActivity.this, EditTournamentsActivity.class);
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
    public void openLiked() {
        //Uses volley to get the necessary information to open the activity for the tournaments that the user has liked
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
                    Intent liked = new Intent(CreateActivity.this, LikesActivity.class);
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
    protected void openLogin() {}
}
