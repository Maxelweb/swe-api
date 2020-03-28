package com.redroundrobin.thirema.apirest.service.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.Device;
import com.redroundrobin.thirema.apirest.models.postgres.Gateway;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.repository.postgres.DeviceRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeviceService {

  private DeviceRepository repo;

  private GatewayService gatewayService;

  private SensorService sensorService;

  @Autowired
  public DeviceService(DeviceRepository deviceRepository) {
    this.repo = deviceRepository;
  }

  @Autowired
  public void setGatewayService(GatewayService gatewayService) {
    this.gatewayService = gatewayService;
  }

  @Autowired
  public void setSensorService(SensorService sensorService) {
    this.sensorService = sensorService;
  }

  public List<Device> findAll() {
    return (List<Device>) repo.findAll();
  }

  public List<Device> findAllByGatewayId(int gatewayId) {
    Gateway gateway = gatewayService.findById(gatewayId);
    if (gateway != null) {
      return (List<Device>) repo.findAllByGateway(gateway);
    } else {
      return null;
    }
  }

  public Device findById(int id) {
    return repo.findById(id).orElse(null);
  }

  public Device findBySensor(int sensorId) {
    Sensor sensor = sensorService.findById(sensorId);
    if (sensor != null) {
      return repo.findBySensors(sensor);
    } else {
      return null;
    }
  }
}