package com.lbconsulting.a1list.presentation.ui.adapters;

import android.content.Context;
import android.graphics.Color;
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
import android.widget.Toast;

import com.lbconsulting.a1list.R;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.repositories.ListThemeRepository_Impl;
import com.lbconsulting.a1list.utils.CommonMethods;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;


/**
 * An ArrayAdapter for displaying ListThemes.
 */
public class ListThemeDialogArrayAdapter extends ArrayAdapter<ListTheme> {

    private final Context mContext;
    private final ListView mListView;

    private ListTheme mSelectedTheme;
    private ListThemeRepository_Impl mListThemeRepository;

    public ListThemeDialogArrayAdapter(Context context, ListView listView) {
        super(context, 0);
        this.mContext = context;
        this.mListView = listView;
        Timber.i("ListThemeDialogArrayAdapter(): Initialized");
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
        return ListTheme.getSQLiteId();
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

        ListTheme listTheme;
        for (position = 0; position < getCount(); position++) {
            listTheme = getItem(position);
            if (listTheme.getUuid().equals(soughtUuid)) {
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

            if (mSelectedTheme.isTransparent()) {
                holder.llRowThemeName.setBackgroundColor(Color.TRANSPARENT);
                mListView.setDivider(null);
                mListView.setDividerHeight(0);
            } else {
                holder.llRowThemeName.setBackground(getBackgroundDrawable(mSelectedTheme.getStartColor(), mSelectedTheme.getEndColor()));
                mListView.setDivider(new ColorDrawable(ContextCompat.getColor(mContext, R.color.greyLight3_50Transparent)));
                mListView.setDividerHeight(1);
            }

        }

        holder.tvThemeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedTheme = (ListTheme) v.getTag();
                Toast.makeText(mContext, mSelectedTheme.getName() + " selected.", Toast.LENGTH_SHORT).show();

            }
        });

        holder.btnEditThemeName.setVisibility(View.GONE);


        // save the item so it can be retrieved later
        holder.tvThemeName.setTag(mSelectedTheme);

        // Return the completed view to render on screen
        return convertView;
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

    static class ListThemeViewHolder {
        @Bind(R.id.tvThemeName)
        TextView tvThemeName;
        @Bind(R.id.btnEditThemeName)
        ImageButton btnEditThemeName;
        @Bind(R.id.llRowThemeName)
        LinearLayout llRowThemeName;


        public ListThemeViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}

