package ru.spbau.shevchenko.chatbattle.backend;


import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import ru.spbau.shevchenko.chatbattle.Player;

public class RequestMaker {
    private static final String DOMAIN_NAME = "http://qwsafex.pythonanywhere.com";

    private enum Method {
        GET, POST, PUT, DELETE
    }

    private static void sendRequest(String url, Method method, final RequestCallback callback) {
        //Log.d("sendRequest", url);
        final HttpRequestBase request;
        if (method == Method.GET) {
            request = new HttpGet(url);
        } else if (method == Method.DELETE) {
            request = new HttpDelete(url);
        } else { // Method.POST or Method.PUT
            sendRequest(url, method, callback, "");
            return;
        }
        new AsyncTask<String, Integer, String>() {
            @Override
            protected String doInBackground(String... url) {
                HttpClient client = new DefaultHttpClient();
                HttpResponse response;
                StringBuilder plainResponse = new StringBuilder();
                try {
                    response = client.execute(request);
                    InputStream inputStream = response.getEntity().getContent();
                    BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
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
            protected void onPostExecute(String response) {
                callback.run(response);
            }
        }.execute();
    }

    private static void sendRequest(final String url, Method method, final RequestCallback callback, final String data) {
        //Log.d("sendRequest", url);
        final HttpEntityEnclosingRequestBase request;
        if (method == Method.POST) {
            request = new HttpPost(url);
        } else if (method == Method.PUT) {
            request = new HttpPut(url);
        } else {
            sendRequest(url, method, callback);
            return;
        }
        new AsyncTask<String, Integer, String>() {
            @Override
            protected String doInBackground(String... urls) {
                HttpClient client = new DefaultHttpClient();
                StringBuilder plainResponse = new StringBuilder();
                Log.d("sendRequest()", "Data: " + data);
                try {
                    StringEntity dataEntity = new StringEntity(data);
                    request.setHeader("Content-Type", "application/json");
                    request.setEntity(dataEntity);
                    HttpResponse response = client.execute(request);
                    InputStream inputStream = response.getEntity().getContent();
                    BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
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
            protected void onPostExecute(String response) {
                callback.run(response);
            }
        }.execute();
    }

    static public void findBattle(Player.Role role, int id) {
        sendRequest(RequestMaker.DOMAIN_NAME + "/battlemaker/" + role.toString().toLowerCase() + "/" + Integer.toString(id), Method.POST, RequestCallback.DO_NOTHING);
    }

    static public void checkIfFound(int id, RequestCallback callback) {
        sendRequest(RequestMaker.DOMAIN_NAME + "/players/" + Integer.toString(id), Method.GET, callback);
    }

    static public void pullMessages(int chatId, int messageCount, RequestCallback callback) {
        sendRequest(RequestMaker.DOMAIN_NAME + "/chat/get/" + chatId + "/" + messageCount,
                Method.GET,
                callback);
    }

    static public void sendMessage(String messageData) {
        sendRequest(RequestMaker.DOMAIN_NAME + "/chat/send", Method.POST, RequestCallback.DO_NOTHING, messageData);
    }

    static public void getPlayersIds(int chatId, RequestCallback callback) {
        sendRequest(RequestMaker.DOMAIN_NAME + "/profile_manager/players/" + chatId, Method.GET, callback);
    }

    static public void chooseWinner(int chosen) {
        sendRequest(RequestMaker.DOMAIN_NAME + "/chat/close/" + ProfileManager.getPlayer().getId() + "/" + chosen, Method.POST, RequestCallback.DO_NOTHING);
    }

    static public void signIn(String password, String login, RequestCallback callback) {
        sendRequest(RequestMaker.DOMAIN_NAME + "/sign_in/" + login + "/" + password, Method.GET, callback);
    }

    static public void singUp(String player_data, RequestCallback callback) {
        sendRequest(RequestMaker.DOMAIN_NAME + "/players", Method.POST, callback, player_data);
    }

    static public void chatStatus(int id, int chatId, RequestCallback callback) {
        sendRequest(RequestMaker.DOMAIN_NAME + "/chat/chat_status/" + id + "/" + chatId,
                RequestMaker.Method.GET, callback);
    }
    public static void getWhiteboard(String whiteboardTag, RequestCallback callback){
        sendRequest(RequestMaker.DOMAIN_NAME + "/whiteboards/" + whiteboardTag, Method.GET, callback);
    }

    static public void deleteFromQueue(int id) {
        sendRequest(RequestMaker.DOMAIN_NAME + "/battlemaker/" + id, Method.DELETE, RequestCallback.DO_NOTHING);
    }

    static public void accept(int id) {
        sendRequest(RequestMaker.DOMAIN_NAME + "/chat/accept/" + id, Method.POST, RequestCallback.DO_NOTHING);
    }

    static public void decline(int id) {
        sendRequest(RequestMaker.DOMAIN_NAME + "/chat/decline/" + id, Method.POST, RequestCallback.DO_NOTHING);
    }
}
