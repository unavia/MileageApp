package ca.kendallroth.mileageapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class AuthActivity extends AppCompatActivity {

  private static final int NUM_ITEMS = 2;

  private AccountTabAdapter mTabAdapter;
  private ViewPager mViewPager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_auth);

    mTabAdapter = new AccountTabAdapter(getSupportFragmentManager());

    mViewPager = (ViewPager) findViewById(R.id.view_pager);
    mViewPager.setAdapter(mTabAdapter);

    // TODO: Use tab button clicks to update current tab

    // Set the initial view item ("Login" fragment)
    mViewPager.setCurrentItem(0);

    // Hide the Action bar (on all "Authentication" activities)
    getSupportActionBar().hide();
  }

  public static class AccountTabAdapter extends FragmentPagerAdapter {
    public AccountTabAdapter(FragmentManager fragmentManager) {
      super(fragmentManager);
    }

    @Override
    public int getCount() {
      return NUM_ITEMS;
    }

    @Override
    public Fragment getItem(int position) {
      Fragment fragment;

      // Return the requested (selected) fragment
      switch (position) {
        case 0:
          fragment =  LoginFragment.newInstance("Login");
          break;
        case 1:
          fragment = RegisterFragment.newInstance("Register");
          break;
        default:
          fragment = null;
          break;
      }

      return fragment;
    }
  }
}
