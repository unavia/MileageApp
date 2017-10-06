package ca.kendallroth.mileageapp.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.View;

import ca.kendallroth.mileageapp.R;
import ca.kendallroth.mileageapp.utils.AuthUtils;
import ca.kendallroth.mileageapp.utils.Response;
import ca.kendallroth.mileageapp.utils.StatusCode;

/**
 * Fragment to allow users to view and change global settings
 */
public class SettingsFragment extends PreferenceFragment {

  AlertDialog mConfirmDatabaseClearDialog;
  AlertDialog mConfirmDeleteAccountDialog;
  Preference mClearDatabasePreference;
  Preference mDeleteAccountPreference;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Load the preferences from an XML resource
    addPreferencesFromResource(R.xml.preferences);

    // Initialize the UI components (must be after resource load)
    initView();
  }

  /**
   * Initialize the UI components
   */
  private void initView() {
    // Clear Database preference "button"
    mClearDatabasePreference = (Preference) findPreference(getString(R.string.pref_key_clear_db));
    mClearDatabasePreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        // Show the confirmation dialog for resetting the database
        mConfirmDatabaseClearDialog.show();

        return true;
      }
    });

    // Delete account preference "button"
    mDeleteAccountPreference = (Preference) findPreference(getString(R.string.pref_key_delete_account));
    mDeleteAccountPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        // Show the confirmation dialog for deleting the user's account
        mConfirmDeleteAccountDialog.show();

        return true;
      }
    });

    // Confirm database clear dialog
    mConfirmDatabaseClearDialog = new AlertDialog.Builder(getActivity())
        .setMessage(getString(R.string.dialog_message_clear_database))
        .setNegativeButton(R.string.dialog_button_no, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            // Close the Confirmation dialog
            onClearDatabaseCancel();
          }
        })
        .setPositiveButton(R.string.dialog_button_yes, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            // Confirm clearing the database
            onClearDatabaseConfirm();
          }
        })
        .setTitle(R.string.dialog_title_clear_database)
        .create();

    // Confirm account deletion dialog
    mConfirmDeleteAccountDialog = new AlertDialog.Builder(getActivity())
        .setMessage(getString(R.string.dialog_message_delete_account))
        .setNegativeButton(R.string.dialog_button_no, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            // Close the Confirmation dialog
            onDeleteAccountCancel();
          }
        })
        .setPositiveButton(R.string.dialog_button_yes, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            // Confirm deleting the account
            onDeleteAccountConfirm();
          }
        })
        .setTitle(R.string.dialog_title_delete_account)
        .create();
  }

  /**
   * Handler for the Clear Database confirmation dialog cancel action
   */
  public void onClearDatabaseCancel() {
    // Close the confirmation dialog
    mConfirmDatabaseClearDialog.hide();
  }

  /**
   * Handler for the Clear Database confirmation dialog confirm action
   */
  public void onClearDatabaseConfirm() {
    // Clear the authentication database
    Response clearDatabaseResponse = AuthUtils.resetAuthFile();

    // Need to use the android "content" layout as the snackbar anchor (since this is a fragment)
    View snackbarRoot = getActivity().findViewById(android.R.id.content);

    CharSequence snackbarResource = clearDatabaseResponse.getStatusCode() == StatusCode.SUCCESS
        ? getString(R.string.success_clear_database)
        : getString(R.string.failure_clear_database);
    Snackbar resultSnackbar = Snackbar.make(snackbarRoot, snackbarResource, Snackbar.LENGTH_SHORT);
    resultSnackbar.show();

    // TODO: Logout the user and send to Login page
  }


  /**
   * Handler for the Delete Account confirmation dialog cancel action
   */
  public void onDeleteAccountCancel() {
    // Close the confirmation dialog
    mConfirmDeleteAccountDialog.hide();
  }

  /**
   * Handler for the Delete Account confirmation dialog confirm action
   */
  public void onDeleteAccountConfirm() {
    // TODO: Delete the user's account
    Response accountDeleteResponse = AuthUtils.removeAuthUser("", "");

    // Need to use the android "content" layout as the snackbar anchor (since this is a fragment)
    View snackbarRoot = getActivity().findViewById(android.R.id.content);

    CharSequence snackbarResource = accountDeleteResponse.getStatusCode() == StatusCode.SUCCESS
        ? getString(R.string.success_delete_account)
        : getString(R.string.failure_delete_account);

    snackbarResource = "Method not implemented";

    Snackbar resultSnackbar = Snackbar.make(snackbarRoot, snackbarResource, Snackbar.LENGTH_SHORT);
    resultSnackbar.show();
  }
}
