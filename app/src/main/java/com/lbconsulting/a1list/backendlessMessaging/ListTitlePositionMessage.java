package com.lbconsulting.a1list.backendlessMessaging;


import com.google.gson.Gson;
import com.lbconsulting.a1list.domain.model.ListTitlePosition;

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
}
