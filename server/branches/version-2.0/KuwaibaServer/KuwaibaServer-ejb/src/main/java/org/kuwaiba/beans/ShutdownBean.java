/**
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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

import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import org.kuwaiba.apis.persistence.PersistenceService;

/**
 * This bean holds the application's shutdown logic
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@Singleton
@Startup
public class ShutdownBean {
    @PreDestroy
    public void shutdown() {
        try {
            PersistenceService persistenceService = PersistenceService.getInstance();
            persistenceService.stop();
        } catch (IllegalStateException ise) {
            System.out.println("[KUWAIBA] " + ise.getMessage());
        }
    }
}
