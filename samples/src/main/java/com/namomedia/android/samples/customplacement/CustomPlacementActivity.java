package com.namomedia.android.samples.customplacement;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.namomedia.android.Namo;
import com.namomedia.android.NamoAdData;
import com.namomedia.android.NamoAdListener;
import com.namomedia.android.NamoAdPlacer;
import com.namomedia.android.NamoPositionAdjuster;
import com.namomedia.android.NamoTargeting;
import com.namomedia.android.samples.R;

import java.util.Locale;

public class CustomPlacementActivity extends ActionBarActivity implements NamoAdListener {

  /**
   * The {@link android.support.v4.view.PagerAdapter} that will provide
   * fragments for each of the sections. We use a
   * {@link FragmentPagerAdapter} derivative, which will keep every
   * loaded fragment in memory. If this becomes too memory intensive, it
   * may be best to switch to a
   * {@link android.support.v4.app.FragmentStatePagerAdapter}.
   */
  private SectionsPagerAdapter sectionsPagerAdapter;

  /**
   * The {@link ViewPager} that will host the section contents.
   */
  private ViewPager viewPager;
  private NamoAdPlacer adPlacer;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_custom_placement);

    // Initialize the Namo SDK.
    // Once you have Namo Dashboard access, you should replace "app-test-id" with your real
    // app ID from https://dashboard.namomedia.com/monetize/apps
    Namo.initialize(/* context */ this, "app-test-id");

    // Create the NamoAdPlacer to request ads and handle automatically positioning them.
    this.adPlacer = Namo.createAdPlacer(/* context */ this);
    adPlacer.registerAdViewBinder(new SimpleAdViewBinder(this, R.layout.custom_placement_ad_layout, "ad_view_sample_1"));
    adPlacer.setAdListener(this);

    // Creating targeting information for better ad results.
    NamoTargeting targeting = new NamoTargeting();
    targeting.setGender(NamoTargeting.Gender.FEMALE);

    // NOTE: If you don't set android:config-changes="orientation" in AndroidManifest.xml
    // for this activity, new ads will be requested every time the device is rotated.
    adPlacer.requestAds(targeting);

    // Create the adapter that will return a fragment for each of the three
    // primary sections of the activity.
    sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

    // Set up the ViewPager with the sections adapter.
    viewPager = (ViewPager) findViewById(R.id.pager);
    viewPager.setAdapter(sectionsPagerAdapter);
  }

  @Override
  public void onAttachFragment(Fragment fragment) {
    if (fragment instanceof AdViewFragment) {
      ((AdViewFragment) fragment).setAdPlacer(adPlacer);
    }
  }

  @Override
  public void onAdsLoaded(NamoPositionAdjuster namoPositionAdjuster) {
    viewPager.invalidate();
    sectionsPagerAdapter.notifyDataSetChanged();
  }

  /**
   * A placeholder fragment containing a simple view.
   */
  public static class PlaceholderFragment extends Fragment {

    private static final int[] PAGE_COLORS = {Color.BLACK, Color.RED, Color.GREEN,
        Color.BLUE, Color.YELLOW, Color.MAGENTA,
        Color.CYAN, Color.LTGRAY};
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    // Required by the Fragment manager to re-instantiate fragments when they are destroyed.
    public PlaceholderFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PlaceholderFragment newInstance(int sectionNumber) {
      PlaceholderFragment fragment = new PlaceholderFragment();
      Bundle args = new Bundle();
      args.putInt(ARG_SECTION_NUMBER, sectionNumber);
      fragment.setArguments(args);
      return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
      View rootView = inflater.inflate(R.layout.custom_placement_fragment_main, container, false);
      int sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);
      rootView.setBackgroundColor(PAGE_COLORS[sectionNumber % PAGE_COLORS.length]);
      TextView textView = (TextView) rootView.findViewById(R.id.section_label);
      textView.setText("Page " + Integer.toString(sectionNumber));
      return rootView;
    }
  }

  /**
   * An AdViewFragment to contain the rendered ad view.
   */
  public static class AdViewFragment extends Fragment {
    private static final int NO_VALUE = -1234;
    private static final String ARG_AD_POSITION = "ad_position";
    private NamoAdPlacer adPlacer;

    // Required by the Fragment manager to re-instantiate fragments when they are destroyed.
    public AdViewFragment() {
    }

    public static AdViewFragment newInstance(int adPosition) {
      AdViewFragment fragment = new AdViewFragment();
      Bundle bundle = new Bundle();
      bundle.putInt(ARG_AD_POSITION, adPosition);
      fragment.setArguments(bundle);
      return fragment;
    }

    public void setAdPlacer(NamoAdPlacer adPlacer) {
      this.adPlacer = adPlacer;
    }

    @Override
    public void onAttach(Activity activity) {
      super.onAttach(activity);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      int adPosition = getArguments().getInt(ARG_AD_POSITION, NO_VALUE);

      if (adPosition != NO_VALUE && adPlacer != null) {
        NamoAdData adData = adPlacer.getPositionAdjuster().getAdData(adPosition);
        // This is set by the activity in onAttachFragment
        return adPlacer.placeAd(adData, null, container);
      }

      // This is an error.
      return null;
    }
  }

  /**
   * A {@link FragmentStatePagerAdapter} that returns a fragment corresponding to
   * one of the sections/tabs/pages.
   */
  public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

    public SectionsPagerAdapter(FragmentManager fm) {
      super(fm);
    }

    @Override
    public int getItemPosition(Object object) {
      // This is used to force recreating all views when invalidate() is called on the ViewPager.
      return POSITION_NONE;
    }

    @Override
    public Fragment getItem(int position) {
      if (adPlacer.getPositionAdjuster().isAd(position)) {
        return AdViewFragment.newInstance(position);
      } else {
        // Return a PlaceholderFragment (defined as a static inner class below).
        return PlaceholderFragment.newInstance(
            adPlacer.getPositionAdjuster().getOriginalPosition(position) + 1);
      }
    }

    @Override
    public int getCount() {
      // Without ads there are 12 items. You can test adjustment by disabling network connectivity
      // on the device.
      return adPlacer.getPositionAdjuster().getAdjustedCount(12);
    }

    @Override
    public CharSequence getPageTitle(int position) {
      Locale locale = Locale.getDefault();
      return getString(R.string.title_section).toUpperCase(locale) + Integer.toString(position);
    }
  }
}
