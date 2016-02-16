package com.lbconsulting.a1list.presentation.ui.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.lbconsulting.a1list.R;
import com.lbconsulting.a1list.presentation.presenters.MainPresenter.View;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements View {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.i("onCreate()");
        setContentView(R.layout.activity_main);
    }

    @Override
    public void showProgress() {
        Timber.i("showProgress()");
    }

    @Override
    public void hideProgress() {
        Timber.i("hideProgress()");
    }

    @Override
    public void showError(String message) {
        Timber.e("showError(): %s", message);
    }


    @Override
    protected void onResume() {
        super.onResume();
        Timber.i("onResume()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Timber.i("onPause()");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Timber.i("onDestroy()");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
