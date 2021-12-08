package com.example.researchproject_javasecurity;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import org.chromium.base.Callback;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.wear.phone.interactions.authentication.CodeChallenge;
import androidx.wear.phone.interactions.authentication.CodeVerifier;
import androidx.wear.phone.interactions.authentication.OAuthRequest;
import androidx.wear.phone.interactions.authentication.OAuthResponse;
import androidx.wear.phone.interactions.authentication.RemoteAuthClient;

public class TokenAccessor {

    public static final String TAG = "Authenticate";
    public OAuthListener listener;
    public RemoteAuthClient mClient;
    private static final String CLIENT_ID = "20049550827-ogn72uthppn43f3mmdfc7j1p7e8nfon8.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "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";
    private String redirectUri;
    private String token;

    public TokenAccessor(OAuthListener listener) {
        this.listener = listener;
        mClient = RemoteAuthClient.create(listener.getActivity());
    }

    public void getAccessToken() {
        SharedPreferences sp = listener.getActivity().getPreferences(Context.MODE_PRIVATE);
        token = sp.getString("accessToken", null);
        if(token != null) {
            listener.receiveApiToken(token);
        }
        else {
            startOAuthFlow();
        }
    }

    public String getRefreshToken() {
        SharedPreferences sp = listener.getActivity().getPreferences(Context.MODE_PRIVATE);
        token = sp.getString("refreshToken", null);
        if(token != null) {
            return token;
        }
        else {
            startOAuthFlow();
        }
        return null;
    }

    public void setAccessToken(String token) {
        SharedPreferences sp = listener.getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("accessToken", token);
        editor.commit();
    }

    public void setRefreshToken(String token) {
        SharedPreferences sp = listener.getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("refreshToken", token);
        editor.commit();
    }

    protected void startOAuthFlow() {
        CodeVerifier code = new CodeVerifier();
        Uri uri = new Uri.Builder().encodedPath("https://accounts.google.com/o/auth2/v2/auth").appendQueryParameter("scope", "https://www.googleapis.com/oauth2/v4/token")
                .build();
        OAuthRequest request = new OAuthRequest.Builder(listener.getActivity().getApplicationContext()).setAuthProviderUrl(uri)
                .setCodeChallenge(new CodeChallenge(code)).setClientId(CLIENT_ID).build();

        mClient.sendAuthorizationRequest(request, Executors.newSingleThreadExecutor(), new MyAuthCallback());

    }

    /**
     * Helper method to update display with fetched results on the activity view.
     *
     * @param text Returned text to display
     */
    private void updateStatus(final String text) {
        listener.getActivity().runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        listener.updateStatus(text);
                    }
                });
    }

    private HttpURLConnection createHttpPostObject() {
        HttpURLConnection conn = null;
        try {
            URL url = new URL("https://accounts.google.com/o/oauth2/token");
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getClass().toString() + "occurred. Stack trace:\n\n"
                    + Log.getStackTraceString(e));
        }
        return conn;
    }

    private String[] acquireTokens(HttpURLConnection conn, String urlParameters) {
        String[] tokens = new String[2];
        try {
            // Send post request
            conn.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            // Retrieve post response
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            Log.d(TAG, response.toString());

            updateStatus(
                    "Google OAuth 2.0 API token exchange occurred. Response: "
                            + response.toString());
            try {
                JSONObject jsonResponse = new JSONObject(response.toString());
                tokens[0] = jsonResponse.getString("access_token");
                tokens[1] = jsonResponse.getString("refresh_token");
                if (TextUtils.isEmpty(tokens[0]) && TextUtils.isEmpty(tokens[1])) {
                    updateStatus(
                            "Google OAuth 2.0 API token exchange failed. No access token in response.");
                }
            } catch (JSONException e) {
                Log.e(TAG, "Bad JSON returned:\n\n" + Log.getStackTraceString(e));
            }
        } catch (IOException e) {
            Log.e(TAG, "Exception occurred:\n\n" + Log.getStackTraceString(e));
        }
        return tokens;
    }

    public void refreshToken() {
        Runnable runnable =
                new Runnable() {
                    public void run() {
                        String refreshToken = getRefreshToken();
                        if(refreshToken == null) {
                            updateStatus("Google OAuth 2.0 API refresh token not found. Initiating OAuth2.0 ...");
                            return;
                        }
                        HttpURLConnection httpPost = createHttpPostObject();
                        String urlParams =
                                "&client_id=" + CLIENT_ID +
                                        "&client_secret=" + CLIENT_SECRET +
                                        "&refresh_token=" + refreshToken +
                                        "&grant_type=" + "refresh_token";
                        String[] tokens = acquireTokens(httpPost, urlParams);
                        setAccessToken(tokens[0]);
                        listener.receiveApiToken(tokens[0]);
                    }
                };
        ExecutorService executor = Executors.newCachedThreadPool();
        executor.submit(runnable);
    }

    private class MyAuthCallback extends RemoteAuthClient.Callback
    {
        public void onAuthorizationResponse (OAuthRequest oAuthRequest, OAuthResponse oAuthResponse){
        Log.d(TAG, "onResult(). requestUrl:" + oAuthRequest + " responseUrl: " + oAuthResponse);
        updateStatus("Request completed. Response URL: " + oAuthResponse);

        /**
         * ASynchronous task to submit the POST request containing the Authorization code
         * to get the API token.
         */
        Runnable runnable =
                new Runnable() {

                    public void run() {
                        Uri response = oAuthResponse.getResponseUrl();
                        String code = response.getQueryParameter("code");
                        if (TextUtils.isEmpty(code)) {
                            updateStatus("Google OAuth 2.0 API token exchange failed. No code query parameter in response URL");
                        }
                        HttpURLConnection httpPost = createHttpPostObject();
                        String urlParams =
                                "code=" + code +
                                        "&client_id=" + CLIENT_ID +
                                        "&client_secret=" + CLIENT_SECRET +
                                        "&redirect_uri=" + redirectUri +
                                        "&grant_type=" + "authorization_code";
                        String[] tokens = null;
                        if (httpPost != null) {
                            tokens = acquireTokens(httpPost, urlParams);
                            setAccessToken(tokens[0]);
                            setRefreshToken(tokens[1]);
                            updateStatus("Google OAuth 2.0 API token retrieved. Access Token: " + tokens[0] + " Refresh Token: " + tokens[1]);
                            listener.receiveApiToken(tokens[0]);
                        }

                    }
                };
        ExecutorService executor = Executors.newCachedThreadPool();
        executor.submit(runnable);
    }

        @Override
        public void onAuthorizationError (@NonNull OAuthRequest oAuthRequest,int error){
        Log.e(TAG, "onAuthorizationError called: " + error);
    }
    }

}