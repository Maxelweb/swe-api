package com.redroundrobin.thirema.apirest.models.postgres;

public class SensorNotFoundException extends Exception {
  public SensorNotFoundException(String message) {
    super(message);
  }
}
