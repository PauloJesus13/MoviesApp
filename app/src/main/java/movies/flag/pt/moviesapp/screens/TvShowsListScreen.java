package movies.flag.pt.moviesapp.screens;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import movies.flag.pt.AppEnv;
import movies.flag.pt.moviesapp.R;
import movies.flag.pt.moviesapp.adapters.TvShowsAdapter;
import movies.flag.pt.moviesapp.http.entities.TvShow;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static movies.flag.pt.moviesapp.R.layout.header_item;

public class TvShowsListScreen extends Screen {

    public static String TV_SHOW_ID = "tv_show_id";

    private static final String MOST_POPULAR = "most_popular_tv_shows";

    private ListView listView;
    private ProgressBar loader;
    private Button showMoreInfoButton;
    private ArrayList<TvShow> tvShows;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TvShowsAdapter adapter;
    private TextView header;

    private int pageNumber = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tv_shows_list_screen);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.statusBarColor));
        }

        tvShows = new ArrayList<>();
        adapter = new TvShowsAdapter(this, tvShows);

        findViews();

        if (AppEnv.isNetworkConnected(this)) {
            serverTvShowRequest(false, pageNumber);
            header.setVisibility(View.GONE);
        } else {
            getArrayList(MOST_POPULAR);
        }

        addListeners();

    }

    private void findViews() {
        listView = findViewById(R.id.tv_shows_list_view);
        listView.addFooterView(((LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.footer_item, null, false));
        showMoreInfoButton = listView.findViewById(R.id.footer_item_button);
        loader = findViewById(R.id.tv_shows_loader);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshListView);
        listView.addHeaderView(((LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(header_item, null, false));
        header = listView.findViewById(R.id.header);
    }

    private void addListeners() {

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(TvShowsListScreen.this, TvShowsDetailsScreen.class);
                intent.putExtra(TV_SHOW_ID, tvShows.get(position - 1).getId());
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if (tvShows != null) {
                    serverTvShowRequest(true, pageNumber);
                }

            }
        });

        showMoreInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                listView.setEnabled(false);
                listView.setAlpha(.5f);
                pageNumber++;
                serverTvShowRequest(false, pageNumber);

            }
        });

    }

    private void serverTvShowRequest(final boolean isRefresh, final int pageNumber) {

        // Show Loader
        if (!isRefresh) {
            loader.setVisibility(View.VISIBLE);
        } else {
            listView.setEnabled(false);
            listView.setAlpha(.5f);
        }

        String url = getResources().getString(R.string.server_endpoint) + "/tv/popular?api_key=b6948414a6f42be3075eee653436b668&page=" + pageNumber;

        OkHttpClient client = new OkHttpClient();
        final Request request = new Request
                .Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                } else {

                    try {
                        String responseStr = response.body().string();
                        JSONObject json = new JSONObject(responseStr);
                        JSONArray tvShowResults = json.getJSONArray("results");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isRefresh) {
                                    tvShows.clear();
                                    adapter.notifyDataSetChanged();
                                    listView.setVisibility(View.GONE);
                                    showMoreInfoButton.setVisibility(View.GONE);
                                }
                            }
                        });

                        for (int i = 0; i < tvShowResults.length(); i++) {

                            JSONObject tvShowInfo = tvShowResults.getJSONObject(i);

                            TvShow tvShow = new TvShow();
                            tvShow.setId(tvShowInfo.getInt("id"));
                            tvShow.setVoteAverage(tvShowInfo.getDouble("vote_average"));
                            tvShow.setOriginalName(tvShowInfo.getString("original_name"));
                            tvShow.setName(tvShowInfo.getString("name"));
                            tvShow.setPosterPath(tvShowInfo.getString("poster_path"));
                            tvShow.setFirstAirDate(tvShowInfo.getString("first_air_date"));
                            tvShow.setBitmap(getImage(tvShow.getPosterPath()));

                            tvShows.add(tvShow);
                        }

                        saveArrayList(tvShows, MOST_POPULAR);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loader.setVisibility(View.INVISIBLE);
                                listView.setEnabled(true);
                                listView.setAlpha(1);
                                listView.setVisibility(View.VISIBLE);
                                swipeRefreshLayout.setRefreshing(false);
                                showMoreInfoButton.setVisibility(View.VISIBLE);
                                listView.setAdapter(adapter);
                                int startPosition = isRefresh ? 0 : pageNumber;
                                listView.setSelectionFromTop((20 * (startPosition - 1)) - 4, 1);
                                adapter.notifyDataSetChanged();
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                // handlings
            }
        });

    }

    public void saveArrayList(ArrayList<TvShow> tvShows, String key) {

        SharedPreferences sharedPreferences = getSharedPreferences(AppEnv.SHARED_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(tvShows);
        editor.putString(key, json);
        editor.putString(AppEnv.SAVED_DATA, getCurrentTime());
        editor.apply();

    }

    public ArrayList<TvShow> getArrayList(String key) {

        SharedPreferences sharedPreferences = getSharedPreferences(AppEnv.SHARED_PREFERENCES, MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(key, null);
        Type type = new TypeToken<ArrayList<TvShow>>() {}.getType();

        ArrayList<TvShow> tvShows = gson.fromJson(json, type);

        adapter = new TvShowsAdapter(TvShowsListScreen.this, tvShows);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        header.setText(sharedPreferences.getString(AppEnv.SAVED_DATA, "No data saved!"));

        return tvShows;
    }

    private Bitmap getImage(String path) {

        try {

            String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w185";
            String str = IMAGE_BASE_URL + path + "?api_key=b6948414a6f42be3075eee653436b668";

            URL url = new URL(str);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            InputStream is = connection.getInputStream();

            return BitmapFactory.decodeStream(is);

        } catch (Exception e) {
            return BitmapFactory.decodeResource(getResources(), R.drawable.no_results_found_poster);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private String getCurrentTime() {

        long milliseconds = System.currentTimeMillis();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd,yyyy HH:mm");
        Date resultDate = new Date(milliseconds);
        String temp = getString(R.string.header_item_text_view_date) + " " + simpleDateFormat.format(resultDate);

        return temp;

    }

}

