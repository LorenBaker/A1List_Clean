package com.alist.events.persistence_service;

import com.alist.models.ListTheme;
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
 * ListThemeTableEventHandler handles events for all entities. This is accomplished
 * with the @Asset( "ListTheme" ) annotation.
 * The methods in the class correspond to the events selected in Backendless
 * Console.
 */

@Asset("ListTheme")
public class ListThemeTableEventHandler extends com.backendless.servercode.extension.PersistenceExtender<ListTheme> {
    @Override
    public void beforeCreate(RunnerContext context, ListTheme clientListTheme) throws Exception {
        validateClientListTheme(clientListTheme);
    }

    @Override
    public void afterCreate(RunnerContext context, ListTheme listTheme, ExecutionResult<ListTheme> result) throws Exception {
        // add your code here
    }

    @Override
    public void beforeUpdate(RunnerContext context, ListTheme clientListTheme) throws Exception {
        validateClientListTheme(clientListTheme);
    }

    @Override
    public void afterUpdate(RunnerContext context, ListTheme listTheme, ExecutionResult<ListTheme> result) throws Exception {
        // add your code here
    }

    private ListTheme getListThemeByUuid(String uuid) {

        ListTheme listTheme = null;

        BackendlessDataQuery dataQuery = new BackendlessDataQuery();
        String whereClause = String.format("uuid = '%s'", uuid);
        dataQuery.setWhereClause(whereClause);

        BackendlessCollection<ListTheme> listThemes = Backendless.Data.of(ListTheme.class).find(dataQuery);
        Iterator<ListTheme> iterator = listThemes.getCurrentPage().iterator();
        if (iterator.hasNext()) {
            listTheme = iterator.next();
        }

        return listTheme;
    }

    private void validateClientListTheme(ListTheme clientListTheme) {
        ListTheme cloudListTheme = getListThemeByUuid(clientListTheme.getUuid());
        if (cloudListTheme != null) {
            Date clientListThemeDate = clientListTheme.getUpdated();
            if (clientListThemeDate == null) {
                clientListThemeDate = clientListTheme.getCreated();
            }

            Date cloudListThemeDate = cloudListTheme.getUpdated();
            if (cloudListThemeDate == null) {
                cloudListThemeDate = cloudListTheme.getCreated();
            }

            if (clientListThemeDate.after(cloudListThemeDate)) {
                if (clientListTheme.getObjectId() == null) {
                    clientListTheme.setObjectId(cloudListTheme.getObjectId());
                }
            } else {
                updateClient(cloudListTheme);
                // Throw an exception and should stop further execution of the API request
                throw new EventException((short) 0, "Attempted to overwrite a newer ListTheme");
            }
        }
    }

    private void updateClient(ListTheme cloudListTheme) {
        // TODO: Send the newer cloudListTheme to the client that sent the older clientListTheme
    }

}
        