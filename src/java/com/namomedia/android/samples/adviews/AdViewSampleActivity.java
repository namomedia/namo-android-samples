package com.namomedia.android.samples.adviews;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;

import com.namomedia.android.Namo;
import com.namomedia.android.NamoListAdapter;
import com.namomedia.android.samples.MainActivity;
import com.namomedia.android.samples.R;
import com.namomedia.android.samples.common.ExampleListAdapter;

/**
 * Activity that connects to the sdk and shows a list with just ads.
 */
public class AdViewSampleActivity extends ListActivity {

  private static final String AD_LAYOUT_ID_KEY = "AD_LAYOUT_ID";
  private static final String AD_FORMAT_ID_KEY = "AD_FORMAT_ID";

  public static Intent createIntent(MainActivity context, int layoutId, String formatId) {
    Intent intent = new Intent(context, AdViewSampleActivity.class);
    intent.putExtra(AD_LAYOUT_ID_KEY, layoutId);
    intent.putExtra(AD_FORMAT_ID_KEY, formatId);
    return intent;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    int adLayoutId = getIntent().getIntExtra(AD_LAYOUT_ID_KEY, R.layout.namo_ad_view_sample_1);
    String formatId = getIntent().getStringExtra(AD_FORMAT_ID_KEY);
    setTitle(formatId);

    NamoListAdapter namoAdapter = Namo.createListAdapter(this, new ExampleListAdapter(this));
    namoAdapter.registerAdLayout(adLayoutId, formatId);
    setListAdapter(namoAdapter);
    namoAdapter.requestAds();
  }
}

