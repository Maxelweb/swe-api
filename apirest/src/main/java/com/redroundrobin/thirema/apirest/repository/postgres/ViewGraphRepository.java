package com.redroundrobin.thirema.apirest.repository.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.models.postgres.View;
import com.redroundrobin.thirema.apirest.models.postgres.ViewGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ViewGraphRepository extends CrudRepository<ViewGraph, Integer> {

  Iterable<ViewGraph> findAllBySensor1OrSensor2(Sensor sensor1, Sensor sensor2);

  Iterable<ViewGraph> findAllByView(View view);

  @Query("SELECT VG FROM ViewGraph VG JOIN VG.view V JOIN V.userId U WHERE U.userId = :userId")
  Iterable<ViewGraph> findAllByUserId(int userId);

  @Query("SELECT VG FROM ViewGraph VG JOIN VG.view V JOIN V.userId U "
      + "WHERE U.userId = :userId AND V.viewId = :viewId")
  Iterable<ViewGraph> findAllByUserIdAndViewId(int userId, int viewId);

  @Query("SELECT VG FROM ViewGraph VG JOIN VG.view V JOIN V.userId U "
      + "WHERE VG.graphId = :id AND U.userId = :userId")
  ViewGraph findByIdAndUserId(int id, int userId);
}
