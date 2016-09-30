/**
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>
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
            persistenceServiceProperties.setProperty("dbPath", (String)context.lookup("java:comp/env/dbPath")); //NOI18N
            persistenceServiceProperties.setProperty("backgroundsPath", (String)context.lookup("java:comp/env/backgroundsPath")); //NOI18N
            persistenceServiceProperties.setProperty("corporateLogo", (String)context.lookup("java:comp/env/corporateLogo")); //NOI18N
            persistenceService.setConfiguration(persistenceServiceProperties);
            persistenceService.start();
        }catch (IllegalStateException ise) {
            System.out.println("[KUWAIBA] Error initializing Persistence Service: " + ise.getMessage());
        }catch (NamingException ne) {
            System.out.println("[KUWAIBA] Error reading Persistence Service configuration variables: " + ne.getMessage());
        }
    }

}
