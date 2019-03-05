package com.neotropic.databasemodule.repository;

import com.arangodb.springframework.repository.ArangoRepository;

/**
 * Interface which extends ArangoRepository. This gives us access to CRUD
 * operations.
 *
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
public interface IProcessRepository extends ArangoRepository<Process, String> {
    
}
