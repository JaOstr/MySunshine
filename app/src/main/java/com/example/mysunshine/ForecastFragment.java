package com.example.mysunshine;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ForecastFragment extends Fragment {
    private final String TAG = ForecastFragment.class.getSimpleName();
    private ArrayAdapter<String> mAdapter;
    public ForecastFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        String[] arr = {
                "Today—Sunny—88/63",
                "Tomorrow—Foggy—70/46",
                "Weds—Cloudy—72/63",
                "Thurs—Sunny—64/51",
                "Fri—Sunny—70/46",
                "Sat—Sunny—76/68"};
        List<String> list = Arrays.asList(arr);

        mAdapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                list);

        ListView listView = rootView.findViewById(R.id.listview);
        listView.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecast_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_refresh) {
            FetchWeatherTask task = new FetchWeatherTask();
            task.execute("Warsaw,pl");
            try {
                String x = task.get();
                Log.d(TAG, "Got: " + x);
            } catch (Exception ex) {
                Log.w(TAG, "Got exception!");
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class FetchWeatherTask extends AsyncTask<String, Void, String> {
        private static final String TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String forecastJsonStr = null;

            try {
                //My solution - use the 5day/3hours API, available for free
                //https://api.openweathermap.org/data/2.5/forecast?q=Warsaw,pl&units=metric&appid=31ef6fa86724f932475b92bdb9d389ae
                //Kathy's solution uses 16-day forecast, now a paid service :(
                //https://api.openweathermap.org/data/2.5/forecast/daily?q=Warsaw,pl&mode=json&units=metric&cnt=7&appid=31ef6fa86724f932475b92bdb9d389ae
                String place = strings[0];
                final String URL =
                        "https://api.openweathermap.org/data/2.5/forecast?";

                Uri builtUri = Uri.parse(URL)
                        .buildUpon()
                        .appendQueryParameter("q", place)
                        .appendQueryParameter("mode", "json")
                        .appendQueryParameter("units", "metric")
                        .appendQueryParameter("appid", "31ef6fa86724f932475b92bdb9d389ae")
                        .build();

                String stringUrl = builtUri.toString();
                Log.v(TAG, "URL: " + stringUrl);
                URL url = new URL(stringUrl);

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                forecastJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(TAG, "Error closing stream", e);
                    }
                }
            }
            return forecastJsonStr;
        }
    }
}
