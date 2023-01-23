package com.example.hiddenboss;

import android.content.Context;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Calendar;

//inputValidators contains functions for validating the input from the user when they create or edit a tournament
public class InputValidators {
    private Context mContext;

    public InputValidators(Context mContext) {
        this.mContext = mContext;
    }

    public boolean validateLocation(String location, TextInputLayout inputLayout){
        //Checks if the location is a string that is shorter then 50 characters and not empty
        if(location.isEmpty()){
            inputLayout.setError("Field can't be empty");
            return false;
        } else if (location.length() > 50){
            inputLayout.setError("Field too long");
            return false;
        } else {
            inputLayout.setError(null);
            return true;
        }

    }

    public boolean validateGame(String gameName, ArrayList<String> gameNames, AutoCompleteTextView enterGame){
        //Validates weather or not the name of the game exists in the database and if it's not empty
        if (gameName.isEmpty()){
            enterGame.setError("Field can't be empty");
            return false;
        } else if(!gameNames.contains(gameName)){
            enterGame.setError("The game is not recognised, tournaments of that game can't be created at the moment");
            return false;
        }
        enterGame.setError(null);
        return true;
    }

    public boolean validateDateTime(TextView startDateView, TextView startTimeView, TextView endDateView, TextView endTimeView, Calendar startDateTime, Calendar endDateTime){
        //Validates if all the fields have been entered and if the start comes before the end
        if (startDateView.getText().toString().trim().isEmpty() ||
                startTimeView.getText().toString().trim().isEmpty() ||
                endDateView.getText().toString().trim().isEmpty() ||
                endTimeView.getText().toString().trim().isEmpty()){
            Toast.makeText(mContext,"Enter time and date for when the tournament starts and ends",Toast.LENGTH_LONG).show();
            return false;
        } else if (startDateTime.after(endDateTime)){
            Toast.makeText(mContext,"Start can't come after end", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    public boolean validateDescription(String description, TextInputLayout inputDescription){
        //Validates the description of the tournament so that is's not empty and under 200 characters
        if(description.isEmpty()){
            inputDescription.setError("Field can't be empty");
            return false;
        } else if (description.length() > 200){
            inputDescription.setError("Field too long");
            return false;
        } else {
            inputDescription.setError(null);
            return true;
        }

    }

    public boolean validateTitle(String title, TextInputLayout inputTitle) {
        //Validates the title of the tournament so that it's not empty and under 50 characters
        if (title.isEmpty()) {
            inputTitle.setError("Field can't be empty");
            return false;
        } else if (title.length() > 50) {
            inputTitle.setError("Field too long");
            return false;
        } else {
            inputTitle.setError(null);
            return true;
        }
    }
}
