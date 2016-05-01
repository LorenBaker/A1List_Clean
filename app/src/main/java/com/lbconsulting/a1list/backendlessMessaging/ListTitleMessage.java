package com.lbconsulting.a1list.backendlessMessaging;


import com.backendless.Backendless;
import com.backendless.messaging.MessageStatus;
import com.google.gson.Gson;
import com.lbconsulting.a1list.domain.model.ListTitle;
import com.lbconsulting.a1list.utils.MySettings;

import timber.log.Timber;

/**
 * This class holds the message payload for actions associated with ListTitles
 */
public class ListTitleMessage {


    ListTitle listTitle;
    int action;
    int target;

    public ListTitleMessage() {

    }

    public ListTitleMessage(ListTitle listTitle, int action, int target) {
        this.listTitle = listTitle;
        this.action = action;
        this.target = target;
    }

    public static String toJson(ListTitle listTitle, int action, int target) {
        String listTitleJsonString = "";

        try {
            ListTitleMessage message = new ListTitleMessage(listTitle, action, target);
            Gson gson = new Gson();
            listTitleJsonString = gson.toJson(message);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return listTitleJsonString;
    }

    public static ListTitleMessage fromJason(String jsonString) {
        Gson gson = new Gson();
        return gson.fromJson(jsonString, ListTitleMessage.class);
    }

    public ListTitle getListTitle() {
        return listTitle;
    }

    public void setListTitle(ListTitle listTitle) {
        this.listTitle = listTitle;
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

    public static void sendMessage(ListTitle listTitle, int action) {
        String messageChannel = MySettings.getActiveUserID();

        int target = Messaging.TARGET_ALL_DEVICES;
        String listTitleMessageJson = ListTitleMessage.toJson(listTitle, action, target);
        MessageStatus messageStatus = Backendless.Messaging.publish(messageChannel, listTitleMessageJson);
        if (messageStatus.getErrorMessage() == null) {
            // successfully sent message to Backendless.
            switch (action){
                case Messaging.ACTION_CREATE:
                    Timber.i("sendMessage(): CREATE \"%s\" message successfully sent.", listTitle.getName());
                    break;

                case Messaging.ACTION_UPDATE:
                    Timber.i("sendMessage(): UPDATE \"%s\" message successfully sent.", listTitle.getName());
                    break;

                case Messaging.ACTION_DELETE:
                    Timber.i("sendMessage(): DELETE \"%s\" message successfully sent.", listTitle.getName());
                    break;
            }
        } else {
            // error sending message to Backendless.
            Timber.e("sendMessage(): FAILED to send message for \"%s\". %s.",
                    listTitle.getName(), messageStatus.getErrorMessage());
        }
    }
}
