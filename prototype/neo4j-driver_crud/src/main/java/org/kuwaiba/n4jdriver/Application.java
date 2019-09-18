/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kuwaiba.n4jdriver;

import org.kuwaiba.n4jdriver.model.Person;

/**
 * Main application entry point. This application creates, updates, fetches and deletes 
 * business objects on and from a Neo4J database using the driver available at {@link https://github.com/neo4j/neo4j-java-driver}.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class Application {
    public static void main(String[] args) {
        PersistenceService service = new PersistenceService(Configuration.CONST_DBHOST, 
                Configuration.CONST_DBUSER, Configuration.CONST_DBPASSWORD, Configuration.CONST_DBPORT);
        
        try {
            System.out.println("Opening connection to the database server");
            service.open();

            System.out.println("Creating Person 1...");
            Person aNewPerson1 = new Person(-1, "Person 1");
            service.createPerson(aNewPerson1);
            System.out.println("Person 1 created with id " + aNewPerson1.getId());

            System.out.println("Creating Person 2...");
            Person aNewPerson2 = new Person(-1, "Person 2");
            service.createPerson(aNewPerson2);
            System.out.println("Person 2 created with id " + aNewPerson2.getId());

            System.out.println("Changing Person 2 to Person 2 Max...");
            aNewPerson2.setName("Person 2 Max");
            service.updatePerson(aNewPerson2);

            System.out.println("Fetching modified Person 2...");
            Person aNewPerson2Max = service.getPerson(aNewPerson2.getId());
            System.out.println("The new name is " + aNewPerson2Max.getName());

            System.out.println("Deleting Person 1...");
            service.deletePerson(aNewPerson1.getId());

            System.out.println("Checking if Person 1 was actually deleted...");
            if(service.getPerson(aNewPerson1.getId()) == null)
                System.out.println("Person 1 was deleted as expected");
            else
                System.out.println("Person 1 was not deleted as expected");

            System.out.println("Closing connection...");
            service.shutdown();
        } catch (PersistenceService.ConnectionException ex) {
            System.out.println(String.format("The connection to the database could not be completed: %s", ex.getMessage()));
        }
    }
}
