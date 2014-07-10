package com.namomedia.android.samples.common;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Helper class to provide canned data for the demo list adapters.
 */
public class Data {

  public static <T> List<T> fromJson(
      Context context, int resourceId, TypeToken<List<T>> typeToken) {
    Gson gson = new Gson();
    InputStream inputStream = context.getResources().openRawResource(resourceId);
    InputStreamReader reader = new InputStreamReader(inputStream);
    JsonReader jsonReader = new JsonReader(reader);
    return gson.fromJson(jsonReader, typeToken.getType());
  }
}
