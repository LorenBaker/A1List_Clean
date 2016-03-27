package com.lbconsulting.a1list.presentation.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;

import com.lbconsulting.a1list.AndroidApplication;
import com.lbconsulting.a1list.R;
import com.lbconsulting.a1list.domain.executor.impl.ThreadExecutor;
import com.lbconsulting.a1list.domain.interactors.listItem.impl.SaveListItemsToCloud_InBackground;
import com.lbconsulting.a1list.domain.model.ListItem;
import com.lbconsulting.a1list.domain.repositories.ListItemRepository_Impl;
import com.lbconsulting.a1list.threading.MainThreadImpl;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;


/**
 * An ArrayAdapter for displaying a ListAttributes.
 */
public class FavoritesArrayAdapter extends ArrayAdapter<ListItem> implements SaveListItemsToCloud_InBackground.Callback{

    private final Context mContext;
    private final ListItemRepository_Impl mListItemRepository = AndroidApplication.getListItemRepository();

    public FavoritesArrayAdapter(Context context, List<ListItem> data) {
        super(context, 0);
        this.mContext = context;
        setData(data);
        Timber.i("FavoritesArrayAdapter() Initialized");
    }

    private void setData(List<ListItem> data) {
        if (data == null) {
            Timber.i("setData(): setData: data NULL");
        }
        clear();
        if (data != null) {
            addAll(data);
            Timber.i("setData(): Loaded %d ListItems.", data.size());
        }
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FavoritesViewHolder holder;

        // Get the data item for this position
        ListItem item = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_favorites, parent, false);
            holder = new FavoritesViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (FavoritesViewHolder) convertView.getTag();
        }

        // Populate the data into the template view using the data object
        if (item != null) {
            holder.ckItemName.setText(item.getName());
            boolean isInList = !item.isMarkedForDeletion();
            holder.ckItemName.setChecked(isInList);
            item.setChecked(isInList);
        }

        // save the item so it can be retrieved later
        holder.ckItemName.setTag(item);

        // Return the completed view to render on screen
        return convertView;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        Timber.i("notifyDataSetChanged()");
    }

    public void selectCheckedItems() {
        List<ListItem> listItems = new ArrayList<>();
        ListItem listItem;
        for (int i = 0; i < getCount(); i++) {
            listItem = getItem(i);
            listItem.setMarkedForDeletion(!listItem.isChecked());
            listItem.setChecked(false);
            listItems.add(listItem);
        }
        mListItemRepository.updateInLocalStorage(listItems);
        new SaveListItemsToCloud_InBackground(ThreadExecutor.getInstance(),
                MainThreadImpl.getInstance(),this,listItems).execute();
    }

    @Override
    public void onListItemListSavedToBackendless(String successMessage, List<ListItem> successfullySavedListItems) {
        Timber.i("onListItemListSavedToBackendless(): %s.", successMessage);
    }

    @Override
    public void onListItemListSaveToBackendlessFailed(String errorMessage, List<ListItem> successfullySavedListItems) {
        Timber.e("onListItemListSavedToBackendless(): %s.", errorMessage);
    }

    private class FavoritesViewHolder {
        public final CheckBox ckItemName;

        public FavoritesViewHolder(View base) {
            ckItemName = (CheckBox) base.findViewById(R.id.ckItemName);
            ckItemName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox checkBox = (CheckBox) v;
                    ListItem clickedItem = (ListItem) v.getTag();
                    if (clickedItem != null) {
                        clickedItem.setChecked(checkBox.isChecked());
                    }
                }
            });
        }
    }
}

