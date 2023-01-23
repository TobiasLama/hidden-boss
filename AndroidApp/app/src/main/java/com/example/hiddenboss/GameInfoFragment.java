package com.example.hiddenboss;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// Fragment that shows information the game a particular tournament is held in.
public class GameInfoFragment extends Fragment {
    private RequestQueue gameInfoQueue;
    private static final String Tag = "GameInfoFragment";

    public GameInfoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_game_info, container, false);

        // Make pointers to the fragment's objects so that they can be given values
        final TextView titleText = view.findViewById(R.id.titleInfo);
        final TextView releaseText = view.findViewById(R.id.releaseInfo);
        final TextView publisherText = view.findViewById(R.id.publisherInfo);
        final TextView platformText = view.findViewById(R.id.platformInfo);

        gameInfoQueue = Volley.newRequestQueue(view.getContext());
        // Dynamically create different URLs depending on the game by passing data from the parent activity
        String url = "https://hidden-boss-server.herokuapp.com/games/" + getArguments().getString("game_id");

        // Sends a request to retrieve information about the game and set the fragment view's contents
        JsonObjectRequest getGame = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    JSONObject game = new JSONObject(response.getString("game"));
                    Log.d(Tag, "Game name: " + game.getString("title"));

                    titleText.setText(game.getString("title"));
                    releaseText.setText(game.getString("release_year"));
                    publisherText.setText(game.getString("publisher"));

                    // If game platforms are specified, set platformText to display these. If not, display a message explaining that none were given.
                    JSONArray platforms = game.getJSONArray("platforms");
                    if (platforms.length() > 0){
                        String platformString = "";
                        // Loop through every platform specified, compiling them all into one string.
                        for (int currentPlatform = 0; currentPlatform < platforms.length(); currentPlatform++){
                            if (currentPlatform == platforms.length()-1){
                                platformString = platformString + platforms.getString(currentPlatform);
                            }
                            else{
                                platformString = platformString + platforms.getString(currentPlatform) + ", ";
                            }
                        }
                        platformText.setText(platformString);
                    }
                    else{
                        platformText.setText("Not specified");
                    }
                }
                catch(JSONException e){
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(Tag, "Volley error with status code " + String.valueOf(error.networkResponse.statusCode));
            }
        });
        gameInfoQueue.add(getGame);
        return view;
    }
}
