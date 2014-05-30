package com.namomedia.android.samples;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.namomedia.android.Namo;
import com.namomedia.android.samples.adviews.AdViewSampleActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Parent activity to host demos.
 */
public class MainActivity extends ListActivity {

  private List<ListItem> items;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Initialize the Namo SDK.
    // Once you have Namo Dashboard access, you should replace "app-test-id" with your real
    // app ID from https://dashboard.namomedia.com/monetize/apps
    Namo.initialize(/* context */ this, "app-test-id");

    // Get items and sort by category
    Map<String, List<ListItem>> categoryMap = new TreeMap<String, List<ListItem>>();
    for (ListItem item : findActivityItems()) {
      String key = item.category;
      if (key == null) {
        continue;
      }

      List<ListItem> items = categoryMap.get(key);
      if (items == null) {
        items = new ArrayList<ListItem>();
        categoryMap.put(key, items);
      }
      items.add(item);
    }

    // Make all list items, including headers
    items = new ArrayList<ListItem>();
    for (String category : categoryMap.keySet()) {
      items.add(new ListItem(category));
      items.addAll(categoryMap.get(category));
    }

    // Set the adapter
    setListAdapter(new ListItemAdapter(this, items));
  }

  private List<ListItem> findActivityItems() {
    List<ListItem> items = new ArrayList<ListItem>();

    // Query for all activities in this package.
    Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
    mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
    PackageManager packageManager = getPackageManager();
    PackageInfo packageInfo;
    try {
      packageInfo = packageManager.getPackageInfo(
          "com.namomedia.android.samples", PackageManager.GET_ACTIVITIES | PackageManager
              .GET_META_DATA
      );
    } catch (PackageManager.NameNotFoundException e) {
      throw new AssertionError("Not expected");
    }
    ActivityInfo[] activityList = packageInfo.activities;

    // Build a list item for each activity.
    for (ActivityInfo info : activityList) {
      String title = info.loadLabel(packageManager).toString();

      String category = null;
      if (info.metaData != null) {
        category = info.metaData.getString("category");
      }

      Intent intent = new Intent();
      intent.setClassName(info.applicationInfo.packageName, info.name);

      items.add(new ListItem(title, category, intent));
    }

    // Build a list item for each sample
    int[] adLayoutIds = {
        R.layout.namo_ad_view_sample_1,
        R.layout.namo_ad_view_sample_2,
        R.layout.namo_ad_view_sample_3,
        R.layout.namo_ad_view_sample_4,
        R.layout.namo_ad_view_sample_5
    };
    for (int i = 0; i < adLayoutIds.length; ++i) {
      int num = i + 1;
      Intent intent = AdViewSampleActivity.createIntent(
          this, adLayoutIds[i], "namo_ad_view_sample_" + num);
      items.add(new ListItem("Ad View Sample " + num, "Samples", intent));
    }

    return items;
  }

  @Override
  @SuppressWarnings("unchecked")
  protected void onListItemClick(ListView listView, View view, int position, long id) {
    ListItem listItem = items.get(position);
    startActivity(listItem.intent);
  }

  /**
   * Stores items in the demo list
   */
  private static class ListItem {

    final String title;
    final String category;
    final Intent intent;

    ListItem(String title) {
      this(title, null, null);
    }

    ListItem(String title, String category, Intent intent) {
      this.title = (intent == null) ? title.toUpperCase() : title;
      this.category = category;
      this.intent = intent;
    }

    boolean isHeader() {
      return intent == null;
    }
  }

  private static class ListItemAdapter extends BaseAdapter {

    private final List<ListItem> items;
    private final LayoutInflater inflater;

    public ListItemAdapter(Context context, List<ListItem> items) {
      this.items = items;
      inflater = LayoutInflater.from(context);
    }

    @Override
    public int getViewTypeCount() {
      return 2;  // Headers and regular items.
    }

    @Override
    public int getItemViewType(int position) {
      ListItem item = items.get(position);
      return item.isHeader() ? 0 : 1;
    }

    @Override
    public int getCount() {
      return items.size();
    }

    @Override
    public ListItem getItem(int position) {
      return items.get(position);
    }

    @Override
    public long getItemId(int position) {
      return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      ListItem item = items.get(position);
      if (convertView == null) {
        if (item.isHeader()) {
          convertView = inflater.inflate(R.layout.section_header_list_item, parent, false);
        } else {
          convertView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        }
      }

      TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
      textView.setText(item.title);
      return convertView;
    }

    @Override
    public boolean isEnabled(int position) {
      return !items.get(position).isHeader();
    }
  }
}
