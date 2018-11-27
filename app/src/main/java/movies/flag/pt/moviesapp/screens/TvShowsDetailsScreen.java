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

public class TvShowsDetailsScreen extends Screen {

    private ImageView tvShowBackDrop;
    private TextView tvShowTitleLabel;
    private TextView tvShowGenresTitle;
    private TextView tvShowGenresLabel;
    private TextView tvShowOverviewTitle;
    private TextView tvShowOverviewLabel;
    private TextView tvShowRatingLabel;
    private TextView tvShowReleaseDateTitle;
    private TextView tvShowReleaseDate;
    private ImageView tvShowRatingImage;
    private TextView tvShowLanguageLabel;
    private TextView tvShowLanguageTitle;
    private ProgressBar loader;
    private ImageView shareIcon;

    private int tvShowId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tv_show_details_screen);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.statusBarColor));
        }

        tvShowId = getIntent().getIntExtra(TvShowsListScreen.TV_SHOW_ID, tvShowId);
        //Log.i("id", String.valueOf(tvShowId));

        findViews();
        getTvShowDetails();
        addListeners();

    }

    private void findViews() {
        tvShowBackDrop = findViewById(R.id.tv_show_details_backdrop);
        tvShowTitleLabel = findViewById(R.id.tv_show_details_title);
        tvShowGenresTitle = findViewById(R.id.tv_show_details_genres_label);
        tvShowGenresLabel = findViewById(R.id.tv_show_details_genres);
        tvShowOverviewTitle = findViewById(R.id.tv_show_details_overview_label);
        tvShowOverviewLabel = findViewById(R.id.tv_show_details_overview);
        tvShowRatingLabel = findViewById(R.id.tv_show_details_rating_label);
        tvShowReleaseDateTitle = findViewById(R.id.tv_show_details_release_date_label);
        tvShowReleaseDate = findViewById(R.id.tv_show_details_release_date);
        tvShowRatingImage = findViewById(R.id.tv_show_details_rating_image);
        tvShowLanguageLabel = findViewById(R.id.tv_show_details_language);
        tvShowLanguageTitle = findViewById(R.id.tv_show_details_language_label);
        loader = findViewById(R.id.tv_show_details_loader);
        shareIcon = findViewById(R.id.tv_show_details_share_icon);
    }

    private void addListeners() {

        shareIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, tvShowTitleLabel.getText().toString() + "\n" + tvShowRatingLabel.getText().toString());
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.tv_show_details_screen_share_title)));

            }
        });

    }

    private void getTvShowDetails() {

        // Show Loader
        loader.setVisibility(View.VISIBLE);

        String url = getResources().getString(R.string.server_endpoint) + "/tv/" + tvShowId + "?api_key=b6948414a6f42be3075eee653436b668";

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
                        final String title = jsonObject.getString("name");
                        final String originalLanguage = jsonObject.getString("original_language");
                        final String backDropPath = jsonObject.getString("backdrop_path");
                        final String overview = jsonObject.getString("overview");
                        final String releaseDate = jsonObject.getString("first_air_date");

                        final String finalStr = genresStr;
                        final Bitmap bitmap = getImage(backDropPath);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                loader.setVisibility(View.GONE);
                                shareIcon.setVisibility(View.VISIBLE);
                                tvShowBackDrop.setImageBitmap(bitmap);
                                tvShowTitleLabel.setText(title);
                                tvShowOverviewTitle.setVisibility(View.VISIBLE);
                                tvShowOverviewLabel.setText(overview);
                                tvShowRatingLabel.setText(String.valueOf(voteAverage));
                                tvShowReleaseDateTitle.setVisibility(View.VISIBLE);
                                tvShowReleaseDate.setText(releaseDate);
                                tvShowRatingImage.setVisibility(View.VISIBLE);
                                tvShowLanguageLabel.setText(originalLanguage);
                                tvShowLanguageTitle.setVisibility(View.VISIBLE);
                                tvShowGenresLabel.setText(finalStr);
                                tvShowGenresTitle.setVisibility(View.VISIBLE);

                            }
                        });

                        /*runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loader.setVisibility(View.INVISIBLE);
                            }
                        });*/

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