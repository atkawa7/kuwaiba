/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
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

package org.neotropic.kuwaiba.web;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.neotropic.kuwaiba.core.persistence.ConnectionManager;
import org.neotropic.kuwaiba.core.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.persistence.exceptions.ConnectionException;
import org.neotropic.kuwaiba.core.persistence.metadata.MetadataEntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

/**
 * Application entry point.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    @Component
    public static class Bootstrap {
        @Autowired
        private ConnectionManager cmn;
        @Autowired
        private MetadataEntityManager mem;
        @Autowired
        private ApplicationEntityManager aem;
        @Autowired
        private BusinessEntityManager bem;
        @Value("${db.path}")
        private String dbPath;
        @Value("${db.host}")
        private String dbHost;
        @Value("${db.port}")
        private int dbPort;
        
        @PostConstruct
        void init() {
            Properties dbProperties = new Properties();
            dbProperties.put("dbPath", dbPath);
            dbProperties.put("dbHost", dbHost);
            dbProperties.put("dbPort", dbPort);
            
            cmn.setConfiguration(dbProperties);
            try {
                Logger.getLogger(Application.class.getName()).log(Level.INFO, "Starting connection to the database...");
                cmn.openConnection();
                Logger.getLogger(Application.class.getName()).log(Level.INFO, "Connection established");
                
            } catch (ConnectionException ex) {
                Logger.getLogger(Application.class.getName()).log(Level.WARNING, "Error connecting to the database");
                Logger.getLogger(Application.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        @PreDestroy
        void shutdown() {
            System.out.println("Closing conection");
            cmn.closeConnection();
        }
    }
}
