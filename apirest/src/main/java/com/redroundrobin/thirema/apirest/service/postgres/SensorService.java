package com.redroundrobin.thirema.apirest.service.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.Alert;
import com.redroundrobin.thirema.apirest.models.postgres.Device;
import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.models.postgres.ViewGraph;
import com.redroundrobin.thirema.apirest.repository.postgres.SensorRepository;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SensorService {

  private SensorRepository repo;

  private AlertService alertService;

  private DeviceService deviceService;

  private EntityService entityService;

  private ViewGraphService viewGraphService;

  @Autowired
  public SensorService(SensorRepository repo) {
    this.repo = repo;
  }

  @Autowired
  public void setAlertService(AlertService alertService) {
    this.alertService = alertService;
  }

  @Autowired
  public void setDeviceService(DeviceService deviceService) {
    this.deviceService = deviceService;
  }

  @Autowired
  public void setEntityService(EntityService entityService) {
    this.entityService = entityService;
  }

  @Autowired
  public void setViewGraphService(ViewGraphService viewGraphService) {
    this.viewGraphService = viewGraphService;
  }

  public List<Sensor> findAll() {
    return (List<Sensor>) repo.findAll();
  }

  public List<Sensor> findAllByDeviceId(int deviceId) {
    Device device = deviceService.findById(deviceId);
    if (device != null) {
      return (List<Sensor>) repo.findAllByDevice(device);
    } else {
      return null;
    }
  }

  public List<Sensor> findAllByEntityId(int entityId) {
    Entity entity = entityService.findById(entityId);
    if (entity != null) {
      return (List<Sensor>) repo.findAllByEntities(entity);
    } else {
      return Collections.emptyList();
    }
  }

  public Sensor findById(int sensorId) {
    return repo.findById(sensorId).orElse(null);
  }

  public Sensor findByAlertId(int alertId) {
    Alert alert = alertService.findById(alertId);
    if (alert != null) {
      return repo.findByAlerts(alert);
    } else {
      return null;
    }
  }

  public Sensor findByViewGraphId(int viewGraphId) {
    ViewGraph viewGraph = viewGraphService.findById(viewGraphId);
    if (viewGraph != null) {
      return repo.findByViewGraphs1OrViewGraphs2(viewGraph, viewGraph);
    } else {
      return null;
    }
  }
}