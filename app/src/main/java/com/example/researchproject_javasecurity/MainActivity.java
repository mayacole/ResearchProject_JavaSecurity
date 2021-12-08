package com.example.researchproject_javasecurity;

import android.app.Activity;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements ListenerForOAuth {

    private TextView textView;
    private Button button;
    private TokenAccessor tokenAccessor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (Button) findViewById(R.id.authenticateButton);

        tokenAccessor = new TokenAccessor(this);
        tokenAccessor.startOAuthFlow();
    }

    public void getToken(View view) {

        tokenAccessor.getAccess();
    }

    public Activity getActivity() {

        return this;
    }

    @Override
    public void updateStatus(String status) {

        textView.setText(status);
    }

    public void receiveApiToken(String token) {

        updateStatus("Token received. Token: " + token);
    }

    public void onError() {
        updateStatus("Error retrieving book library.");
        tokenAccessor.refreshToken();
    }
}