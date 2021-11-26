package com.example.researchproject_javasecurity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.researchproject_javasecurity.databinding.ActivityMainBinding;

public class MainActivity extends Activity {

    private TextView mTextView;
    private ActivityMainBinding binding;

    //Client ID: 20049550827-qbsvqno3n8rp6tnl6fpr6b1h160dufst.apps.googleusercontent.com

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mTextView = binding.text;
    }
}