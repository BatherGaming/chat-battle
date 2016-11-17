package ru.spbau.shevchenko.chatbattle.backend;


import android.os.AsyncTask;
import android.renderscript.ScriptGroup;
import android.util.JsonWriter;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

public class RequestMaker {
    public static final String domainName = "http://qwsafex.pythonanywhere.com";

    public enum Method {
        GET, POST, PUT, DELETE
    }

    public static void sendRequest(String url, Method method, final RequestCallback callback) {
        final HttpRequestBase request;
        if (method == Method.GET){
            request = new HttpGet(url);
        }
        else if(method == Method.DELETE){
            request = new HttpDelete(url);
        }
        else { // Method.POST or Method.PUT
            sendRequest(url, method, callback, "");
            return;
        }
        new AsyncTask<String, Integer, String>() {
            @Override
            protected String doInBackground(String... url) {
                HttpClient client = new DefaultHttpClient();
                HttpResponse response = null;
                StringBuilder plainResponse = new StringBuilder();
                try {
                    response = client.execute(request);
                    InputStream inputStream = response.getEntity().getContent();
                    BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
                    String line = "";
                    while ((line = rd.readLine()) != null) {
                        plainResponse.append(line);
                    }
                }
                catch(IOException e){
                    Log.e("sendRequest()", e.getMessage());
                    return "{'error': True}"; // TODO: think about that
                }
                return plainResponse.toString();
            }

            @Override
            protected void onPostExecute(String response){
                callback.run(response);
            }
        }.execute(url);
    }

    public static void sendRequest(String url, Method method, final RequestCallback callback, final String data) {
        final HttpEntityEnclosingRequest request;
        if (method == Method.POST){
            request = new HttpPost(url);
        }
        else if(method == Method.PUT){
            request = new HttpPut(url);
        }
        else {
            sendRequest(url, method, callback);
            return;
        }
        new AsyncTask<String, Integer, String>() {
            @Override
            protected String doInBackground(String... url) {
                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost(url[0]);
                StringBuilder plainResponse = new StringBuilder();
                Log.d("sendRequest()", "Data: " + data);
                try {
                    StringEntity dataEntity = new StringEntity(data);
                    post.setHeader("Content-Type", "application/json");
                    post.setEntity(dataEntity);
                    HttpResponse response = client.execute(post);
                    InputStream inputStream = response.getEntity().getContent();
                    BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
                    String line = "";
                    while ((line = rd.readLine()) != null) {
                        plainResponse.append(line);
                    }
                } catch (IOException e) {
                    Log.e("sendRequest()", e.getMessage());
                    return "{'error': True}"; // TODO: think about that
                }
                return plainResponse.toString();
            }
            @Override
            protected void onPostExecute(String response){
                callback.run(response);
            }
        }.execute(url);
    }
}
