  package ca.kendallroth.mileageapp.components;

  import android.content.Context;
  import android.support.design.widget.TabLayout;
  import android.support.v4.app.FragmentPagerAdapter;
  import android.support.v4.view.ViewPager;
  import android.support.v4.view.ViewPager.OnPageChangeListener;
  import android.util.AttributeSet;
  import android.util.Log;
  import android.view.LayoutInflater;
  import android.widget.LinearLayout;

  import ca.kendallroth.mileageapp.R;

  /**
   * Tabbed Fragment layout with swipe functionality
   */
  public class ContentSwitcher extends LinearLayout {
    // UI components
    private TabLayout mTabLayout;
    private ViewPager mTabPager;

    // Constructor called by programmatic creation
    public ContentSwitcher(Context context) {
      super(context);

      Log.d("MileageApp", "ca.kendallroth.mileageapp.components.ContentSwitcher initialized");

      init(context);
    }

    // Constructor called by XML creation
    public ContentSwitcher(Context context, AttributeSet attributes) {
      super(context, attributes);

      Log.d("MileageApp", "ca.kendallroth.mileageapp.components.ContentSwitcher initialized (2)");

      init(context);
    }

    /**
     * Initialize the layout and sub-components
     * @param context Android context
     */
    private void init(Context context) {
      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

      inflater.inflate(R.layout.control_content_switcher, this);

      // Initialize UI components
      mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
      mTabPager = (ViewPager) findViewById(R.id.tab_pager);
    }

    /**
     * Set the FragmentPagerAdapter for the ViewPager tabs
     * @param fragmentPagerAdapter Custom adapter responsible for displaying tab fragments
     */
    public void setPagerAdapter(FragmentPagerAdapter fragmentPagerAdapter) {
      mTabPager.setAdapter(fragmentPagerAdapter);
    }

    /**
     * Set the OnPageChangeListener to handle page change events
     * @param pageChangeListener Custom listener function to handle page change events
     */
    public void setPageChangeListener(OnPageChangeListener pageChangeListener) {
      mTabPager.addOnPageChangeListener(pageChangeListener);
    }
  }
