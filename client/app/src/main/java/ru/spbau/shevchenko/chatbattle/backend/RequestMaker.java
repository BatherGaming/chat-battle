package ru.spbau.shevchenko.chatbattle.backend;


/**
 * Created by ilya on 11/1/16.
 */

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

public class RequestMaker {
    public enum Method{
        GET, POST, PUT, DELETE
    }
    public static void sendRequest(String url, String data, Method method, RequestCallback callback) {
        switch(method){
            case POST: {
                sendPostRequest(url, data, callback);
                break;
            }
            case GET: {
                sendGetRequest(url, callback);
            }
            case PUT: {
                sendPutRequest(url, data, callback);
            }
            case DELETE: {
                sendDeleteRequest(url, data, callback);
            }
        }
    }

    private static void sendDeleteRequest(String url, String data, RequestCallback callback) {

    }

    private static void sendPutRequest(String url, String data, RequestCallback callback) {

    }

    private static void sendGetRequest(String url, final RequestCallback callback) {
        new AsyncTask<String, Integer, String>() {
            @Override
            protected String doInBackground(String... url) {
                HttpClient client = new DefaultHttpClient();
                HttpGet request = new HttpGet(url[0]);
                HttpResponse response = null;
                StringBuilder plainResponse = new StringBuilder();
                try {
                    response = client.execute(request);
                    InputStream inputStream = null;
                    inputStream = response.getEntity().getContent();
                    BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
                    String line = "";
                    while ((line = rd.readLine()) != null) {
                        plainResponse.append(line);
                    }
                }
                catch(IOException e){
                    Log.e("sendGetRequest()", e.getMessage());
                    return "{'errror': True}"; // TODO: think about that
                }
                return plainResponse.toString();
            }

            @Override
            protected void onPostExecute(String response){
                callback.run(response);
            }
        }.execute(url);
    }

    private static void sendPostRequest(String url, String data, RequestCallback callback) {
    }
}
