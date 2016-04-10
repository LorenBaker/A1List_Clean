package com.lbconsulting.a1list.domain.interactors.appSettings;

import com.lbconsulting.a1list.domain.executor.Executor;
import com.lbconsulting.a1list.domain.executor.MainThread;
import com.lbconsulting.a1list.domain.interactors.base.AbstractInteractor;
import com.lbconsulting.a1list.utils.CommonMethods;

/**
 * An interactor that saves the provided AppSettings to Backendless.
 */
public class SaveDirtyObjectsToBackendless_InBackground extends AbstractInteractor {

    public SaveDirtyObjectsToBackendless_InBackground(Executor threadExecutor, MainThread mainThread) {
        super(threadExecutor, mainThread);
    }


    @Override
    public void run() {
        CommonMethods.saveDirtyObjectsToCloud();
    }

}
