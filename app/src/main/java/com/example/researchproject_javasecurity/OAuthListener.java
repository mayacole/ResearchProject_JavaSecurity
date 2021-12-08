package com.example.researchproject_javasecurity;

import android.app.Activity;

public interface OAuthListener {
    Activity getActivity();
    void updateStatus(String status);
    void receiveApiToken(String token);
}
