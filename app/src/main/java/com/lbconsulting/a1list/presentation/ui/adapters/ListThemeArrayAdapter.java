package com.lbconsulting.a1list.presentation.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.lbconsulting.a1list.R;
import com.lbconsulting.a1list.domain.executor.impl.ThreadExecutor;
import com.lbconsulting.a1list.domain.interactors.listTheme.impl.ToggleListThemeBooleanField_InBackground;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.repositories.ListThemeRepository_Impl;
import com.lbconsulting.a1list.domain.storage.ListThemesSqlTable;
import com.lbconsulting.a1list.presentation.ui.activities.ListThemeActivity;
import com.lbconsulting.a1list.threading.MainThreadImpl;
import com.lbconsulting.a1list.utils.CommonMethods;

import java.util.List;

import timber.log.Timber;


/**
 * An ArrayAdapter for displaying a ListTheme.
 */
public class ListThemeArrayAdapter extends ArrayAdapter<ListTheme>  {

    private final Context mContext;
    private final ListView mListView;
    private final boolean mShowBtnEditThemeName;
    private final View mSnackbarView;
    private ListTheme mSelectedTheme;
    private ListThemeRepository_Impl mListThemeRepository;
    private ToggleListThemeBooleanField_InBackground.Callback mCallback;

    public ListThemeArrayAdapter(Context context, ListView listView, boolean showBtnEditThemeName,
                                 View snackbarView) {
        super(context, 0);
        this.mContext = context;
        this.mCallback = (ToggleListThemeBooleanField_InBackground.Callback) context;
        this.mListView = listView;
        this.mShowBtnEditThemeName = showBtnEditThemeName;
        mSnackbarView = snackbarView;
        mListThemeRepository = new ListThemeRepository_Impl(context);
        Timber.i("ListThemeArrayAdapter(): Initialized");
    }

    public void setData(List<ListTheme> data) {
        if (data == null) {
            Timber.i("setData(): data null");
        }
        clear();
        if (data != null) {
            addAll(data);
            Timber.i("setData(): Loaded %d ListThemes.", data.size());
        }
    }

    @Override
    public long getItemId(int position) {
        ListTheme ListTheme = getItem(position);
        return ListTheme.getId();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public int getPosition(ListTheme soughtItem) {
        return getItemPosition(soughtItem.getUuid());
    }

    private int getItemPosition(String soughtUuid) {
        int position;
        boolean found = false;

        ListTheme item;
        for (position = 0; position < getCount(); position++) {
            item = getItem(position);
            if (item.getUuid().equals(soughtUuid)) {
                found = true;
                break;
            }
        }

        if (!found) {
            position = 0;
        }
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ListThemeViewHolder holder;

        // Get the data item for this position
        mSelectedTheme = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_themes, parent, false);
            holder = new ListThemeViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ListThemeViewHolder) convertView.getTag();
        }

        // Populate the data into the template view using the data object
        if (mSelectedTheme != null) {
            holder.tvThemeName.setText(mSelectedTheme.getName());
            holder.tvThemeName.setTextSize(TypedValue.COMPLEX_UNIT_SP, mSelectedTheme.getTextSize());
            holder.tvThemeName.setTextColor(mSelectedTheme.getTextColor());

            int horizontalPadding = CommonMethods.convertDpToPixel(mSelectedTheme.getHorizontalPaddingInDp());
            int verticalPadding = CommonMethods.convertDpToPixel(mSelectedTheme.getVerticalPaddingInDp());
            holder.tvThemeName.setPadding(horizontalPadding, verticalPadding,
                    horizontalPadding, verticalPadding);

//            if (mSelectedTheme.isTransparent()) {
//                holder.llRowThemeName.setBackgroundColor(Color.TRANSPARENT);
//                mListView.setDivider(null);
//                mListView.setDividerHeight(0);
//            } else {
                holder.llRowThemeName.setBackground(getBackgroundDrawable(mSelectedTheme.getStartColor(), mSelectedTheme.getEndColor()));
                mListView.setDivider(new ColorDrawable(ContextCompat.getColor(mContext, R.color.greyLight3_50Transparent)));
                mListView.setDividerHeight(1);
//            }

            if (mSelectedTheme.isStruckOut()) {
                setStrikeOut(holder.tvThemeName, mSelectedTheme.isBold());
            } else {
                setNoStrikeOut(holder.tvThemeName, mSelectedTheme.isBold(), mSelectedTheme.getTextColor());
            }
        }

        holder.tvThemeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedTheme = (ListTheme) v.getTag();
                mSelectedTheme.setStruckOut(!mSelectedTheme.isStruckOut());
                if (mSelectedTheme.isStruckOut()) {
                    setStrikeOut((TextView) v, mSelectedTheme.isBold());
                } else {
                    setNoStrikeOut((TextView) v, mSelectedTheme.isBold(), mSelectedTheme.getTextColor());
                }
                new ToggleListThemeBooleanField_InBackground(ThreadExecutor.getInstance(),
                        MainThreadImpl.getInstance(), mCallback,
                        mSelectedTheme, ListThemesSqlTable.COL_STRUCK_OUT).execute();

            }
        });

        if (mShowBtnEditThemeName) {
            holder.btnEditThemeName.setVisibility(View.VISIBLE);
        } else {
            holder.btnEditThemeName.setVisibility(View.GONE);
        }

        holder.btnEditThemeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListTheme selectedTheme = (ListTheme) v.getTag();
                startListThemeActivity(selectedTheme);

//                CommonMethods.showSnackbar(mSnackbarView, "btnEditThemeName: " + selectedTheme.getListItem() + " clicked.", Snackbar.LENGTH_SHORT);

//                        showEditThemeNameDialog(selectedTheme.getUuid());
            }
        });

        // save the item so it can be retrieved later
        holder.tvThemeName.setTag(mSelectedTheme);
        holder.btnEditThemeName.setTag(mSelectedTheme);

        // Return the completed view to render on screen
        return convertView;
    }

    private void startListThemeActivity(ListTheme selectedTheme) {
        Gson gson = new Gson();
        String listThemeJson = gson.toJson(selectedTheme);
        Intent listThemeActivityIntent = new Intent(mContext, ListThemeActivity.class);
        listThemeActivityIntent.putExtra(ListThemeActivity.ARG_LIST_THEME_JSON, listThemeJson);
        listThemeActivityIntent.putExtra(ListThemeActivity.ARG_MODE, ListThemeActivity.EDIT_EXISTING_LIST_THEME);
        mContext.startActivity(listThemeActivityIntent);
    }

    private void setStrikeOut(TextView tv, boolean isBold) {
        if (isBold) {
            tv.setTypeface(null, Typeface.BOLD_ITALIC);
        } else {
            tv.setTypeface(null, Typeface.ITALIC);
        }
        tv.setTextColor(ContextCompat.getColor(mContext, R.color.crimson));
        tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
    }

    private void setNoStrikeOut(TextView tv, boolean isBold, int textColor) {
        if (isBold) {
            tv.setTypeface(null, Typeface.BOLD);
        } else {
            tv.setTypeface(null, Typeface.NORMAL);
        }
        tv.setTextColor(textColor);
        tv.setPaintFlags(tv.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
    }

    private Drawable getBackgroundDrawable(int startColor, int endColor) {
        int colors[] = {startColor, endColor};
        return new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
    }

//    private void showEditThemeNameDialog(String themeUuid) {
//        CommonMethods.showSnackbar(mSnackbarView,selectedTheme.getListItem()+" selected.", Snackbar.LENGTH_SHORT);
//        EventBus.getDefault().post(new MyEvents.showEditAttributesNameDialog(themeUuid));
//    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        Timber.i("notifyDataSetChanged()");
    }


    private class ListThemeViewHolder {
        public final TextView tvThemeName;
        public final ImageButton btnEditThemeName;
        public final LinearLayout llRowThemeName;

        public ListThemeViewHolder(View base) {
            tvThemeName = (TextView) base.findViewById(R.id.tvThemeName);
            btnEditThemeName = (ImageButton) base.findViewById(R.id.btnEditThemeName);
            llRowThemeName = (LinearLayout) base.findViewById(R.id.llRowThemeName);
        }
    }
}

