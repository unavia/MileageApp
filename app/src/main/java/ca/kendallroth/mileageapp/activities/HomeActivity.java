package ca.kendallroth.mileageapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import ca.kendallroth.mileageapp.R;

/**
 * Main activity after authentication has occurred (automatic or Login activity)
 */
public class HomeActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home);

    // Initialize the UI
    initView();
  }

  /**
   * Initialize the UI components
   */
  private void initView() {
    // Toolbar
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    toolbar.setTitle(getString(R.string.title_activity_home));

    setSupportActionBar(toolbar);

    // Floating Action button
    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
            .setAction("Action", null).show();
      }
    });
  }

  /**
   * Create the app bar menu
   * @param menu Placeholder menu
   * @return Menu creation status
   */
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu_home, menu);
    return true;
  }

  /**
   * Event handler for options menu item selection
   * @param item Selected menu item
   * @return Operation success indicator
   */
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle item selection
    switch (item.getItemId()) {
      case R.id.action_settings:
        // Start the Settings activity
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);

        return true;
      default:
        // Unhandled options should return false (done in super class)
        return super.onOptionsItemSelected(item);
    }
  }

  /**
   * Move the app to the background on "Back" button press
   */
  @Override
  public void onBackPressed() {
    // Move the app to the Android background rather than allowing the user to return to "Login" activity
    moveTaskToBack(true);
  }
}
