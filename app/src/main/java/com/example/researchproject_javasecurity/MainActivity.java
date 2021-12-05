package com.example.researchproject_javasecurity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import net.openid.appauth.*;
import android.util.*;



import com.example.researchproject_javasecurity.databinding.ActivityMainBinding;

public class MainActivity extends Activity {
    private static final int RC_AUTH=100;
    private AuthorizationService mAuthorizationService;
    private AuthStateManager mAuthState;

    //private TextView mTextView;
    //private ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuthState = AuthStateManager.getInstance(this);
        mAuthorizationService = new AuthorizationService(this);

        if(mAuthState.getCurrent().isAuthorized()) {
            Log.d("Authenticated", "Done");
            //Need to finish this
            //Possibly add a logout button if the AuthenticationState has authenticated already
        } else { //if the AuthenticationState has not been authenticated, then set serviceConfig. Here we are calling out our clientID and Oauth and creating a profile.
            AuthorizationServiceConfiguration serviceConfig = new AuthorizationServiceConfiguration(Uri.parse("https://accounts.google.com/o/auth2/v2/auth"),
                    Uri.parse("https://www.googleapis.com/oauth2/v4/token"));
            String clientId = "20049550827-qbsvqno3n8rp6tnl6fpr6b1h160dufst.apps.googleusercontent.com";
            Uri redirectUri = Uri.parse("com.google.codelabs.appauth:/oauth2callback");

            AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(serviceConfig, clientId, ResponseTypeValues.CODE, redirectUri);
            builder.setScopes("profile");

            AuthorizationRequest request = builder.build();

            AuthorizationService authService = new AuthorizationService(this);
            Intent intent = authService.getAuthorizationRequestIntent(request);
            startActivityForResult(intent, RC_AUTH);
        }
        
        //setContentView(binding.getRoot());

        //mTextView = binding.text;

    }
}