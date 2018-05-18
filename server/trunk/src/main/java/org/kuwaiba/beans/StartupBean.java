/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>
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
package org.kuwaiba.beans;

import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.kuwaiba.apis.persistence.PersistenceService;

/**
 * This bean holds the application's startup logic
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@Singleton
@Startup
public class StartupBean {
    
    @PostConstruct
    public void init() {
        try {
            PersistenceService persistenceService = PersistenceService.getInstance();
            Context context = new InitialContext();
            Properties persistenceServiceProperties = new Properties();
            try {
                persistenceServiceProperties.put("dbPath", (String)context.lookup("java:comp/env/dbPath")); //NOI18N
            }catch (NamingException ne) {
                persistenceServiceProperties.put("dbPath","/data/db/kuwaiba.db"); //NOI18N
                System.out.println("[KUWAIBA] Error reading the dbPath configuration variable. Using the default value instead: " + ne.getMessage());
            }
            
            try {
                persistenceServiceProperties.put("backgroundsPath", (String)context.lookup("java:comp/env/backgroundsPath")); //NOI18N
            }catch (NamingException ne) {
                persistenceServiceProperties.put("backgroundsPath", "/data/img/backgrounds"); //NOI18N
                System.out.println("[KUWAIBA] Error reading the backgroundsPath configuration variable. Using the default value instead: " + ne.getMessage());
            }
            
            try {
                persistenceServiceProperties.put("attachmentsPath", (String)context.lookup("java:comp/env/attachmentsPath")); //NOI18N
            }catch (NamingException ne) {
                persistenceServiceProperties.put("attachmentsPath", "/data/files/attachments"); //NOI18N
                System.out.println("[KUWAIBA] Error reading the attachmentsPath configuration variable. Using the default value instead: " + ne.getMessage());
            }
            
            try {
                persistenceServiceProperties.put("maxAttachmentSize", (float)context.lookup("java:comp/env/maxAttachmentSize")); //NOI18N
            }catch (NamingException ne) {
                persistenceServiceProperties.put("maxAttachmentSize", 10); //NOI18N
                System.out.println("[KUWAIBA] Error reading the maxAttachmentSize configuration variable. Using the default value instead: " + ne.getMessage());
            }
            
            try {
                persistenceServiceProperties.put("corporateLogo", (String)context.lookup("java:comp/env/corporateLogo")); //NOI18N
            }catch (NamingException ne) {
                persistenceServiceProperties.put("corporateLogo", "http://neotropic.co/img/logo_blue.png"); //NOI18N
                System.out.println("[KUWAIBA] Error reading the corporateLogo configuration variable. Using the default value instead: " + ne.getMessage());
            }
            
            try {
                persistenceServiceProperties.put("companyName", (String)context.lookup("java:comp/env/companyName")); //NOI18N
            }catch (NamingException ne) {
                persistenceServiceProperties.put("companyName", "Neotropic SAS"); //NOI18N
                System.out.println("[KUWAIBA] Error reading the companyName configuration variable. Using the default value instead: " + ne.getMessage());
            }
            
            try {
                persistenceServiceProperties.put("enforceBusinessRules", context.lookup("java:comp/env/enforceBusinessRules")); //NOI18N
            }catch (NamingException ne) {
                persistenceServiceProperties.put("enforceBusinessRules", false); //NOI18N
                System.out.println("[KUWAIBA] Error reading the enforceBusinessRules configuration variable. Using the default value instead: " + ne.getMessage());
            }
            
            try {
                persistenceServiceProperties.put("maxRoutes", context.lookup("java:comp/env/maxRoutes")); //NOI18N
            }catch (NamingException ne) {
                persistenceServiceProperties.put("maxRoutes", 10); //NOI18N
                System.out.println("[KUWAIBA] Error reading the maxRoutes configuration variable. Using the default value instead: " + ne.getMessage());
            }
            
            try {
                persistenceServiceProperties.put("enableSecurityManager", context.lookup("java:comp/env/enableSecurityManager")); //NOI18N
            }catch (NamingException ne) {
                persistenceServiceProperties.put("enableSecurityManager", false);
                System.out.println("[KUWAIBA] Error reading the enableSecurityManager configuration variable. Using the default value instead: " + ne.getMessage());
            }
            
            try {
                persistenceServiceProperties.put("formFilesPath", (String)context.lookup("java:comp/env/formFilesPath")); //NOI18N
            }catch (NamingException ne) {
                persistenceServiceProperties.put("formFilesPath", "/data/files/forms"); //NOI18N
                System.out.println("[KUWAIBA] Error reading the formFilesPath configuration variable. Using the default value instead: " + ne.getMessage());
            }
            
            try {
                persistenceServiceProperties.put("formImagesPath", (String)context.lookup("java:comp/env/formImagesPath")); //NOI18N
            }catch (NamingException ne) {
                persistenceServiceProperties.put("formImagesPath", "/data/img/forms"); //NOI18N
                System.out.println("[KUWAIBA] Error reading the formImagesPath configuration variable. Using the default value instead: " + ne.getMessage());
            }
            
            persistenceService.setConfiguration(persistenceServiceProperties);
            persistenceService.start();
        } catch (IllegalStateException | NamingException ex) {
            System.out.println("[KUWAIBA] Error initializing Persistence Service: " + ex.getMessage());
        }
    }
}
