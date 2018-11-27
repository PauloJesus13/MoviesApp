package movies.flag.pt.moviesapp.helpers;

import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import movies.flag.pt.moviesapp.utils.DLog;

/**
 * Created by Ricardo Neves on 06/06/2017.
 */

public final class RequestsHelper {

    private static final String TAG = RequestsHelper.class.getSimpleName();

    private static final String UTF_8 = "UTF-8";
    private static final Gson GSON = new Gson();

    /**
     * This method is responsible to execute the request and return a String response
     * (JSON returned by the server)
     * */
    public static String executeRequest(URL url) {
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            return convertStreamToString(in);
        }
        catch(IOException ex){
            DLog.e(TAG, "executeRequest error: " + ex);
        }
        finally {
            if(urlConnection != null){
                urlConnection.disconnect();
            }
        }

        return null;
    }

    public static String convertStreamToString(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line + "\n");
        }
        is.close();
        return sb.toString();
    }

    public static String encodeUTF8(String value) {

        if (value == null) {
            return null;
        }

        try {
            String tempEncode = URLEncoder.encode(value, UTF_8);
            return tempEncode;
        } catch (UnsupportedEncodingException e) {
            DLog.e(TAG, "encodeUTF8() error encoding to UTF-8: " + e.getMessage());
            return "";
        }
    }

    public static StringBuilder addQueryParam(StringBuilder sb, String key, String value) {
        return sb.append(sb.indexOf("?") >= 0 ? "&" : "?").append(key)
                .append("=").append(encodeUTF8(value));
    }

    public static <RequestEntity>  RequestEntity fromJson(String json, Class<RequestEntity> klass){
        return GSON.fromJson(json, klass);
    }

}