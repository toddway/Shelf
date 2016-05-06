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

//        String s = new Shelf(getCacheDir())
//                .shelfable("test", String.class, Observable.just("it works!"))
//                .observeNew()
//                .toBlocking()
//                .first();
//        ((TextView) findViewById(R.id.textview)).setText(s);

    }
}
