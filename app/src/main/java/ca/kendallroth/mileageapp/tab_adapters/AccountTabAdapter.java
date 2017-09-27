package ca.kendallroth.mileageapp.tab_adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import java.util.Arrays;
import java.util.List;

import ca.kendallroth.mileageapp.LoginFragment;
import ca.kendallroth.mileageapp.RegisterFragment;

/**
 * Custom FragmentPagerAdapter for the Account ContentSwitcher
 */
public class AccountTabAdapter extends FragmentPagerAdapter {

  private static int NUM_ITEMS = 2;

  // UI Components
  private List<String> mTabTitles;

  public AccountTabAdapter(FragmentManager fragmentManager) {
    super(fragmentManager);

    // Set tab titles list
    mTabTitles = Arrays.asList("Login", "Register");

    // DEBUG: Log an error if the amount of tab titles doesn't equal the amount of actual tab items
    if (mTabTitles.size() != NUM_ITEMS) {
      Log.e("MileageApp", "Tab title count is different than NUM_ITEMS (number of tabs)");
    }
  }

  @Override
  public int getCount() {
    return NUM_ITEMS;
  }

  @Override
  public CharSequence getPageTitle(int position) {
    // Return the page title at the requested position
    return mTabTitles.get(position);
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
