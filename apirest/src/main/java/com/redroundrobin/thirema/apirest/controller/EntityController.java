package com.redroundrobin.thirema.apirest.controller;

import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.service.postgres.EntityService;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.service.timescale.LogService;
import com.redroundrobin.thirema.apirest.utils.JwtUtil;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/entities")
public class EntityController extends CoreController {
  protected Logger logger = LoggerFactory.getLogger(this.getClass());

  private final EntityService entityService;

  public EntityController(EntityService entityService, JwtUtil jwtUtil, LogService logService,
                          UserService userService) {
    super(jwtUtil, logService, userService);
    this.entityService = entityService;
  }

  @GetMapping(value = {""})
  public ResponseEntity<List<Entity>> getEntities(
      @RequestHeader(value = "Authorization") String authorization,
      @RequestParam(name = "sensor", required = false) Integer sensorId,
      @RequestParam(name = "user", required = false) Integer userId) {
    User user = this.getUserFromAuthorization(authorization);
    if (user.getType() == User.Role.ADMIN && sensorId == null && userId == null) {
      return ResponseEntity.ok(entityService.findAll());
    } else if (sensorId == null
        && (user.getType() == User.Role.ADMIN || (userId != null && userId == user.getId()))) {
      return ResponseEntity.ok(entityService.findAllByUserId(userId));
    } else if (sensorId == null && user.getType() != User.Role.ADMIN && userId == null) {
      return ResponseEntity.ok(entityService.findAllByUserId(user.getId()));
    } else if (user.getType() == User.Role.ADMIN) {
      return ResponseEntity.ok(entityService.findAllBySensorId(sensorId));
    } else if (userId == null || userId == user.getId()) {
      return ResponseEntity.ok(entityService.findAllBySensorIdAndUserId(sensorId, user.getId()));
    } else {
      return ResponseEntity.ok(Collections.emptyList());
    }
  }

  @GetMapping(value = {"/{entityId:.+}"})
  public ResponseEntity<Entity> getEntity(
      @RequestHeader(value = "Authorization") String authorization,
      @PathVariable(name = "entityId") Integer entityId) {
    User user = this.getUserFromAuthorization(authorization);
    if (user.getType() == User.Role.ADMIN || user.getEntity().getId() == entityId) {
      return ResponseEntity.ok(entityService.findById(entityId));
    } else {
      logger.debug("RESPONSE STATUS: FORBIDDEN. User " + user.getId()
          + " is not an administrator or the entity Id is not the same as the user entity");
      return new ResponseEntity(HttpStatus.FORBIDDEN);
    }
  }
}
