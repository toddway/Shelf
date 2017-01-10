package com.example.shelf;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;

import com.toddway.shelf.Shelf;

import rx.Observable;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String s = Observable.just("it works!")
                .compose(new Shelf(getCacheDir()).item("test").cacheThenNew(String.class))
                .toBlocking()
                .first();

        ((TextView) findViewById(R.id.textview)).setText(s);

    }
}
