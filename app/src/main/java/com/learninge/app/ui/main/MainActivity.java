package com.learninge.app.ui.main;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.learninge.app.R;

/**
 * Màn hình tạm để kiểm chứng khung project (T-001).
 * T-005 thay bằng MainActivity có BottomNavigationView 5 tab và NavHostFragment.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
