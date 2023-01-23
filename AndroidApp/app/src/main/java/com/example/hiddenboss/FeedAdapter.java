package com.example.hiddenboss;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.feedHolder>{
    public interface listButtonListener{
        void tournamentInfo(Tournaments tournament);
        void openLogin();
    }


    //Tag string for debugging and the global variables
    private static final String Tag = "RecyclerViewAdapter";
    private ArrayList<Tournaments> feedTournaments;
    private Context mContext;
    private listButtonListener listener;
    private RequestQueue likeQueue;
    private SharedPreferences preferences;
    private Converters converters = new Converters();


    FeedAdapter(ArrayList<Tournaments> feedTournaments, Context mContext, listButtonListener mListener) {
        this.feedTournaments = feedTournaments;
        this.mContext = mContext;
        this.listener = mListener;
        this.likeQueue = Volley.newRequestQueue(mContext);
        this.preferences = mContext.getSharedPreferences("com.example.hiddenboss",Context.MODE_PRIVATE);
    }

    static class feedHolder extends RecyclerView.ViewHolder{
        //feedHolder class containing all the necessary attributes

        CircleImageView image;
        TextView tournamentTitle;
        TextView tournamentDateTime;
        TextView tournamentLocation;
        LinearLayout parentLayout;
        TextView likes;
        ImageButton likeButton;

        feedHolder(@NonNull View itemView) {
            //All the feedHolder attributes are given pointers to objects on the screen
            super(itemView);
            image = itemView.findViewById(R.id.gameImage);
            tournamentTitle = itemView.findViewById(R.id.tournamentTitle);
            tournamentDateTime = itemView.findViewById(R.id.tournamentDate);
            tournamentLocation = itemView.findViewById(R.id.tournamentLocation);
            parentLayout = itemView.findViewById(R.id.feedItemLayout);
            likes = itemView.findViewById(R.id.amountOfLikes);
            likeButton = itemView.findViewById(R.id.likeButton);
        }
    }

    @NonNull
    @Override
    public feedHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Creates the recyclerview itself
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_item, parent, false);
        return new feedHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final feedHolder holder, final int position) {
        //Fills in the layout of each item
        Log.d(Tag, "onBindViewHolder: Called");

        Glide.with(mContext)
                .asBitmap()
                .load(feedTournaments.get(position).getGame().getImg())
                .into(holder.image);

        holder.tournamentTitle.setText(feedTournaments.get(position).getTitle());
        holder.tournamentDateTime.setText(converters.DateToString(
                feedTournaments.get(position).getStart_year(),
                feedTournaments.get(position).getStart_month(),
                feedTournaments.get(position).getStart_day())
         + " " + feedTournaments.get(position).getStart_time());
        holder.tournamentLocation.setText(feedTournaments.get(position).getLocation());
        holder.likes.setText(String.valueOf(feedTournaments.get(position).getLiked_by().size()));


        //If the user has liked the tournament it sets the color to light blue, otherwise it will stay black
        if(feedTournaments.get(position).getLiked_by().contains(preferences.getString("userName","Missing user"))){
            holder.likeButton.setColorFilter(ContextCompat.getColor(mContext,R.color.colorAccent));
        }
        holder.likeButton.setOnClickListener(new View.OnClickListener() {
            //If the user presses like the method handleLike is used
            @Override
            public void onClick(View v) {
                handleLike(holder,feedTournaments.get(position));
            }
        });

        //Aside from the like button if the user presses an item the user will be moved to a registration view for the corresponding tournament
        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(Tag,"onClick: clicked on " + feedTournaments.get(position).getTitle());
                listener.tournamentInfo(feedTournaments.get(position));
            }
        });

    }

    @Override
    //Amount of items to use in the list
    public int getItemCount() {
        return feedTournaments.size();
    }


    private void handleLike(final feedHolder holder, Tournaments currentTournament){
        //The method handleLike is used to determine if the user is likeing or unliking a tournament and run the appropriate method.
        final String url = "https://hidden-boss-server.herokuapp.com/tourneys/" + currentTournament.getId() + "/likes";
        StringRequest getLikedStatus = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response.contains(preferences.getString("userName","Missing user"))){
                    unLike(url, holder);
                } else {
                    like(url,holder);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(mContext,error.toString(),Toast.LENGTH_LONG).show();

            }
        });
        likeQueue.add(getLikedStatus);

    }

    private void like(final String url, final feedHolder holder){
        //like does a volley request to add the user to the list of users that has liked the tournament, after that is calls the updateLike method
        StringRequest like = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                holder.likeButton.setColorFilter(ContextCompat.getColor(mContext,R.color.colorAccent));
                updateLikes(holder,url);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error instanceof AuthFailureError){
                    listener.openLogin();
                }
            }
        }){
            @Override
            public Map<String, String> getHeaders() {
                try{
                    super.getHeaders();
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + preferences.getString("token","Missing token"));
                    return headers;
                } catch (AuthFailureError a){
                    listener.openLogin();
                }
                return null;
            }
        };

        likeQueue.add(like);
    }

    private void unLike(final String url, final feedHolder holder){
        //unLike does a volley request to remove the user from the list of users that has liked the tournament, after that is calls the updateLike method
        StringRequest unLike = new StringRequest(Request.Method.DELETE, url, new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            holder.likeButton.setColorFilter(ContextCompat.getColor(mContext,R.color.black));
            updateLikes(holder,url);

        }
    }, new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            if(error instanceof AuthFailureError){listener.openLogin();}
        }
    }){
        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            super.getHeaders();
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + preferences.getString("token","Missing token"));
            return headers;
        }
    };

        likeQueue.add(unLike);}

    private void updateLikes(final feedHolder holder, String url){
        //Update likes does a volley request to get the list of users that has liked the tournament, then sets the amount of likes equal to its length
        JsonObjectRequest updateRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray likes = response.getJSONArray("liked_by");
                    holder.likes.setText(String.valueOf(likes.length()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error instanceof AuthFailureError) {
                    Toast.makeText(mContext,error.toString(),Toast.LENGTH_LONG).show();

                }
            }
        });
        likeQueue.add(updateRequest);
    }


}

