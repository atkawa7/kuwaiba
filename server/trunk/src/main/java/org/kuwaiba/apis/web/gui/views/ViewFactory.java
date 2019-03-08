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

package org.kuwaiba.apis.web.gui.views;

import java.lang.reflect.InvocationTargetException;
import org.kuwaiba.apis.persistence.application.ApplicationEntityManager;
import org.kuwaiba.apis.persistence.business.BusinessEntityManager;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;

/**
 * A factory class used to lunch plug and play views.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ViewFactory {
    /**
     * Reference to the Metadata Entity Manager
     */
    private MetadataEntityManager mem;
    /**
     * Reference to the Application Entity Manager
     */
    private ApplicationEntityManager aem;
    /**
     * Reference to the Business Entity Manager
     */
    private BusinessEntityManager bem;
    
    public ViewFactory(MetadataEntityManager mem, ApplicationEntityManager aem, BusinessEntityManager bem) {
        this.mem = mem;
        this.aem = aem;
        this.bem= bem;
    }
    
    /**
     * Creates an instance of a view, given its canonical name (that is, its name including the package information)
     * @param viewId The FQN of the class.
     * @return The instance of the view.
     * @throws java.lang.InstantiationException If the class provided do not exists or it's not subclass of AbstractView.
     */
    public AbstractView createViewInstance(String viewId) throws InstantiationException {
        try {
            Object aView = Class.forName(viewId).getConstructor(MetadataEntityManager.class, ApplicationEntityManager.class, BusinessEntityManager.class).newInstance(mem, aem, bem);
            if (!(aView instanceof AbstractView))
                throw new InstantiationException(String.format("The view identifier provided (%s) is not an AbstractView subclass", viewId));
            
            return (AbstractView)aView;
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException ex) {
            throw new InstantiationException(String.format("View with id %s could not be instantiated: %s", viewId, ex.getLocalizedMessage()));
        }
        
    }
}
