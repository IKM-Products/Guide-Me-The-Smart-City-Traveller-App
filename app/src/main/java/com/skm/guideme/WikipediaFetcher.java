package com.skm.guideme;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import androidx.annotation.NonNull;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONObject;

import java.io.IOException;

/** @noinspection ALL*/
public class WikipediaFetcher {

    private final OkHttpClient client;
    private final Handler mainHandler;
    private String name;
    private String fallbackEnglishName;

    public WikipediaFetcher(Context context) {
        client = new OkHttpClient();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public void getPlaceSummary(String name, String fallbackEnglishName, TextView textView) {
        this.name = name;
        this.fallbackEnglishName = fallbackEnglishName;
    }

    public String getFallbackEnglishName() {
        return fallbackEnglishName;
    }

    public void setFallbackEnglishName(String fallbackEnglishName) {
        this.fallbackEnglishName = fallbackEnglishName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Callback interface to return summary and image URL asynchronously.
     */
    public interface SummaryCallback {
        void onSummaryFetched(String summary, String imageUrl);
    }

    /**
     * Fetches Wikipedia summary and thumbnail image URL for a place.
     * Tries Nepali title first; if no summary found, falls back to English title.
     */
    public void getPlaceSummaryWithImage(String nepaliTitle, String englishTitle, SummaryCallback callback) {
        fetchFromWikipedia(nepaliTitle, (summary, imageUrl) -> {
            if (summary == null || summary.isEmpty()) {
                // If Nepali title fails, try English title
                fetchFromWikipedia(englishTitle, callback);
            } else {
                callback.onSummaryFetched(summary, imageUrl);
            }
        });
    }

    /**
     * Internal method to call Wikipedia API and parse summary + thumbnail image.
     */
    private void fetchFromWikipedia(String title, SummaryCallback callback) {
        // Encode the title for URL
        String encodedTitle = title.replace(" ", "%20");

        // Wikipedia API URL for extracts + page images in JSON format
        String url = "https://en.wikipedia.org/w/api.php?action=query&prop=extracts|pageimages" +
                "&exintro&explaintext&format=json&pithumbsize=300&titles=" + encodedTitle;

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                mainHandler.post(() -> callback.onSummaryFetched(null, null));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    mainHandler.post(() -> callback.onSummaryFetched(null, null));
                    return;
                }

                String jsonString = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(jsonString);
                    JSONObject query = jsonObject.optJSONObject("query");
                    if (query == null) {
                        mainHandler.post(() -> callback.onSummaryFetched(null, null));
                        return;
                    }

                    JSONObject pages = query.optJSONObject("pages");
                    if (pages == null) {
                        mainHandler.post(() -> callback.onSummaryFetched(null, null));
                        return;
                    }

                    // pages key is dynamic pageid, so iterate keys
                    String pageId = pages.keys().next();
                    JSONObject page = pages.optJSONObject(pageId);
                    if (page == null) {
                        mainHandler.post(() -> callback.onSummaryFetched(null, null));
                        return;
                    }

                    // Extract summary
                    String extract = page.optString("extract", "");

                    // Extract thumbnail image URL if exists
                    String imageUrl = null;
                    JSONObject thumbnail = page.optJSONObject("thumbnail");
                    if (thumbnail != null) {
                        imageUrl = thumbnail.optString("source", null);
                    }

                    String finalImageUrl = imageUrl;

                    mainHandler.post(() -> callback.onSummaryFetched(extract, finalImageUrl));

                } catch (Exception e) {
                    e.printStackTrace();
                    mainHandler.post(() -> callback.onSummaryFetched(null, null));
                }
            }
        });
    }
}
