package com.redroundrobin.thirema.apirest.repository.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.models.postgres.View;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ViewRepository extends CrudRepository<View, Integer> {
  List<View> findAllByUser(User user);

  View findByIdAndUser(int viewId, User user);
}
