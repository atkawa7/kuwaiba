/*
 *  Copyright 2011 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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
 *  under the License.
 */

package org.kuwaiba.tools;

import org.kuwaiba.core.exceptions.EntityManagerNotAvailableException;
import java.util.List;
import javax.ejb.Remote;

/**
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@Remote
public interface ToolsBeanRemote {
    /**
     * Checks for entity classes without proper accessors
     * @return a list of strings with the class name and the corresponding problem
     */
    public List<String> diagnoseAccessors();
    /**
     * Resets the class metadata information as well as the containment information
     * @throws Exception
     */
    public void buildMetaModel() throws Exception;
    /**
     * Creates/reset the admin account (username = admin, password = kuwaiba)
     */
    public void resetAdmin()  throws EntityManagerNotAvailableException;
}
