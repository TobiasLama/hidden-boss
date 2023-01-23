
package com.example.hiddenboss;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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

//LikedAdapter is the recyclerviewadapter used for all the tournaments the user has liked
public class LikedAdapter extends RecyclerView.Adapter<LikedAdapter.likedHolder> {
    public interface login{
        void openLoginActivity();
    }
        //Tag string used for debugging and global variables
        private static final String Tag = "RecyclerViewAdapter";
        private Context mContext;
        private ArrayList<Tournaments> likedTournaments = new ArrayList<>();
        private SharedPreferences preferences;
        private RequestQueue likeQueue;
        private Converters converters = new Converters();
        private login openLogin;

    LikedAdapter(Context mContext, ArrayList<Tournaments> likedTournaments, LikedAdapter.login openLogin) {
        //Global variables are assigned values
            this.mContext = mContext;
            this.likedTournaments = likedTournaments;
            this.likeQueue = Volley.newRequestQueue(mContext);
            this.preferences = mContext.getSharedPreferences("com.example.hiddenboss",Context.MODE_PRIVATE);
            this.openLogin = openLogin;
    }

        static class likedHolder extends RecyclerView.ViewHolder {
            //likedHolder class containing all the necessary attributes

            CircleImageView image;
            TextView tournamentTitle;
            TextView tournamentDateTime;
            TextView tournamentLocation;
            LinearLayout parentLayout;
            TextView likes;
            ImageButton likeButton;

            likedHolder(@NonNull View itemView) {
                //The attributes are given pointers to objects on the screen
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
    public likedHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //The recyclerview is created
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_item, parent, false);
        return new likedHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final likedHolder holder, final int position) {
        //The layout and elements of each item is determined in onBindViewHolder
        //The circular image is assigned a bitmap from the url that each item holds
        Glide.with(mContext)
                .asBitmap()
                .load(likedTournaments.get(position).getGame().getImg())
                .into(holder.image);

        //The title, date and location of the item is set
        holder.tournamentTitle.setText(likedTournaments.get(position).getTitle());
        holder.tournamentDateTime.setText(converters.DateToString(
                likedTournaments.get(position).getStart_year(),
                likedTournaments.get(position).getStart_month(),
                likedTournaments.get(position).getStart_day())
                + " " + likedTournaments.get(position).getStart_time());
        holder.tournamentLocation.setText(likedTournaments.get(position).getLocation());

        //Amount of likes and weather or not the user has liked the tournament is set
        holder.likes.setText(String.valueOf(likedTournaments.get(position).getLiked_by().size()));
        if(likedTournaments.get(position).getLiked_by().contains(preferences.getString("userName","Missing user"))){
            holder.likeButton.setColorFilter(ContextCompat.getColor(mContext,R.color.colorAccent));
        }
        //When the liked button is pressed handleLike is called
        holder.likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               handleLike(holder,likedTournaments.get(position));
            }
        });

        //When the item itself is pressed a new registration activity will open and all the information of the tournament object is transferred as extra to the new activity
        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Log.d(Tag,"onClick: clicked on " + likedTournaments.get(position).getTitle());
                final Intent registrationIntent = new Intent(mContext, RegistrationActivity.class);
                registrationIntent.putExtra("title",likedTournaments.get(position).getTitle());
                registrationIntent.putExtra("startEnd",likedTournaments.get(position).getStartToEnd());
                registrationIntent.putExtra("location", likedTournaments.get(position).getLocation());
                registrationIntent.putExtra("host", "Hosted by " + likedTournaments.get(position).getHost_id());
                registrationIntent.putExtra("imgUrl",likedTournaments.get(position).getGame().getImg());
                registrationIntent.putExtra("description", likedTournaments.get(position).getDescription());
                registrationIntent.putExtra("id", likedTournaments.get(position).getId());
                JsonObjectRequest amountOfLikesRequest = new JsonObjectRequest(Request.Method.GET, "https://hidden-boss-server.herokuapp.com/tourneys/" + likedTournaments.get(position).getId()+"/likes", null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            registrationIntent.putExtra("likesString", String.valueOf(response.getJSONArray("liked_by").length()) + " Likes");
                            mContext.startActivity(registrationIntent);
                        } catch (JSONException e) {
                            Toast.makeText(mContext,e.toString(),Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(mContext,String.valueOf(error.networkResponse.statusCode),Toast.LENGTH_LONG).show();
                    }
                });
                likeQueue.add(amountOfLikesRequest);

            }
        });
    }

    @Override
    //Determines amount of items in the recyclerview
    public int getItemCount() {
            return likedTournaments.size();
        }

    private void handleLike(final LikedAdapter.likedHolder holder, Tournaments currentTournament){
        //When the liked button is pressed handleLike calls the appropriate method depending on if the user is likeing or unlikeing
        final String url = "https://hidden-boss-server.herokuapp.com/tourneys/" + currentTournament.getId() + "/likes";
        StringRequest getLikedStatus = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response.contains(preferences.getString("userName","Missing user"))){
                    unLike(url,holder);
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


    private void like(final String url, final LikedAdapter.likedHolder holder){
        //like does a volley request to add the user to the list of users that has liked the tournament, after that is calls the updateLike method
        StringRequest like = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                holder.likeButton.setColorFilter(ContextCompat.getColor(mContext,R.color.colorAccent));
                updateLikes(holder,url);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {Toast.makeText(mContext,error.toString(),Toast.LENGTH_LONG).show();}
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                try{
                    super.getHeaders();
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + preferences.getString("token","Missing token"));
                    return headers;
                } catch (AuthFailureError a){openLogin.openLoginActivity();}
                return null;

            }
        };
        likeQueue.add(like);
    }

    private void unLike(final String url, final LikedAdapter.likedHolder holder){
        //unLike does a volley request to remove the user from the list of users that has liked the tournament, after that is calls the updateLike method
        StringRequest like = new StringRequest(Request.Method.DELETE, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                holder.likeButton.setColorFilter(ContextCompat.getColor(mContext,R.color.black));
                updateLikes(holder,url);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(mContext,error.toString(),Toast.LENGTH_LONG).show();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                try{
                    super.getHeaders();
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + preferences.getString("token","Missing token"));
                    return headers;
                } catch (AuthFailureError a){
                    openLogin.openLoginActivity();
                }
                return null;

            }
        };
        likeQueue.add(like);}

    private void updateLikes(final likedHolder holder, String url){
        //updateLikes makes a volley request for all the users that has liked the tournament and uses that amount to determine how many likes the tournament now has
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
                if(error instanceof AuthFailureError)
                    Toast.makeText(mContext,error.toString(),Toast.LENGTH_LONG).show();
            }
        });
        likeQueue.add(updateRequest);
    }

}
