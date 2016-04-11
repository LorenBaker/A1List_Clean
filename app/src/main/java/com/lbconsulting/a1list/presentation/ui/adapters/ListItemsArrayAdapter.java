package com.lbconsulting.a1list.presentation.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
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

import com.lbconsulting.a1list.AndroidApplication;
import com.lbconsulting.a1list.R;
import com.lbconsulting.a1list.domain.model.ListItem;
import com.lbconsulting.a1list.domain.model.ListTheme;
import com.lbconsulting.a1list.domain.model.ListTitle;
import com.lbconsulting.a1list.domain.repositories.ListItemRepository_Impl;
import com.lbconsulting.a1list.utils.CommonMethods;
import com.lbconsulting.a1list.utils.MyEvents;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

//import com.nhaarman.listviewanimations.util.Swappable;


/**
 * An ArrayAdapter for displaying a ListItems.
 */
//implements Swappable
public class ListItemsArrayAdapter extends ArrayAdapter<ListItem> {

    private final Context mContext;
    private final ListView mListView;
    private final String mListName;
    private ListTheme mListTheme;
    private ListTitle mListTitle;
    private ListItemRepository_Impl mListItemRepository;

    public ListItemsArrayAdapter(Context context, ListView listView, ListTitle listTitle) {
        super(context, 0);
        this.mContext = context;
        this.mListView = listView;
        this.mListTitle = listTitle;
        this.mListName = listTitle.getName();
        mListItemRepository = AndroidApplication.getListItemRepository();
        Timber.i("ListItemsArrayAdapter() initialized for List: \"%s\".", mListName);
    }

    public void setData(List<ListItem> data, ListTheme listTheme) {
        if (data == null) {
            Timber.i("setData(): data NULL for ListTitle \"%s\".", mListName);
        }
        mListTheme = listTheme;
        clear();
        if (data != null) {
            addAll(data);
            Timber.i("setData(): Loaded %d ListItems for ListTitle \"%s\".", data.size(), mListName);
        }
    }

    public List<ListItem> getListItems() {
        List<ListItem> listItems = new ArrayList<>();
        for (int i = 0; i < getCount(); i++) {
            ListItem listItem = getItem(i);
            listItems.add(listItem);
        }
        return listItems;
    }

    @Override
    public long getItemId(int position) {
        ListItem listItem = getItem(position);
        return listItem.getId();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public int getPosition(ListItem soughtItem) {
        return getItemPosition(soughtItem.getUuid());
    }

    public int getItemPosition(String soughtItemUuid) {
        int position;
        boolean found = false;

        ListItem item;
        for (position = 0; position < getCount(); position++) {
            item = getItem(position);
            if (item.getUuid().equals(soughtItemUuid)) {
                found = true;
                break;
            }
        }

        if (!found) {
            position = 0;
        }
        return position;
    }


    public void setListTheme(ListTheme listTheme) {
        mListTheme = listTheme;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ListItemViewHolder holder;

        // Get the data item for this position
        ListItem item = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_list_item, parent, false);
            holder = new ListItemViewHolder(convertView);

            if (mListTheme != null) {
                holder.tvListItemName.setTextSize(TypedValue.COMPLEX_UNIT_SP, mListTheme.getTextSize());
                holder.tvListItemName.setTextColor(mListTheme.getTextColor());

                int horizontalPadding = CommonMethods.convertDpToPixel(mListTheme.getHorizontalPaddingInDp());
                int verticalPadding = CommonMethods.convertDpToPixel(mListTheme.getVerticalPaddingInDp());
                holder.tvListItemName.setPadding(horizontalPadding, verticalPadding,
                        horizontalPadding, verticalPadding);

                if (mListTheme.isTransparent()) {
                    holder.llRowItemName.setBackgroundColor(Color.TRANSPARENT);
                    mListView.setDivider(null);
                    mListView.setDividerHeight(0);
                } else {
                    Drawable backgroundDrawable = CommonMethods.getBackgroundDrawable(
                            mListTheme.getStartColor(), mListTheme.getEndColor());
                    holder.llRowItemName.setBackground(backgroundDrawable);
                    mListView.setDivider(new ColorDrawable(ContextCompat.getColor(mContext, R.color.greyLight3_50Transparent)));
                    mListView.setDividerHeight(1);
                }

            }
            convertView.setTag(holder);
        } else {
            holder = (ListItemViewHolder) convertView.getTag();
        }

        // Populate the data into the template view using the data object
        holder.tvListItemName.setText(item.getName());
        if (item.isStruckOut()) {
            setStrikeOut(holder.tvListItemName);
        } else {
            setNoStrikeOut(holder.tvListItemName);
        }

        if (item.isFavorite()) {
            setAsFavorite(holder.btnFavorite);
        } else {
            setAsNotFavorite(holder.btnFavorite);
        }

        holder.tvListItemName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListItem clickedItem = (ListItem) v.getTag();
                if (clickedItem != null) {
                    // get the most recent ListItem from the repository to make sure that there is an Backendless objectId.
                    clickedItem = mListItemRepository.retrieveListItemByUuid(clickedItem.getUuid());
                    clickedItem.setStruckOut(!clickedItem.isStruckOut());
                    if (clickedItem.isStruckOut()) {
                        setStrikeOut((TextView) v);
                    } else {
                        setNoStrikeOut((TextView) v);
                    }
                    mListItemRepository.update(clickedItem);
                }
            }
        });

        holder.btnFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListItem clickedItem = (ListItem) v.getTag();
                if (clickedItem != null) {
                    clickedItem.setFavorite(!clickedItem.isFavorite());
                    if (clickedItem.isFavorite()) {
                        setAsFavorite((ImageButton) v);
                    } else {
                        setAsNotFavorite((ImageButton) v);
                    }
                    mListItemRepository.update(clickedItem);
                }
            }
        });

        holder.btnEditItemName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListItem selectedListItem = (ListItem) v.getTag();
                EventBus.getDefault().post(new MyEvents.showEditListItemDialog(selectedListItem));
            }
        });

        // save the item so it can be retrieved later
        holder.tvListItemName.setTag(item);
        holder.btnFavorite.setTag(item);
        holder.btnEditItemName.setTag(item);

        // Return the completed view to render on screen
        return convertView;
    }

    private void toggleStrikeout() {

    }

    private void setAsFavorite(ImageButton btnFavorite) {
        btnFavorite.setImageResource(R.drawable.ic_favorite_black);
        btnFavorite.setAlpha(1f);
    }

    private void setAsNotFavorite(ImageButton btnFavorite) {
        btnFavorite.setImageResource(R.drawable.ic_favorite_border_black);
        btnFavorite.setAlpha(0.50f);
    }


    private void setStrikeOut(TextView tv) {
        if (mListTheme.isBold()) {
            tv.setTypeface(null, Typeface.BOLD_ITALIC);
        } else {
            tv.setTypeface(null, Typeface.ITALIC);
        }
        tv.setTextColor(ContextCompat.getColor(mContext, R.color.crimson));
        tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
    }

    private void setNoStrikeOut(TextView tv) {
        if (mListTheme.isBold()) {
            tv.setTypeface(null, Typeface.BOLD);
        } else {
            tv.setTypeface(null, Typeface.NORMAL);
        }
        tv.setTextColor(mListTheme.getTextColor());
        tv.setPaintFlags(tv.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
    }

//    @Override
//    public void swapItems(int positionOne, int positionTwo) {
//        if (mListTitle.isSortListItemsAlphabetically()) {
//            return;
//        }
//        ListItem itemOne = getItem(positionOne);
//        ListItem itemTwo = getItem(positionTwo);
//
//        long origItemOneSortKey = itemOne.getManualSortKey();
//        long origItemTwoSortKey = itemTwo.getManualSortKey();
//
//        itemOne.setManualSortKey(origItemTwoSortKey);
//        itemTwo.setManualSortKey(origItemOneSortKey);
//
//        EventBus.getDefault().post(new MyEvents.updateFragListItemsUI(mListTitle.getUuid()));
//    }


    private class ListItemViewHolder {
        // TODO: Use Butter knife
        public final LinearLayout llRowItemName;
        public final TextView tvListItemName;
        public final ImageButton btnFavorite;
        public final ImageButton btnEditItemName;

        public ListItemViewHolder(View base) {
            llRowItemName = (LinearLayout) base.findViewById(R.id.llRowItemName);
            tvListItemName = (TextView) base.findViewById(R.id.tvItemName);
            btnFavorite = (ImageButton) base.findViewById(R.id.btnFavorite);
            btnEditItemName = (ImageButton) base.findViewById(R.id.btnEditItemName);
        }
    }
}

