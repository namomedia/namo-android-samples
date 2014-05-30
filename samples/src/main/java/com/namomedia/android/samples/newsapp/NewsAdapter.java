package com.namomedia.android.samples.newsapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.namomedia.android.samples.R;
import com.namomedia.android.samples.common.ImageLoader;

import java.util.List;

/**
 * Adapter for the news list stream
 */
public class NewsAdapter extends BaseAdapter {

  private final List<NewsAppItem> items;
  private final Context context;
  private final ImageLoader imageLoader;

  public NewsAdapter(Context context, List<NewsAppItem> items, ImageLoader imageLoader) {
    this.context = context;
    this.items = items;
    this.imageLoader = imageLoader;
  }

  @Override
  public int getCount() {
    return items.size();
  }

  @Override
  public NewsAppItem getItem(int position) {
    return items.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    View view = convertView;
    if (view == null) {
      view = LayoutInflater.from(context).inflate(R.layout.newsapp_list_item, null);
      view.setTag(new ViewHolder(view));
    }

    ViewHolder holder = (ViewHolder) view.getTag();
    holder.bindItem(getItem(position));
    return view;
  }

  private class ViewHolder {

    TextView title;
    TextView text;
    ImageView thumbnail;

    ViewHolder(View view) {
      title = (TextView) view.findViewById(R.id.title);
      text = (TextView) view.findViewById(R.id.text);
      thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
    }

    void bindItem(NewsAppItem item) {
      title.setText(item.title);
      text.setText(item.text);

      int imageId = context.getResources().getIdentifier(
          item.image, "drawable", context.getPackageName());
      imageLoader.download(imageId, thumbnail);
    }
  }
}
