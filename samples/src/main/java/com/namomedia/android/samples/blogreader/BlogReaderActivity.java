package com.namomedia.android.samples.blogreader;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.gson.reflect.TypeToken;
import com.namomedia.android.Namo;
import com.namomedia.android.NamoListAdapter;
import com.namomedia.android.NamoTargeting;
import com.namomedia.android.samples.R;
import com.namomedia.android.samples.common.Data;
import com.namomedia.android.samples.common.ImageLoader;

import java.util.Calendar;
import java.util.List;


/**
 * Activity that shows a Blog Reader demo with embedded ads.
 */
public class BlogReaderActivity extends Activity {

  ListView listView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
    setContentView(R.layout.blogreader_activity);
    getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.blogreader_window_title);

    listView = (ListView) findViewById(R.id.list);

    List<BlogReaderItem> data = Data.fromJson(
        this, R.raw.blogreader_data, new TypeToken<List<BlogReaderItem>>() {
        }
    );
    ListAdapter adapter = new BlogReaderAdapter(this, data, new ImageLoader());
    NamoListAdapter namoAdapter = Namo.createListAdapter(this, adapter);
    namoAdapter.registerAdLayout(R.layout.blogreader_ad_list_item, "blogreader_ad_list_item");
    listView.setAdapter(namoAdapter);

    NamoTargeting targeting = new NamoTargeting();
    targeting.addInterests("girls", "hoodies");
    targeting.setGender(NamoTargeting.Gender.FEMALE);
    Calendar birthDate = Calendar.getInstance();
    birthDate.set(1985, Calendar.JANUARY, 8);
    targeting.setBirthDay(birthDate.getTime());
    namoAdapter.requestAds(targeting);
  }
}
