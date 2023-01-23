package com.example.hiddenboss;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.gson.internal.bind.util.ISO8601Utils;

import java.util.List;
import java.util.Objects;


//BaseActivity is an abstract class holding the code all activities have in common
public abstract class BaseActivity extends AppCompatActivity{
    //SharedPreferences are defined here so no other activity needs to do it
    SharedPreferences preferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences("com.example.hiddenboss", Context.MODE_PRIVATE);

    }

    //The toolbar is defined here with a function for the activities to call in order to set it up
    public void setupToolbar(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    public void removeBackButton(){
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Enables the items in the toolbar
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.toolbar_items,menu);
        menu.findItem(R.id.refreshButton).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //Determines what abstract function each item in the toolbar calls on click
        switch (item.getItemId()){

            case R.id.viewTournaments:
                openMyTourneys();
                return true;

            case R.id.registeredTournaments:
                openRegister();
                return true;

            case R.id.likedTournaments:
                openLiked();
                return true;
            case R.id.createTournament:
                openCreate();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    //Abstract methods are defined here for the activities to use. Each method opens a new activity
    protected abstract void openCreate();

    protected abstract void openLiked();

    protected abstract void openRegister();

    protected abstract void openMyTourneys();

    protected abstract void openLogin();


}
