package ca.kendallroth.mileageapp.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import org.dom4j.Document;
import org.dom4j.Node;

import java.util.List;

import ca.kendallroth.mileageapp.R;
import ca.kendallroth.mileageapp.activities.HomeActivity;
import ca.kendallroth.mileageapp.utils.AccountUtils;
import ca.kendallroth.mileageapp.utils.XMLFileUtils;

/**
 * Fragment to allow users to login (or automatically authenticate)
 */
public class LoginFragment extends Fragment {

  // Login asynchronous task
  private LoginTask mAuthTask = null;

  // UI references.
  private Button mLoginButton;
  private EditText mEmailInput;
  private EditText mPasswordInput;
  private TextInputLayout mEmailViewLayout;
  private TextInputLayout mPasswordViewLayout;
  private ProgressDialog mProgressDialog;
  private View mFormLayout;

  // Helper constants for argument variable names
  private static final String ARG_TITLE = "mTitle";

  private String mTitle;

  //private OnFragmentInteractionListener mListener;

  public LoginFragment() {
    // Required empty public constructor
  }

  // Use this factory method to create a new instance of this fragment using the provided parameters
  public static LoginFragment newInstance(String title) {
    LoginFragment fragment = new LoginFragment();

    Bundle args = new Bundle();
    args.putString(ARG_TITLE, title);
    fragment.setArguments(args);

    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Log.d("MileageApp", "LoginFragment.onCreate");

    if (getArguments() != null) {
      mTitle = getArguments().getString(ARG_TITLE);
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View loginView = inflater.inflate(R.layout.fragment_login, container, false);

    // Initialize the view and variables
    this.initView(loginView);

    return loginView;
  }

  /**
   * Initialize the Fragment view after view creation
   * @param loginView Fragment view
   */
  private void initView(View loginView) {
    // Email input
    mEmailInput = (EditText) loginView.findViewById(R.id.email_input);
    mEmailViewLayout = (TextInputLayout) loginView.findViewById(R.id.email_layout);

    // Password input
    mPasswordInput = (EditText) loginView.findViewById(R.id.password_input);
    mPasswordViewLayout = (TextInputLayout) loginView.findViewById(R.id.password_layout);
    mPasswordInput.setOnEditorActionListener(new OnEditorActionListener() {
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
    mLoginButton = (Button) loginView.findViewById(R.id.login_button);
    mLoginButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        attemptLogin();
      }
    });

    // Progress dialog
    mProgressDialog = new ProgressDialog(loginView.getContext());
    mProgressDialog.setMessage(getString(R.string.progress_message_login_attempt));

    // Layout views
    mFormLayout = loginView.findViewById(R.id.form_layout);
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
    String email = mEmailInput.getText().toString();
    String password = mPasswordInput.getText().toString();

    boolean cancel = false;
    View focusView = null;

    // Check for a valid password, if the user entered one.
    if (TextUtils.isEmpty(password)) {
      mPasswordViewLayout.setError(getString(R.string.error_field_required));
      focusView = mPasswordInput;
      cancel = true;
    }
    else if (!AccountUtils.validatePassword(password)) {
      mPasswordViewLayout.setError(getString(R.string.error_invalid_password));
      focusView = mPasswordInput;
      cancel = true;
    }

    // Check for a valid email address (is last to set focus properly).
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
   * Clear the inputs and errors
   */
  public void clearLoginInputs() {
    mEmailInput.setText("");
    mEmailViewLayout.setError(null);

    mPasswordInput.setText("");
    mPasswordViewLayout.setError(null);
  }

  // TODO: Rename method, update argument and hook method into UI event
  /*public void onButtonPressed(Uri uri) {
    if (mListener != null) {
      mListener.onFragmentInteraction(uri);
    }
  }*/

  /*@Override
  public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof OnFragmentInteractionListener) {
      mListener = (OnFragmentInteractionListener) context;
    } else {
      throw new RuntimeException(context.toString()
          + " must implement OnFragmentInteractionListener");
    }
  }*/

  /*@Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
  }*/

  /**
   * This interface must be implemented by activities that contain this
   * fragment to allow an interaction in this fragment to be communicated
   * to the activity and potentially other fragments contained in that
   * activity.
   * <p>
   * See the Android Training lesson <a href=
   * "http://developer.android.com/training/basics/fragments/communicating.html"
   * >Communicating with Other Fragments</a> for more information.
   */
  /*public interface OnFragmentInteractionListener {
    // TODO: Update argument type and name
    void onFragmentInteraction(Uri uri);
  }*/


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

      Document document;

      try {
        // Read XML file with user information
        document = XMLFileUtils.getFile(getActivity().getBaseContext(), XMLFileUtils.USERS_FILE_NAME);

        // Select all the "user" nodes in the document
        List<Node> users = document.selectNodes("/users/user");

        Log.d("MileageApp", String.format("Login attempt from '%s' with password '%s'", mEmail, mPassword));

        for (Node user : users) {
          // Compare the entered email and password against the "registered" accounts
          if (user.valueOf("@email").equals(mEmail)) {
            boolean validAuthAttempt = user.valueOf("@password").equals(mPassword);

            Log.d("MileageApp.auth", String.format("Login attempt %s", validAuthAttempt ? "successful" : "failed"));

            return validAuthAttempt;
          }
        }
      } catch (Exception e) {
        // Return false (no match) if the file parsing fails or throws an exception
        return false;
      }

      return false;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
      mAuthTask = null;
      mProgressDialog.dismiss();

      if (success) {
        // Set navigation history (home) and start the Home activity
        Intent homeActivityIntent = new Intent(getContext().getApplicationContext(), HomeActivity.class);
        homeActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        startActivity(homeActivityIntent);
        getActivity().finish();
      } else {
        mPasswordViewLayout.setError(getString(R.string.error_incorrect_password));
        mPasswordInput.requestFocus();
      }

      // Need to use the android "content" layout as the snackbar anchor (since this is a fragment)
      View snackbarRoot = getActivity().findViewById(android.R.id.content);

      // Define a snackbar based on the operation status
      CharSequence snackbarResource = success
          ? getString(R.string.success_login)
          : getString(R.string.failure_login);
      Snackbar resultSnackbar = Snackbar.make(snackbarRoot, snackbarResource, Snackbar.LENGTH_SHORT);
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
