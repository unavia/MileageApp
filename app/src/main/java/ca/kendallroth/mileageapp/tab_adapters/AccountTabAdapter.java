package ca.kendallroth.mileageapp.tab_adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Custom FragmentPagerAdapter for the Account ContentSwitcher
 */
public class AccountTabAdapter extends FragmentPagerAdapter {

  // Expose a list of fragments in order to call public functions
  //  Taken from: https://stackoverflow.com/questions/25629042/calling-a-fragment-method-from-an-activity-android-tabs
  private ArrayList<Fragment> mAdapterFragments;

  // UI Components
  private List<String> mTabTitles;

  public AccountTabAdapter(FragmentManager fragmentManager, ArrayList<Fragment> adapterFragments) {
    super(fragmentManager);

    // Set the list of adapter fragments
    mAdapterFragments = adapterFragments;

    // Set tab titles list
    mTabTitles = Arrays.asList("Login", "Register");
  }

  @Override
  public int getCount() {
    return mAdapterFragments.size();
  }

  @Override
  public int getItemPosition(Object object) {
    return POSITION_NONE;
  }

  @Override
  public CharSequence getPageTitle(int position) {
    // Return the page title at the requested position
    return mTabTitles.get(position);
  }

  @Override
  public Fragment getItem(int position) {
    // Return the Fragment at the requested position
    return mAdapterFragments.get(position);
  }
}
