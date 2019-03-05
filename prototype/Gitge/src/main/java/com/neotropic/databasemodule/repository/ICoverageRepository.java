/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neotropic.databasemodule.repository;

import com.arangodb.springframework.repository.ArangoRepository;

/**
 * * Interface which extends ArangoRepository. This gives us access to CRUD
 * operations.
 *
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
public interface ICoverageRepository extends ArangoRepository<ICoverageRepository, String> {

}
