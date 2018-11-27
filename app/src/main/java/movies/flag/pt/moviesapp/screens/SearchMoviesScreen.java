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
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import movies.flag.pt.AppEnv;
import movies.flag.pt.moviesapp.R;
import movies.flag.pt.moviesapp.adapters.MoviesAdapter;
import movies.flag.pt.moviesapp.http.entities.Movie;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static movies.flag.pt.moviesapp.R.layout.footer_item;
import static movies.flag.pt.moviesapp.R.layout.header_item;

public class SearchMoviesScreen extends Screen {

    public static String MOVIE_ID = "movie_id";

    private EditText searchMovieInput;
    private TextView noResultsLabel;
    private TextView header;
    private ImageButton searchMovieButton;
    private ListView searchListView;
    private Button showMoreInfoButton;
    private ProgressBar loader;
    private ArrayList<Movie> movies;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MoviesAdapter adapter;

    private int pageNumber = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_movies_screen);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.statusBarColor));
        }

        movies = new ArrayList<>();
        adapter = new MoviesAdapter(this, movies);

        findViews();

        if (AppEnv.isNetworkConnected(this)) {
            header.setVisibility(View.GONE);
            searchMovieButton.setEnabled(true);
            searchMovieButton.setAlpha(1f);
            searchMovieInput.setEnabled(true);
            searchMovieInput.setAlpha(1f);
        } else {
            getArrayList(AppEnv.SEARCHED_MOVIES);
            searchMovieButton.setEnabled(false);
            searchMovieButton.setAlpha(.5f);
            searchMovieInput.setEnabled(false);
            searchMovieInput.setAlpha(.5f);
            loader.setVisibility(View.GONE);
        }

        addListeners();

    }

    private void findViews() {
        searchListView = findViewById(R.id.search_movies_list_view);
        searchListView.addFooterView(((LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(footer_item, null, false));
        showMoreInfoButton = findViewById(R.id.footer_item_button);
        searchMovieInput = findViewById(R.id.search_movies_screen_input);
        searchMovieButton = findViewById(R.id.search_movies_screen_button);
        loader = findViewById(R.id.search_movies_loader);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshListView);
        noResultsLabel = findViewById(R.id.search_movies_no_results_label);
        searchListView.addHeaderView(((LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(header_item, null, false));
        header = searchListView.findViewById(R.id.header);
    }

    private void addListeners() {

        searchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                Intent intent = new Intent(SearchMoviesScreen.this, MovieDetailsScreen.class);
                intent.putExtra(MOVIE_ID, movies.get(position).getId());
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

            }
        });

        searchMovieButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String movieNameInput = searchMovieInput.getText().toString();

                if (movieNameInput.matches("")) {
                    Toast.makeText(SearchMoviesScreen.this, "Please enter a movie name to search", Toast.LENGTH_SHORT).show();
                } else {
                    loader.setVisibility(View.VISIBLE);
                    searchMovieInput.onEditorAction(EditorInfo.IME_ACTION_DONE);
                    searchMovieRequest(true, searchMovieInput.getText().toString(), pageNumber);
                    noResultsLabel.setVisibility(View.GONE);
                    //Log.e("Movies ", String.valueOf(movies.size()));
                }

            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if (movies != null) {
                    searchMovieRequest(true, searchMovieInput.getText().toString(), pageNumber);
                }

            }
        });

        showMoreInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                searchListView.setEnabled(false);
                searchListView.setAlpha(.5f);
                pageNumber++;
                searchMovieRequest(false, searchMovieInput.getText().toString(), pageNumber);

            }
        });

    }

    private void searchMovieRequest(final boolean isSearch, String movieNameInput, final int pageNumber) {

        // Show Loader
        if (!isSearch) {
            loader.setVisibility(View.VISIBLE);
        } else {
            searchListView.setEnabled(false);
            searchListView.setAlpha(.5f);
        }

        String url = getResources().getString(R.string.server_endpoint) + "/search/movie?api_key=b6948414a6f42be3075eee653436b668&query=" + movieNameInput + "&page=" + pageNumber;

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
                                if (isSearch) {
                                    movies.clear();
                                    adapter.notifyDataSetChanged();
                                    showMoreInfoButton.setVisibility(View.GONE);
                                }
                            }
                        });

                        for (int i = 0; i < results.length(); i++) {

                            JSONObject movieInfo = results.getJSONObject(i);

                            Movie movie = new Movie();
                            movie.setVoteCount(movieInfo.getInt("vote_count"));
                            movie.setId(movieInfo.getInt("id"));
                            movie.setVoteAverage(movieInfo.getDouble("vote_average"));
                            movie.setTitle(movieInfo.getString("title"));
                            movie.setPopularity(movieInfo.getDouble("popularity"));
                            movie.setPosterPath(movieInfo.getString("poster_path"));
                            movie.setOriginalLanguage(movieInfo.getString("original_language"));
                            movie.setOriginalTitle(movieInfo.getString("original_title"));
                            movie.setBackdropPath(movieInfo.getString("backdrop_path"));
                            movie.setOverview(movieInfo.getString("overview"));
                            movie.setReleaseDate(movieInfo.getString("release_date"));
                            movie.setBitmap(getImage(movie.getPosterPath()));

                            movies.add(movie);
                        }

                        saveArrayList(movies, AppEnv.SEARCHED_MOVIES);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if (movies.size() == 0) {
                                    searchListView.setVisibility(View.GONE);
                                    noResultsLabel.setVisibility(View.VISIBLE);
                                } else {
                                    searchListView.setVisibility(View.VISIBLE);
                                }

                                loader.setVisibility(View.INVISIBLE);
                                searchListView.setEnabled(true);
                                searchListView.setAlpha(1);
                                swipeRefreshLayout.setRefreshing(false);
                                MoviesAdapter adapter = new MoviesAdapter(SearchMoviesScreen.this, movies);
                                searchListView.setAdapter(adapter);
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

        adapter = new MoviesAdapter(SearchMoviesScreen.this, movies);
        searchListView.setAdapter(adapter);
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

        long millisenconds = System.currentTimeMillis();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd,yyyy HH:mm");
        Date resultDate = new Date(millisenconds);
        String temp = getString(R.string.header_item_text_view_date) + " " + simpleDateFormat.format(resultDate);

        return temp;

    }

}