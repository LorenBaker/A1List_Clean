package com.lbconsulting.a1list.backendlessMessaging;

import com.backendless.Backendless;
import com.backendless.messaging.MessageStatus;
import com.google.gson.Gson;
import com.lbconsulting.a1list.domain.model.ListItem;
import com.lbconsulting.a1list.utils.MySettings;

import timber.log.Timber;

/**
 * This class holds the message payload for actions associated with ListItems
 */
public class ListItemMessage {


    ListItem listItem;
    int action;
    int target;

    public ListItemMessage() {

    }

    public ListItemMessage(ListItem listItem, int action, int target) {
        this.listItem = listItem;
        this.action = action;
        this.target = target;
    }

    public static String toJson(ListItem listItem, int action, int target) {
        ListItemMessage message = new ListItemMessage(listItem, action, target);
        Gson gson = new Gson();
        return gson.toJson(message);
    }

    public static ListItemMessage fromJason(String jsonString) {
        Gson gson = new Gson();
        return gson.fromJson(jsonString, ListItemMessage.class);
    }

    public ListItem getListItem() {
        return listItem;
    }

    public void setListItem(ListItem listItem) {
        this.listItem = listItem;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public int getTarget() {
        return target;
    }

    public void setTarget(int target) {
        this.target = target;
    }

    public static void sendMessage(ListItem listItem, int action) {
        String messageChannel = MySettings.getActiveUserID();

        int target = Messaging.TARGET_ALL_DEVICES;
        String listItemMessageJson = ListItemMessage.toJson(listItem, action, target);
        MessageStatus messageStatus = Backendless.Messaging.publish(messageChannel, listItemMessageJson);
        if (messageStatus.getErrorMessage() == null) {
            // successfully sent message to Backendless.
            switch (action){
                case Messaging.ACTION_CREATE:
                    Timber.i("sendMessage(): CREATE \"%s\" message successfully sent.", listItem.getName());
                    break;

                case Messaging.ACTION_UPDATE:
                    Timber.i("sendMessage(): UPDATE \"%s\" message successfully sent.", listItem.getName());
                    break;

                case Messaging.ACTION_DELETE:
                    Timber.i("sendMessage(): DELETE \"%s\" message successfully sent.", listItem.getName());
                    break;

            }

        } else {
            // error sending message to Backendless.
            Timber.e("sendMessage(): FAILED to send message for \"%s\". %s.",
                    listItem.getName(), messageStatus.getErrorMessage());
        }
    }






}
