/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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
package org.kuwaiba;

import java.util.Properties;
import org.jboss.logging.Logger;
import org.kuwaiba.core.min.apis.persistence.PersistenceService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * This bean holds the application's startup logic
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Component
public class StartupBean implements InitializingBean{
    private static final Logger LOG 
      = Logger.getLogger(StartupBean.class);
    
    @Autowired
    private PersistenceService persistenceService;
    
    @Autowired
    private Environment env;
 
    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            persistenceService = PersistenceService.getInstance();
            
            Properties persistenceServiceProperties = new Properties();

            persistenceServiceProperties.put("dbPath", env.getProperty("kuwaiba.dbPath")); //NOI18N
            persistenceServiceProperties.put("dbHost", env.getProperty("kuwaiba.dbHost")); //NOI18N
            persistenceServiceProperties.put("dbPort", env.getProperty("kuwaiba.dbPort", int.class)); //NOI18N
            persistenceServiceProperties.put("backgroundsPath", env.getProperty("kuwaiba.backgroundsPath")); //NOI18N
            persistenceServiceProperties.put("attachmentsPath", env.getProperty("kuwaiba.attachmentsPath")); //NOI18N
            persistenceServiceProperties.put("maxAttachmentSize", env.getProperty("kuwaiba.maxAttachmentSize", int.class)); //NOI18N
            persistenceServiceProperties.put("corporateLogo", env.getProperty("kuwaiba.corporateLogo")); //NOI18N
            persistenceServiceProperties.put("companyName", env.getProperty("kuwaiba.companyName")); //NOI18N
            persistenceServiceProperties.put("enforceBusinessRules", env.getProperty("kuwaiba.enforceBusinessRules", boolean.class)); //NOI18N
            persistenceServiceProperties.put("maxRoutes", env.getProperty("kuwaiba.maxRoutes", int.class)); //NOI18N
            persistenceServiceProperties.put("enableSecurityManager", env.getProperty("kuwaiba.enableSecurityManager", boolean.class)); //NOI18N
            persistenceServiceProperties.put("debugMode", env.getProperty("kuwaiba.debugMode", boolean.class)); //NOI18N
            persistenceServiceProperties.put("processEnginePath", env.getProperty("kuwaiba.processEnginePath")); //NOI18N
            persistenceServiceProperties.put("processesPath", env.getProperty("kuwaiba.processesPath")); //NOI18N
            
            persistenceService.setConfiguration(persistenceServiceProperties);
            persistenceService.start();
        } catch (IllegalStateException ex) {
            System.out.println("[KUWAIBA] Error initializing Persistence Service: " + ex.getMessage());
        }
    }

}
