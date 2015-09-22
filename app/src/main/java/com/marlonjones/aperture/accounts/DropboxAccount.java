package com.marlonjones.aperture.accounts;

import android.content.Context;

import com.marlonjones.aperture.accounts.base.Account;
import com.marlonjones.aperture.adapters.MediaAdapter;
import com.marlonjones.aperture.api.AlbumEntry;

/**
 * @author Aidan Follestad (afollestad)
 */
public class DropboxAccount extends Account {

    private final int mId;

    public DropboxAccount(Context context, int id) {
        super(context);
        mId = id;
    }

    @Override
    public int id() {
        return mId;
    }

    @Override
    public int type() {
        return TYPE_GOOGLE_DRIVE;
    }

    @Override
    public String name() {
        return null;
    }

    @Override
    public boolean hasIncludedFolders() {
        return false;
    }

    @Override
    public void getAlbums(MediaAdapter.SortMode sort, MediaAdapter.FileFilterMode filter, AlbumCallback callback) {
        callback.onAlbums(null);
    }

    @Override
    public void getIncludedFolders(AlbumEntry[] preEntries, AlbumCallback callback) {
    }

    @Override
    public void getEntries(String albumPath, int overviewMode, boolean explorerMode, MediaAdapter.FileFilterMode filter, MediaAdapter.SortMode sort, EntriesCallback callback) {
    }
}
