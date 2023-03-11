package cse340.finalproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashSet;
import java.util.Set;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_layout);

        TabView nav = findViewById(R.id.bottom_nav); // Tabs at the bottom of screen.
        nav.setOnNavigationItemSelectedListener(item -> {

            // Set tab contents based on selected item.
            switch (item.getItemId()) {
                case R.id.tab_profile:
                    Intent switchActivityIntent = new Intent(this, ProfileActivity.class);
                    startActivity(switchActivityIntent);
                    finish();
                    return true;
                case R.id.tab_chat:
                    SharedPreferences pref = getSharedPreferences(ProfileActivity.PREFERENCES_KEY,MODE_PRIVATE);
                    String name = pref.getString(ProfileActivity.NAME_KEY, "");
                    String age = pref.getString(ProfileActivity.AGE_KEY, "");
                    Set<String> struggles = pref.getStringSet(ProfileActivity.STRUGGLES_KEY, new HashSet<>());
                    if (name.equals("") || age.equals("")) {
                        Toast.makeText(getBaseContext(), R.string.name_and_age_requirement, Toast.LENGTH_LONG).show();
                    } else if (Integer.parseInt(age) < 18) {
                        Toast.makeText(getBaseContext(), R.string.adult_age_requirement, Toast.LENGTH_LONG).show();
                    } else if (struggles.size() == 0) {
                        Toast.makeText(getBaseContext(), R.string.topic_selection_requirement, Toast.LENGTH_LONG).show();
                    } else {
                        switchActivityIntent = new Intent(this, ChatActivity.class);
                        startActivity(switchActivityIntent);
                        finish();
                    }
                    return true;
                case R.id.tab_about:
                    return true;
                default:
                    Log.e(getString(R.string.error_tag), getString(R.string.unrecognized_nav_error) + item.getTitle());
            }
            return false;
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        // Used to skip animation for transition
        // learned from https://stackoverflow.com/questions/6972295/how-to-switch-activity-without-animation-in-android
        overridePendingTransition(0, 0);
    }
}