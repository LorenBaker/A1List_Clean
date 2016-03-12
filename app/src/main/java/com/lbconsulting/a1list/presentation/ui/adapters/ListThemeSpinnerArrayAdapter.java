package com.lbconsulting.a1list.presentation.ui.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.lbconsulting.a1list.R;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.utils.CommonMethods;
import com.lbconsulting.a1list.utils.MyEvents;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Method;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;


/**
 * An ArrayAdapter for displaying ListThemes.
 */
public class ListThemeSpinnerArrayAdapter extends ArrayAdapter<ListTheme> {

    private final Context mContext;
//    private final ListView mListView;

    private ListTheme mSelectedTheme;
    private Spinner mSpinner;
//    private ListThemeRepository_Impl mListThemeRepository;

    public ListThemeSpinnerArrayAdapter(Context context, Spinner spinner) {
        super(context, 0);
        this.mContext = context;
        this.mSpinner = spinner;
//        this.mListView = listView;
        Timber.i("ListThemeSpinnerArrayAdapter(): Initialized");
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

        populateListTheme(holder, mSelectedTheme);

        return convertView;
    }

    private void populateListTheme(ListThemeViewHolder holder, ListTheme listTheme) {
        // Populate the data into the template view using the data object
        if (listTheme != null) {
            //
            holder.tvListThemeName.setText(String.format(mContext.getString(R.string.tvListThemeName_text), listTheme.getName()));
            holder.tvListThemeName.setTextSize(TypedValue.COMPLEX_UNIT_SP, listTheme.getTextSize());
            holder.tvListThemeName.setTextColor(listTheme.getTextColor());

            int horizontalPadding = CommonMethods.convertDpToPixel(listTheme.getHorizontalPaddingInDp());
            int verticalPadding = CommonMethods.convertDpToPixel(listTheme.getVerticalPaddingInDp());
            holder.tvListThemeName.setPadding(horizontalPadding, verticalPadding,
                    horizontalPadding, verticalPadding);
            holder.llRowThemeName.setBackground(getBackgroundDrawable(listTheme.getStartColor(), listTheme.getEndColor()));

        }

        holder.btnEditThemeName.setVisibility(View.GONE);

        // save the item so it can be retrieved later
        holder.tvListThemeName.setTag(listTheme);
    }

    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        ListThemeViewHolder holder;
        // Get the data item for this position
        ListTheme selectedTheme = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_themes, parent, false);
            holder = new ListThemeViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ListThemeViewHolder) convertView.getTag();
        }

        populateListTheme(holder, selectedTheme);

        holder.tvListThemeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedTheme = (ListTheme) v.getTag();
                EventBus.getDefault().post(new MyEvents.updateListTitleActivityUI(mSelectedTheme));
//                Toast.makeText(mContext, mSelectedTheme.getName() + " selected.", Toast.LENGTH_SHORT).show();

                int position = getPosition(mSelectedTheme);
                mSpinner.setSelection(position);

                // hide the spinner dropdown
                if (mSpinner != null) {
                    try {
                        Method method = Spinner.class.getDeclaredMethod("onDetachedFromWindow");
                        method.setAccessible(true);
                        method.invoke(mSpinner);
                    } catch (Exception e) {
                        Timber.e("onClick(): Exception: %s.", e.getMessage());
                    }
                }


            }
        });

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
        TextView tvListThemeName;
        @Bind(R.id.btnEditThemeName)
        ImageButton btnEditThemeName;
        @Bind(R.id.llRowThemeName)
        LinearLayout llRowThemeName;


        public ListThemeViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}

