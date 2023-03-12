package cse340.finalproject;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.RequiresApi;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 The TabView class represents an a custom BottomNavigationView used to display tabs.
 This class and has been taken from the Layout assignment
 */
public class TabView extends BottomNavigationView {
    public TabView(Context context) {
        super(context);
    }

    public TabView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /**
     * Gets the view associated with a menu item
     * @param id of the menu item
     * @return the view
     */
    private View getMenuItemView(int id) {
        ViewGroup items = (ViewGroup) getChildAt(0);
        return items.getChildAt(id);
    }

    /**
     * Override the draw message to update the non-visual content of the menu
     * @param canvas The canvas to draw on
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        Menu menu = getMenu();
        Resources resources = getResources();

        // Loop through the tabs
        for (int i = 0; i < menu.size(); i++) {
            View menuItemView = getMenuItemView(i);
            // Check if tab is selected
            boolean isChecked = menu.getItem(i).isChecked();
            // Set the content description for the navbar to show which tab is selected
            if (isChecked) {
                setContentDescription(
                        resources.getString(R.string.accessible_tab, menu.size(), i + 1));
            }

            // Set up the content description for the menu item, indicating whether it is selected
            String resourceName = resources.getResourceEntryName(menuItemView.getId());
            if (resourceName.equals(getContext().getString(R.string.tab_profile))) {
                menuItemView.setContentDescription(getContext().getString(R.string.profile));
            } else if (resourceName.equals(getContext().getString(R.string.tab_chat))) {
                menuItemView.setContentDescription(getContext().getString(R.string.chat));
            } else {
                menuItemView.setContentDescription(getContext().getString(R.string.about));
            }
        }
    }
}
