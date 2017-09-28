package ca.kendallroth.mileageapp.fragments;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.List;

import ca.kendallroth.mileageapp.R;
import ca.kendallroth.mileageapp.utils.AccountUtils;
import ca.kendallroth.mileageapp.utils.XMLFileUtils;

/**
 * Fragment for enabling a user to register for the app
 */
public class RegisterFragment extends Fragment {

  // Create Account asynchronous task
  private CreateAccountTask mAuthTask;

  // UI references.
  private Button mCreateAccountButton;
  private Button mLoginPromptButton;
  private EditText mEmailView;
  private EditText mNameView;
  private EditText mPasswordView;
  private EditText mPasswordConfirmView;
  private TextInputLayout mEmailViewLayout;
  private TextInputLayout mNameViewLayout;
  private TextInputLayout mPasswordViewLayout;
  private TextInputLayout mPasswordConfirmViewLayout;
  private ProgressDialog mProgressDialog;
  private View mCreateAccountFormView;

  // Helper constants for argument variable names
  private static final String ARG_TITLE = "mTitle";

  private String mTitle;

  //private OnFragmentInteractionListener mListener;

  public RegisterFragment() {
    // Required empty public constructor
  }

  // Use this factory method to create a new instance of this fragment using the provided parameters
  public static RegisterFragment newInstance(String title) {
    RegisterFragment fragment = new RegisterFragment();

    Bundle args = new Bundle();
    args.putString(ARG_TITLE, title);
    fragment.setArguments(args);

    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Log.d("MileageApp", "RegisterFragment.onCreate");

    if (getArguments() != null) {
      mTitle = getArguments().getString(ARG_TITLE);
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View createAccountView = inflater.inflate(R.layout.fragment_register, container, false);

    // Email field
    mEmailView = (EditText) createAccountView.findViewById(R.id.email_input);
    mEmailViewLayout = (TextInputLayout) createAccountView.findViewById(R.id.email_layout);

    // Name field
    mNameView = (EditText) createAccountView.findViewById(R.id.name_input);
    mNameViewLayout = (TextInputLayout) createAccountView.findViewById(R.id.name_layout);

    // Password field
    mPasswordView = (EditText) createAccountView.findViewById(R.id.password_input);
    mPasswordViewLayout = (TextInputLayout) createAccountView.findViewById(R.id.password_layout);

    // Password confirmation field
    mPasswordConfirmView = (EditText) createAccountView.findViewById(R.id.password_confirm_input);
    mPasswordConfirmViewLayout = (TextInputLayout) createAccountView.findViewById(R.id.password_confirm_layout);

    // Create account button
    mCreateAccountButton = (Button) createAccountView.findViewById(R.id.create_account_button);
    mCreateAccountButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        doAccountCreate();
      }
    });

    // Progress dialog
    mProgressDialog = new ProgressDialog(getContext());
    mProgressDialog.setMessage(getString(R.string.progress_message_create_account));

    // Layout views
    mCreateAccountFormView = (View) createAccountView.findViewById(R.id.create_account_form);

    return createAccountView;
  }


  /**
   * Clear the inputs and errors
   */
  private void clearLoginInputs() {
    mEmailView.setText("");
    mEmailViewLayout.setError(null);

    mNameView.setText("");
    mNameViewLayout.setError(null);

    mPasswordView.setText("");
    mPasswordViewLayout.setError(null);

    mPasswordConfirmView.setText("");
    mPasswordConfirmViewLayout.setError(null);
  }

  /**
   * Attempt to create an account
   */
  private void doAccountCreate() {
    // Only permit one "Account Create" request at a time
    if (mAuthTask != null) {
      return;
    }

    // Reset errors.
    mEmailViewLayout.setError(null);
    mNameViewLayout.setError(null);
    mPasswordViewLayout.setError(null);
    mPasswordConfirmViewLayout.setError(null);

    // Store values at the time of the login attempt.
    String email = mEmailView.getText().toString();
    String name = mNameView.getText().toString();
    String password = mPasswordView.getText().toString();
    String passwordConfirm = mPasswordConfirmView.getText().toString();

    boolean cancel = false;
    View focusView = null;

    // Check for a valid password confirmation (must match), if the user entered one.
    if (TextUtils.isEmpty(password)) {
      mPasswordConfirmViewLayout.setError(getString(R.string.error_field_required));
      focusView = mPasswordConfirmView;
      cancel = true;
    }
    else if (!AccountUtils.validatePasswordConfirm(password, passwordConfirm)) {
      mPasswordConfirmViewLayout.setError(getString(R.string.error_mismatching_passwords));
      focusView = mPasswordConfirmView;
      cancel = true;
    }

    // Check for a valid password, if the user entered one.
    if (TextUtils.isEmpty(password)) {
      mPasswordViewLayout.setError(getString(R.string.error_field_required));
      focusView = mPasswordView;
      cancel = true;
    }
    else if (!AccountUtils.validatePassword(password)) {
      mPasswordViewLayout.setError(getString(R.string.error_invalid_password));
      focusView = mPasswordView;
      cancel = true;
    }

    // Check for a valid email address
    if (TextUtils.isEmpty(email)) {
      mEmailViewLayout.setError(getString(R.string.error_field_required));
      focusView = mEmailView;
      cancel = true;
    } else if (!AccountUtils.validateEmail(email)) {
      mEmailViewLayout.setError(getString(R.string.error_invalid_email));
      focusView = mEmailView;
      cancel = true;
    }

    // Check for a valid name (is last to set focus properly).
    if (TextUtils.isEmpty(name)) {
      mNameViewLayout.setError(getString(R.string.error_field_required));
      focusView = mNameView;
      cancel = true;
    } else if (!AccountUtils.validateName(name)) {
      mNameViewLayout.setError(getString(R.string.error_invalid_name));
      focusView = mNameView;
      cancel = true;
    }

    if (cancel) {
      // Ignore create attempt (due to error) and set focus to last field with error
      focusView.requestFocus();
    } else {
      // Show a progress spinner and start a background task to perform the account creation attempt
      mProgressDialog.show();

      mAuthTask = new CreateAccountTask(email, name, password);
      mAuthTask.execute((Void) null);
    }
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
   * Represents an asynchronous account creation task used to create a user account
   */
  public class CreateAccountTask extends AsyncTask<Void, Void, Boolean> {

    private final String mEmail;
    private final String mName;
    private final String mPassword;

    CreateAccountTask(String email, String name, String password) {
      mEmail = email;
      mName = name;
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

      try {
        // Get the user file for modification (adding user)
        Document usersFile = XMLFileUtils.getFile(getActivity().getBaseContext(), XMLFileUtils.USERS_FILE_NAME);
        Element rootElement = usersFile.getRootElement();

        List<Node> users = usersFile.selectNodes("/users/user");
        boolean isUniqueUser = true;

        // Verify that the user does not already exist
        for (Node user : users) {
          if (user.valueOf("@email").equals(mEmail)) {
            isUniqueUser = false;
            break;
          }
        }

        // TODO: This needs to be modified to return a proper error (if possible)
        if (!isUniqueUser) {
          return false;
        }

        // Add the new user to the document
        //  NOTE: This only works because it is the root element - how else could it work?
        rootElement.addElement("user")
            .addAttribute("email", mEmail)
            .addAttribute("name", mName)
            .addAttribute("password", mPassword);

        // Write the modified file back out
        XMLFileUtils.createFile(getActivity().getBaseContext(), XMLFileUtils.USERS_FILE_NAME, usersFile);

        Log.d("MileageApp.auth", String.format("New user added: '%s', '%s', '%s'", mName, mEmail, mPassword));

        return true;
      } catch(Exception e) {
        return false;
      }
    }

    @Override
    protected void onPostExecute(final Boolean success) {
      mAuthTask = null;
      mProgressDialog.dismiss();

      if (success) {
        // NOTE: Determine what to do on successful account creation (likely send email)
        // TODO: Redirect to Login screen (now a tab)
        //getActivity().finish();
      } else {
        mEmailViewLayout.setError(getString(R.string.error_account_already_exists));
        mEmailViewLayout.requestFocus();
      }

      // TODO: Find why this doesn't work (even on failure)
      // Define a snackbar based on the operation status
      CharSequence snackbarResource = success
          ? getString(R.string.success_create_account)
          : getString(R.string.failure_create_account);
      Snackbar resultSnackbar = Snackbar.make(mCreateAccountFormView, snackbarResource, Snackbar.LENGTH_SHORT);
      resultSnackbar.show();
    }

    @Override
    protected void onCancelled() {
      // NOTE: Should it be possible to cancel Account Creation?
      mAuthTask = null;
      mProgressDialog.dismiss();
    }
  }
}

