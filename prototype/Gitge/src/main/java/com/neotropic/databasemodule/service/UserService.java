/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neotropic.databasemodule.service;

import com.neotropic.databasemodule.entity.User;
import com.neotropic.databasemodule.repository.IUserRepository;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
@Service
public class UserService {

    @Autowired
    private IUserRepository repository;

    public List<User> getAllUsers() {
        Iterable<User> iteratorToCollection = repository.findAll();
        List<User> myList = StreamSupport.stream(iteratorToCollection.spliterator(), false)
                .collect(Collectors.toList());

        return myList;
    }

    public List<User> getAllUsers(int offset, int limit) {
        Iterable<User> iteratorToCollection = repository.findAll();
        List<User> myList = StreamSupport.stream(iteratorToCollection.spliterator(), false)
                .collect(Collectors.toList());
        return myList;
    }

    public boolean saveUser(User user) {

        try {
            User newUSer = repository.save(user);
            if (newUSer != null) {
                return true;
            }
        } catch (Exception ex) {
            System.out.println("Exception" + ex);
            return false;
        }
        return false;
    }

    public long userCount() {
        return repository.count();
    }
}
