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
package org.inventory.scriptqueries;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.HashMap;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalScriptQuery;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;

/**
 * Service class for this module. It also implements the property change listener for the ScripttQeryNodes
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ScriptQueriesManagerService implements VetoableChangeListener {
    private static ScriptQueriesManagerService instance;
    
    private ScriptQueriesManagerService() {
    }
    
    public static ScriptQueriesManagerService getInstance() {
        return instance == null ? instance = new ScriptQueriesManagerService() : instance;        
    }

    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        LocalScriptQuery scriptQuery = (LocalScriptQuery) evt.getSource();
        
        switch (evt.getPropertyName()) {
            case Constants.PROPERTY_NAME:
            case Constants.PROPERTY_DESCRIPTION:
            case Constants.PROPERTY_SCRIPT:
            case Constants.PROPERTY_COUNTABLE:
                if (!CommunicationsStub.getInstance().updateScriptQueryProperties(scriptQuery.getId(), evt.getPropertyName(), (String) evt.getNewValue())) {
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                        NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                    throw new PropertyVetoException(null, null); //This exception goes to /dev/null anyway
                }
                break;
            default: // The rest are script query parameters
                if (evt.getPropertyName() != null && evt.getPropertyName().contains("PARAM_")) { //NOI18N
                    HashMap<String, String> parameters = new HashMap();
                    parameters.put(evt.getPropertyName().replaceFirst("PARAM_", ""), (String) evt.getNewValue()); //NOI18N

                    if (!CommunicationsStub.getInstance().updateScriptQueryParameters(scriptQuery.getId(), parameters)) {
                        NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                            NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                        throw new PropertyVetoException(null, null); //This exception goes to /dev/null anyway                    
                    }
                }
                break;
        }
    }
    
    
}
