package com.marlonjones.aperture.adapters.base;

import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;

import com.marlonjones.aperture.adapters.MediaAdapter;
import com.marlonjones.aperture.api.base.MediaEntry;

import java.io.File;

/**
 * @author Aidan Follestad (afollestad)
 */
public abstract class HybridCursorAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    public abstract void clear();

    public abstract void addAll(MediaEntry[] entries);

    public abstract void changeContent(Cursor cursor, Uri from, boolean clear, boolean explorerMode);

    public abstract void changeContent(File[] content, boolean explorerMode, MediaAdapter.FileFilterMode mode);
}