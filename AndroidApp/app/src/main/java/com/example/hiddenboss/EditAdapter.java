package com.example.hiddenboss;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import de.hdodenhof.circleimageview.CircleImageView;

//editAdapter is the recyclerview that the user sees when they look at their own tournaments.
// It differs from the rest of the recyclerviews used in that it allows the user
//to open a view where they can edit information about their tournament or delete it

public class EditAdapter extends RecyclerView.Adapter<EditAdapter.editHolder> {
    //Global variables
    private static final String Tag = "EditAdapter";
    private Context mContext;
    private ArrayList<Tournaments> myTournaments;
    private Converters converters = new Converters();
    private final int editRequestCode = 200;

    public EditAdapter(Context mContext, ArrayList<Tournaments> myTournaments) {
        this.mContext = mContext;
        this.myTournaments = myTournaments;
    }

    class editHolder extends RecyclerView.ViewHolder {
        //editHolder class containing all the necessary attributes

        CircleImageView image;
        TextView tournamentTitle;
        TextView tournamentDateTime;
        TextView tournamentLocation;
        LinearLayout parentLayout;
        ImageButton editButton;

        editHolder(@NonNull View itemView) {
            //All attributes are given pointers to objects on the screen
            super(itemView);
            image = itemView.findViewById(R.id.gameImage);
            tournamentTitle = itemView.findViewById(R.id.tournamentTitle);
            tournamentDateTime = itemView.findViewById(R.id.tournamentDate);
            tournamentLocation = itemView.findViewById(R.id.tournamentLocation);
            parentLayout = itemView.findViewById(R.id.feedItemLayout);
            editButton = itemView.findViewById(R.id.editButton);


        }
    }


    @NonNull
    @Override
    //onCreateViewHolder creates the viewholder used by the recyclerview adapter
    public editHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_tournament_item, parent, false);
        EditAdapter.editHolder Holder = new EditAdapter.editHolder(view);
        return Holder;
    }

    @SuppressLint("SetTextI18n")
    @Override
    //In onBindViewHolder the functionality of the recyclerview's items are determined
    public void onBindViewHolder(@NonNull final editHolder holder, final int position) {
        //The glide library is used to allow us to use the circle image functionality. Each tournament object holds the
        //url for the image
        Glide.with(mContext)
                .asBitmap()
                .load(myTournaments.get(position).getGame().getImg())
                .into(holder.image);
        //Sets the information for location, date and title from the existing information in the tournament object
        holder.tournamentLocation.setText(myTournaments.get(position).getLocation());
        holder.tournamentTitle.setText(myTournaments.get(position).getTitle());
        holder.tournamentDateTime.setText(converters.DateToString(
                myTournaments.get(position).getStart_year(),
                myTournaments.get(position).getStart_month(),
                myTournaments.get(position).getStart_day())
                + " " + myTournaments.get(position).getStart_time());


        //The editButton is shaped like a pen. When pressed the user will come to a view that looks identical
        // to when they create a tournament but it is used to edit existing tournaments, not create new ones.
        holder.editButton.setOnClickListener(new View.OnClickListener() {
            //The volley library is used to get all information about the requested tournament
            RequestQueue myTournamentsQueue = Volley.newRequestQueue(mContext);
            @Override
            public void onClick(View v) {
                String urlGames ="https://hidden-boss-server.herokuapp.com/games";
                JsonObjectRequest gameJSON = new JsonObjectRequest(Request.Method.GET, urlGames, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //When the information is received an intent is used to open a new view with the information as extra. The intent opens a tournamentEditActivity
                            JSONArray gamesArray = response.getJSONArray("games");
                            Intent editIntent = new Intent(mContext,TournamentEditActivity.class);
                            editIntent.putExtra("gamesJSON", gamesArray.toString());
                            editIntent.putExtra("tourneyToEdit",myTournaments.get(position));
                            ((Activity) mContext).startActivityForResult(editIntent,editRequestCode);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
                myTournamentsQueue.add(gameJSON);

            }
        });

        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            //When anything other then the editButton is pressed on the item the user will enter the registration activity same as on other pages
            public void onClick(final View v) {
                //The information about the object is put inside the intent as extra. The reason only likes are fetched with a volley request is because
                //since this is the users own tournament no other information can change since the last volley request
                RequestQueue myTournamentsQueue = Volley.newRequestQueue(mContext);
                Log.d(Tag,"onClick: clicked on " + myTournaments.get(position).getTitle());
                final Intent registrationIntent = new Intent(mContext, RegistrationActivity.class);
                registrationIntent.putExtra("title",myTournaments.get(position).getTitle());
                registrationIntent.putExtra("startEnd",myTournaments.get(position).getStartToEnd());
                registrationIntent.putExtra("location", myTournaments.get(position).getLocation());
                registrationIntent.putExtra("host", "Hosted by " + myTournaments.get(position).getHost_id());
                registrationIntent.putExtra("imgUrl",myTournaments.get(position).getGame().getImg());
                registrationIntent.putExtra("description", myTournaments.get(position).getDescription());
                registrationIntent.putExtra("id", myTournaments.get(position).getId());

                JsonObjectRequest amountOfLikesRequest = new JsonObjectRequest(Request.Method.GET, "https://hidden-boss-server.herokuapp.com/tourneys/" + myTournaments.get(position).getId()+"/likes",null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            registrationIntent.putExtra("likesString", String.valueOf(response.getJSONArray("liked_by").length()) + " Likes");
                            mContext.startActivity(registrationIntent);
                        } catch (JSONException e) {
                            Toast.makeText(mContext, e.toString(), Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(mContext,String.valueOf(error.networkResponse.statusCode),Toast.LENGTH_LONG).show();
                    }
                });
                myTournamentsQueue.add(amountOfLikesRequest);

            }
        });

    }

    @Override
    //This determines the length of the recyclerview
    public int getItemCount() {
        return myTournaments.size();
    }
}