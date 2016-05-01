package com.lbconsulting.a1list.backendlessMessaging;


import com.backendless.Backendless;
import com.backendless.messaging.MessageStatus;
import com.google.gson.Gson;
import com.lbconsulting.a1list.domain.model.ListTitle;
import com.lbconsulting.a1list.domain.model.ListTitlePosition;
import com.lbconsulting.a1list.utils.MySettings;

import timber.log.Timber;

/**
 * This class holds the message payload for actions associated with ListTitlePositions
 */
public class ListTitlePositionMessage {

    ListTitlePosition listTitlePosition;
    int action;
    int target;

    public ListTitlePositionMessage() {
    }

    public ListTitlePositionMessage(ListTitlePosition listTitlePosition, int action, int target) {
        this.listTitlePosition = listTitlePosition;
        this.action = action;
        this.target = target;
    }

    public static String toJson(ListTitlePosition listTitlePosition, int action, int target) {
        String listTitlePositionJsonString = "";

        try {
            ListTitlePositionMessage message = new ListTitlePositionMessage(listTitlePosition, action, target);
            Gson gson = new Gson();
            listTitlePositionJsonString = gson.toJson(message);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return listTitlePositionJsonString;
    }

    public static ListTitlePositionMessage fromJason(String jsonString) {
        Gson gson = new Gson();
        return gson.fromJson(jsonString, ListTitlePositionMessage.class);
    }

    public ListTitlePosition getListTitlePosition() {
        return listTitlePosition;
    }

    public void setListTitlePosition(ListTitlePosition listTitlePosition) {
        this.listTitlePosition = listTitlePosition;
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


    public static void sendMessage(ListTitle listTitle, ListTitlePosition listTitlePosition, int action) {
        String messageChannel = MySettings.getActiveUserID();

        int target = Messaging.TARGET_ALL_DEVICES;
        String listTitleMessageJson = ListTitlePositionMessage.toJson(listTitlePosition, action, target);
        MessageStatus messageStatus = Backendless.Messaging.publish(messageChannel, listTitleMessageJson);
        if (messageStatus.getErrorMessage() == null) {
            // successfully sent message to Backendless.

            switch (action){
                case Messaging.ACTION_CREATE:
                    Timber.i("sendMessage(): CREATE \"%s's\" ListTitlePosition message successfully sent.", listTitle.getName());
                    break;

                case Messaging.ACTION_UPDATE:
                    Timber.i("sendMessage(): UPDATE \"%s's\" ListTitlePosition message successfully sent.", listTitle.getName());
                    break;

                case Messaging.ACTION_DELETE:
                    Timber.i("sendMessage(): DELETE \"%s's\" ListTitlePosition message successfully sent.", listTitle.getName());
                    break;
            }

        } else {
            // error sending message to Backendless.
            Timber.e("sendMessage(): FAILED to send message for \"%s's\" ListTitlePosition. %s.",
                    listTitle.getName(), messageStatus.getErrorMessage());
        }
    }
}
