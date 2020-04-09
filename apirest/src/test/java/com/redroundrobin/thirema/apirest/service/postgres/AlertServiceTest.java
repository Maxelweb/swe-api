package com.redroundrobin.thirema.apirest.service.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.Alert;
import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.repository.postgres.AlertRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.EntityRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.SensorRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.UserRepository;
import com.redroundrobin.thirema.apirest.utils.exception.ElementNotFoundException;
import com.redroundrobin.thirema.apirest.utils.exception.InvalidFieldsValuesException;
import com.redroundrobin.thirema.apirest.utils.exception.MissingFieldsException;
import com.redroundrobin.thirema.apirest.utils.exception.NotAuthorizedException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class AlertServiceTest {

  @MockBean
  private AlertRepository alertRepo;

  @MockBean
  private EntityRepository entityRepo;

  @MockBean
  private SensorRepository sensorRepo;

  @MockBean
  private UserRepository userRepo;


  private AlertService alertService;


  private Entity entity1;
  private Entity entity2;
  private Entity entity3;

  private Sensor sensor1;
  private Sensor sensor2;
  private Sensor sensor3;

  private Alert alert1;
  private Alert alert2;
  private Alert alert3;

  private User admin1;
  private User user1;
  private User user2;
  private User user3;
  private User mod2;


  @Before
  public void setUp() {
    alertService = new AlertService(alertRepo,entityRepo,sensorRepo,userRepo);

    // ----------------------------------------- Set Entities --------------------------------------
    entity1 = new Entity(1, "entity1", "location1");
    entity2 = new Entity(2, "entity2", "location2");
    entity3 = new Entity(3, "entity3", "location3");

    List<Entity> allEntities = new ArrayList<>();
    allEntities.add(entity1);
    allEntities.add(entity2);
    allEntities.add(entity3);


    // ----------------------------------------- Set Sensors --------------------------------------
    sensor1 = new Sensor(1, "type1", 1);
    sensor2 = new Sensor(2, "type2", 2);
    sensor3 = new Sensor(3, "type3", 3);

    List<Sensor> allSensors = new ArrayList<>();
    allSensors.add(sensor1);
    allSensors.add(sensor2);
    allSensors.add(sensor3);


    // ----------------------------------------- Set Alerts --------------------------------------
    alert1 = new Alert(1, 10.0, Alert.Type.GREATER, entity1, sensor1);
    alert2 = new Alert(2, 10.0, Alert.Type.GREATER, entity1, sensor1);
    alert3 = new Alert(3, 10.0, Alert.Type.GREATER, entity2, sensor2);

    List<Alert> allAlerts = new ArrayList<>();
    allAlerts.add(alert1);
    allAlerts.add(alert2);
    allAlerts.add(alert3);


    // ----------------------------------------- Set Users --------------------------------------
    user1 = new User(1, "name1", "surname1", "email1", "pass1", User.Role.USER);
    user2 = new User(2, "name2", "surname2", "email2", "pass2", User.Role.USER);
    user3 = new User(3, "name3", "surname3", "email3", "pass3", User.Role.USER);
    mod2 = new User(5, "name5", "surname5", "email5", "pass5", User.Role.MOD);
    admin1 = new User(4, "name4", "surname4", "email4", "pass4", User.Role.ADMIN);

    List<User> allUsers = new ArrayList<>();
    allUsers.add(user1);
    allUsers.add(user2);
    allUsers.add(user3);
    allUsers.add(mod2);
    allUsers.add(admin1);


    // ---------------------------------- Set Alerts to Users -----------------------------------
    Set<Alert> user1Alerts = new HashSet<>();
    user1Alerts.add(alert1);
    user1.setDisabledAlerts(user1Alerts);

    Set<Alert> user2Alerts = new HashSet<>();
    user2Alerts.add(alert3);
    user2.setDisabledAlerts(user2Alerts);

    user3.setDisabledAlerts(Collections.emptySet());

    admin1.setDisabledAlerts(Collections.emptySet());


    // ---------------------------------- Set Entities to Users ----------------------------------
    user1.setEntity(entity1);
    mod2.setEntity(entity2);


    // ------------------------------- Set Sensors to Entites ------------------------------------
    Set<Sensor> entity1Sensors = new HashSet<>();
    entity1Sensors.add(sensor1);
    entity1.setSensors(entity1Sensors);

    Set<Sensor> entity2Sensors = new HashSet<>();
    entity2Sensors.add(sensor2);
    entity2.setSensors(entity2Sensors);

    Set<Sensor> entity3Sensors = new HashSet<>();
    entity3Sensors.add(sensor2);
    entity3Sensors.add(sensor3);
    entity3.setSensors(entity3Sensors);
    // entity1 has sensor1, entity2 has sensor2, entity3 has sensor2 and sensor3



    when(alertRepo.findAllByDeletedFalse()).thenReturn(allAlerts);
    when(alertRepo.findAllByEntityAndDeletedFalse(any(Entity.class))).thenAnswer(i -> {
      Entity entity = i.getArgument(0);
      return allAlerts.stream().filter(a -> entity.equals(a.getEntity()))
          .collect(Collectors.toList());
    });
    when(alertRepo.findAllByEntityAndSensorAndDeletedFalse(any(Entity.class),any(Sensor.class))).thenAnswer(i -> {
      Entity entity = i.getArgument(0);
      Sensor sensor = i.getArgument(1);
      if (entity != null && sensor != null) {
        return allAlerts.stream().filter(a -> entity.equals(a.getEntity()) && sensor.equals(a.getSensor()))
            .collect(Collectors.toList());
      } else {
        return Collections.emptyList();
      }
    });
    when(alertRepo.findAllBySensorAndDeletedFalse(any(Sensor.class))).thenAnswer(i -> {
      Sensor sensor = i.getArgument(0);
      return allAlerts.stream().filter(a -> sensor.equals(a.getSensor()))
          .collect(Collectors.toList());
    });
    when(alertRepo.findAllByUsersAndDeletedFalse(any(User.class))).thenAnswer(i -> {
      User user = i.getArgument(0);
      return allAlerts.stream().filter(a -> user.getDisabledAlerts().contains(a))
          .collect(Collectors.toList());
    });
    when(alertRepo.findById(anyInt())).thenAnswer(i -> {
      return allAlerts.stream().filter(a -> i.getArgument(0).equals(a.getId()))
          .findFirst();
    });
    when(alertRepo.save(any(Alert.class))).thenAnswer(i -> {
      Alert alert = i.getArgument(0);
      if (alert.getId() == alert3.getId()) {
        return new Alert();
      } else {
        return i.getArgument(0);
      }
    });

    when(entityRepo.findById(anyInt())).thenAnswer(i -> {
      return allEntities.stream().filter(e -> i.getArgument(0).equals(e.getId()))
          .findFirst();
    });

    when(sensorRepo.findById(anyInt())).thenAnswer(i -> {
      return allSensors.stream().filter(s -> i.getArgument(0).equals(s.getId()))
          .findFirst();
    });
    when(sensorRepo.findBySensorIdAndEntities(anyInt(),any(Entity.class))).thenAnswer(i -> {
      Sensor sensor = allSensors.stream().filter(s -> i.getArgument(0).equals(s.getId())).findFirst().orElse(null);
      Entity entity = i.getArgument(1);
      if (sensor != null && entity.getSensors().contains(sensor)) {
        return sensor;
      } else {
        return null;
      }
    });

    when(userRepo.findById(anyInt())).thenAnswer(i -> {
      return allUsers.stream().filter(u -> i.getArgument(0).equals(u.getId()))
          .findFirst();
    });

  }

  @Test
  public void findAllAlerts() {
    List<Alert> alerts = alertService.findAll();

    assertTrue(!alerts.isEmpty());
  }



  @Test
  public void findAllAlertsByEntityId() {
    List<Alert> alerts = alertService.findAllByEntityId(entity1.getId());

    assertTrue(!alerts.isEmpty());
  }

  @Test
  public void findAllAlertsByNotExistentEntityId() {
    List<Alert> alerts = alertService.findAllByEntityId(10);

    assertTrue(alerts.isEmpty());
  }



  @Test
  public void findAllAlertsByEntityIdAndSensorId() {
    List<Alert> alerts = alertService.findAllByEntityIdAndSensorId(entity1.getId(), sensor1.getId());

    assertTrue(!alerts.isEmpty());
  }

  @Test
  public void findAllAlertsByEntityIdAndNotExistentSensorId() {
    List<Alert> alerts = alertService.findAllByEntityIdAndSensorId(entity1.getId(), 9);

    assertTrue(alerts.isEmpty());
  }



  @Test
  public void findAllAlertsBySensorId() {
    List<Alert> alerts = alertService.findAllBySensorId(sensor1.getId());

    assertTrue(alerts.stream().count() == 2);
  }

  @Test
  public void findAllAlertsByNotExistentSensorId() {
    List<Alert> alerts = alertService.findAllBySensorId(4);

    assertTrue(alerts.isEmpty());
  }



  @Test
  public void findAllDisabledAlertsByUserId() {
    List<Alert> alerts = alertService.findAllDisabledByUserId(user1.getId());

    assertTrue(alerts.stream().count() == 1);
  }

  @Test
  public void findAllDisabledAlertsByNotExistentUserId() {
    List<Alert> alerts = alertService.findAllDisabledByUserId(10);

    assertTrue(alerts.isEmpty());
  }



  @Test
  public void findAlertById() {
    Alert alert = alertService.findById(alert1.getId());

    assertNotNull(alert);
  }



  @Test
  public void createAlertSuccessfull() {
    Map<String, Object> newAlertFields = new HashMap<>();
    newAlertFields.put("threshold", 10.0);
    newAlertFields.put("type", Alert.Type.GREATER.toValue());
    newAlertFields.put("sensor", sensor1.getId());
    newAlertFields.put("entity", entity1.getId());

    try {
      Alert alert = alertService.createAlert(user1, newAlertFields);

      assertNotNull(alert);
    } catch (Exception e) {
      System.out.println(e);
      assertTrue(false);
    }
  }

  @Test
  public void createAlertWithNotExistentTypeByAdmin() {
    Map<String, Object> newAlertFields = new HashMap<>();
    newAlertFields.put("threshold", 10.0);
    newAlertFields.put("type", 10);
    newAlertFields.put("sensor", sensor1.getId());
    newAlertFields.put("entity", entity1.getId());

    try {
      Alert alert = alertService.createAlert(admin1, newAlertFields);

      assertTrue(false);
    } catch (InvalidFieldsValuesException e) {
      assertEquals("The type with provided id is not found", e.getMessage());
      assertTrue(true);
    } catch (Exception e) {
      System.out.println(e);
      assertTrue(false);
    }
  }

  @Test
  public void createAlertWithoutEntityThrowsMissingFieldsException() {
    Map<String, Object> newAlertFields = new HashMap<>();
    newAlertFields.put("threshold", 10.0);
    newAlertFields.put("type", Alert.Type.GREATER.toValue());
    newAlertFields.put("sensor", sensor1.getId());

    try {
      Alert alert = alertService.createAlert(user1, newAlertFields);

      assertTrue(false);
    } catch (MissingFieldsException e) {
      assertEquals("One or more needed fields are missing", e.getMessage());
      assertTrue(true);
    } catch (Exception e) {
      assertTrue(false);
    }
  }

  @Test
  public void createAlertWithInvalidEntityThrowsInvalidFieldsValuesException() {
    Map<String, Object> newAlertFields = new HashMap<>();
    newAlertFields.put("threshold", 10.0);
    newAlertFields.put("type", Alert.Type.GREATER.toValue());
    newAlertFields.put("sensor", sensor1.getId());
    newAlertFields.put("entity", entity2.getId());

    try {
      Alert alert = alertService.createAlert(user1, newAlertFields);

      assertTrue(false);
    } catch (InvalidFieldsValuesException e) {
      assertEquals("The entity with provided id is not found or not authorized", e.getMessage());
      assertTrue(true);
    } catch (Exception e) {
      System.out.println(e);
      assertTrue(false);
    }
  }

  @Test
  public void createAlertWithInvalidSensorThrowsInvalidFieldsValuesException() {
    Map<String, Object> newAlertFields = new HashMap<>();
    newAlertFields.put("threshold", 10.0);
    newAlertFields.put("type", Alert.Type.GREATER.toValue());
    newAlertFields.put("sensor", sensor3.getId());
    newAlertFields.put("entity", entity1.getId());

    try {
      Alert alert = alertService.createAlert(user1, newAlertFields);

      assertTrue(false);
    } catch (InvalidFieldsValuesException e) {
      assertEquals("The sensor with provided id is not found or not authorized", e.getMessage());
      assertTrue(true);
    } catch (MissingFieldsException e) {
      System.out.println(e);
      assertTrue(false);
    }
  }



  @Test
  public void enbleUserAlertDisableUser1AlertSuccessfull() {
    when(userRepo.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
    try {
      boolean disabled = alertService.enableUserAlert(user1, alert1.getId(), true);

      assertTrue(disabled);
    } catch (ElementNotFoundException e) {
      e.printStackTrace();
      assertTrue(false);
    } catch (NotAuthorizedException e) {
      e.printStackTrace();
      assertTrue(false);
    }
  }

  @Test
  public void enbleUserAlertAlreadyEnabledUser1AlertSuccessfull() {
    when(userRepo.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
    try {
      boolean disabled = alertService.enableUserAlert(user1, alert2.getId(), true);

      assertTrue(disabled);
    } catch (ElementNotFoundException e) {
      e.printStackTrace();
      assertTrue(false);
    } catch (NotAuthorizedException e) {
      e.printStackTrace();
      assertTrue(false);
    }
  }

  @Test
  public void enbleUserAlertDisableUser1AlertSimulateDBError() {
    when(userRepo.save(any(User.class))).thenAnswer(i -> admin1);
    try {
      boolean disabled = alertService.enableUserAlert(user1, alert2.getId(), false);

      assertFalse(disabled);
    } catch (ElementNotFoundException e) {
      e.printStackTrace();
      assertTrue(false);
    } catch (NotAuthorizedException e) {
      e.printStackTrace();
      assertTrue(false);
    }
  }

  @Test
  public void enbleUserAlertEnableUser1AlertThrowNotAuthorizedException() {
    try {
      boolean disabled = alertService.enableUserAlert(user1, alert3.getId(), true);

      assertTrue(false);
    } catch (ElementNotFoundException e) {
      e.printStackTrace();
      assertTrue(false);
    } catch (NotAuthorizedException e) {
      e.printStackTrace();
      assertTrue(true);
    }
  }

  @Test
  public void enbleUserAlertEnableUser1AlertThrowElementNotFoundException() {
    try {
      boolean disabled = alertService.enableUserAlert(user1, 10, true);

      assertTrue(false);
    } catch (ElementNotFoundException e) {
      e.printStackTrace();
      assertTrue(true);
    } catch (NotAuthorizedException e) {
      e.printStackTrace();
      assertTrue(false);
    }
  }



  @Test
  public void deleteAlertByAdminSuccessfull() {
    try {
      boolean deleted = alertService.deleteAlert(admin1, alert1.getId());

      assertTrue(deleted);
    } catch (ElementNotFoundException e) {
      e.printStackTrace();
      assertTrue(false);
    } catch (NotAuthorizedException e) {
      e.printStackTrace();
      assertTrue(false);
    }
  }

  @Test
  public void deleteAlertByMod3SimulateDBError() {
    try {
      boolean deleted = alertService.deleteAlert(mod2, alert3.getId());

      assertFalse(deleted);
    } catch (ElementNotFoundException e) {
      e.printStackTrace();
      assertTrue(false);
    } catch (NotAuthorizedException e) {
      e.printStackTrace();
      assertTrue(false);
    }
  }

  @Test
  public void deleteAlertByUserThrowNotAuthorizedException() {
    try {
      boolean deleted = alertService.deleteAlert(user1, alert1.getId());

      assertTrue(false);
    } catch (ElementNotFoundException e) {
      e.printStackTrace();
      assertTrue(false);
    } catch (NotAuthorizedException e) {
      e.printStackTrace();
      assertTrue(true);
    }
  }

  @Test
  public void deleteAlertByAdmin1WithNotExistentAlertThrowElementNotFoundException() {
    when(alertRepo.save(any(Alert.class))).thenAnswer(i -> {
      if (i.getArgument(0).equals(alert3)) {
        return alert3;
      } else {
        return i.getArgument(0);
      }
    });
    try {
      boolean deleted = alertService.deleteAlert(admin1, 10);

      assertTrue(false);
    } catch (ElementNotFoundException e) {
      e.printStackTrace();
      assertTrue(true);
    } catch (NotAuthorizedException e) {
      e.printStackTrace();
      assertTrue(false);
    }
  }



  @Test
  public void deleteAlertBySensorIdByAdmin1Successfull() {
    doNothing().when(alertRepo).setDeletedBySensor(anyBoolean(), any(Sensor.class));
    try {
      alertService.deleteAlertsBySensorId(sensor1.getId());

      assertTrue(true);
    } catch (ElementNotFoundException e) {
      e.printStackTrace();
      assertTrue(false);
    }
  }

  @Test
  public void deleteAlertByNotExistentSensorIdByAdmin1ThrowElementNotFoundException() {
    doNothing().when(alertRepo).setDeletedBySensor(anyBoolean(), any(Sensor.class));
    try {
      alertService.deleteAlertsBySensorId(10);

      assertTrue(false);
    } catch (ElementNotFoundException e) {
      e.printStackTrace();
      assertTrue(true);
    }
  }
}
