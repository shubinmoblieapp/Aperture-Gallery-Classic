package com.marlonjones.aperture.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.marlonjones.aperture.adapters.ExcludedFolderAdapter;
import com.marlonjones.aperture.providers.ExcludedFolderProvider;
import com.marlonjones.aperture.ui.base.ThemedActivity;

/**
 * @author Aidan Follestad (afollestad)
 */
public class ExcludedFolderActivity extends ThemedActivity {

    private ExcludedFolderAdapter mAdapter;
    private final static int CHOOSE_FOLDER_REQUEST = 5000;

    @Override
    protected int darkTheme() {
        return com.marlonjones.aperture.R.style.AppTheme_Settings_Dark;
    }

    @Override
    protected int lightTheme() {
        return com.marlonjones.aperture.R.style.AppTheme_Settings;
    }

    @Override
    protected boolean allowStatusBarColoring() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.marlonjones.aperture.R.layout.activity_excludedfolders);
        Toolbar toolbar = (Toolbar) findViewById(com.marlonjones.aperture.R.id.toolbar);
        toolbar.setBackgroundColor(primaryColor());
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView mRecyclerView = (RecyclerView) findViewById(com.marlonjones.aperture.R.id.list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new ExcludedFolderAdapter(this, ExcludedFolderProvider.getAll(this));
        mRecyclerView.setAdapter(mAdapter);

        invalidateEmptyText();
    }

    public void invalidateEmptyText() {
        findViewById(com.marlonjones.aperture.R.id.empty).setVisibility(mAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        setResult(RESULT_OK);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(com.marlonjones.aperture.R.menu.excluded_folders, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == com.marlonjones.aperture.R.id.add) {
            startActivityForResult(new Intent(this, MainActivity.class)
                    .setAction(MainActivity.ACTION_SELECT_ALBUM), CHOOSE_FOLDER_REQUEST);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_FOLDER_REQUEST && resultCode == RESULT_OK) {
            String path = data.getData().getPath();
            ExcludedFolderProvider.add(this, path);
            mAdapter.add(path);
            invalidateEmptyText();
        }
    }
}
