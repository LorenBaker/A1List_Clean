package com.alist.backendlessMessaging;


import com.alist.models.ListItem;
import com.google.gson.Gson;

import java.io.IOException;

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
        String listItemJsonString = "";

        try {
            ListItemMessage message = new ListItemMessage(listItem, action, target);
            Gson gson = new Gson();
            listItemJsonString = gson.toJson(message);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return listItemJsonString;
    }

//    public static ListItemMessage fromJason(String jsonString) {
//        Gson gson = new Gson();
//        return gson.fromJson(jsonString, ListItemMessage.class);
//    }

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
}
