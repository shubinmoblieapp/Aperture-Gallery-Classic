package com.marlonjones.aperture.adapters;

import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.marlonjones.aperture.providers.ExcludedFolderProvider;
import com.marlonjones.aperture.ui.ExcludedFolderActivity;
import com.marlonjones.aperture.utils.Utils;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExcludedFolderAdapter extends RecyclerView.Adapter<ExcludedFolderAdapter.ViewHolder> implements View.OnClickListener {

    private final ExcludedFolderActivity mContext;
    private final List<String> mDataset;
    private final int primaryTextColor;

    @Override
    public void onClick(View v) {
        final int index = (Integer) v.getTag();
        final String path = mDataset.get(index);
        new MaterialDialog.Builder(mContext)
                .content(Html.fromHtml(mContext.getString(com.marlonjones.aperture.R.string.confirm_exclude_album_remove, path)))
                .positiveText(com.marlonjones.aperture.R.string.yes)
                .negativeText(com.marlonjones.aperture.R.string.no)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog materialDialog) {
                        ExcludedFolderProvider.remove(mContext, path);
                        mDataset.remove(index);
                        notifyDataSetChanged();
                        mContext.invalidateEmptyText();
                    }
                }).show();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final View mView;
        public final TextView mTitle;
        public final ImageView mIcon;

        public ViewHolder(View v) {
            super(v);
            mView = v.findViewById(com.marlonjones.aperture.R.id.viewFrame);
            mTitle = (TextView) v.findViewById(com.marlonjones.aperture.R.id.title);
            mIcon = (ImageView) v.findViewById(com.marlonjones.aperture.R.id.icon);
        }
    }

    public ExcludedFolderAdapter(ExcludedFolderActivity context, String[] myDataset) {
        mContext = context;
        mDataset = new ArrayList<>();
        Collections.addAll(mDataset, myDataset);
        primaryTextColor = Utils.resolveColor(context, android.R.attr.textColorPrimary);
    }

    public void add(String path) {
        mDataset.add(path);
        notifyDataSetChanged();
    }

    @Override
    public ExcludedFolderAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(com.marlonjones.aperture.R.layout.list_item_excludedfolder, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mView.setTag(position);
        holder.mView.setOnClickListener(this);
        holder.mTitle.setText(mDataset.get(position));
        holder.mIcon.setImageResource(com.marlonjones.aperture.R.drawable.ic_action_discard);
        holder.mIcon.getDrawable().mutate().setColorFilter(primaryTextColor, PorterDuff.Mode.SRC_ATOP);

    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}