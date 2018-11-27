package movies.flag.pt.moviesapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import movies.flag.pt.AppEnv;
import movies.flag.pt.moviesapp.R;
import movies.flag.pt.moviesapp.http.entities.Movie;

public class MoviesAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Movie> movies;

    public MoviesAdapter(Context context, ArrayList<Movie> movies) {
        this.context = context;
        this.movies = movies;
    }

    @Override
    public int getCount() {
        return movies.size();
    }

    @Override
    public Object getItem(int position) {
        return movies.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Movie movie = (Movie) getItem(position);
        ViewHolder viewHolder;
        View v;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            v = inflater.inflate(R.layout.movie_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.movieImage = v.findViewById(R.id.movie_item_poster_image);
            viewHolder.movieName = v.findViewById(R.id.movie_item_title_label);
            viewHolder.movieYear = v.findViewById(R.id.movie_item_date_label);
            viewHolder.movieRate = v.findViewById(R.id.movie_item_rate_label);

            v.setTag(viewHolder);
        } else {
            v = convertView;
            viewHolder = (ViewHolder) v.getTag();
        }

        if (AppEnv.isNetworkConnected(parent.getContext())) {

            viewHolder.movieImage.setImageBitmap(movie.getBitmap());

        }

        viewHolder.movieName.setText(movie.getTitle());
        viewHolder.movieYear.setText(movie.getReleaseDate());
        viewHolder.movieRate.setText(String.valueOf(movie.getVoteAverage()));

        return v;
    }

    private class ViewHolder {
        ImageView movieImage;
        TextView movieName;
        TextView movieYear;
        TextView movieRate;
    }

}