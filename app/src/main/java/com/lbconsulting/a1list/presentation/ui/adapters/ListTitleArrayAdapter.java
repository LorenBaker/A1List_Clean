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
import com.lbconsulting.a1list.AndroidApplication;
import com.lbconsulting.a1list.R;
import com.lbconsulting.a1list.domain.model.ListTitle;
import com.lbconsulting.a1list.domain.repositories.ListTitleRepository_Impl;
import com.lbconsulting.a1list.presentation.ui.activities.ListTitleActivity;
import com.lbconsulting.a1list.utils.CommonMethods;
import com.lbconsulting.a1list.utils.MyEvents;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import timber.log.Timber;


/**
 * An ArrayAdapter for displaying a ListTitle.
 */
public class ListTitleArrayAdapter extends ArrayAdapter<ListTitle> {

    private final Context mContext;
    private final ListView mListView;
    private final boolean mShowBtnEditListTitleName;
    private ListTitle mSelectedListTitle;
    private ListTitleRepository_Impl mListTitleRepository;

    public ListTitleArrayAdapter(Context context, ListView listView, boolean showBtnEditListTitleName) {
        super(context, 0);
        this.mContext = context;
        this.mListView = listView;
        this.mShowBtnEditListTitleName = showBtnEditListTitleName;
        this.mListTitleRepository = AndroidApplication.getListTitleRepository();
        Timber.i("ListTitleArrayAdapter(): Initialized");
    }

    public void setData(List<ListTitle> data) {
        if (data == null) {
            Timber.i("setData(): data null");
        }
        clear();
        if (data != null) {
            addAll(data);
            Timber.i("setData(): Loaded %d ListTitles.", data.size());
        }
    }

    @Override
    public long getItemId(int position) {
        ListTitle ListTitle = getItem(position);
        return ListTitle.getSQLiteId();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public int getPosition(ListTitle soughtItem) {
        return getItemPosition(soughtItem.getUuid());
    }

    private int getItemPosition(String soughtUuid) {
        int position;
        boolean found = false;

        ListTitle item;
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
        ListTitleViewHolder holder;

        // Get the data item for this position
        mSelectedListTitle = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_list_title, parent, false);
            holder = new ListTitleViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ListTitleViewHolder) convertView.getTag();
        }

        // Populate the data into the template view using the data object
        if (mSelectedListTitle != null) {
            holder.tvListTitleName.setText(mSelectedListTitle.getName());
            holder.tvListTitleName.setTextSize(TypedValue.COMPLEX_UNIT_SP,
                    mSelectedListTitle.retrieveListTheme().getTextSize());
            holder.tvListTitleName.setTextColor(mSelectedListTitle.retrieveListTheme().getTextColor());

            int horizontalPadding = CommonMethods.convertDpToPixel(
                    mSelectedListTitle.retrieveListTheme().getHorizontalPaddingInDp());
            int verticalPadding = CommonMethods.convertDpToPixel(
                    mSelectedListTitle.retrieveListTheme().getVerticalPaddingInDp());
            holder.tvListTitleName.setPadding(horizontalPadding, verticalPadding,
                    horizontalPadding, verticalPadding);
            holder.llRowListTitleName.setBackground(getBackgroundDrawable(
                    mSelectedListTitle.retrieveListTheme().getStartColor(),
                    mSelectedListTitle.retrieveListTheme().getEndColor()));
            mListView.setDivider(new ColorDrawable(ContextCompat.getColor(mContext,
                    R.color.greyLight3_50Transparent)));
            mListView.setDividerHeight(1);

            if (mSelectedListTitle.isStruckOut()) {
                setStrikeOut(holder.tvListTitleName, mSelectedListTitle.retrieveListTheme().isBold());
            } else {
                setNoStrikeOut(holder.tvListTitleName, mSelectedListTitle.retrieveListTheme().isBold(),
                        mSelectedListTitle.retrieveListTheme().getTextColor());
            }
        }

        holder.tvListTitleName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedListTitle = (ListTitle) v.getTag();
                mSelectedListTitle.setStruckOut(!mSelectedListTitle.isStruckOut());
                int strikeOutIncrement;
                if (mSelectedListTitle.isStruckOut()) {
                    strikeOutIncrement = 1;
                    setStrikeOut((TextView) v, mSelectedListTitle.retrieveListTheme().isBold());
                } else {
                    strikeOutIncrement = -1;
                    setNoStrikeOut((TextView) v, mSelectedListTitle.retrieveListTheme().isBold(),
                            mSelectedListTitle.retrieveListTheme().getTextColor());
                }
                EventBus.getDefault().post(new MyEvents.incrementStrikeOutCount(strikeOutIncrement));
//                mListTitleRepository.update(mSelectedListTitle);
                // Only update in local storage. ListTitle will be saved to Cloud when and if ListTitle is deleted.
                mListTitleRepository.updateInLocalStorage(mSelectedListTitle);

            }
        });

        if (mShowBtnEditListTitleName) {
            holder.btnEditListTitleName.setVisibility(View.VISIBLE);
        } else {
            holder.btnEditListTitleName.setVisibility(View.GONE);
        }

        holder.btnEditListTitleName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListTitle selectedListTitle = (ListTitle) v.getTag();
                startListTitleActivity(selectedListTitle);
            }
        });

        // save the item so it can be retrieved later
        holder.tvListTitleName.setTag(mSelectedListTitle);
        holder.btnEditListTitleName.setTag(mSelectedListTitle);

        // Return the completed view to render on screen
        return convertView;
    }

    private void startListTitleActivity(ListTitle selectedListTitle) {
        Gson gson = new Gson();
        String listTitleJson = gson.toJson(selectedListTitle);
        Intent listTitleActivityIntent = new Intent(mContext, ListTitleActivity.class);
        listTitleActivityIntent.putExtra(ListTitleActivity.ARG_LIST_TITLE_JSON, listTitleJson);
        listTitleActivityIntent.putExtra(ListTitleActivity.ARG_MODE, ListTitleActivity.EDIT_EXISTING_LIST_TITLE);
        mContext.startActivity(listTitleActivityIntent);
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

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        Timber.i("notifyDataSetChanged()");
    }

    private class ListTitleViewHolder {
        public final TextView tvListTitleName;
        public final ImageButton btnEditListTitleName;
        public final LinearLayout llRowListTitleName;

        public ListTitleViewHolder(View base) {
            tvListTitleName = (TextView) base.findViewById(R.id.tvListTitleName);
            btnEditListTitleName = (ImageButton) base.findViewById(R.id.btnEditListTitle);
            llRowListTitleName = (LinearLayout) base.findViewById(R.id.llRowListTitle);
        }
    }
}

