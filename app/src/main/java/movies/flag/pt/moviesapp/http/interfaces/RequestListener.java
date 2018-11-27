package movies.flag.pt.moviesapp.http.interfaces;

public interface RequestListener<R> {

    /**
     * Called when server response finish with success
     */
    void onResponseSuccess(R responseEntity);

    /**
     * Called when there is an error calling server (possible internet connection error)
     */
    void onNetworkError();

}