/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.neotropic.kuwaiba.web;

import java.util.Calendar;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.core.persistence.PersistenceService;
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
        // General properties
        @Value("${general.enableSecurityManager}")
        private boolean enableSecurityManager;
        @Value("${general.corporateLogo}")
        private String corporateLogo;
        @Value("${general.companyName}")
        private String companyName;
        @Value("${general.debugMode}")
        private String debugMode;
        
        // Connection properties
        @Value("${db.path}")
        private String dbPath;
        @Value("${db.host}")
        private String dbHost;
        @Value("${db.port}")
        private int dbPort;
        
        // Application properties
        @Value("${aem.enforceBusinessRules}")
        private String enforceBusinessRules;
        @Value("${aem.processEnginePath}")
        private String processEnginePath;
        @Value("${aem.processesPath}")
        private String processesPath;
        @Value("${aem.maxRoutes}")
        private String maxRoutes;
        @Value("${aem.backgroundsPath}")
        private String backgroundsPath;
        
        // Business properties
        @Value("${bem.attachmentsPath}")
        private String attachmentsPath;
        @Value("${bem.maxAttachmentSize}")
        private String maxAttachmentSize;
        
        @Autowired
        private PersistenceService persistenceService;
        @Autowired
        private TranslationService ts;
//        @Autowired
//        private KuwaibaSoapWebService ws;
//        @Autowired
//        private SDHModule modSdh;
        
        @PostConstruct
        void init() {
            Properties generalProperties = new Properties();
            generalProperties.put("enableSecurityManager", enableSecurityManager);
            persistenceService.setGeneralProperties(generalProperties);
            
            Properties connectionProperties = new Properties();
            connectionProperties.put("dbPath", dbPath);
            connectionProperties.put("dbHost", dbHost);
            connectionProperties.put("dbPort", dbPort);
            persistenceService.setConnectionProperties(connectionProperties);

            persistenceService.setMetadataProperties(new Properties());
            
            Properties applicationProperties = new Properties();
            applicationProperties.put("enforceBusinessRules", enforceBusinessRules);
            applicationProperties.put("processEnginePath", processEnginePath);
            applicationProperties.put("processesPath", processesPath);
            applicationProperties.put("maxRoutes", maxRoutes);
            applicationProperties.put("backgroundsPath", backgroundsPath);
            persistenceService.setApplicationProperties(applicationProperties);
            
            Properties businessProperties = new Properties();
            applicationProperties.put("attachmentsPath", attachmentsPath);
            applicationProperties.put("maxAttachmentSize", maxAttachmentSize);
            persistenceService.setBusinessProperties(businessProperties);
            
            try {
                persistenceService.start();
            } catch (IllegalStateException ex) {
                Logger.getLogger(Application.class.getName()).log(Level.SEVERE, 
                        String.format(ts.getTranslatedString("module.persistence.messages.cant-start-persistence-service"), 
                            Calendar.getInstance().getTime(), ex.getLocalizedMessage()));
            }
            
//            Logger.getLogger(SDHModule.class.getName()).log(Level.INFO, 
//                    String.format(ts.getTranslatedString("module.general.messages.initializing"), modSdh.getName()));
//            modSdh.configureModule(persistenceService.getMem(), persistenceService.getAem(), persistenceService.getBem());


//            if (persistenceService.getState().equals(PersistenceService.EXECUTION_STATE.RUNNING)) {
//                Endpoint.publish("http://localhost:8181/kuwaiba/KuwaibaService", ws);
//                Logger.getLogger(PersistenceService.class.getName()).log(Level.INFO, 
//                    String.format("[KUWAIBA] [%s] Web service initialized and running on port %s", 
//                            Calendar.getInstance().getTime(), 8181));
//            } else
//                Logger.getLogger(PersistenceService.class.getName()).log(Level.SEVERE, 
//                    String.format("[KUWAIBA] [%s] Web service could not be initialized because the Persistence Service is not running", 
//                            Calendar.getInstance().getTime()));
        }
        
        @PreDestroy
        void shutdown() {
            try {
                persistenceService.stop();
            } catch (IllegalStateException ex) {
                Logger.getLogger(Application.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage());
            }
        }
    }
}
