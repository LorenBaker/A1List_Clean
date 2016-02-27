package com.lbconsulting.a1list.domain.repositories;

import com.lbconsulting.a1list.domain.model.SampleModel;

/**
 * A sample repositories with CRUD operations on a model.
 */
public interface Repository {

    boolean insert(SampleModel model);

    boolean update(SampleModel model);

    SampleModel get(Object id);

    boolean delete(SampleModel model);
}
