package com.lbconsulting.a1list.backendlessMessaging;

import com.google.gson.Gson;
import com.lbconsulting.a1list.domain.model.ListItem;

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
}
