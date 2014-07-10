package com.namomedia.android.samples.customplacement;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.namomedia.android.NamoAdData;
import com.namomedia.android.NamoAdViewBinder;

/**
 * An ad image binder that works by inflating a layout and looking for id resources to perform
 * binding to ad data.
 */
class SimpleAdViewBinder implements NamoAdViewBinder {
  private final Context context;
  private final int layoutId;
  private final String formatIdentifier;

  SimpleAdViewBinder(Context context, int layoutId, String formatIdentifier) {
    this.context = context;
    this.layoutId = layoutId;
    this.formatIdentifier = formatIdentifier;
  }

  @Override
  public String getFormatIdentifier() {
    return formatIdentifier;
  }

  @Override
  public View createView(ViewGroup parent) {
    View view = LayoutInflater.from(context)
        .inflate(layoutId, parent, /* attachToRoot */ false);
    AdViewHolder holder = new AdViewHolder(view);
    view.setTag(holder);
    return view;
  }

  @Override
  public void bindAdData(View view, NamoAdData adData) {
    AdViewHolder holder;
    Object tag = view.getTag();
    holder = (AdViewHolder) tag;
    holder.setData(adData);
  }

  /**
   * Binds fields from a view hierarchy to well pre-defined view ids.
   */
  static class AdViewHolder {
    private final TextBinder advertiserName;
    private final ImageBinder advertiserLogo;
    private final TextBinder adText;
    private final ImageBinder adImage;
    private final TextBinder adIndicator;

    AdViewHolder(View rootView) {
      this.advertiserName = createTextBinder(rootView, com.namomedia.android.R.id.namo_advertiser_name);
      this.advertiserLogo = createImageBinder(rootView, com.namomedia.android.R.id.namo_advertiser_icon);
      this.adText = createTextBinder(rootView, com.namomedia.android.R.id.namo_ad_text);
      this.adImage = createImageBinder(rootView, com.namomedia.android.R.id.namo_ad_image);
      this.adIndicator = createTextBinder(rootView, com.namomedia.android.R.id.namo_ad_indicator);
    }

    private TextBinder createTextBinder(View rootView, int id) {
      TextView textView = (TextView) rootView.findViewById(id);
      TextBinder binder = new TextBinder(textView);
      return binder.bindTo(id);
    }

    private ImageBinder createImageBinder(View rootView, int id) {
      ImageView imageView = (ImageView) rootView.findViewById(id);
      ImageBinder binder = new ImageBinder(imageView);
      return binder.bindTo(id);
    }

    void setData(NamoAdData data) {
      advertiserName.setData(data);
      advertiserLogo.setData(data);
      adText.setData(data);
      adImage.setData(data);
      adIndicator.setData(data);
    }
  }

  /**
   * Binds a TextView to a target ad data property.
   */
  static class TextBinder {
    private final TextView textView;
    private int targetId;

    TextBinder(TextView textView) {
      this.textView = textView;
    }

    TextBinder bindTo(int targetId) {
      this.targetId = targetId;
      return this;
    }

    void setData(NamoAdData adData) {
      if (textView == null) {
        return;
      }

      if (targetId == com.namomedia.android.R.id.namo_ad_text) {
        adData.loadText().allowChangeFontSize(true).into(textView);
      } else if (targetId == com.namomedia.android.R.id.namo_advertiser_name) {
        adData.loadAdvertiserName().into(textView);
      } else if (targetId == com.namomedia.android.R.id.namo_ad_indicator) {
        // Fill in the value only if empty.
        String value = textView.getText().toString();
        if (value.length() == 0) {
          value = "Ad";
        }
        textView.setText(value);
      }
    }
  }

  /**
   * Binds an ImageView to a target ad data property.
   */
  static class ImageBinder {
    private final ImageView imageView;
    private int targetId;

    ImageBinder(ImageView imageView) {
      this.imageView = imageView;
    }

    ImageBinder bindTo(int targetId) {
      this.targetId = targetId;
      return this;
    }

    void setData(NamoAdData adData) {
      if (imageView == null) {
        return;
      }

      if (targetId == com.namomedia.android.R.id.namo_ad_image) {
        adData.loadImage().into(imageView);
      } else if (targetId == com.namomedia.android.R.id.namo_advertiser_icon) {
        adData.loadAdvertiserIcon().into(imageView);
      }
    }
  }
}

