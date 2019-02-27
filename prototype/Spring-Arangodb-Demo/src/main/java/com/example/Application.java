package com.example;

import com.example.logic.ByExampleRunner;
import com.example.logic.CrudRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application that uses a Arango db on a Spring Data.
 * 
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
@SpringBootApplication
public class Application {

    public static void main(String... args) {
        //SpringApplication.run(CrudRunner.class, args);
        final Class<?>[] runner = new Class<?>[]{CrudRunner.class, ByExampleRunner.class};
        //Object[] runner = new Object[] { CrudRunner.class };
        System.exit(SpringApplication.exit(SpringApplication.run(runner, args)));
        
    }
}
