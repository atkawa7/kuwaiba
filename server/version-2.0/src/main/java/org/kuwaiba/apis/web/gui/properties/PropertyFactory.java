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
package org.kuwaiba.apis.web.gui.properties;

import com.vaadin.ui.UI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadata;

/**
 * A factory class that builds property sets given business objects.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class PropertyFactory {
    /**
     * Builds a property set from a given inventory object
     * @param businessObject The business object
     * @param wsBean A reference to the backend bean
     * @return The set of properties ready to used in a property sheet component
     * @throws org.kuwaiba.exceptions.ServerSideException if retrieving the class metadata or the attribute values raised an exception
     */
    public static List<AbstractProperty> propertiesFromRemoteObject(RemoteObjectLight businessObject, WebserviceBean wsBean) throws ServerSideException {
        
        RemoteSession session = (RemoteSession) UI.getCurrent().getSession().getAttribute("session");
        
        HashMap<String, String> objectAttributes = wsBean.getAttributeValuesAsString(businessObject.getClassName(), businessObject.getId(), 
                session.getIpAddress(), 
                session.getSessionId());

            RemoteClassMetadata classMetadata = wsBean.getClass(businessObject.getClassName(), session.getIpAddress(), 
                    session.getSessionId());
            
            ArrayList<AbstractProperty> objectProperties = new ArrayList<>();
            
            for (int i = 0; i < classMetadata.getAttributesNames().length; i++)
                objectProperties.add(new StringProperty(classMetadata.getAttributesNames()[i], 
                        classMetadata.getAttributesDisplayNames()[i], classMetadata.getAttributesDescriptions()[i], 
                        objectAttributes.get(classMetadata.getAttributesNames()[i]) == null ? "<Not Set>" : objectAttributes.get(classMetadata.getAttributesNames()[i])));
        
        return objectProperties;
    }
}
