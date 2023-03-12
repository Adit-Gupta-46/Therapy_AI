package cse340.finalproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Set;

/**
 The ProfileActivity class represents the activity for the "Profile" section of the app, extending
 the AppCompatActivity class
 */
public class ProfileActivity extends AppCompatActivity {

    // Constants for Shared Preferences
    private Set<String> mSelectedStruggles;
    public static final String PREFERENCES_KEY = "preferences";
    public static final String STRUGGLES_KEY = "struggles";
    public static final String NAME_KEY = "name";
    public static final String AGE_KEY = "age";

    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set content view to profile_layout.xml
        setContentView(R.layout.profile_layout);

        // Retrieve shared preferences
        pref = getSharedPreferences(PREFERENCES_KEY,MODE_PRIVATE);
        EditText nameView = findViewById(R.id.name_text_box);
        nameView.setText(pref.getString(NAME_KEY, null));
        EditText ageView = findViewById(R.id.age_text_box);
        ageView.setText(pref.getString(AGE_KEY, null));
        mSelectedStruggles = pref.getStringSet(STRUGGLES_KEY, new HashSet<>());

        // Get struggle options grid layout and iterate through its children
        GridLayout struggleOptions = findViewById(R.id.struggle_options);
        for (int i = 0; i < struggleOptions.getChildCount(); i++) {
            // Draw items as selected or unselected, based on data from shared preferences
            TextView currentSelection = (TextView) struggleOptions.getChildAt(i);
            if (mSelectedStruggles.contains(currentSelection.getText().toString())) {
                currentSelection.setBackground((getDrawable(R.drawable.selected_border)));
            } else {
                currentSelection.setBackground((getDrawable(R.drawable.unselected_border)));
            }

            // Set an OnClickListener for the current selection
            currentSelection.setOnClickListener(v -> {
                if (!mSelectedStruggles.contains(currentSelection.getText().toString())) {
                    // Add selection to Set and set the background to selected border
                    mSelectedStruggles.add(currentSelection.getText().toString());
                    currentSelection.setBackground((getDrawable(R.drawable.selected_border)));
                } else {
                    // Remove selection from Set and set the background to unselected border
                    mSelectedStruggles.remove(currentSelection.getText().toString());
                    currentSelection.setBackground((getDrawable(R.drawable.unselected_border)));
                }
            });
        }

        // Get bottom navigation view and set OnNavigationItemSelectedListener
        TabView nav = findViewById(R.id.bottom_nav); // Tabs at the bottom of screen.
        nav.setOnNavigationItemSelectedListener(item -> {
            Intent switchActivityIntent;
            // Switch to the selected activity based on the selected item
            switch (item.getItemId()) {
                case R.id.tab_profile:
                    return true;
                case R.id.tab_chat:
                    // nickname, age, and struggles must be provided to give context for therapist
                    String age = ageView.getText().toString();
                    if (nameView.getText().toString().equals("") || age.equals("")) {
                        Toast.makeText(getBaseContext(), R.string.name_and_age_requirement,
                                Toast.LENGTH_LONG).show();
                    } else if (Integer.parseInt(age) < 18) {
                        Toast.makeText(getBaseContext(), R.string.adult_age_requirement,
                                Toast.LENGTH_LONG).show();
                    } else if (mSelectedStruggles.size() == 0) {
                        Toast.makeText(getBaseContext(), R.string.topic_selection_requirement,
                                Toast.LENGTH_LONG).show();
                    } else {
                        switchActivityIntent = new Intent(this, ChatActivity.class);
                        startActivity(switchActivityIntent);
                        finish();
                    }
                    return true;
                case R.id.tab_about:
                    switchActivityIntent = new Intent(this, AboutActivity.class);
                    startActivity(switchActivityIntent);
                    finish();
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

        // Set shared preferences
        SharedPreferences.Editor prefEdit = pref.edit();
        EditText nameView = findViewById(R.id.name_text_box);
        EditText ageView = findViewById(R.id.age_text_box);
        prefEdit.clear();
        prefEdit.putStringSet(STRUGGLES_KEY, mSelectedStruggles);
        prefEdit.putString(NAME_KEY, nameView.getText().toString());
        prefEdit.putString(AGE_KEY, ageView.getText().toString());
        prefEdit.apply();
    }
}