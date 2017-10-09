package ca.kendallroth.mileageapp.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import org.dom4j.Document;
import org.dom4j.Node;

import java.util.List;

import ca.kendallroth.mileageapp.R;
import ca.kendallroth.mileageapp.utils.AccountUtils;
import ca.kendallroth.mileageapp.utils.XMLFileUtils;

/**
 * Request password reset activity that enables a user to request a password reset
 */
public class RequestPasswordResetActivity extends AppCompatActivity {

  // UI Elements
  private AlertDialog mCancelRequestDialog;
  private Button mRequestResetButton;
  private Button mCancelRequestButton;
  private EditText mEmailInput;
  private TextInputLayout mEmailViewLayout;
  private ProgressDialog mProgressDialog;

  // Login asynchronous task
  private RequestPasswordResetTask mRequestPasswordResetTask = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_request_password_reset);

    // Initialize the UI components
    initView();
  }

  /**
   * Handle the Confirmation Dialog cancel action
   */
  public void onConfirmationDialogCancel() {
    // TODO: Is there anything to do?
  }

  /**
   * Handle the Confirmation Dialog confirm action
   */
  public void onConfirmationDialogConfirm() {
    // Close the activity (cancel action)
    Intent intent = new Intent();
    setResult(RESULT_CANCELED, intent);
    finish();
  }

  /**
   * Display the Cancel Request confirmation dialog when the Back button is pressed
   */
  @Override
  public void onBackPressed() {
    // Display the Cancel Request confirmation dialog
    mCancelRequestDialog.show();
  }

  /**
   * Initialize the UI components
   */
  private void initView() {
    // Email input
    mEmailInput = (EditText) findViewById(R.id.email_input);
    mEmailViewLayout = (TextInputLayout) findViewById(R.id.email_layout);

    // Request password reset button
    mRequestResetButton = (Button) findViewById(R.id.request_reset_button);
    mRequestResetButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        // Attempt a Password Reset Request
        attemptPasswordResetRequest();
      }
    });

    // Cancel password reset request button
    mCancelRequestButton = (Button) findViewById(R.id.cancel_request_button);
    mCancelRequestButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        // Display the Cancel Request confirmation dialog
        mCancelRequestDialog.show();
      }
    });

    // Cancel request dialog
    mCancelRequestDialog = new AlertDialog.Builder(this)
        .setMessage(getString(R.string.dialog_message_request_password_reset))
        .setNegativeButton(R.string.dialog_negative_request_password_reset, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            // Close the Confirmation dialog
            onConfirmationDialogCancel();
          }
        })
        .setPositiveButton(R.string.dialog_positive_request_password_reset, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            // Confirm cancelling the change (close activity)
            onConfirmationDialogConfirm();
          }
        })
        .setTitle(R.string.dialog_title_request_password_reset)
        .create();

    // Progress dialog
    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setMessage(getString(R.string.progress_message_request_password_reset));
  }

  /**
   * Attempts to request a password reset for the account specified by the reset request form. If there are
   *  form errors the errors are presented and no attempt is made.
   */
  private void attemptPasswordResetRequest() {
    if (mRequestPasswordResetTask != null) {
      return;
    }

    // Reset errors.
    mEmailViewLayout.setError(null);

    // Store values at the time of the password reset request attempt.
    String email = mEmailInput.getText().toString();

    boolean cancel = false;
    View focusView = null;

    // Check for a valid email address
    if (TextUtils.isEmpty(email)) {
      mEmailViewLayout.setError(getString(R.string.error_field_required));
      focusView = mEmailInput;
      cancel = true;
    } else if (!AccountUtils.validateEmail(email)) {
      mEmailViewLayout.setError(getString(R.string.error_invalid_email));
      focusView = mEmailInput;
      cancel = true;
    }

    if (cancel) {
      // Ignore password reset request attempt (due to error) and set focus to last field with error.
      focusView.requestFocus();
    } else {
      // Hide the soft keyboard
      View view = this.getCurrentFocus();
      if (view != null) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
      }

      // Show a progress spinner, and kick off a background task to perform the password reset request.
      mProgressDialog.show();
      mRequestPasswordResetTask = new RequestPasswordResetTask(email);
      mRequestPasswordResetTask.execute((Void) null);
    }
  }

  /**
   * Clear the inputs and errors
   */
  public void clearInputs() {
    mEmailInput.setText("");
    mEmailViewLayout.setError(null);
    mEmailInput.requestFocus();
  }


  /**
   * Represents an asynchronous task used to request a password reset.
   */
  private class RequestPasswordResetTask extends AsyncTask<Void, Void, Boolean> {

    private final String mEmail;

    RequestPasswordResetTask(String email) {
      mEmail = email;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
      Log.d("MileageApp", String.format("Password reset request attempt from '%s'", mEmail));

      // TODO: attempt authentication against a network service.
      try {
        // Simulate network access
        Thread.sleep(2000);
      } catch (InterruptedException e) {
        return false;
      }

      Document document;

      try {
        // Read XML file with user information
        document = XMLFileUtils.getFile(getBaseContext(), XMLFileUtils.USERS_FILE_NAME);

        // Select all the "user" nodes in the document
        List<Node> users = document.selectNodes("/users/user");

        boolean validResetRequestEmail = false;

        // Verify that the requested user email exists (but don't alert if not)
        for (Node user : users) {
          if (user.valueOf("@email").equals(mEmail)) {
            validResetRequestEmail = true;
            break;
          }
        }

        Log.d("MileageApp.auth", String.format("Password reset request email %s", validResetRequestEmail ? "found" : "not found"));

        // TODO: Enable the user to reset their password

        // Invalid email requests should appear to behave the same as valid email requests,
        // so as to not alert an unauthenticated/invalid user that an email doesn't exist (security).

        // NOTE: Temporarily use whether a valid account was requested to route to activity
        return validResetRequestEmail;
      } catch (Exception e) {
        // Return false (no match) if the file parsing fails or throws an exception
        return false;
      }
    }

    @Override
    protected void onPostExecute(final Boolean success) {
      mRequestPasswordResetTask = null;
      mProgressDialog.dismiss();

      // Pass the email associated with the password reset request back to the parent Activity
      Intent intent = new Intent();
      intent.putExtra("emailAccount", mEmail);

      if (success) {
        // Finish the activity and send the result back to the parent Activity
        setResult(RESULT_OK, intent);
        finish();
      } else {
        // TODO: Indicate that the reset request has failed (ie. not been sent)

        mEmailViewLayout.setError(getString(R.string.error_reset_password_invalid_email));
        mEmailInput.requestFocus();
      }
    }

    @Override
    protected void onCancelled() {
      mRequestPasswordResetTask = null;
      mProgressDialog.dismiss();
    }
  }
}
