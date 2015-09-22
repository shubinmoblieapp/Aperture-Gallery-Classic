package com.marlonjones.aperture.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.GridLayoutManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.marlonjones.aperture.adapters.MediaAdapter;
import com.marlonjones.aperture.adapters.base.HybridCursorAdapter;
import com.marlonjones.aperture.api.AlbumEntry;
import com.marlonjones.aperture.api.base.MediaEntry;
import com.marlonjones.aperture.cab.MediaCab;
import com.marlonjones.aperture.fragments.base.LoaderFragment;
import com.marlonjones.aperture.providers.SortMemoryProvider;
import com.marlonjones.aperture.ui.MainActivity;
import com.marlonjones.aperture.ui.viewer.ViewerActivity;
import com.marlonjones.aperture.utils.Utils;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.bitmap.BitmapInfo;

import java.io.File;

import static android.app.Activity.RESULT_OK;

/**
 * @author Aidan Follestad (afollestad)
 */
public class MediaFragment extends LoaderFragment<MediaAdapter.ViewHolder> implements MediaAdapter.Callback {

    private String mAlbumPath;
    private boolean mLastDarkTheme;

    private static final String STATE_ALBUMPATH = "state_albumpath";

    public static MediaFragment create(String albumPath) {
        MediaFragment frag = new MediaFragment();
        frag.mAlbumPath = albumPath;
        Bundle args = new Bundle();
        args.putString("albumPath", albumPath);
        frag.setArguments(args);
        return frag;
    }

    public String getAlbumPath() {
        return mAlbumPath;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mAlbumPath = savedInstanceState.getString(STATE_ALBUMPATH, null);
        } else if (getArguments() != null) {
            mAlbumPath = getArguments().getString("albumPath");
        }
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mLastDarkTheme = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("dark_theme", false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            MainActivity act = (MainActivity) getActivity();
            if (act.mMediaCab != null)
                act.mMediaCab.setFragment(this, true);

            boolean darkTheme = PreferenceManager.getDefaultSharedPreferences(act).getBoolean("dark_theme", false);
            if (darkTheme != mLastDarkTheme) {
                invalidateLayoutManagerAndAdapter();
            }

            if (mAlbumPath == null || mAlbumPath.equals(AlbumEntry.ALBUM_OVERVIEW) ||
                    mAlbumPath.equals(Environment.getExternalStorageDirectory().getAbsolutePath())) {
                act.drawerArrowOpen = true;
                act.animateDrawerArrow(true);
            } else {
                act.animateDrawerArrow(false);
            }

            if (getTitle() != null)
                act.setTitle(getTitle());
            crumb = act.mCrumbs.findCrumb(getAlbumPath());
        }

        reload();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_ALBUMPATH, mAlbumPath);
    }

    @Override
    protected String getTitle() {
        if (isExplorerMode()) {
            // In explorer mode, the path is displayed in the bread crumbs so the name is shown instead
            return getString(com.marlonjones.aperture.R.string.app_name);
        } else if (mAlbumPath == null || mAlbumPath.equals(AlbumEntry.ALBUM_OVERVIEW) ||
                mAlbumPath.equals(Environment.getExternalStorageDirectory().getAbsolutePath())) {
            return getString(com.marlonjones.aperture.R.string.overview);
        }
        return new File(mAlbumPath).getName();
    }

    @Override
    protected GridLayoutManager getLayoutManager() {
        int columnCount = getViewMode() == MediaAdapter.ViewMode.GRID ? getGridWidth() : 1;
        return new GridLayoutManager(getActivity(), columnCount);
    }

    @Override
    protected int getEmptyText() {
        return com.marlonjones.aperture.R.string.no_photosorvideos;

    }

    @Override
    protected HybridCursorAdapter<MediaAdapter.ViewHolder> initializeAdapter() {
        MainActivity act = (MainActivity) getActivity();
        return new MediaAdapter(act, SortMemoryProvider.remember(getActivity(), mAlbumPath),
                getViewMode(), this, act.isSelectAlbumMode());
    }

    @Override
    public void onClick(int index, View view, MediaEntry entry, boolean longClick) {
        final MainActivity act = (MainActivity) getActivity();
        if (act != null) {
            act.mIsReentering = false;
            act.mTmpState = new Bundle();
            act.mTmpState.putInt(MainActivity.EXTRA_CURRENT_ITEM_POSITION, index);
            act.mTmpState.putInt(MainActivity.EXTRA_OLD_ITEM_POSITION, index);

            if (act.mPickMode || act.isSelectAlbumMode()) {
                if (entry.isFolder() || entry.isAlbum()) {
                    act.switchPage(entry.data(), false, true);
                } else {
                    // This will never be called for album selection mode, only pick mode
                    final File file = new File(entry.data());
                    final Uri uri = Utils.getImageContentUri(getActivity(), file);
                    act.setResult(Activity.RESULT_OK, new Intent().setData(uri));
                    act.finish();
                }
            } else if (longClick) {
                if (act.mMediaCab == null)
                    act.mMediaCab = new MediaCab(getActivity());
                if (!act.mMediaCab.isStarted())
                    act.mMediaCab.start();
                act.mMediaCab.setFragment(this, false);
                act.mMediaCab.toggleEntry(entry);
            } else {
                if (act.mMediaCab != null && act.mMediaCab.isStarted()) {
                    act.mMediaCab.setFragment(this, false);
                    act.mMediaCab.toggleEntry(entry);
                } else {
                    if (entry.isFolder() || entry.isAlbum()) {
                        act.switchPage(entry.data(), false, true);
                    } else {
                        ImageView iv = (ImageView) view.findViewById(com.marlonjones.aperture.R.id.image);
                        BitmapInfo bi = Ion.with(iv).getBitmapInfo();
                        ViewerActivity.MediaWrapper wrapper = ((MediaAdapter) getAdapter()).getMedia();
                        final Intent intent = new Intent(act, ViewerActivity.class)
                                .putExtra("media_entries", wrapper)
                                .putExtra(MainActivity.EXTRA_CURRENT_ITEM_POSITION, index)
                                .putExtra("bitmapInfo", bi != null ? bi.key : null);
                        final String transName = "view_" + index;
                        final ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                                getActivity(), iv, transName);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            //Somehow this works (setting status bar color in both MainActivity and here)
                            //to avoid image glitching through on when ViewActivity is first created.
                            //TODO: Look into why this works and whether some code is unnecessary
                            act.getWindow().setStatusBarColor(act.primaryColorDark());
                            View statusBar = act.getWindow().getDecorView().findViewById(android.R.id.statusBarBackground);
                            if (statusBar != null) {
                                statusBar.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        ActivityCompat.startActivityForResult(act, intent, 2000, options.toBundle());
                                    }
                                });
                                return;
                            }
                        }
                        ActivityCompat.startActivityForResult(act, intent, 2000, options.toBundle());
                    }
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK &&
                (requestCode == MediaCab.COPY_REQUEST_CODE || requestCode == MediaCab.MOVE_REQUEST_CODE)) {
            ((MainActivity) getActivity()).mMediaCab.finishCopyMove(new File(data.getData().getPath()), requestCode);
        }
    }

    private void setStatus(String status) {
        ((MainActivity) getActivity()).setStatus(status);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(com.marlonjones.aperture.R.menu.fragment, menu);
        super.onCreateOptionsMenu(menu, inflater);

        if (getActivity() != null) {
            boolean isMain = getAlbumPath() == null || getAlbumPath().equals(AlbumEntry.ALBUM_OVERVIEW);
            boolean isAlbumSelect = ((MainActivity) getActivity()).isSelectAlbumMode();
            menu.findItem(com.marlonjones.aperture.R.id.choose).setVisible(!isMain && isAlbumSelect);
            menu.findItem(com.marlonjones.aperture.R.id.viewMode).setVisible(!isAlbumSelect);
            menu.findItem(com.marlonjones.aperture.R.id.filter).setVisible(!isAlbumSelect);

            mSortCache = SortMemoryProvider.remember(this, mAlbumPath);
            switch (mSortCache) {
                default:
                    menu.findItem(com.marlonjones.aperture.R.id.sortNameAsc).setChecked(true);
                    break;
                case NAME_DESC:
                    menu.findItem(com.marlonjones.aperture.R.id.sortNameDesc).setChecked(true);
                    break;
                case MODIFIED_DATE_ASC:
                    menu.findItem(com.marlonjones.aperture.R.id.sortModifiedAsc).setChecked(true);
                    break;
                case MODIFIED_DATE_DESC:
                    menu.findItem(com.marlonjones.aperture.R.id.sortModifiedDesc).setChecked(true);
                    break;
            }
            menu.findItem(com.marlonjones.aperture.R.id.sortCurrentDir).setChecked(sortRememberDir);

            MediaAdapter.FileFilterMode filterMode = getFilterMode(getActivity());
            switch (filterMode) {
                default:
                    setStatus(null);
                    menu.findItem(com.marlonjones.aperture.R.id.filterAll).setChecked(true);
                    break;
                case PHOTOS:
                    setStatus(getString(com.marlonjones.aperture.R.string.filtering_photos));
                    menu.findItem(com.marlonjones.aperture.R.id.filterPhotos).setChecked(true);
                    break;
                case VIDEOS:
                    setStatus(getString(com.marlonjones.aperture.R.string.filtering_videos));
                    menu.findItem(com.marlonjones.aperture.R.id.filterVideos).setChecked(true);
                    break;
            }

            switch (getGridWidth()) {
                default:
                    menu.findItem(com.marlonjones.aperture.R.id.gridSizeOne).setChecked(true);
                    break;
                case 2:
                    menu.findItem(com.marlonjones.aperture.R.id.gridSizeTwo).setChecked(true);
                    break;
                case 3:
                    menu.findItem(com.marlonjones.aperture.R.id.gridSizeThree).setChecked(true);
                    break;
                case 4:
                    menu.findItem(com.marlonjones.aperture.R.id.gridSizeFour).setChecked(true);
                    break;
                case 5:
                    menu.findItem(com.marlonjones.aperture.R.id.gridSizeFive).setChecked(true);
                    break;
                case 6:
                    menu.findItem(com.marlonjones.aperture.R.id.gridSizeSix).setChecked(true);
                    break;
            }
        }

        menu.findItem(com.marlonjones.aperture.R.id.viewExplorer).setChecked(isExplorerMode());
        if (getViewMode() == MediaAdapter.ViewMode.GRID) {
            menu.findItem(com.marlonjones.aperture.R.id.viewMode).setIcon(com.marlonjones.aperture.R.drawable.ic_action_view_list);
        } else {
            menu.findItem(com.marlonjones.aperture.R.id.viewMode).setIcon(com.marlonjones.aperture.R.drawable.ic_action_view_grid);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case com.marlonjones.aperture.R.id.choose:
                getActivity().setResult(RESULT_OK, new Intent().setData(Uri.fromFile(new File(getAlbumPath()))));
                getActivity().finish();
                return true;
            case com.marlonjones.aperture.R.id.viewMode:
                if (getViewMode() == MediaAdapter.ViewMode.GRID)
                    setViewMode(MediaAdapter.ViewMode.LIST);
                else setViewMode(MediaAdapter.ViewMode.GRID);
                return true;
            case com.marlonjones.aperture.R.id.viewExplorer:
                setExplorerMode(!isExplorerMode());
                return true;
            case com.marlonjones.aperture.R.id.filterAll:
                setFilterMode(MediaAdapter.FileFilterMode.ALL);
                return true;
            case com.marlonjones.aperture.R.id.filterPhotos:
                setFilterMode(MediaAdapter.FileFilterMode.PHOTOS);
                return true;
            case com.marlonjones.aperture.R.id.filterVideos:
                setFilterMode(MediaAdapter.FileFilterMode.VIDEOS);
                return true;
            case com.marlonjones.aperture.R.id.sortNameAsc:
                setSortMode(MediaAdapter.SortMode.NAME_ASC, mAlbumPath);
                return true;
            case com.marlonjones.aperture.R.id.sortNameDesc:
                setSortMode(MediaAdapter.SortMode.NAME_DESC, sortRememberDir ? mAlbumPath : null);
                return true;
            case com.marlonjones.aperture.R.id.sortModifiedAsc:
                setSortMode(MediaAdapter.SortMode.MODIFIED_DATE_ASC, sortRememberDir ? mAlbumPath : null);
                return true;
            case com.marlonjones.aperture.R.id.sortModifiedDesc:
                setSortMode(MediaAdapter.SortMode.MODIFIED_DATE_DESC, sortRememberDir ? mAlbumPath : null);
                return true;
            case com.marlonjones.aperture.R.id.sortCurrentDir:
                item.setChecked(!item.isChecked());
                if (item.isChecked()) {
                    sortRememberDir = true;
                    setSortMode(mSortCache, mAlbumPath);
                } else {
                    sortRememberDir = false;
                    SortMemoryProvider.forget(getActivity(), mAlbumPath);
                    setSortMode(SortMemoryProvider.remember(getActivity(), null), null);
                }
                return true;
            case com.marlonjones.aperture.R.id.gridSizeOne:
                item.setChecked(!item.isChecked());
                setGridWidth(1);
                break;
            case com.marlonjones.aperture.R.id.gridSizeTwo:
                item.setChecked(!item.isChecked());
                setGridWidth(2);
                break;
            case com.marlonjones.aperture.R.id.gridSizeThree:
                item.setChecked(!item.isChecked());
                setGridWidth(3);
                break;
            case com.marlonjones.aperture.R.id.gridSizeFour:
                item.setChecked(!item.isChecked());
                setGridWidth(4);
                break;
            case com.marlonjones.aperture.R.id.gridSizeFive:
                item.setChecked(!item.isChecked());
                setGridWidth(5);
                break;
            case com.marlonjones.aperture.R.id.gridSizeSix:
                item.setChecked(!item.isChecked());
                setGridWidth(6);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    }


