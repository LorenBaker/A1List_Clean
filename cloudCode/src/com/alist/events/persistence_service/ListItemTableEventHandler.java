package com.alist.events.persistence_service;

import com.alist.backendlessMessaging.ListItemMessage;
import com.alist.backendlessMessaging.Messaging;
import com.alist.models.ListItem;
import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.commons.exception.ExceptionWrapper;
import com.backendless.messaging.MessageStatus;
import com.backendless.persistence.BackendlessDataQuery;
import com.backendless.servercode.ExecutionResult;
import com.backendless.servercode.RunnerContext;
import com.backendless.servercode.annotation.Asset;
import org.w3c.dom.events.EventException;

import java.util.Date;
import java.util.Iterator;

/**
 * ListItemTableEventHandler handles events for all entities. This is accomplished
 * with the @Asset( "ListItem" ) annotation.
 * The methods in the class correspond to the events selected in Backendless
 * Console.
 */

@Asset("ListItem")
public class ListItemTableEventHandler extends com.backendless.servercode.extension.PersistenceExtender<ListItem> {

    @Override
    public void beforeCreate(RunnerContext context, ListItem clientListItem) throws Exception {
        validateClientListItem(clientListItem);
    }

    @Override
    public void afterCreate(RunnerContext context, ListItem listitem, ExecutionResult<ListItem> result) throws Exception {
        // add your code here
    }

    @Override
    public void beforeUpdate(RunnerContext context, ListItem clientListItem) throws Exception {
        validateClientListItem(clientListItem);
    }

    @Override
    public void afterUpdate(RunnerContext context, ListItem listitem, ExecutionResult<ListItem> result) throws Exception {
        try {
            int temp=0;
        } catch (Exception e) {
            e.printStackTrace();
        }
//        ExceptionWrapper exception = result.getException();
//        if (exception != null) {
//            ListItem updatedListItem = result.getResult();
//            if(updatedListItem!=null) {
//                sendListItemUpdateMessage(updatedListItem);
//                String temp = "Code = 0; exceptionClass = class com.backendless.exceptions.persistence.InvalidEntityException; exceptionMessage = Duplicate property:id";
//            }
//        }
    }

    private void sendListItemUpdateMessage(ListItem updatedListItem) {
        String messageChannel = updatedListItem.getMessageChannel();
        int action = Messaging.ACTION_UPDATE;
        int target = Messaging.TARGET_ALL_DEVICES;
        String listItemMessageJson = ListItemMessage.toJson(updatedListItem, action, target);
        MessageStatus messageStatus = Backendless.Messaging.publish(messageChannel, listItemMessageJson);
        int temp = 0;
    }

    private ListItem getListItemByUuid(String uuid) {

        ListItem listItem = null;

        BackendlessDataQuery dataQuery = new BackendlessDataQuery();
        String whereClause = String.format("uuid = '%s'", uuid);
        dataQuery.setWhereClause(whereClause);

        BackendlessCollection<ListItem> listItems = Backendless.Data.of(ListItem.class).find(dataQuery);
        Iterator<ListItem> iterator = listItems.getCurrentPage().iterator();
        int count = listItems.getTotalObjects();
        if (iterator.hasNext()) {
            listItem = iterator.next();
        }

        return listItem;
    }

    private void validateClientListItem(ListItem clientListItem) {

        ListItem cloudListItem = getListItemByUuid(clientListItem.getUuid());
        if (cloudListItem != null) {
            Date clientListItemDate = clientListItem.getUpdated();
            if (clientListItemDate == null) {
                clientListItemDate = clientListItem.getCreated();
            }

            Date cloudListItemDate = cloudListItem.getUpdated();
            if (cloudListItemDate == null) {
                cloudListItemDate = cloudListItem.getCreated();
            }

            if (clientListItemDate.after(cloudListItemDate)) {
                if (clientListItem.getObjectId() == null) {
                    clientListItem.setObjectId(cloudListItem.getObjectId());
                }
            } else {
                updateClient(cloudListItem);
                // Throw an exception and should stop further execution of the API request
                throw new EventException((short) 0, "Attempted to overwrite a newer ListItem");
            }
        }
    }

    private void updateClient(ListItem cloudListItem) {
        // TODO: Send the newer cloudListItem to the client that sent the older clientListItem
    }
}
        