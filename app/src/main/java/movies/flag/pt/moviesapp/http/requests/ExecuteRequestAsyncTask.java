package movies.flag.pt.moviesapp.http.requests;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import movies.flag.pt.moviesapp.R;
import movies.flag.pt.moviesapp.helpers.RequestsHelper;
import movies.flag.pt.moviesapp.http.interfaces.RequestListener;
import movies.flag.pt.moviesapp.utils.DLog;
import movies.flag.pt.moviesapp.utils.ResponseErrors;
import movies.flag.pt.moviesapp.utils.ServerResponse;

/**
 * Created by Ricardo Neves on 19/10/2016.
 */

@SuppressLint("StaticFieldLeak")
public abstract class ExecuteRequestAsyncTask<ResponseEntity> extends AsyncTask<Void, Void, ServerResponse<ResponseEntity>> {

    private static final String API_KEY = "api_key";

    private static final String TAG = ExecuteRequestAsyncTask.class.getSimpleName();

    private Context context;
    private RequestListener<ResponseEntity> requestListener;

    public ExecuteRequestAsyncTask(Context context, RequestListener<ResponseEntity> requestListener){
        this.context = context;
        this.requestListener = requestListener;
    }

    protected abstract String getPath();

    /**
     * Implementations must override this method and call
     * addQueryParam for each query arg
     * */
    protected abstract void addQueryParams(StringBuilder sb);

    /**
     * In Java we cannot get the class type from a generic type,
     * so the implementations must provide it
     * */
    protected abstract Class<ResponseEntity> getResponseEntityClass();


    @Override
    protected final void onPostExecute(ServerResponse<ResponseEntity> serverResponse) {
        super.onPostExecute(serverResponse);
        switch(serverResponse.getErrorType()){
            case ResponseErrors.NO_ERROR:
                requestListener.onResponseSuccess(serverResponse.getResponseEntity());
                break;
            case ResponseErrors.PARSE_ERROR:
                throw new RuntimeException("Parse error! Verify your response entity!");
            case ResponseErrors.NETWORK_ERROR:
                requestListener.onNetworkError();
                break;
            case ResponseErrors.URL_MALFORMED:
                throw new RuntimeException("Url called is malformed! Verify it!");
        }
    }

    /**
     * Documentation: https://developer.android.com/reference/java/net/HttpURLConnection.html
     * */
    @Override
    protected ServerResponse<ResponseEntity> doInBackground(Void... voids) {
        URL url;

        try {
            String urlString = buildUrl();
            DLog.d(TAG, "execute request for url: " + urlString);

            url = new URL(urlString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return new ServerResponse<>(null, ResponseErrors.URL_MALFORMED);
        }

        try {
            String response = RequestsHelper.executeRequest(url);

            DLog.d(TAG, "Response: " + response);

            if(response == null){
                return new ServerResponse<>(null, ResponseErrors.NETWORK_ERROR);
            }

            ResponseEntity responseEntity = RequestsHelper.fromJson(response, getResponseEntityClass());

            return new ServerResponse<>(responseEntity, ResponseErrors.NO_ERROR);
        }
        catch(Exception e){
            DLog.e(TAG, "doInBackground() exception: " + e);
            return new ServerResponse<>(null, ResponseErrors.PARSE_ERROR);
        }

    }

    private String buildUrl(){
        String apiKey = context.getString(R.string.server_api_key);
        StringBuilder sb = new StringBuilder(context.getString(R.string.server_endpoint));
        sb.append(getPath());
        addQueryParam(sb, API_KEY, apiKey);
        addQueryParams(sb);
        return sb.toString();
    }

    protected StringBuilder addQueryParam(StringBuilder sb, String key, String value) {
        return RequestsHelper.addQueryParam(sb, key, value);
    }

}