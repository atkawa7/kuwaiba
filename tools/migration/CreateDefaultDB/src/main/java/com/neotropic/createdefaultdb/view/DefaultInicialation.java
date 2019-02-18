/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neotropic.createdefaultdb.view;

import com.neotropic.createdefaultdb.core.DataBaseContants;
import com.neotropic.createdefaultdb.core.DbManagement;
import com.neotropic.createdefaultdb.core.RelTypes;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.index.RelationshipIndex;
import org.neo4j.io.fs.FileUtils;

/**
 *
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
public class DefaultInicialation {

    private static DbManagement dbManagement;

    // tag::vars[]
    Node firstNode;
    Node secondNode;
    Relationship relationship;
    public String greeting;
    // end::vars[]

    public static void main(final String[] args) throws IOException {
        String firstArg;
        if (args.length == 3) {
            try {

                String DB_FULL_PATH = (args[0]);
                String DB_NAME = (args[1]);
                String DB_ADMIN_PASSWORD = (args[2]);

                dbManagement = new DbManagement(DB_FULL_PATH + File.separator + DB_NAME, DB_ADMIN_PASSWORD);
                dbManagement.createdefaultData();
                dbManagement.shutDown();
            } catch (Exception e) {
               

                System.out.println("ERROR :"+e);
            
            }
        } else {
            System.out.println("Argument needed are 3 but recived " + args.length);
            System.out.println("1. Argument: database name ");
            System.out.println("2. Argument: path to create database ");
            System.out.println("3. Argument: password forf admin usrr");
            System.out.println("Example :");

            System.out.println("java -jar CreateDefaulDB /home/myuser/data mydbKuwaiba 456p09");
            System.exit(1);
        }

        //test();
    }
  
}
