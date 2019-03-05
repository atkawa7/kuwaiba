package com.neotropic.databasemodule.repository;

import com.arangodb.springframework.repository.ArangoRepository;
import com.neotropic.databasemodule.entity.User;
import java.util.List;

/**
 * Interface which extends ArangoRepository. This gives us access to CRUD
 * operations.
 *
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
public interface IUserRepository extends ArangoRepository<User, String> {

   
    
}
