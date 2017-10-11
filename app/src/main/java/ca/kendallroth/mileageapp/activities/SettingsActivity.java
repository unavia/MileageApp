package ca.kendallroth.mileageapp.activities;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import ca.kendallroth.mileageapp.R;
import ca.kendallroth.mileageapp.fragments.SettingsFragment;

/**
 * Settings activity that displays the Preferences fragment
 */
public class SettingsActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);

    // Toolbar
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    toolbar.setTitle(getString(R.string.title_activity_settings));
    setSupportActionBar(toolbar);

    // Enable a support ActionBar for this toolbar with an "Up" button
    ActionBar ab = getSupportActionBar();
    ab.setDisplayHomeAsUpEnabled(true);

    // Display the Preferences fragment as the main content.
    getFragmentManager().beginTransaction()
        .replace(R.id.settings_content, new SettingsFragment())
        .commit();
  }
}
