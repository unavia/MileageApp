package ca.kendallroth.mileageapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;

import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

  /**
   * A dummy authentication store containing known user names and passwords.
   * TODO: remove after connecting to a real authentication system.
   */
  private static final String[] DUMMY_CREDENTIALS = new String[]{
      "kendall@example.com:hello"
  };

  /**
   * Keep track of the login task to ensure we can cancel it if requested.
   */
  private LoginTask mAuthTask = null;

  // UI references.
  private Button mCreateAccountButton;
  private Button mLoginButton;
  private EditText mEmailView;
  private EditText mPasswordView;
  private TextInputLayout mEmailViewLayout;
  private TextInputLayout mPasswordViewLayout;
  private ProgressDialog mProgressDialog;
  private View mLoginFormView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);

    // Email input
    mEmailView = (EditText) findViewById(R.id.login_email);
    mEmailViewLayout = (TextInputLayout) findViewById(R.id.login_email_layout);

    // Password input
    mPasswordView = (EditText) findViewById(R.id.login_password);
    mPasswordViewLayout = (TextInputLayout) findViewById(R.id.login_password_layout);
    mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
        // Attempt login on <Enter> keypress while the Password field has focus
        if (id == R.id.login || id == EditorInfo.IME_NULL) {
          attemptLogin();
          return true;
        }
        return false;
      }
    });

    // Login button
    mLoginButton = (Button) findViewById(R.id.login_button);
    mLoginButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        attemptLogin();
      }
    });

    // Create Account button
    mCreateAccountButton = (Button) findViewById(R.id.create_account_button);
    mCreateAccountButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        displayCreateAccountActivity();
      }
    });

    // Progress dialog
    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setMessage(getString(R.string.progress_message_login_attempt));

    // Layout views
    mLoginFormView = findViewById(R.id.login_form);

    // Hide the Action bar (on all "Authentication" activities)
    getSupportActionBar().hide();
  }


  /**
   * Attempts to sign in or register the account specified by the login form.
   * If there are form errors (invalid email, missing fields, etc.), the
   * errors are presented and no actual login attempt is made.
   */
  private void attemptLogin() {
    if (mAuthTask != null) {
      return;
    }

    // Reset errors.
    mEmailViewLayout.setError(null);
    mPasswordViewLayout.setError(null);

    // Store values at the time of the login attempt.
    String email = mEmailView.getText().toString();
    String password = mPasswordView.getText().toString();

    boolean cancel = false;
    View focusView = null;

    // Check for a valid password, if the user entered one.
    if (TextUtils.isEmpty(password)) {
      mPasswordViewLayout.setError(getString(R.string.error_field_required));
      focusView = mPasswordView;
      cancel = true;
    }
    else if (!isPasswordValid(password)) {
      mPasswordViewLayout.setError(getString(R.string.error_invalid_password));
      focusView = mPasswordView;
      cancel = true;
    }

    // Check for a valid email address (is last to set focus properly).
    if (TextUtils.isEmpty(email)) {
      mEmailViewLayout.setError(getString(R.string.error_field_required));
      focusView = mEmailView;
      cancel = true;
    } else if (!isEmailValid(email)) {
      mEmailViewLayout.setError(getString(R.string.error_invalid_email));
      focusView = mEmailView;
      cancel = true;
    }

    if (cancel) {
      // Ignore login attempt (due to error) and set focus to last field with error
      focusView.requestFocus();
    } else {
      // Show a progress spinner, and kick off a background task to perform the user login attempt.
      mProgressDialog.show();
      mAuthTask = new LoginTask(email, password);
      mAuthTask.execute((Void) null);
    }
  }

  /**
   * Validate the email
   * @param email Email address
   * @return Whether the email address is valid
   */
  private boolean isEmailValid(String email) {
    //TODO: Replace with updated logic
    return email.contains("@") && email.contains(".");
  }

  /**
   * Validate the password
   * @param password Password
   * @return Whether the password is valid
   */
  private boolean isPasswordValid(String password) {
    //TODO: Replace with updated logic
    return password.length() > 4;
  }


  /**
   * Clear the inputs and errors
   */
  private void clearLoginInputs() {
    mEmailView.setText("");
    mEmailViewLayout.setError(null);

    mPasswordView.setText("");
    mPasswordViewLayout.setError(null);
  }

  /**
   * Display the Create Account activity
   */
  private void displayCreateAccountActivity() {
    // Clear inputs (for return)
    clearLoginInputs();

    Intent createAccount = new Intent(this, CreateAccountActivity.class);
    startActivity(createAccount);
  }

  /**
   * Represents an asynchronous login/registration task used to authenticate the user.
   */
  public class LoginTask extends AsyncTask<Void, Void, Boolean> {

    private final String mEmail;
    private final String mPassword;

    LoginTask(String email, String password) {
      mEmail = email;
      mPassword = password;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
      // TODO: attempt authentication against a network service.

      try {
        // Simulate network access.
        Thread.sleep(2000);
      } catch (InterruptedException e) {
        return false;
      }

      for (String credential : DUMMY_CREDENTIALS) {
        String[] pieces = credential.split(":");
        if (pieces[0].equals(mEmail)) {
          // Account exists, return true if the password matches.
          return pieces[1].equals(mPassword);
        }
      }

      return true;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
      mAuthTask = null;
      mProgressDialog.dismiss();

      if (success) {
        // NOTE: Expand to redirect to app home page

        //finish();
      } else {
        mPasswordViewLayout.setError(getString(R.string.error_incorrect_password));
        mPasswordView.requestFocus();
      }

      // Define a snackbar based on the operation status
      CharSequence snackbarResource = success
          ? getString(R.string.success_login)
          : getString(R.string.failure_login);
      Snackbar resultSnackbar = Snackbar.make(mLoginFormView, snackbarResource, Snackbar.LENGTH_SHORT);
      resultSnackbar.show();
    }

    @Override
    protected void onCancelled() {
      // NOTE: Should logging in be canceallable?
      mAuthTask = null;
      mProgressDialog.dismiss();
    }
  }
}

