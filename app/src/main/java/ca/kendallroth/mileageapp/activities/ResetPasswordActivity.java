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
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.List;

import ca.kendallroth.mileageapp.R;
import ca.kendallroth.mileageapp.utils.AccountUtils;
import ca.kendallroth.mileageapp.utils.XMLFileUtils;

/**
 * Reset password activity that enables a user to reset their password
 */
public class ResetPasswordActivity extends AppCompatActivity {

  private AlertDialog mCancelResetDialog;
  private Button mResetPasswordButton;
  private EditText mPasswordInput;
  private EditText mPasswordConfirmInput;
  private ProgressDialog mProgressDialog;
  private TextInputLayout mPasswordViewLayout;
  private TextInputLayout mPasswordConfirmViewLayout;
  private TextView mEmailView;

  // Email address that sent the request
  private String mEmailAccount;

  // Login asynchronous task
  private ResetPasswordTask mResetPasswordTask = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_reset_password);

    // Get the email associated with the password reset (must be BEFORE `initView()`)
    Intent intent = getIntent();
    mEmailAccount = intent.getStringExtra("emailAccount");

    // Exit immediately if no valid email address was provided for the reset
    if (!AccountUtils.validateEmail(mEmailAccount)) {
      // TODO: Needs to be polished (ie. proper return status?)
      Intent responseIntent = new Intent();
      responseIntent.putExtra("validEmail", false);
      finish();
    }

    // Initialize the UI Components
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
    mCancelResetDialog.show();
  }

  /**
   * Initialize the UI Components
   */
  private void initView() {
    // Email input (for display only)
    mEmailView = (TextView) findViewById(R.id.email_text);
    mEmailView.setText(String.format("%s: %s", getString(R.string.label_password_reset), mEmailAccount));

    // Password input
    mPasswordInput = (EditText) findViewById(R.id.password_input);
    mPasswordViewLayout = (TextInputLayout) findViewById(R.id.password_layout);

    // Password confirmation input
    mPasswordConfirmInput = (EditText) findViewById(R.id.password_confirm_input);
    mPasswordConfirmViewLayout = (TextInputLayout) findViewById(R.id.password_confirm_layout);
    mPasswordConfirmInput.setOnEditorActionListener(new OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        // Attempt resetting a password on <Enter> keypress while the Confirm Password input has focus
        if (actionId == R.id.email_input || actionId == EditorInfo.IME_NULL) {
          doPasswordReset(mEmailAccount);
          return true;
        }

        return false;
      }
    });

    // Password reset button
    mResetPasswordButton = (Button) findViewById(R.id.reset_password_button);
    mResetPasswordButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        // Attempt to reset the password
        doPasswordReset(mEmailAccount);
      }
    });

    // Cancel request dialog
    mCancelResetDialog = new AlertDialog.Builder(this)
        .setMessage(getString(R.string.dialog_message_reset_password))
        .setNegativeButton(R.string.dialog_negative_reset_password, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            // Close the Confirmation dialog
            onConfirmationDialogCancel();
          }
        })
        .setPositiveButton(R.string.dialog_positive_reset_password, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            // Confirm cancelling the change (close activity)
            onConfirmationDialogConfirm();
          }
        })
        .setTitle(R.string.dialog_title_reset_password)
        .create();

    // Progress dialog
    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setMessage(getString(R.string.progress_message_reset_password));

  }

  /**
   * Attempts to reset a password for the account specified by the reset form. If there are
   *  form errors the errors are presented and no attempt is made.
   */
  private void doPasswordReset(String accountEmail) {
    if (mResetPasswordTask != null) {
      return;
    }

    // Reset errors
    mPasswordViewLayout.setError(null);
    mPasswordConfirmViewLayout.setError(null);

    // Store values at the time of the password reset attempt
    String password = mPasswordInput.getText().toString();
    String passwordConfirm = mPasswordConfirmInput.getText().toString();

    boolean cancel = false;
    View focusView = null;

    // Check for a valid password confirmation
    if (TextUtils.isEmpty(password)) {
      mPasswordViewLayout.setError(getString(R.string.error_field_required));
      focusView = mPasswordConfirmInput;
      cancel = true;
    } else if (!AccountUtils.validatePasswordConfirm(password, passwordConfirm)) {
      mPasswordViewLayout.setError(getString(R.string.error_mismatching_passwords));
      focusView = mPasswordConfirmInput;
      cancel = true;
    }

    // Check for a valid password
    if (TextUtils.isEmpty(password)) {
      mPasswordViewLayout.setError(getString(R.string.error_field_required));
      focusView = mPasswordInput;
      cancel = true;
    } else if (!AccountUtils.validatePassword(password)) {
      mPasswordViewLayout.setError(getString(R.string.error_invalid_password));
      focusView = mPasswordInput;
      cancel = true;
    }

    if (cancel) {
      // Ignore password reset attempt (due to error) and set focus to last field with error.
      focusView.requestFocus();
    } else {
      // Hide the soft keyboard
      View view = this.getCurrentFocus();
      if (view != null) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
      }

      // Show a progress spinner, and kick off a background task to perform the password reset.
      mProgressDialog.show();
      mResetPasswordTask = new ResetPasswordTask(accountEmail, password);
      mResetPasswordTask.execute((Void) null);
    }
  }

  /**
   * Clear the inputs and errors
   */
  public void clearInputs() {
    // Password confirmation input
    mPasswordInput.setText("");
    mPasswordViewLayout.setError(null);
    mPasswordInput.requestFocus();

    // Password input
    mPasswordInput.setText("");
    mPasswordViewLayout.setError(null);
    mPasswordInput.requestFocus();
  }


  /**
   * Represents an asynchronous task used to reset a password.
   */
  private class ResetPasswordTask extends AsyncTask<Void, Void, Boolean> {

    private final String mEmail;
    private final String mPassword;

    ResetPasswordTask(String email, String password) {
      mEmail = email;
      mPassword = password;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
      Log.d("MileageApp", String.format("Password reset attempt from '%s'", mEmail));

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

        boolean passwordResetSuccessful = false;

        // Verify that the requested user email exists (but don't alert if not)
        for (Node user : users) {
          if (user.valueOf("@email").equals(mEmail)) {
            // Update the password
            Element userElement = (Element) user;
            userElement.addAttribute("password", mPassword);

            // Write the updated file
            XMLFileUtils.createFile(getBaseContext(), XMLFileUtils.USERS_FILE_NAME, document);

            passwordResetSuccessful = true;
            break;
          }
        }

        Log.d("MileageApp.auth", String.format("Password reset for email %s", passwordResetSuccessful ? "successful" : "failed"));

        return passwordResetSuccessful;
      } catch (Exception e) {
        // Return false (no match) if the file parsing fails or throws an exception
        return false;
      }
    }

    @Override
    protected void onPostExecute(final Boolean success) {
      mResetPasswordTask = null;
      mProgressDialog.dismiss();

      if (success) {
        // Indicate a successful password reset
        setResult(RESULT_OK);
        finish();
      } else {
        // TODO: Indicate that the reset has failed
      }
    }

    @Override
    protected void onCancelled() {
      mResetPasswordTask = null;
      mProgressDialog.dismiss();
    }
  }
}
