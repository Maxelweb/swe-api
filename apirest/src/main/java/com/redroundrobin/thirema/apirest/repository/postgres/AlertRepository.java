package com.redroundrobin.thirema.apirest.repository.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.Alert;
import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertRepository extends CrudRepository<Alert, Integer> {
  Iterable<Alert> findAllByEntity(Entity entity);

  Iterable<Alert> findAllBySensor(Sensor sensor);

  @Query("SELECT A FROM User U JOIN U.disabledAlerts A WHERE U = :user")
  Iterable<Alert> findAllByUsers(User user);
}
