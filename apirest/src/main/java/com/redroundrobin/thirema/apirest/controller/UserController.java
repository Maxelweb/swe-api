package com.redroundrobin.thirema.apirest.controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.redroundrobin.thirema.apirest.models.UserDisabledException;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.service.postgres.EntityService;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.utils.*;
import com.redroundrobin.thirema.apirest.utils.JwtUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import com.redroundrobin.thirema.apirest.utils.UserRoleNotFoundException;
import com.redroundrobin.thirema.apirest.utils.MissingFieldsException;
import com.redroundrobin.thirema.apirest.utils.ValuesNotAllowedException;
import com.redroundrobin.thirema.apirest.utils.NotAuthorizedToInsertUserException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

  @Autowired
  private JwtUtil jwtTokenUtil;

  @Autowired
  private UserService userService;

  @Autowired
  private EntityService entityService;

  //richiesta fatta da un utente autenticato per vedere i device visibili a un altro utente
  @GetMapping(value = {"/users/{userid:.+}/devices"})
  public ResponseEntity<?> getUserDevices(@RequestHeader("Authorization") String authorization,
                                          @PathVariable("userid") int requiredUserId) {
    String token = authorization.substring(7);
    User user = userService.findByEmail(jwtTokenUtil.extractUsername(token));
    User requiredUser = userService.find(requiredUserId);
    if (requiredUser != null && (user.getUserId() == requiredUserId
        || user.getType() == User.Role.ADMIN  || user.getType() == User.Role.MOD
        && requiredUser.getType() != User.Role.ADMIN
            && user.getEntity().getEntityId() == requiredUser.getEntity().getEntityId())) {
      return ResponseEntity.ok(userService.userDevices(requiredUserId));
    } else {
      return new ResponseEntity(HttpStatus.FORBIDDEN);
    }
  }

  //creazione di un nuovo utente
  @PostMapping(value = {"/users/create"})
  public ResponseEntity<Object> createUser(@RequestHeader("Authorization") String authorization,
                                           @RequestBody String jsonStringUser) {
    String token = authorization.substring(7);
    User user = userService.findByEmail(jwtTokenUtil.extractUsername(token));
    JsonObject jsonUser = JsonParser.parseString(jsonStringUser).getAsJsonObject();
    try {
      return ResponseEntity.ok(userService.serializeUser(jsonUser, user));
    }
    catch(KeysNotFoundException | MissingFieldsException | ValuesNotAllowedException
        | UserRoleNotFoundException | EntityNotFoundException | NotAuthorizedToInsertUserException e){
      return new ResponseEntity(e.getMessage() , HttpStatus.BAD_REQUEST);
    }
  }

  /*In input prende JsonObject coi field da modificare dello userId*/
  @PutMapping(value = {"/users/{userid:.+}/edit"})
  public ResponseEntity<Object> editUser(@RequestHeader("Authorization") String authorization,
                                         @RequestBody String rawFieldsToEdit,
                                         @PathVariable("userid") int userId) {
    String token = authorization.substring(7);
    String editingUserEmail = jwtTokenUtil.extractUsername(token);
    User editingUser = userService.findByEmail(editingUserEmail);
    JsonObject fieldsToEdit = JsonParser.parseString(rawFieldsToEdit).getAsJsonObject();
    User userToEdit = userService.find(userId);

    if (userToEdit != null) {
      HashMap<String, Object> response = new HashMap<>();

      User user;
      try {
        if (editingUser.getType() == User.Role.ADMIN) {
          user = userService.editByAdministrator(userToEdit, fieldsToEdit);
          response.put("user", user);
        } else if (editingUser.getUserId() == userToEdit.getUserId()) {
          Date previousExpiration = jwtTokenUtil.extractExpiration(token);

          user = userService.editItself(userToEdit, fieldsToEdit);
          response.put("user", user);

          if (!user.getEmail().equals(editingUserEmail)) {
            try {
              UserDetails userDetails = userService.loadUserByEmail(user.getEmail());
              String newToken = jwtTokenUtil.generateTokenWithExpiration("webapp",
                  previousExpiration,userDetails);
              response.put("token", jwtTokenUtil.generateToken("webapp", userDetails));
            } catch (UsernameNotFoundException unfe) {
              return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            } catch (UserDisabledException ude) {
              return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
          }
        } else if (editingUser.getType() == User.Role.MOD
            && editingUser.getEntity().equals(userToEdit.getEntity())) {
          user = userService.editByModerator(userToEdit, fieldsToEdit);
          response.put("user", user);
        } else {
          return new ResponseEntity(HttpStatus.FORBIDDEN);
        }
      } catch (NotAllowedToEditFieldsException natef) {
        return new ResponseEntity(HttpStatus.FORBIDDEN);
      } catch (DataIntegrityViolationException dive) {
        if (dive.getMostSpecificCause().getMessage()
            .contains("duplicate key value violates unique constraint")) {

          Pattern pattern = Pattern.compile("Key \\((.+)\\)=\\((.+)\\) already exists");
          Matcher matcher = pattern.matcher(dive.getMostSpecificCause().getMessage());
          if (matcher.find()) {
            return new ResponseEntity("The value of " + matcher.group(1) + " already exists",
                HttpStatus.CONFLICT);
          } else {
            return new ResponseEntity("",HttpStatus.CONFLICT);
          }
        } else {
          return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
      } catch (EntityNotFoundException | KeysNotFoundException | UserRoleNotFoundException nf) {
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
      } catch (TfaNotPermittedException tnpe) {
        return new ResponseEntity(tnpe.getMessage(),HttpStatus.CONFLICT);
      }
      return ResponseEntity.ok(response);
    } else {
      return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }
  }

  //dato un token valido restituisce l'ente di appertenenza o tutti gli enti
  //se il token è di un amministratore
  @GetMapping(value = {"users/entities"})
  public ResponseEntity<Object> getAnotherUserEntity(
      @RequestHeader("Authorization") String authorization) {
    String token = authorization.substring(7);
    User user = userService.findByEmail(jwtTokenUtil.extractUsername(token));
    if (user.getType() == User.Role.ADMIN) {
      return ResponseEntity.ok(entityService.findAll());
    } else {
      //utente moderatore || utente membro
      return ResponseEntity.ok(user.getEntity());
    }
  }

  //dato un token valido restituisce l'ente di appertenenza o tutti gli enti
  //se il token è di un amministratore
  @GetMapping(value = {"users/{userId:.+}/entities"})
  public ResponseEntity<Object> getUserEntity(
      @RequestHeader("Authorization") String authorization, @PathVariable("userId") int userId)  {
    String token = authorization.substring(7);
    User user = userService.findByEmail(jwtTokenUtil.extractUsername(token));
    if (user.getType() == User.Role.ADMIN) {
      return ResponseEntity.ok(entityService.findAll());
    } else {
      User userToRetrieve = userService.find(userId);
      if (userToRetrieve != null
          && userToRetrieve.getEntity().getEntityId() == user.getEntity().getEntityId()) {
        return ResponseEntity.ok(user.getEntity());
      } else {
        return new ResponseEntity(HttpStatus.FORBIDDEN);
      }
    }
  }

  //tutti gli user
  @GetMapping(value = {"/users"})
  public List<User> users() {
    return userService.findAll();
  }

  //un determinato user
  @GetMapping(value = {"/user/{userid:.+}"})
  public User user(@PathVariable("userid") int userId) {
    return userService.find(userId);
  }

}
