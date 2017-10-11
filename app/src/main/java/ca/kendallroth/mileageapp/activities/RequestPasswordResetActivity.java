package ca.kendallroth.mileageapp.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import ca.kendallroth.mileageapp.R;
import ca.kendallroth.mileageapp.utils.AccountUtils;
import ca.kendallroth.mileageapp.utils.AuthUtils;
import ca.kendallroth.mileageapp.utils.Response;
import ca.kendallroth.mileageapp.utils.StatusCode;

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
    mEmailInput.setOnEditorActionListener(new OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        // Attempt requesting a password reset on <Enter> keypress while the Email input has focus
        if (actionId == R.id.email_input || actionId == EditorInfo.IME_NULL) {
          doPasswordResetRequest();
          return true;
        }

        return false;
      }
    });

    // Request password reset button
    mRequestResetButton = (Button) findViewById(R.id.request_reset_button);
    mRequestResetButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        // Attempt a Password Reset Request
        doPasswordResetRequest();
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
  private void doPasswordResetRequest() {
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
  private class RequestPasswordResetTask extends AsyncTask<Void, Void, Response> {

    private final String mEmail;

    RequestPasswordResetTask(String email) {
      mEmail = email;
    }

    @Override
    protected Response doInBackground(Void... params) {
      Log.d("MileageApp", String.format("Password reset request attempt from '%s'", mEmail));

      StatusCode responseStatus = null;
      String responseString = "";

      // TODO: attempt authentication against a network service.
      try {
        // Simulate network access
        Thread.sleep(2000);
      } catch (InterruptedException e) {
        responseStatus = StatusCode.FAILURE;
        responseString = "code_failure_request_password_reset_interrupted_connection";
        return new Response(responseStatus, responseString);
      }

      // Invalid email requests should appear to behave the same as valid email requests,
      // so as to not alert an unauthenticated/invalid user that an email doesn't exist (security).

      // TODO: Enable the user to reset their password
      return AuthUtils.findAuthUser(mEmail);
    }

    @Override
    protected void onPostExecute(final Response response) {
      mRequestPasswordResetTask = null;
      mProgressDialog.dismiss();

      CharSequence snackbarResource = null;

      // Pass the email associated with the password reset request back to the parent Activity
      Intent intent = new Intent();
      intent.putExtra("emailAccount", mEmail);

      // Handle the password reset request attempt response
      if (response.getStatusCode() == StatusCode.SUCCESS) {
        // Finish the activity and send the result back to the parent Activity
        setResult(RESULT_OK, intent);
        finish();
      } else if (response.getStatusCode() == StatusCode.ERROR) {
        // Indicate that an invalid account was entered
        mEmailViewLayout.setError(getString(R.string.error_reset_password_invalid_email));
        mEmailInput.requestFocus();
      } else {
        // Login attempt failed (unknown)
        snackbarResource = getString(R.string.failure_request_password_reset);
      }

      if (snackbarResource != null) {
        // Need to use the android "content" layout as the snackbar anchor
        View snackbarRoot = findViewById(android.R.id.content);
        Snackbar.make(snackbarRoot, snackbarResource, Snackbar.LENGTH_SHORT).show();
      }
    }

    @Override
    protected void onCancelled() {
      mRequestPasswordResetTask = null;
      mProgressDialog.dismiss();
    }
  }
}
