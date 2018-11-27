package movies.flag.pt.moviesapp.http.entities;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class TvShowResponse {

    @SerializedName("page")
    private int page;

    @SerializedName("results")
    private List<TvShow> tvshows = new ArrayList<>();

    @SerializedName("total_pages")
    private int totalPages;

    public Integer getPage()  {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public List<TvShow> getTvshows() {
        return tvshows;
    }

    public void setTvshows(List<TvShow> tvshows) {
        this.tvshows = tvshows;
    }
}
