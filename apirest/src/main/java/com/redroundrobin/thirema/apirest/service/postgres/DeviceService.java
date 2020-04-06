package com.redroundrobin.thirema.apirest.service.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.Device;
import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.Gateway;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.repository.postgres.DeviceRepository;
import java.util.Collections;
import java.util.List;

import com.redroundrobin.thirema.apirest.repository.postgres.EntityRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.GatewayRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.SensorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeviceService {

  private DeviceRepository deviceRepo;

  private GatewayRepository gatewayRepo;

  private SensorRepository sensorRepo;

  private EntityRepository entityRepo;

  @Autowired
  public DeviceService(DeviceRepository deviceRepository) {
    this.deviceRepo = deviceRepository;
  }

  public List<Device> findAll() {
    return (List<Device>) deviceRepo.findAll();
  }

  public List<Device> findAllByEntityId(int entityId) {
    Entity entity = entityRepo.findById(entityId).orElse(null);
    if (entity != null) {
      return (List<Device>) deviceRepo.findAllByEntityId(entityId);
    } else {
      return Collections.emptyList();
    }
  }

  public List<Device> findAllByGatewayId(int gatewayId) {
    Gateway gateway = gatewayRepo.findById(gatewayId).orElse(null);
    if (gateway != null) {
      return (List<Device>) deviceRepo.findAllByGateway(gateway);
    } else {
      return Collections.emptyList();
    }
  }

  public Device findById(int id) {
    return deviceRepo.findById(id).orElse(null);
  }

  public Device findBySensorId(int sensorId) {
    Sensor sensor = sensorRepo.findById(sensorId).orElse(null);
    if (sensor != null) {
      return deviceRepo.findBySensors(sensor);
    } else {
      return null;
    }
  }

  public Device findByIdAndEntityId(int id, int entityId) {
    return deviceRepo.findByIdAndEntityId(id, entityId);
  }

  public Device findByGatewayIdAndRealDeviceId(int gatewayId, int realDeviceId) {
    Gateway gateway = gatewayRepo.findById(gatewayId).orElse(null);
    if (gateway != null) {
      return deviceRepo.findByGatewayAndRealDeviceId(gateway, realDeviceId);
    } else {
      return null;
    }
  }

  @Autowired
  public void setEntityRepository(EntityRepository entityRepository) {
    this.entityRepo = entityRepository;
  }

  @Autowired
  public void setGatewayRepository(GatewayRepository gatewayRepository) {
    this.gatewayRepo = gatewayRepository;
  }

  @Autowired
  public void setSensorRepository(SensorRepository sensorRepository) {
    this.sensorRepo = sensorRepository;
  }

}