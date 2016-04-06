package com.lbconsulting.a1list.utils;

/**
 * Used to record various statistics about the result of a sync operation.
 */
public class SyncStats {

    public int numNetworkNotAvailableExceptions;

    public int numAppSettingsBackendlessExceptions;
    public int numListThemeBackendlessExceptions;
    public int numListTitleBackendlessExceptions;
    public int numListItemBackendlessExceptions;

    public int numAppSettingsDownloadExceptions;
    public int numListThemeDownloadExceptions;
    public int numListTitleDownloadExceptions;
    public int numListItemDownloadExceptions;

    public int numAppSettingsConflictsDetected;
    public int numListThemeConflictsDetected;
    public int numListTitleConflictsDetected;
    public int numListItemConflictsDetected;



    public int numAppSettingsUpdates;
    public int numListThemeUpdates;
    public int numListTitleUpdates;
    public int numListItemUpdates;

    public int numAppSettingsDeletes;
    public int numListThemeDeletes;
    public int numListTitleDeletes;
    public int numListItemDeletes;

    public int numAppSettingsInserts;
    public int numListThemeInserts;
    public int numListTitleInserts;
    public int numListItemInserts;


    public int numAppSettingsNoUpdateRequired;
    public int numListThemeNoUpdateRequired;
    public int numListTitleNoUpdateRequired;
    public int numListItemNoUpdateRequired;

    public int numAppSettingsSkipped;
    public int numListThemeSkipped;
    public int numListTitleSkipped;
    public int numListItemSkipped;


    public SyncStats() {
        clear();
    }

    //region Stats Reporting

    public String getAllStats() {
        String results = networkNotAvailableExceptions();
        results = results + appSettingsStats() + "\n" + listThemeStats() + "\n" + listTitleStats() + "\n" + listItemStats();
        return results;
    }

    public String networkNotAvailableExceptions() {
        String results = "";
        if (numNetworkNotAvailableExceptions > 0)
            results = String.format(" Network stats [ numNetworkNotAvailableExceptions: %d", numAppSettingsBackendlessExceptions);
        return results;
    }

    public String appSettingsStats() {
        StringBuilder sb = new StringBuilder();
        sb.append(" AppSettings stats [");

        if (numAppSettingsBackendlessExceptions > 0)
            sb.append(" numAppSettingsBackendlessExceptions: ").append(numAppSettingsBackendlessExceptions);

        if (numAppSettingsDownloadExceptions > 0)
            sb.append(" numAppSettingsDownloadExceptions: ").append(numAppSettingsDownloadExceptions);

        if (numAppSettingsConflictsDetected > 0)
            sb.append(" numAppSettingsConflictsDetected: ").append(numAppSettingsConflictsDetected);

        if (numAppSettingsInserts > 0)
            sb.append(" numAppSettingsInserts: ").append(numAppSettingsInserts);

        if (numAppSettingsUpdates > 0)
            sb.append(" numAppSettingsUpdates: ").append(numAppSettingsUpdates);

        if (numAppSettingsDeletes > 0)
            sb.append(" numAppSettingsDeletes: ").append(numAppSettingsDeletes);

        if (numAppSettingsNoUpdateRequired > 0)
            sb.append(" numAppSettingsNoUpdateRequired: ").append(numAppSettingsNoUpdateRequired);

        if (numAppSettingsSkipped > 0)
            sb.append(" numAppSettingsSkipped: ").append(numAppSettingsSkipped);


        sb.append("]");
        return sb.toString();
    }

    public String listThemeStats() {
        StringBuilder sb = new StringBuilder();
        sb.append(" ListTheme stats [");

        if (numListThemeBackendlessExceptions > 0)
            sb.append(" numListThemeBackendlessExceptions: ").append(numListThemeBackendlessExceptions);

        if (numListThemeDownloadExceptions > 0)
            sb.append(" numListThemeDownloadExceptions: ").append(numListThemeDownloadExceptions);

        if (numListThemeConflictsDetected > 0)
            sb.append(" numListThemeConflictsDetected: ").append(numListThemeConflictsDetected);

        if (numListThemeInserts > 0)
            sb.append(" numListThemeInserts: ").append(numListThemeInserts);

        if (numListThemeUpdates > 0)
            sb.append(" numListThemeUpdates: ").append(numListThemeUpdates);

        if (numListThemeDeletes > 0)
            sb.append(" numListThemeDeletes: ").append(numListThemeDeletes);

        if (numListThemeNoUpdateRequired > 0)
            sb.append(" numListThemeNoUpdateRequired: ").append(numListThemeNoUpdateRequired);

        if (numListThemeSkipped > 0)
            sb.append(" numListThemeSkipped: ").append(numListThemeSkipped);


        sb.append("]");
        return sb.toString();
    }

    public String listTitleStats() {
        StringBuilder sb = new StringBuilder();
        sb.append(" ListTitle stats [");

        if (numListTitleBackendlessExceptions > 0)
            sb.append(" numListTitleBackendlessExceptions: ").append(numListTitleBackendlessExceptions);

        if (numListTitleDownloadExceptions > 0)
            sb.append(" numListTitleDownloadExceptions: ").append(numListTitleDownloadExceptions);

        if (numListTitleConflictsDetected > 0)
            sb.append(" numListTitleConflictsDetected: ").append(numListTitleConflictsDetected);

        if (numListTitleInserts > 0)
            sb.append(" numListTitleInserts: ").append(numListTitleInserts);

        if (numListTitleUpdates > 0)
            sb.append(" numListTitleUpdates: ").append(numListTitleUpdates);

        if (numListTitleDeletes > 0)
            sb.append(" numListTitleDeletes: ").append(numListTitleDeletes);

        if (numListTitleNoUpdateRequired > 0)
            sb.append(" numListTitleNoUpdateRequired: ").append(numListTitleNoUpdateRequired);

        if (numListTitleSkipped > 0)
            sb.append(" numListTitleSkipped: ").append(numListTitleSkipped);


        sb.append("]");
        return sb.toString();
    }


    public String listItemStats() {
        StringBuilder sb = new StringBuilder();
        sb.append(" ListItem stats [");

        if (numListItemBackendlessExceptions > 0)
            sb.append(" numListItemBackendlessExceptions: ").append(numListItemBackendlessExceptions);

        if (numListItemDownloadExceptions > 0)
            sb.append(" numListItemDownloadExceptions: ").append(numListItemDownloadExceptions);

        if (numListItemConflictsDetected > 0)
            sb.append(" numListItemConflictsDetected: ").append(numListItemConflictsDetected);

        if (numListItemInserts > 0)
            sb.append(" numListItemInserts: ").append(numListItemInserts);

        if (numListItemUpdates > 0)
            sb.append(" numListItemUpdates: ").append(numListItemUpdates);

        if (numListItemDeletes > 0)
            sb.append(" numListItemDeletes: ").append(numListItemDeletes);

        if (numListItemNoUpdateRequired > 0)
            sb.append(" numListItemNoUpdateRequired: ").append(numListItemNoUpdateRequired);

        if (numListItemSkipped > 0)
            sb.append(" numListItemSkipped: ").append(numListItemSkipped);


        sb.append("]");
        return sb.toString();
    }

    @Override
    public String toString() {
        return getAllStats();
    }
    //endregion

    /**
     * Reset all the counters to 0.
     */
    public void clear() {
        numNetworkNotAvailableExceptions = 0;

        numAppSettingsBackendlessExceptions = 0;
        numListThemeBackendlessExceptions = 0;
        numListTitleBackendlessExceptions = 0;
        numListItemBackendlessExceptions = 0;

        numAppSettingsDownloadExceptions = 0;
        numListThemeDownloadExceptions = 0;
        numListTitleDownloadExceptions = 0;
        numListItemDownloadExceptions = 0;

        numAppSettingsConflictsDetected = 0;
        numListThemeConflictsDetected = 0;
        numListTitleConflictsDetected = 0;
        numListItemConflictsDetected = 0;

        numAppSettingsInserts = 0;
        numListThemeInserts = 0;
        numListTitleInserts = 0;
        numListItemInserts = 0;

        numAppSettingsUpdates = 0;
        numListThemeUpdates = 0;
        numListTitleUpdates = 0;
        numListItemUpdates = 0;

        numAppSettingsDeletes = 0;
        numListThemeDeletes = 0;
        numListTitleDeletes = 0;
        numListItemDeletes = 0;

        numAppSettingsNoUpdateRequired = 0;
        numListThemeNoUpdateRequired = 0;
        numListTitleNoUpdateRequired = 0;
        numListItemNoUpdateRequired = 0;

        numAppSettingsSkipped = 0;
        numListThemeSkipped = 0;
        numListTitleSkipped = 0;
        numListItemSkipped = 0;
    }

}