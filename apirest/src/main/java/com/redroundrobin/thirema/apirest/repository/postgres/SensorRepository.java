package com.redroundrobin.thirema.apirest.repository.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.Alert;
import com.redroundrobin.thirema.apirest.models.postgres.Device;
import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.models.postgres.ViewGraph;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SensorRepository extends CrudRepository<Sensor, Integer> {
  Iterable<Sensor>  findAllByDevice(Device device);

  Iterable<Sensor> findAllByEntities(Entity entity);

  Iterable<Sensor> findAllByViewGraphs1OrViewGraphs2(ViewGraph viewGraph1, ViewGraph viewGraph2);

  Iterable<Sensor> findAllByDeviceAndEntities(Device device, Entity entity);

  Sensor findByAlerts(Alert alert);

  Sensor findByDeviceAndRealSensorId(Device device, int realSensorId);

  Sensor findByDeviceAndRealSensorIdAndEntities(Device device, int realSensorId, Entity entity);
}