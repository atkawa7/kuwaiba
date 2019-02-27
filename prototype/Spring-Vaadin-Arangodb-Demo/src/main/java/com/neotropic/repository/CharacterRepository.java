/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neotropic.repository;

import com.neotropic.entity.Character;
import com.arangodb.springframework.repository.ArangoRepository;
import java.util.List;

/**
 * Now that we have our data model we want to store data. For this, we create a
 * repository interface which extends ArangoRepository. This gives us access to
 * CRUD operations, page in and query by example mechanics.
 *
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
public interface CharacterRepository extends ArangoRepository<Character, String> {

    
    List<Character> findBySurnameStartsWithIgnoreCase(String surname);
}
