package cse340.finalproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashSet;
import java.util.Set;

/**
 The AboutActivity class represents the activity for the "About" section of the app, extending
 the AppCompatActivity class
 */
public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set content view to about_layout.xml
        setContentView(R.layout.about_layout);

        // Get bottom navigation view and set OnNavigationItemSelectedListener
        TabView nav = findViewById(R.id.bottom_nav); // Tabs at the bottom of screen.
        nav.setOnNavigationItemSelectedListener(item -> {
            Intent switchActivityIntent;
            // Switch to the selected activity based on the selected item
            switch (item.getItemId()) {
                case R.id.tab_profile:
                    switchActivityIntent = new Intent(this, ProfileActivity.class);
                    startActivity(switchActivityIntent);
                    finish();
                    return true;
                case R.id.tab_chat:
                    // nickname, age, and struggles must be in shared preferences to give
                    // context for therapist
                    SharedPreferences pref = getSharedPreferences(ProfileActivity.PREFERENCES_KEY,
                            MODE_PRIVATE);
                    String name = pref.getString(ProfileActivity.NAME_KEY, "");
                    String age = pref.getString(ProfileActivity.AGE_KEY, "");
                    Set<String> struggles = pref.getStringSet(ProfileActivity.STRUGGLES_KEY,
                            new HashSet<>());
                    if (name.equals("") || age.equals("")) {
                        Toast.makeText(getBaseContext(), R.string.name_and_age_requirement,
                                Toast.LENGTH_LONG).show();
                    } else if (Integer.parseInt(age) < 18) {
                        Toast.makeText(getBaseContext(), R.string.adult_age_requirement,
                                Toast.LENGTH_LONG).show();
                    } else if (struggles.size() == 0) {
                        Toast.makeText(getBaseContext(), R.string.topic_selection_requirement,
                                Toast.LENGTH_LONG).show();
                    } else {
                        switchActivityIntent = new Intent(this, ChatActivity.class);
                        startActivity(switchActivityIntent);
                        finish();
                    }
                    return true;
                case R.id.tab_about:
                    return true;
                default:
                    // Log error if tab item is not recognized
                    Log.e(getString(R.string.error_tag), getString(R.string.unrecognized_nav_error) + item.getTitle());
            }
            return false;
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        // Used to skip animation for transition
        // learned from https://stackoverflow.com/questions/6972295/how-to-switch-activity-
        // without-animation-in-android
        overridePendingTransition(0, 0);
    }
}