package com.alist.events.persistence_service;

import com.alist.models.ListTitle;
import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.persistence.BackendlessDataQuery;
import com.backendless.servercode.ExecutionResult;
import com.backendless.servercode.RunnerContext;
import com.backendless.servercode.annotation.Asset;
import org.w3c.dom.events.EventException;

import java.util.Date;
import java.util.Iterator;

/**
 * ListTitleTableEventHandler handles events for all entities. This is accomplished
 * with the @Asset( "ListTitle" ) annotation.
 * The methods in the class correspond to the events selected in Backendless
 * Console.
 */

@Asset("ListTitle")
public class ListTitleTableEventHandler extends com.backendless.servercode.extension.PersistenceExtender<ListTitle> {

    @Override
    public void beforeCreate(RunnerContext context, ListTitle clientListTitle) throws Exception {
        validateClientListTitle(clientListTitle);
    }

    @Override
    public void afterCreate(RunnerContext context, ListTitle listTitle, ExecutionResult<ListTitle> result) throws Exception {
        // add your code here
    }

    @Override
    public void beforeUpdate(RunnerContext context, ListTitle clientListTitle) throws Exception {
        validateClientListTitle(clientListTitle);
    }

    @Override
    public void afterUpdate(RunnerContext context, ListTitle listTitle, ExecutionResult<ListTitle> result) throws Exception {
        // add your code here
    }

    private ListTitle getListTitleByUuid(String uuid) {

        ListTitle listTitle = null;

        BackendlessDataQuery dataQuery = new BackendlessDataQuery();
        String whereClause = String.format("uuid = '%s'", uuid);
        dataQuery.setWhereClause(whereClause);

        BackendlessCollection<ListTitle> listTitles = Backendless.Data.of(ListTitle.class).find(dataQuery);
        Iterator<ListTitle> iterator = listTitles.getCurrentPage().iterator();
        if (iterator.hasNext()) {
            listTitle = iterator.next();
        }

        return listTitle;
    }

    private void validateClientListTitle(ListTitle clientListTitle) {
        ListTitle cloudListTitle = getListTitleByUuid(clientListTitle.getUuid());
        if (cloudListTitle != null) {
            Date clientListTitleDate = clientListTitle.getUpdated();
            if (clientListTitleDate == null) {
                clientListTitleDate = clientListTitle.getCreated();
            }

            Date cloudListTitleDate = cloudListTitle.getUpdated();
            if (cloudListTitleDate == null) {
                cloudListTitleDate = cloudListTitle.getCreated();
            }

            if (clientListTitleDate.after(cloudListTitleDate)) {
                if (clientListTitle.getObjectId() == null) {
                    clientListTitle.setObjectId(cloudListTitle.getObjectId());
                }
            } else {
                updateClient(cloudListTitle);
                // Throw an exception and should stop further execution of the API request
                throw new EventException((short) 0, "Attempted to overwrite a newer ListTitle");
            }
        }
    }

    private void updateClient(ListTitle cloudListTitle) {
        // TODO: Send the newer cloudListTitle to the client that sent the older clientListTitle
    }
}
        