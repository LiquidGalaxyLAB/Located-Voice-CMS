package com.gsoc.vedantsingh.locatedvoicecms;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;


public class CreateItemActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_item);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, CreateItemFragment.newInstance())
                    .commit();
        }
    }
}