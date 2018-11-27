package movies.flag.pt;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;

public class AppEnv extends Application {

    public static final String SHARED_PREFERENCES = "sharedPreferences";
    //public static final String DATE_LIST_VIEW_ADDED = "dateListViewAdded";
    public static final String SAVED_DATA = "savedData";
    public static final String SEARCHED_MOVIES = "searched_movies";
    public static final String NOW_PLAYING = "nowPlaying";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return connectivityManager.getActiveNetworkInfo() != null;
    }

}
