package com.example.hiddenboss;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

// A RecyclerViewAdapter for displaying all comments made on a tournament.
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentHolder> {
    private Context mContext;
    private ArrayList<Comment> comments;
    private static final String Tag = "CommentAdapter";

    public CommentAdapter(ArrayList<Comment> comments, Context mContext){
        this.comments = comments;
        this.mContext = mContext;
    }

    // The ViewHolder for this RecyclerView represents a single comment.
    public class CommentHolder extends RecyclerView.ViewHolder{
        // Variable id from the Comment class is not essential information for users and is thus omitted from the ViewHolder
        TextView user;
        TextView message;

        public CommentHolder(@NonNull View itemView){
            super(itemView);
            user = itemView.findViewById(R.id.commentUser);
            message = itemView.findViewById(R.id.commentMessage);
        }

    }

    @NonNull
    @Override
    public CommentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        // Creates the RecyclerView, but does not set the items' contents.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comments_item, parent, false);
        return new CommentHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final CommentHolder holder, final int position){
        // Sets an item view's contents.
        holder.user.setText(comments.get(position).getUser());
        holder.message.setText(comments.get(position).getMessage());
    }

    @Override
    public int getItemCount(){
        // Returns the size of one's dataset, in this case the amount of comments.
        return comments.size();
    }

    public void updateComments(ArrayList<Comment> comments){
        // Updates the RecyclerView with potentially new data. This used when p user posts a comment, so that their comment feed is automatically updated.
        this.comments = comments;
        notifyDataSetChanged();
    }

}
