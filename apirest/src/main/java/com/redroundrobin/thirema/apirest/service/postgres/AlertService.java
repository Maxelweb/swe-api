package com.redroundrobin.thirema.apirest.service.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.Alert;
import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.models.postgres.View;
import com.redroundrobin.thirema.apirest.models.postgres.ViewGraph;
import com.redroundrobin.thirema.apirest.repository.postgres.AlertRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.redroundrobin.thirema.apirest.utils.exception.InvalidFieldsValuesException;
import com.redroundrobin.thirema.apirest.utils.exception.MissingFieldsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AlertService {

  private AlertRepository repo;

  private EntityService entityService;

  private SensorService sensorService;

  private UserService userService;

  private boolean checkFields(Map<String, Object> fields) {
    List<String> allowedFields = new ArrayList<>();
    allowedFields.add("threshold");
    allowedFields.add("type");
    allowedFields.add("sensor");
    allowedFields.add("entity");

    return fields.containsKey("threshold") && fields.containsKey("type")
        && (fields.containsKey("sensor") && fields.containsKey("entity"));
  }

  private Alert addEditAlert(User user, Alert alert, Map<String, Object> fields)
      throws InvalidFieldsValuesException {
    if (alert == null) {
      alert = new Alert();
    }

    for (Map.Entry<String, Object> entry : fields.entrySet()) {
      switch (entry.getKey()) {
        case "threshold":
            alert.setThreshold((double) entry.getValue());
          break;
        case "type":
          if (Alert.Type.isValid((int) entry.getValue())) {
            alert.setType(Alert.Type.values()[(int) entry.getValue()]);
          } else {
            throw new InvalidFieldsValuesException("The type with provided id is not found");
          }
          break;
        case "sensor":
          Sensor sensor;
          if (user.getType() == User.Role.ADMIN) {
            sensor = sensorService.findById((int) entry.getValue());
          } else {
            sensor = sensorService.findByIdAndEntityId((int) entry.getValue(),
                user.getEntity().getId());
          }
          if (sensor != null) {
            alert.setSensor(sensor);
          } else {
            throw new InvalidFieldsValuesException("The sensor with provided id is not found or "
                + "not authorized");
          }
          break;
        case "entity":
          Entity entity;
          if (user.getType() == User.Role.ADMIN
              || user.getEntity().getId() == (int) entry.getValue()) {
            entity = entityService.findById((int) entry.getValue());
          } else {
            entity = null;
          }
          if (entity != null) {
            alert.setEntity(entity);
          } else {
            throw new InvalidFieldsValuesException("The entity with provided id is not found or "
                + "not authorized");
          }
          break;
        default:
      }
    }

    return repo.save(alert);
  }

  @Autowired
  public AlertService(AlertRepository alertRepository) {
    this.repo = alertRepository;
  }

  @Autowired
  public void setEntityService(EntityService entityService) {
    this.entityService = entityService;
  }

  @Autowired
  public void setSensorService(SensorService sensorService) {
    this.sensorService = sensorService;
  }

  @Autowired
  public void setUserService(UserService userService) {
    this.userService = userService;
  }

  public List<Alert> findAll() {
    return (List<Alert>) repo.findAll();
  }

  public List<Alert> findAllByEntityId(int entityId) {
    Entity entity = entityService.findById(entityId);
    if (entity != null) {
      return (List<Alert>) repo.findAllByEntity(entity);
    } else {
      return Collections.emptyList();
    }
  }

  public List<Alert> findAllByEntityIdAndSensorId(int entityId, int sensorId) {
    Entity entity = entityService.findById(entityId);
    Sensor sensor = sensorService.findById(sensorId);
    if (entity != null && sensor != null) {
      return (List<Alert>) repo.findAllByEntityAndSensor(entity, sensor);
    } else {
      return Collections.emptyList();
    }
  }

  public List<Alert> findAllBySensorId(int sensorId) {
    Sensor sensor = sensorService.findById(sensorId);
    if (sensor != null) {
      return (List<Alert>) repo.findAllBySensor(sensor);
    } else {
      return Collections.emptyList();
    }
  }

  public List<Alert> findAllByUserId(int userId) {
    User user = userService.findById(userId);
    if (user != null) {
      return (List<Alert>) repo.findAllByUsers(user);
    } else {
      return Collections.emptyList();
    }
  }

  public Alert findById(int id) {
    return repo.findById(id).orElse(null);
  }

  public Alert createAlert(User user, Map<String, Object> newAlertFields)
      throws InvalidFieldsValuesException, MissingFieldsException {
    if (this.checkFields(newAlertFields)) {
      return this.addEditAlert(user, null, newAlertFields);
    } else {
      throw new MissingFieldsException("One or more needed fields are missing");
    }
  }
}