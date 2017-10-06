package ca.kendallroth.mileageapp.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

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

    // Display the Preferences fragment as the main content.
    getFragmentManager().beginTransaction()
        .replace(android.R.id.content, new SettingsFragment())
        .commit();
  }
}
