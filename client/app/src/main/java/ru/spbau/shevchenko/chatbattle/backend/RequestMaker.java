package ru.spbau.shevchenko.chatbattle.backend;


import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;

import ru.spbau.shevchenko.chatbattle.Player;

public class RequestMaker {
    private static final String DOMAIN_NAME = "http://qwsafex.pythonanywhere.com";
    private static final int CONNECTION_TIMEOUT = 5000; // milliseconds
    private static final int SOCKET_TIMEOUT = 5000; // milliseconds

    private enum Method {
        GET, POST, PUT, DELETE
    }


    @SuppressWarnings("WeakerAccess")
    public static void pullMessages(int chatId, int messageCount, RequestCallback callback) {
        sendRequest(RequestMaker.DOMAIN_NAME + "/chat/get/" + chatId + "/" + messageCount,
                Method.GET,
                callback);
    }

    @SuppressWarnings("WeakerAccess")
    public static void sendMessage(String messageData, RequestCallback callback) { // TODO: improve internet trouble handling
        sendRequest(RequestMaker.DOMAIN_NAME + "/chat/send", Method.POST, callback, 5000, messageData);
    }

    public static void getChats(RequestCallback callback) {
        sendRequest(RequestMaker.DOMAIN_NAME + "/chat/list", Method.GET, callback);
    }

    public static void getSummary(int chatId, RequestCallback callback) {
        sendRequest(RequestMaker.DOMAIN_NAME + "/chat/summary/" + chatId, Method.GET, callback);
    }

    public static void chooseWinner(int chosen, RequestCallback choseCallback) { // ++++
        sendRequest(RequestMaker.DOMAIN_NAME + "/chat/close/" + ProfileManager.getPlayer().getId() + "/" + chosen, Method.POST, choseCallback);
    }

    public static void chatStatus(int id, int chatId, RequestCallback callback) {
        sendRequest(RequestMaker.DOMAIN_NAME + "/chat/chat_status/" + id + "/" + chatId,
                RequestMaker.Method.GET, callback);
    }

    @SuppressWarnings("WeakerAccess")
    public static void getWhiteboard(String whiteboardTag, RequestCallback callback) {
        sendRequest(RequestMaker.DOMAIN_NAME + "/chat/get_whiteboard/" + whiteboardTag, Method.GET, callback);
    }

    public static void getTimeLeft(int chatId, RequestCallback callback) {
        sendRequest(RequestMaker.DOMAIN_NAME + "/chat/time_left/" + chatId, Method.GET, callback);
    }

    public static void kick(int playerId, int chatId, RequestCallback callback) {
        sendRequest(RequestMaker.DOMAIN_NAME + "/chat/kick/" + chatId + "/" + playerId, Method.POST, callback);
    }

    public static void accept(int id) {
        sendRequest(RequestMaker.DOMAIN_NAME + "/chat/accept/" + id, Method.POST, RequestCallback.DO_NOTHING);
    }

    public static void decline(int id) {
        sendRequest(RequestMaker.DOMAIN_NAME + "/chat/decline/" + id, Method.POST, RequestCallback.DO_NOTHING);
    }

    public static void mute(int playerId, int chatId, int muteTime) {
        sendRequest(RequestMaker.DOMAIN_NAME + "/chat/mute/" + chatId + "/" + playerId + "/" + muteTime,
                Method.POST, RequestCallback.DO_NOTHING);
    }

    @SuppressWarnings("WeakerAccess")
    public static void findBattle(Player.Role role, int id) {
        Log.d("FindBattle", "TRUE");
        // TODO: handle internet troubles
        sendRequest(RequestMaker.DOMAIN_NAME + "/battlemaker/" + role.toString().toLowerCase() + "/" + Integer.toString(id), Method.POST, RequestCallback.DO_NOTHING, 50000);
    }

    public static void deleteFromQueue(int id) {
        sendRequest(RequestMaker.DOMAIN_NAME + "/battlemaker/" + id, Method.DELETE, RequestCallback.DO_NOTHING);
    }

    @SuppressWarnings("WeakerAccess")
    public static void checkIfFound(int id, RequestCallback callback) {
        sendRequest(RequestMaker.DOMAIN_NAME + "/players/" + Integer.toString(id), Method.GET, callback);
    }

    @SuppressWarnings("WeakerAccess")
    public static void getLeaderboard(RequestCallback callback) {
        sendRequest(RequestMaker.DOMAIN_NAME + "/players/leaderboard", Method.GET, callback);
    }

    @SuppressWarnings("WeakerAccess")
    public static void signUp(String player_data, RequestCallback callback) { // ++++
        sendRequest(RequestMaker.DOMAIN_NAME + "/players", Method.POST, callback, 10000, player_data);
    }

    public static void changePassword(int id, String oldPassword, String newPassword, RequestCallback callback) {
        sendRequest(RequestMaker.DOMAIN_NAME + "/players/change_pass/" + id + "/" + oldPassword + "/" + newPassword,
                Method.POST, callback, 5000);
    }

    @SuppressWarnings("WeakerAccess")
    public static void getPlayersIds(int chatId, RequestCallback callback) {
        sendRequest(RequestMaker.DOMAIN_NAME + "/profile_manager/players/" + chatId, Method.GET, callback);
    }

    public static void getRatings(String idsString, RequestCallback callback) {
        sendRequest(RequestMaker.DOMAIN_NAME + "/profile_manager/ratings/" + idsString, Method.GET, callback);
    }

    @SuppressWarnings("WeakerAccess")
    public static void signIn(String password, String login, RequestCallback callback) { // ++++
        sendRequest(RequestMaker.DOMAIN_NAME + "/profile_manager/sign_in/" + login + "/" + password, Method.GET, callback, 10000);
    }

    public static void reset_password(String login, RequestCallback callback) {
        sendRequest(RequestMaker.DOMAIN_NAME + "/profile_manager/reset_password/" + login, Method.POST, callback);
    }

    private static void sendRequest(final String url, Method method, final RequestCallback callback, int timeout) {
        sendRequest(url, method, callback, timeout, "");
    }

    private static void sendRequest(String url, Method method, final RequestCallback callback) {
        sendRequest(url, method, callback, 1);
    }

    private static void sendRequest(final String url, final Method method, final RequestCallback callback,
                                    final long timeout_, final String data) {
        final long timeout = (timeout_ == 0 ? Long.MAX_VALUE : timeout_);
        final long startTime = System.currentTimeMillis();
        final HttpRequestBase request;
        switch (method) {
            case GET: {
                request = new HttpGet(url);
                break;
            }
            case DELETE: {
                request = new HttpDelete(url);
                break;
            }
            case PUT: {
                request = new HttpPut(url);
                break;
            }
            case POST: {
                request = new HttpPost(url);
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown http method.");
        }
        new AsyncTask<String, Integer, RequestResult>() {
            @SuppressWarnings("deprecation")
            @Override
            protected RequestResult doInBackground(String... urls) {
                BasicHttpParams httpParameters = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParameters, CONNECTION_TIMEOUT);
                HttpConnectionParams.setSoTimeout(httpParameters, SOCKET_TIMEOUT);

                HttpClient client = new DefaultHttpClient(httpParameters);
                HttpResponse response;
                StringBuilder plainResponse = new StringBuilder();
                StringEntity dataEntity = null;
                if (method == Method.PUT || method == Method.POST) {
                    try {
                        dataEntity = new StringEntity(data, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    request.setHeader("Content-Type", "application/json");
                    ((HttpEntityEnclosingRequest) request).setEntity(dataEntity);
                }
                while (true) {
                    try {
                        response = client.execute(request);
                        break;
                    } catch (IOException e) {
                        if (System.currentTimeMillis() - startTime > timeout) {
                            return new RequestResult(data, RequestResult.Status.FAILED_CONNECTION);
                        }
                    }
                }
                try {
                    InputStream inputStream = response.getEntity().getContent();
                    BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    while ((line = rd.readLine()) != null) {
                        plainResponse.append(line);
                    }
                } catch (IOException e) {
                    Log.e("sendRequest()", e.getMessage());
                    return new RequestResult("", RequestResult.Status.ERROR);
                }

                return new RequestResult(plainResponse.toString());
            }

            @Override
            protected void onPostExecute(RequestResult result) {
                callback.run(result);
            }
        }.execute();
    }
}
