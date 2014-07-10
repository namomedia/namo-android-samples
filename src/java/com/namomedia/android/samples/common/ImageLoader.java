/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.namomedia.android.samples.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This helper class download images from the Internet and binds those with the provided ImageView.
 * <p/>
 * It requires the INTERNET permission, which should be added to your application's manifest file.
 * <p/>
 * A local cache of downloaded images is maintained internally to improve performance.
 */
public class ImageLoader {

  private static final String LOG_TAG = "ImageLoader";
  private static final int HARD_CACHE_CAPACITY = 40;
  // Hard cache, with a fixed maximum capacity and a life duration
  private final static HashMap<Integer, Bitmap> sHardBitmapCache =
      new LinkedHashMap<Integer, Bitmap>(HARD_CACHE_CAPACITY / 2, 0.75f, true) {
        private static final long serialVersionUID = -7190622541619388252L;

        @Override
        protected boolean removeEldestEntry(Map.Entry<Integer, Bitmap> eldest) {
          if (size() > HARD_CACHE_CAPACITY) {
            // Entries push-out of hard reference cache are transferred to soft reference cache
            sSoftBitmapCache.put(eldest.getKey(), new SoftReference<Bitmap>(eldest.getValue()));
            return true;
          } else {
            return false;
          }
        }
      };
  // Soft cache for bitmap kicked out of hard cache
  private final static ConcurrentHashMap<Integer, SoftReference<Bitmap>> sSoftBitmapCache =
      new ConcurrentHashMap<Integer, SoftReference<Bitmap>>(HARD_CACHE_CAPACITY / 2);
  private static final int DELAY_BEFORE_PURGE = 30 * 1000; // in milliseconds
  private final Handler purgeHandler = new Handler();
  private final Runnable purger = new Runnable() {
    public void run() {
      clearCache();
    }
  };

  /**
   * Returns true if the current download has been canceled or if there was no download in progress
   * on this image view. Returns false if the download in progress deals with the same url. The
   * download is not stopped in that case.
   */
  private static boolean cancelPotentialDownload(int resourceId, ImageView imageView) {
    BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);

    if (bitmapDownloaderTask != null) {
      int bitmapId = bitmapDownloaderTask.resourceId;
      if (resourceId != bitmapId) {
        bitmapDownloaderTask.cancel(true);
      } else {
        // The same URL is already being downloaded.
        return false;
      }
    }
    return true;
  }

    /*
     * Same as download but the image is always downloaded and the cache is not used.
     * Kept private at the moment as its interest is not clear.
       private void forceDownload(String url, ImageView view) {
          forceDownload(url, view, null);
       }
     */

  /**
   * @param imageView Any imageView
   * @return Retrieve the currently active download task (if any) associated with this imageView.
   * null if there is no such task.
   */
  private static BitmapDownloaderTask getBitmapDownloaderTask(ImageView imageView) {
    if (imageView != null) {
      Drawable drawable = imageView.getDrawable();
      if (drawable instanceof DownloadedDrawable) {
        DownloadedDrawable downloadedDrawable = (DownloadedDrawable) drawable;
        return downloadedDrawable.getBitmapDownloaderTask();
      }
    }
    return null;
  }

  /**
   * @param resourceId The URL of the image to download.
   * @param imageView  The ImageView to bind the downloaded image to.
   */
  public void download(int resourceId, ImageView imageView) {
    resetPurgeTimer();

    Bitmap bitmap = getBitmapFromCache(resourceId);

    if (bitmap == null) {
      forceDownload(resourceId, imageView);
    } else {
      cancelPotentialDownload(resourceId, imageView);
      imageView.setImageBitmap(bitmap);
    }
  }

  /**
   * Same as download but the image is always downloaded and the cache is not used. Kept private at
   * the moment as its interest is not clear.
   */
  private void forceDownload(int resourceId, ImageView imageView) {
    if (cancelPotentialDownload(resourceId, imageView)) {
      BitmapDownloaderTask task = new BitmapDownloaderTask(imageView);
      DownloadedDrawable downloadedDrawable = new DownloadedDrawable(task);
      imageView.setImageDrawable(downloadedDrawable);
      task.execute(resourceId);
    }
  }

  /**
   * Clears the image cache used internally to improve performance. Note that for memory efficiency
   * reasons, the cache will automatically be cleared after a certain inactivity delay.
   */
  public void clearCache() {
    sHardBitmapCache.clear();
    sSoftBitmapCache.clear();
  }

  private void resetPurgeTimer() {
    purgeHandler.removeCallbacks(purger);
    purgeHandler.postDelayed(purger, DELAY_BEFORE_PURGE);
  }

  /**
   * @param resourceId The URL of the image that will be retrieved from the cache.
   * @return The cached bitmap or null if it was not found.
   */
  private Bitmap getBitmapFromCache(Integer resourceId) {
    // First try the hard reference cache
    synchronized (sHardBitmapCache) {
      final Bitmap bitmap = sHardBitmapCache.get(resourceId);
      if (bitmap != null) {
        // Bitmap found in hard cache
        // Move element to first position, so that it is removed last
        sHardBitmapCache.remove(resourceId);
        sHardBitmapCache.put(resourceId, bitmap);
        return bitmap;
      }
    }

    // Then try the soft reference cache
    SoftReference<Bitmap> bitmapReference = sSoftBitmapCache.get(resourceId);
    if (bitmapReference != null) {
      final Bitmap bitmap = bitmapReference.get();
      if (bitmap != null) {
        // Bitmap found in soft cache
        return bitmap;
      } else {
        // Soft reference has been Garbage Collected
        sSoftBitmapCache.remove(resourceId);
      }
    }

    return null;
  }

  /**
   * A fake Drawable that will be attached to the imageView while the download is in progress.
   * <p/>
   * Contains a reference to the actual download task, so that a download task can be stopped if
   * a new binding is required, and makes sure that only the last started download process can bind
   * its result, independently of the download finish order.
   */
  static class DownloadedDrawable extends ColorDrawable {

    private final WeakReference<BitmapDownloaderTask> bitmapDownloaderTaskReference;

    public DownloadedDrawable(BitmapDownloaderTask bitmapDownloaderTask) {
      super(Color.BLACK);
      bitmapDownloaderTaskReference =
          new WeakReference<BitmapDownloaderTask>(bitmapDownloaderTask);
    }

    public BitmapDownloaderTask getBitmapDownloaderTask() {
      return bitmapDownloaderTaskReference.get();
    }
  }

  /**
   * The actual AsyncTask that will asynchronously download the image.
   */
  class BitmapDownloaderTask extends AsyncTask<Integer, Void, Bitmap> {

    private static final int IO_BUFFER_SIZE = 4 * 1024;
    private final WeakReference<ImageView> imageViewReference;
    private final Context context;
    private int resourceId;

    public BitmapDownloaderTask(ImageView imageView) {
      context = imageView.getContext().getApplicationContext();
      imageViewReference = new WeakReference<ImageView>(imageView);
    }

    /**
     * Actual image load method.
     */
    @Override
    protected Bitmap doInBackground(Integer... params) {
      resourceId = params[0];
      InputStream is = context.getResources().openRawResource(resourceId);
      return BitmapFactory.decodeStream(is);
    }

    /**
     * Once the image is downloaded, associates it to the imageView
     */
    @Override
    protected void onPostExecute(Bitmap bitmap) {
      if (isCancelled()) {
        bitmap = null;
      }

      // Add bitmap to cache
      if (bitmap != null) {
        synchronized (sHardBitmapCache) {
          sHardBitmapCache.put(resourceId, bitmap);
        }
      }

      if (imageViewReference != null) {
        ImageView imageView = imageViewReference.get();
        BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);
        // Change bitmap only if this process is still associated with it
        if (this == bitmapDownloaderTask) {
          imageView.setImageBitmap(bitmap);
        }
      }
    }

    public void copy(InputStream in, OutputStream out) throws IOException {
      byte[] b = new byte[IO_BUFFER_SIZE];
      int read;
      while ((read = in.read(b)) != -1) {
        out.write(b, 0, read);
      }
    }
  }
}
