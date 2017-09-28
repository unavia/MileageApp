package ca.kendallroth.mileageapp.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;

import ca.kendallroth.mileageapp.fragments.LoginFragment;
import ca.kendallroth.mileageapp.fragments.RegisterFragment;
import ca.kendallroth.mileageapp.fragments.RegisterFragment.AccountCreateListener;
import ca.kendallroth.mileageapp.R;
import ca.kendallroth.mileageapp.components.ContentSwitcher;
import ca.kendallroth.mileageapp.tab_adapters.AccountTabAdapter;
import ca.kendallroth.mileageapp.utils.ClearableFragment;
import ca.kendallroth.mileageapp.utils.ScrollableFragment;

/**
 * Authorization activity that displays Login and Register workflows in a ViewPager
 */
public class AuthActivity extends AppCompatActivity implements AccountCreateListener {

  private ArrayList<Fragment> mAdapterFragments;
  private AccountTabAdapter mTabAdapter;
  private ContentSwitcher mContentSwitcher;
  private OnPageChangeListener mTabChangeListener;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_auth);

    // DEBUG: Sample OnPageChangeListener for scrolling/selecting tabs (fragments)
    mTabChangeListener = new OnPageChangeListener() {
      @Override
      public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        // Do nothing
      }

      @Override
      public void onPageSelected(int position) {
        Log.d("MileageApp", String.format("%s position selected", position));
      }

      @Override
      public void onPageScrollStateChanged(int state) {
        // Do nothing
      }
    };

    // Instantiate fragments and add to fragment list (never removed/destroyed)
    Fragment loginFragment = LoginFragment.newInstance("Login");
    Fragment registerFragment = RegisterFragment.newInstance("Register");

    mAdapterFragments = new ArrayList<Fragment>();
    mAdapterFragments.add(loginFragment);
    mAdapterFragments.add(registerFragment);

    // Create the Account ContentSwitcher (for switching between "Login" and "Register" screens)
    mContentSwitcher = (ContentSwitcher) findViewById(R.id.content_switcher);
    mTabAdapter = new AccountTabAdapter(getSupportFragmentManager(), mAdapterFragments);
    mContentSwitcher.setPagerAdapter(mTabAdapter);
    mContentSwitcher.setPageChangeListener(mTabChangeListener);

    // Hide the Action bar (on all "Authentication" activities)
    getSupportActionBar().hide();
  }

  @Override
  public void onAccountCreateRequest(String email, String password) {
    // TODO: Do something here with account creation

    Log.d("MileageApp", String.format("onAccountCreateRequest for '%s' with credentials '%s'", email, password));

    // Set to the login page fragment
    mContentSwitcher.setPage(0);

    // Clear the login page fragment
    Fragment fragment = mAdapterFragments.get(0);
    LoginFragment loginFragment = (LoginFragment) fragment;
    loginFragment.clearInputs();
    loginFragment.scrollToTop(false);
    loginFragment.prefillLogin(email);
  }
}
