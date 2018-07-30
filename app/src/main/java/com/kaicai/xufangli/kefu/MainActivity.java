package com.kaicai.xufangli.kefu;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.m7.imkfsdk.KfStartHelper;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KfStartHelper helper = new KfStartHelper(MainActivity.this);
                helper.initSdkChat( "39941890-8a6f-11e8-a09b-3399d2002bb6", "userName", "userId");
            }
        });
    }
}
