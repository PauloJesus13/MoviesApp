package movies.flag.pt.moviesapp.screens;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import movies.flag.pt.moviesapp.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MovieDetailsScreen extends Screen {

    private ImageView movieBackdrop;
    private TextView movieTitleLabel;
    private TextView movieGenresTitle;
    private TextView movieGenresLabel;
    private TextView movieOverviewTitle;
    private TextView movieOverviewLabel;
    private TextView movieRatingLabel;
    private TextView movieReleaseDateTitle;
    private TextView movieReleaseDate;
    private ImageView movieRatingImage;
    private TextView movieLanguageLabel;
    private TextView movieLanguageTitle;
    private ProgressBar loader;
    private ImageView shareIcon;

    private int movieId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_details_screen);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.statusBarColor));
        }

        movieId = getIntent().getIntExtra(MoviesListScreen.MOVIE_ID, movieId);
        //Log.i("id", String.valueOf(movieId));

        findViews();
        getMovieDetails();
        addListeners();

    }

    private void findViews() {
        movieBackdrop = findViewById(R.id.movie_details_backdrop);
        movieTitleLabel = findViewById(R.id.movie_details_title);
        movieGenresTitle = findViewById(R.id.movie_details_genres_label);
        movieGenresLabel = findViewById(R.id.movie_details_genres);
        movieOverviewTitle = findViewById(R.id.movie_details_overview_label);
        movieOverviewLabel = findViewById(R.id.movie_details_overview);
        movieRatingLabel = findViewById(R.id.movie_details_rating_label);
        movieReleaseDateTitle = findViewById(R.id.movie_details_release_date_label);
        movieReleaseDate = findViewById(R.id.movie_details_release_date);
        movieRatingImage = findViewById(R.id.movie_details_rating_image);
        movieLanguageLabel = findViewById(R.id.movie_details_language);
        movieLanguageTitle = findViewById(R.id.movie_details_language_label);
        loader = findViewById(R.id.movies_details_loader);
        shareIcon = findViewById(R.id.movie_details_share_icon);
    }

    private void addListeners() {

        shareIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, movieTitleLabel.getText().toString() + "\n" + movieRatingLabel.getText().toString());
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.movie_details_screen_share_title)));

            }
        });

    }

    private void getMovieDetails() {

        // Show Loader
        loader.setVisibility(View.VISIBLE);

        String url = getResources().getString(R.string.server_endpoint) + "/movie/" + movieId + "?api_key=b6948414a6f42be3075eee653436b668";

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
                        Log.e("Response ", responseStr);

                        JSONObject jsonObject = new JSONObject(responseStr);

                        final JSONArray genres = jsonObject.getJSONArray("genres");

                        String genresStr = "";

                        for (int i = 0; i < genres.length(); i++) {

                            genresStr = genresStr + String.valueOf(genres.getJSONObject(i).getString("name"));

                            if (i + 1 != genres.length()) {
                                genresStr = genresStr + " / ";
                            }

                        }

                        final Double voteAverage = jsonObject.getDouble("vote_average");
                        final String title = jsonObject.getString("title");
                        final String originalLanguage = jsonObject.getString("original_language");
                        final String backDropPath = jsonObject.getString("backdrop_path");
                        final String overview = jsonObject.getString("overview");
                        final String releaseDate = jsonObject.getString("release_date");

                        final String finalStr = genresStr;
                        final Bitmap bitmap = getImage(backDropPath);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                loader.setVisibility(View.GONE);
                                shareIcon.setVisibility(View.VISIBLE);
                                movieBackdrop.setImageBitmap(bitmap);
                                movieTitleLabel.setText(title);
                                movieOverviewTitle.setVisibility(View.VISIBLE);
                                movieOverviewLabel.setText(overview);
                                movieRatingLabel.setText(String.valueOf(voteAverage));
                                movieReleaseDateTitle.setVisibility(View.VISIBLE);
                                movieReleaseDate.setText(releaseDate);
                                movieRatingImage.setVisibility(View.VISIBLE);
                                movieLanguageLabel.setText(originalLanguage);
                                movieLanguageTitle.setVisibility(View.VISIBLE);
                                movieGenresLabel.setText(finalStr);
                                movieGenresTitle.setVisibility(View.VISIBLE);

                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

            }
        });

    }

    private Bitmap getImage(String path) {
        try {
            String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/original";
            String str = IMAGE_BASE_URL + path + "?api_key=" + getString(R.string.server_api_key);
            Log.e("Result ", str);
            URL url = new URL(str);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            InputStream is = connection.getInputStream();

            return BitmapFactory.decodeStream(is);

        } catch (Exception e) {
            e.printStackTrace();
            return BitmapFactory.decodeResource(getResources(), R.drawable.no_results_found);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}