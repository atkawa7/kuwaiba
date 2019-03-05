package com.neotropic.databasemodule.repository;

import com.arangodb.springframework.repository.ArangoRepository;
import com.neotropic.databasemodule.entity.Operator;

/**
 * Interface which extends ArangoRepository. This gives us access to CRUD
 * operations.
 *
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
public interface IOperatorRepository extends ArangoRepository<Operator, String> {

}
