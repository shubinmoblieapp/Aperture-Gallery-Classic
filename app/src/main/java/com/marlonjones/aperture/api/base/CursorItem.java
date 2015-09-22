package com.marlonjones.aperture.api.base;

import android.database.Cursor;

/**
 * @author Aidan Follestad (afollestad)
 */
public interface CursorItem<T> {

    T load(Cursor from);

    String[] projection();
}
