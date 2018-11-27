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
import movies.flag.pt.moviesapp.http.entities.TvShow;

public class TvShowsAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<TvShow> tvShows;

    public TvShowsAdapter(Context context, ArrayList<TvShow> tvShows) {
        this.context = context;
        this.tvShows = tvShows;
    }

    @Override
    public int getCount() {
        return tvShows.size();
    }

    @Override
    public Object getItem(int position) {
        return tvShows.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        TvShow tvShow = (TvShow) getItem(position);
        ViewHolder viewHolder;
        View v;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            v = inflater.inflate(R.layout.tv_show_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.tvShowImage = v.findViewById(R.id.tv_show_item_poster_image);
            viewHolder.tvShowName = v.findViewById(R.id.tv_show_item_title_label);
            viewHolder.tvShowYear = v.findViewById(R.id.tv_show_item_date_label);
            viewHolder.tvShowRate = v.findViewById(R.id.tv_show_item_rate_label);

            v.setTag(viewHolder);
        } else {
            v = convertView;
            viewHolder = (ViewHolder) v.getTag();
        }

        if (AppEnv.isNetworkConnected(parent.getContext())) {

            viewHolder.tvShowImage.setImageBitmap(tvShow.getBitmap());

        }
        viewHolder.tvShowName.setText(tvShow.
                getOriginalName());
        viewHolder.tvShowYear.setText(tvShow.getFirstAirDate());
        viewHolder.tvShowRate.setText(String.valueOf(tvShow.getVoteAverage()));

        return v;
    }

    private class ViewHolder {
        ImageView tvShowImage;
        TextView tvShowName;
        TextView tvShowYear;
        TextView tvShowRate;
    }

}