package com.example.hiddenboss;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// Fragment that displays comments that have been made on a tournament with the help of, among others, RecyclerView and the Adapter CommentAdapter.
public class CommentFragment extends Fragment {
    private ArrayList<Comment> comments = new ArrayList<>();
    private CommentAdapter adapter;
    private SharedPreferences preferences;
    private RequestQueue commentQueue;
    private static final String Tag = "CommentFragment";

    public CommentFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_comment, container, false);

        commentQueue = Volley.newRequestQueue(view.getContext());
        // Dynamically create different URLs depending on the tournament by passing data from the parent activity
        String url = "https://hidden-boss-server.herokuapp.com/tourneys/" + getArguments().getString("tourney_id") + "/comments";
        // Create a request to retrieve all comments, so that they then can be displayed
        JsonObjectRequest getComments = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonComments = response.getJSONArray("comments");

                            // Loop through and make a Comment object for every comment in the JSON response data
                            for (int commentIndex = 0; commentIndex < jsonComments.length(); commentIndex++) {
                                JSONObject currentComment = jsonComments.getJSONObject(commentIndex);

                                String commentUser = currentComment.getString("user");
                                String commentMessage = currentComment.getString("message");
                                String commentId = currentComment.getString("id");
                                comments.add(new Comment(commentUser, commentMessage, commentId));
                            }

                            // The RecyclerView for the list of comments is made using the Comment objects from the JSON data
                            RecyclerView commentsView = view.findViewById(R.id.comments);
                            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(view.getContext());
                            commentsView.setLayoutManager(layoutManager);
                            adapter = new CommentAdapter(comments, view.getContext());
                            commentsView.setAdapter(adapter);
                        }
                        catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(Tag, "Volley error with status code " + String.valueOf(error.networkResponse.statusCode));
            }
        });
        // Add request to request queue
        commentQueue.add(getComments);

        Button commentButton = view.findViewById(R.id.postComment);
        // commentText needs to be final since it is is accessed from within the on-click listener
        final TextInputEditText commentText = view.findViewById(R.id.commentInput);
        // commentButton sends the user-inputted text from commentText as a commment
        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // If a user is currently logged in, post the comment. If not, open LoginActivity, since a user needs to be logged in to make a comment
                preferences = getContext().getSharedPreferences("com.example.hiddenboss", Context.MODE_PRIVATE);

                if (preferences.getBoolean("isLoggedIn",false)) {
                    postComment(commentText);
                }
                else {
                    Intent loginIntent = new Intent(getActivity(), LoginActivity.class);
                    startActivityForResult(loginIntent, 13);
                }
            }
        });
        return view;
    }

    private void postComment(final TextInputEditText comment){
        // Function where the request to post a comment is made
        // commentText, comment and commentJSON need to be final since they're accessed and modified from within the on-click listener
        final String commentText = comment.getEditableText().toString();
        // A JSON string which commentJSON is made from
        String jsonString = String.format("{\'message\': \'%s\'}", comment.getText());
        final JSONObject commentJson;
        try{
            commentJson = new JSONObject(jsonString);
            Log.d(Tag, "commentJSON content: " + commentJson.toString());
            // Dynamically create different URLs depending on the tournament by passing data from the parent activity
            final String url = "https://hidden-boss-server.herokuapp.com/tourneys/" + getArguments().getString("tourney_id") + "/comments";
            // Make the request which posts comments
            StringRequest postComment = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(Tag, "Successfully posted comment");

                    JsonObjectRequest updateComments = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                /* CLears the current list of comments, and rebuilds it to include the new, posted comment,
                                   as well as any other new comments that have been made since onCreateView */
                                JSONArray jsonComments = response.getJSONArray("comments");
                                comments.clear();

                                // Adds comments in a way identical to getCommments
                                for (int commentIndex = 0; commentIndex < jsonComments.length(); commentIndex++) {
                                    JSONObject currentComment = jsonComments.getJSONObject(commentIndex);

                                    String commentUser = currentComment.getString("user");
                                    String commentMessage = currentComment.getString("message");
                                    String commentId = currentComment.getString("id");

                                    comments.add(new Comment(commentUser, commentMessage, commentId));
                                }

                                // Update the RecyclerView to display the new list of comments
                                if(comments.size() == 1){
                                    RecyclerView commentsView = getView().findViewById(R.id.comments);
                                    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getView().getContext());
                                    commentsView.setLayoutManager(layoutManager);
                                    adapter = new CommentAdapter(comments, getView().getContext());
                                    commentsView.setAdapter(adapter);
                                }
                                else{
                                    adapter.updateComments(comments);
                                }
                                // Clear the TextInputEditText of all text, so that a new comment can be written seamlessly
                                comment.setText("");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(Tag, "Volley error with status code " + String.valueOf(error.networkResponse.statusCode));
                        }
                    }
                    );
                    // Add the request to the request queue
                    commentQueue.add(updateComments);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(Tag, "Volley error with status code " + String.valueOf(error.networkResponse.statusCode));
                }
            }) {
                        @Override
                        public byte[] getBody(){
                            // getBody is necessary in order to send JSON data with a StringRequest
                            return commentJson.toString().getBytes();
                        }
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            /* Commenting on a tournament is an action that requires user authentication.
                               Thus, authentication needs to be sent in the request header. */
                            super.getHeaders();
                            Map<String, String> headers = new HashMap<>();
                            // An Authorization request header is put into the Map, getting the access token from a SharedPreferences file.
                            headers.put("Authorization", "Bearer " +  preferences.getString("token","Missing token"));
                            return headers;
                        }
                        @Override
                        public String getBodyContentType() {
                            // define the data sent with the request as JSON
                            return "application/json; charset=utf-8";
                        }
                    };
                    commentQueue.add(postComment);
                }
        catch(JSONException x){
                    x.printStackTrace();
                }
            }
        }
