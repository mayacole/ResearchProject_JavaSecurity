package com.example.researchproject_javasecurity;

import android.app.Activity;

public interface ListenerForOAuth {
    Activity getActivity();
    void updateStatus(String status);
    void receiveApiToken(String token);
}
