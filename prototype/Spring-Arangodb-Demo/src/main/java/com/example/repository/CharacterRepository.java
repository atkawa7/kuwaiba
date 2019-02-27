package com.example.repository;

import com.example.entity.Character;
import com.arangodb.springframework.repository.ArangoRepository;

/**
 * Now that we have our data model we want to store data. For this, we create a
 * repository interface which extends ArangoRepository. This gives us access to
 * CRUD operations, page in and query by example mechanics.
 *
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
public interface CharacterRepository extends ArangoRepository<Character, String> {
 
}
 
