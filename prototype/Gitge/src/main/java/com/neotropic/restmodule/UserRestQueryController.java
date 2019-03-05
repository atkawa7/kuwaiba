/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neotropic.restmodule;

import com.neotropic.databasemodule.entity.User;
import com.neotropic.databasemodule.repository.IUserRepository;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
@RestController
public class UserRestQueryController {

    @Autowired
    private IUserRepository repository;

    @GetMapping("/users")
    public List<User> getAllUsers() {
        Iterable<User> iteratorToCollection = repository.findAll();
        List<User> myList = StreamSupport.stream(iteratorToCollection.spliterator(), false)
                .collect(Collectors.toList());        
        return myList;
    }
}
