package com.namomedia.android.samples.common;

import android.content.Context;
import android.widget.ArrayAdapter;

/**
 * Dummy adapter useful for testing. This adapter just shows the items 1 to N.
 */
public class ExampleListAdapter extends ArrayAdapter<String> {

  public ExampleListAdapter(Context context) {
    this(context, 100);
  }

  public ExampleListAdapter(Context context, int numItems) {
    super(context, android.R.layout.simple_list_item_1);
    for (int i = 0; i < numItems; ++i) {
      this.add("Row " + i);
    }
  }
}
