package com.example.hiddenboss;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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

//TournamentEditActivity is the activity where users can edit their own existing tournaments
public class TournamentEditActivity extends BaseActivity{

    private Tournaments tournamentToEdit;
    private AutoCompleteTextView game;
    private TextInputLayout title,description,location;
    private TextView startDate, endDate, startTime, endTime;
    private Calendar startDateTime,endDateTime;
    private ArrayList<String> gameNames = new ArrayList<>();
    private JSONArray gamesJSON;
    private Button update, delete;
    private RequestQueue editQueue;
    private Converters converters = new Converters();
    private DialogPickers pickers = new DialogPickers();
    private InputValidators validators;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //All the variables are assigned values
        super.onCreate(savedInstanceState);
        editQueue = Volley.newRequestQueue(this);
        validators = new InputValidators(this);
        setContentView(R.layout.activity_tournament_edit);
        Intent intent = getIntent();
        tournamentToEdit = Objects.requireNonNull(intent.getExtras()).getParcelable("tourneyToEdit");
        game = findViewById(R.id.editGame);
        ArrayAdapter<String> suggestionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, gameNames);
        game.setAdapter(suggestionAdapter);
        title = findViewById(R.id.editTitle);
        description = findViewById(R.id.editDescription);
        location = findViewById(R.id.editLocation);
        startDate = findViewById(R.id.editStartDate);
        startTime = findViewById(R.id.editStartTime);
        endDate = findViewById(R.id.editEndDate);
        endTime = findViewById(R.id.editEndTime);
        update = findViewById(R.id.updateTournament);
        delete = findViewById(R.id.deleteButton);
        startDateTime = Calendar.getInstance();
        endDateTime = Calendar.getInstance();
        //Toolbar is set up from base activity
        setupToolbar();
        try {
            //The listArray with game names is assigned elements from the JSONArray inside the intent
            gamesJSON = new JSONArray( intent.getStringExtra("gamesJSON"));
            for (int i = 0; i < gamesJSON.length(); i++) {
                JSONObject currentGame = gamesJSON.getJSONObject(i);
                Gson gson = new Gson();
                gameNames.add(gson.fromJson(String.valueOf(currentGame), Games.class).getTitle());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //Methods for entering existing information about the tournament and initiation of listeners are called
        enterPreviousInfo();
        initOnClickListeners();

    }

    @SuppressLint("SetTextI18n")
    private void enterPreviousInfo(){
        //enterPreviousInfo sets all the TextViews and TextInputLayouts to the information that the tournament object already contain
        game.setText(tournamentToEdit.getGame().getTitle());
        Objects.requireNonNull(title.getEditText()).setText(tournamentToEdit.getTitle());
        Objects.requireNonNull(description.getEditText()).setText(tournamentToEdit.getDescription());
        Objects.requireNonNull(location.getEditText()).setText(tournamentToEdit.getLocation());
        startDate.setText(converters.DateToString(tournamentToEdit.getStart_year(), tournamentToEdit.getStart_month(), tournamentToEdit.getStart_day()));
        endDate.setText(converters.DateToString(tournamentToEdit.getEnd_year(), tournamentToEdit.getEnd_month(), tournamentToEdit.getEnd_day()));
        startTime.setText(tournamentToEdit.getStart_time());
        endTime.setText(tournamentToEdit.getEnd_time());

    }

    private void initOnClickListeners(){
        //initOnClickListeners initiates all the onClickListeners used for the activity

        //DialogPickers for picking start and end dates and times.
        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickers.datePicker(v,startDate, startDateTime);
            }
        });

        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickers.timePicker(v,startTime, startDateTime);
            }
        });

        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickers.datePicker(v,endDate,endDateTime);
            }
        });

        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickers.timePicker(v,endTime,endDateTime);
            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            //The update button uses inputValidators to check if all the input is valid. If it is it uses volley to update the information in the database
            @Override
            public void onClick(View v) {
                if(confirmInput()){
                    String editUrl = "https://hidden-boss-server.herokuapp.com/tourneys/" + tournamentToEdit.getId() + "/edit";
                    final JSONObject editedTournament = new JSONObject();
                    try {
                        editedTournament.put("description",Objects.requireNonNull(description.getEditText()).getText().toString());
                        editedTournament.put("end_day", endDateTime.get(Calendar.DAY_OF_MONTH));
                        editedTournament.put("end_month",endDateTime.get(Calendar.MONTH));
                        editedTournament.put("end_time",converters.TimeToString(endDateTime.get(Calendar.HOUR_OF_DAY),endDateTime.get(Calendar.MINUTE)));
                        editedTournament.put("end_year",endDateTime.get(Calendar.YEAR));
                        editedTournament.put("game_id",getGameId(gamesJSON,Objects.requireNonNull(game).getText().toString()));
                        editedTournament.put("location", Objects.requireNonNull(location.getEditText()).getText().toString());
                        editedTournament.put("start_day", startDateTime.get(Calendar.DAY_OF_MONTH));
                        editedTournament.put("start_month", startDateTime.get(Calendar.MONTH));
                        editedTournament.put("start_time",converters.TimeToString(startDateTime.get(Calendar.HOUR_OF_DAY),endDateTime.get(Calendar.MINUTE)));
                        editedTournament.put("start_year", startDateTime.get(Calendar.YEAR));
                        editedTournament.put("title", Objects.requireNonNull(title.getEditText()).getText().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    StringRequest submitEdit = new StringRequest(Request.Method.POST, editUrl, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            updateMyList();
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                        }
                    }){
                        @Override
                        public byte[] getBody() throws AuthFailureError {
                            return editedTournament.toString().getBytes();
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
                    editQueue.add(submitEdit);
                }
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //The delete button removes the tournament from the database if the user presses "YES" when asked if they are sure
                AlertDialog.Builder builder = new AlertDialog.Builder(TournamentEditActivity.this);
                builder.setTitle("Delete this tournament?");
                builder.setMessage("Are you sure you want to delete this tournament?");
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteTournament();
                    }
                });

                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

            }
        });

    }

    public boolean confirmInput(){
        //Uses validators' methods for validating the different kinds of input
        return !(!validators.validateTitle(Objects.requireNonNull(title.getEditText()).getText().toString().trim(),title)
                | !validators.validateDescription(Objects.requireNonNull(description.getEditText()).getText().toString().trim(),description)
                | !validators.validateLocation(Objects.requireNonNull(location.getEditText()).getText().toString().trim(), location)
                | !validators.validateDateTime(startDate,startTime,endDate,endTime,startDateTime,endDateTime)
                | !validators.validateGame(game.getText().toString(),gameNames,game));
    }

    private String getGameId(JSONArray array, String title) throws JSONException {
        //getGameId goes through the JSONArray of games to find the desired title and return its id
        for (int i = 0; i < array.length(); i++) {
            JSONObject current = array.getJSONObject(i);
            if (current.optString("title").equals(title)){
                return current.optString("id");
            }
        }
        return null;
    }

    private void deleteTournament(){
        //deleteTournament makes a volley request for deleting the desired tournament
        String deleteUrl =" https://hidden-boss-server.herokuapp.com/tourneys/" + tournamentToEdit.getId();

        StringRequest deleteTournament = new StringRequest(Request.Method.DELETE, deleteUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                updateMyList();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error instanceof AuthFailureError){openLogin();}
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                super.getHeaders();
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " +  preferences.getString("token","Missing token"));
                return headers;
            }
        };

        editQueue.add(deleteTournament);
    }

    private void updateMyList(){
        //updateMyList updates the feed after the user has either deleted or edited a tournament
        String urlMyTournaments ="https://hidden-boss-server.herokuapp.com/users/" + preferences.getString("userName", "error") + "/hosted_tourneys";

        JsonObjectRequest getMyTournaments = new JsonObjectRequest(Request.Method.GET, urlMyTournaments, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Gson gson = new Gson();
                    JSONArray myTournamentsJSON = response.getJSONArray("tourneys");
                    final ArrayList<Tournaments> myTourneys = new ArrayList<>();
                    for (int i = 0; i < myTournamentsJSON.length(); i++) {
                        JSONObject myCurrentTournament = myTournamentsJSON.getJSONObject(i);
                        final Tournaments myTournaments=gson.fromJson(String.valueOf(myCurrentTournament), Tournaments.class);
                        myTourneys.add(myTournaments);
                    }

                    Intent updateListIntent = new Intent();
                    updateListIntent.putParcelableArrayListExtra("myTourneys",myTourneys);
                    setResult(RESULT_FIRST_USER,updateListIntent);
                    finish();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse.statusCode == 404){
                    Intent updateListIntent = new Intent();
                    updateListIntent.putParcelableArrayListExtra("myTourneys",new ArrayList<Tournaments>());
                    setResult(RESULT_FIRST_USER,updateListIntent);
                    finish();
                }
            }
        });
        editQueue.add(getMyTournaments);
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
                            Intent openCreateTournament = new Intent(TournamentEditActivity.this, CreateActivity.class);
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
        editQueue.add(fetchGamesFromDB);
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
                    Intent liked = new Intent(TournamentEditActivity.this, LikesActivity.class);
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
        editQueue.add(getLikedTournaments);
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
                    Intent registered = new Intent(TournamentEditActivity.this, RegisteredActivity.class);
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
        editQueue.add(getRegisteredTournaments);
    }

    @Override
    //openMyTourneys does nothing here
    protected void openMyTourneys() { }

    @Override
    protected void openLogin() {
        //Opens the login activty
        final int testLoginOfflineCode = 13;
        Intent loginIntent = new Intent(TournamentEditActivity.this, LoginActivity.class);
        startActivityForResult(loginIntent, testLoginOfflineCode);
    }



}
