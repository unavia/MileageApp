package ca.kendallroth.mileageapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import ca.kendallroth.mileageapp.R;

/**
 * Main activity after authentication has occurred (automatic or Login activity)
 */
public class HomeActivity extends AppCompatActivity {

  Button mSettingsButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home);

    // Initialize the UI
    initView();

    // Toolbar
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    // Floating Action button
    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        onSettingsClick();

        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
            .setAction("Action", null).show();
      }
    });
  }

  /**
   * Initialize the UI components
   */
  private void initView() {
    // Settings button
    mSettingsButton = (Button) findViewById(R.id.settings_button);
    mSettingsButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        // Open the Settings activity
        onSettingsClick();
      }
    });
  }

  /**
   * Start the Settings activity
   */
  public void onSettingsClick() {
    Intent settingsIntent = new Intent(this, SettingsActivity.class);
    startActivity(settingsIntent);
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
