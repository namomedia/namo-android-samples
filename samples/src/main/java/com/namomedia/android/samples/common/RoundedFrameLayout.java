package com.namomedia.android.samples.common;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.namomedia.android.samples.R;

/**
 * Class that clips a LinearLayout content in order to show rounded corners.
 */
public class RoundedFrameLayout extends FrameLayout {

  /**
   * Used locally to tag Logs
   */
  @SuppressWarnings("unused")
  private static final String TAG = RoundedFrameLayout.class.getSimpleName();
  private final Path mPath = new Path();

  public RoundedFrameLayout(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init();
  }

  public RoundedFrameLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public RoundedFrameLayout(Context context) {
    super(context);
    init();
  }

  private void init() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      // we have to remove the hardware acceleration if we want the clip
      setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    mPath.reset();
    float round = getResources().getDimension(R.dimen.bl_clipped_corner_radius);
    mPath.addRoundRect(new RectF(
            getPaddingLeft(),
            getPaddingTop(),
            w - getPaddingRight(),
            h - getPaddingBottom()),
        round, round, Path.Direction.CW
    );
  }

  @Override
  protected void dispatchDraw(Canvas canvas) {
    canvas.clipPath(mPath);
    super.dispatchDraw(canvas);
  }
}