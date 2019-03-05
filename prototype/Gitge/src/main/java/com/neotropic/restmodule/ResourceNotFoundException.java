/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neotropic.restmodule;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * REST APIs throw a ResourceNotFoundException when a any REST resource class
 * was not found in the database. Following is the definition of
 * ResourceNotFoundException class The exception class contains a
 * @ResponseStatus(HttpStatus.NOT_FOUND) annotation to tell spring boot to
 * respond with a 404 NOT FOUND status when this exception is thrown.
 *
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
