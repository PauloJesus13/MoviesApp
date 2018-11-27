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
import android.util.Log;
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
import java.util.Locale;

import movies.flag.pt.AppEnv;
import movies.flag.pt.moviesapp.R;
import movies.flag.pt.moviesapp.adapters.MoviesAdapter;
import movies.flag.pt.moviesapp.http.entities.Movie;
import movies.flag.pt.moviesapp.http.entities.MoviesResponse;
import movies.flag.pt.moviesapp.http.interfaces.RequestListener;
import movies.flag.pt.moviesapp.http.requests.GetNowPlayingMoviesAsyncTask;
import movies.flag.pt.moviesapp.utils.DLog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static movies.flag.pt.moviesapp.R.layout.footer_item;
import static movies.flag.pt.moviesapp.R.layout.header_item;

public class MoviesListScreen extends Screen {

    public static String MOVIE_ID = "movie_id";

    private ListView listView;
    private ProgressBar loader;
    private Button showMoreInfoButton;
    private ArrayList<Movie> movies;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MoviesAdapter adapter;
    private TextView header;

    //private String date;
    private int pageNumber = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movies_list_screen);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.statusBarColor));
        }

        movies = new ArrayList<>();
        adapter = new MoviesAdapter(this, movies);

        findViews();

        if (AppEnv.isNetworkConnected(this)) {
            serverMovieRequest(false, pageNumber);
            header.setVisibility(View.GONE);
        } else {
            getArrayList(AppEnv.NOW_PLAYING);
        }

        addListeners();

    }

    private void executeRequestExample() {
        // Example to request get now playing movies
        new GetNowPlayingMoviesAsyncTask(this, new RequestListener<MoviesResponse>() {
            @Override
            public void onResponseSuccess(MoviesResponse responseEntity) {
                DLog.d(tag, "onResponseSuccess");
            }

            @Override
            public void onNetworkError() {
                DLog.d(tag, "onNetworkError");
            }
        }).execute();
    }

    private void findViews() {
        listView = findViewById(R.id.movies_list_view);
        listView.addFooterView(((LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(footer_item, null, false));
        showMoreInfoButton = listView.findViewById(R.id.footer_item_button);
        loader = findViewById(R.id.movies_loader);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshListView);
        listView.addHeaderView(((LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(header_item, null, false));
        header = listView.findViewById(R.id.header);
    }

    private void addListeners() {

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(MoviesListScreen.this, MovieDetailsScreen.class);
                intent.putExtra(MOVIE_ID, movies.get(position - 1).getId());
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if (movies != null) {
                    serverMovieRequest(true, pageNumber);
                }

            }
        });

        showMoreInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                listView.setEnabled(false);
                listView.setAlpha(.5f);
                pageNumber++;
                serverMovieRequest(false, pageNumber);

            }
        });

    }

    private void serverMovieRequest(final boolean isRefresh, final int pageNumber) {

        // Show Loader
        if (!isRefresh) {
            loader.setVisibility(View.VISIBLE);
        } else {
            listView.setEnabled(false);
            listView.setAlpha(.5f);
        }
        //Log.e("Language ", Locale.getDefault().getDisplayLanguage());

        String url = getResources().getString(R.string.server_endpoint) + "/movie/now_playing?api_key=b6948414a6f42be3075eee653436b668&page=" + pageNumber;

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
                        JSONArray results = json.getJSONArray("results");
                        //Log.e("Response: ", responseStr);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isRefresh) {
                                    movies.clear();
                                    adapter.notifyDataSetChanged();
                                    showMoreInfoButton.setVisibility(View.GONE);
                                }
                            }
                        });

                        for (int i = 0; i < results.length(); i++) {

                            JSONObject movieInfo = results.getJSONObject(i);

                            Movie movie = new Movie();
                            movie.setId(movieInfo.getInt("id"));
                            movie.setVoteAverage(movieInfo.getDouble("vote_average"));
                            movie.setTitle(movieInfo.getString("title"));
                            movie.setPosterPath(movieInfo.getString("poster_path"));
                            movie.setReleaseDate(movieInfo.getString("release_date"));
                            movie.setBitmap(getImage(movie.getPosterPath()));

                            movies.add(movie);
                        }

                        saveArrayList(movies, AppEnv.NOW_PLAYING);

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

    public void saveArrayList(ArrayList<Movie> movies, String key) {

        SharedPreferences sharedPreferences = getSharedPreferences(AppEnv.SHARED_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(movies);
        editor.putString(key, json);
        editor.putString(AppEnv.SAVED_DATA, getCurrentTime());
        editor.apply();

    }

    public ArrayList<Movie> getArrayList(String key) {

        SharedPreferences sharedPreferences = getSharedPreferences(AppEnv.SHARED_PREFERENCES, MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(key, null);
        Type type = new TypeToken<ArrayList<Movie>>() {
        }.getType();

        ArrayList<Movie> movies = gson.fromJson(json, type);

        adapter = new MoviesAdapter(MoviesListScreen.this, movies);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        header.setText(sharedPreferences.getString(AppEnv.SAVED_DATA, "No data saved!"));

        return movies;

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