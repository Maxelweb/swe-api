package com.redroundrobin.thirema.apirest.models.postgres;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@javax.persistence.Entity
@Table(name = "alerts")
public class Alert {

  public enum Type {
    LOWER, GREATER, EQUAL;

    @JsonValue
    public int toValue() {
      return ordinal();
    }

    public static boolean isValid(int type) {
      for (int i = 0; i < ViewGraph.Correlation.values().length; ++i) {
        if (type == i) {
          return true;
        }
      }
      return false;
    }
  }

  @Id
  @GeneratedValue(generator = "alerts_alert_id_seq", strategy = GenerationType.SEQUENCE)
  @SequenceGenerator(
      name = "alerts_alert_id_seq",
      sequenceName = "alerts_alert_id_seq",
      allocationSize = 50
  )
  @Column(name = "alert_id")
  private int alertId;
  private double threshold;
  private Type type;
  private boolean deleted;

  @ManyToOne
  @JoinColumn(name = "entity_id")
  @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "entityId")
  @JsonIdentityReference(alwaysAsId = true)
  private Entity entity;

  @ManyToOne
  @JoinColumn(name = "sensor_id")
  @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "sensorId")
  @JsonIdentityReference(alwaysAsId = true)
  private Sensor sensor;

  @JsonIgnore
  @ManyToMany(mappedBy = "disabledAlerts")
  private List<User> users;


  public void setAlertId(int alertId) {
    this.alertId = alertId;
  }

  @JsonProperty(value = "alertId")
  public int getAlertId() {
    return alertId;
  }

  public void setThreshold(double threshold) {
    this.threshold = threshold;
  }

  public double getThreshold() {
    return threshold;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public Type getType() {
    return type;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  public boolean getDeleted() {
    return this.deleted;
  }

  public void setSensor(Sensor sensor) {
    this.sensor = sensor;
  }

  public Sensor getSensor() {
    return sensor;
  }

  public void setEntity(Entity entity) {
    this.entity = entity;
  }

  public Entity getEntity() {
    return entity;
  }

  public List<User> getUsers() {
    return users;
  }

  public void setUsers(List<User> users) {
    this.users = users;
  }
}
