package com.namomedia.android.samples.blogreader;

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
 * Adapter for the Blog Reader list stream.
 */
class BlogReaderAdapter extends BaseAdapter {

  private final List<BlogReaderItem> items;
  private final Context context;
  private final ImageLoader imageLoader;

  public BlogReaderAdapter(Context context, List<BlogReaderItem> items, ImageLoader imageLoader) {
    this.context = context;
    this.items = items;
    this.imageLoader = imageLoader;
  }

  @Override
  public int getCount() {
    return items.size();
  }

  @Override
  public BlogReaderItem getItem(int position) {
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
      view = LayoutInflater.from(context).inflate(R.layout.blogreader_list_item, null);
      view.setTag(new ViewHolder(view));
    }

    ViewHolder holder = (ViewHolder) view.getTag();
    holder.bindItem(getItem(position));
    return view;

  }

  @Override
  public boolean isEnabled(int position) {
    return false;
  }

  private class ViewHolder {

    final ImageView image;
    final TextView blogName;
    final TextView time;
    final TextView title;
    final TextView content;
    final TextView likeCount;

    ViewHolder(View view) {
      image = (ImageView) view.findViewById(R.id.image);
      blogName = (TextView) view.findViewById(R.id.blog_name);
      time = (TextView) view.findViewById(R.id.time);
      title = (TextView) view.findViewById(R.id.title);
      content = (TextView) view.findViewById(R.id.content);
      likeCount = (TextView) view.findViewById(R.id.likeCount);
    }

    void bindItem(BlogReaderItem item) {
      blogName.setText(item.blogName);
      time.setText(item.time);
      title.setText(item.title);
      content.setText(item.content);
      likeCount.setText(item.likeCount);

      int imageId = context.getResources().getIdentifier(
          item.image, "drawable", context.getPackageName());
      imageLoader.download(imageId, image);
    }
  }
}
