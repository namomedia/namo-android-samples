package com.namomedia.android.samples.newsapp;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;

import com.google.gson.reflect.TypeToken;
import com.namomedia.android.Namo;
import com.namomedia.android.NamoListAdapter;
import com.namomedia.android.samples.R;
import com.namomedia.android.samples.common.Data;
import com.namomedia.android.samples.common.ImageLoader;

import java.util.List;

/**
 * Activity that shows a New York Times app demo with embedded ads.
 */
public class NewsAppActivity extends ListActivity {

  NamoListAdapter namoAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    List<NewsAppItem> data = Data.fromJson(this, R.raw.newsapp_data, new TypeToken<List<NewsAppItem>>() {
    });
    ListAdapter adapter = new NewsAdapter(this, data, new ImageLoader());
    namoAdapter = Namo.createListAdapter(this, adapter);
    getListView().setOnItemClickListener(
        namoAdapter.createWrappedClickListener(new AdapterView.OnItemClickListener() {
          @Override
          public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            Log.d("NewsAppActivity", String.format("Clicked item %d", position));
          }
        })
    );
    namoAdapter.registerAdLayout(R.layout.newsapp_ad_list_item, "newsapp_ad_list_item");
    setListAdapter(namoAdapter);
    namoAdapter.requestAds();
  }
}
