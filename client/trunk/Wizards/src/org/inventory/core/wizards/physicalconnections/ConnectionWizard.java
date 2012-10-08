/*
 *  Copyright 2010, 2011, 2012 Neotropic SAS <contact@neotropic.co>.
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 */

package org.inventory.core.wizards.physicalconnections;

import org.inventory.core.services.api.LocalObjectLight;

/**
 * This class manages the wizard life cycle
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ConnectionWizard {

    private ConnectionWizardWizardAction action;
    private String connectionClass;
    private LocalObjectLight aSide;
    private LocalObjectLight bSide;
    private LocalObjectLight connectionParent;
    private int wizardType;
    public static final int WIZARDTYPE_CONTAINERS = 1;
    public static final int WIZARDTYPE_CONNECTIONS = 2;
    
    public ConnectionWizard(int wizardType, LocalObjectLight aSide,
            LocalObjectLight bSide, String connectionClass, LocalObjectLight connectionParent) {
        this.connectionClass = connectionClass;
        this.aSide = aSide;
        this.bSide = bSide;
        this.connectionParent = connectionParent;
        this.wizardType = wizardType;
        this.action = new ConnectionWizardWizardAction(this);
    }

    public void show(){
        action.actionPerformed(null);
    }

    public LocalObjectLight getASide() {
        return aSide;
    }

    public LocalObjectLight getBSide() {
        return bSide;
    }

    public String getConnectionClass() {
        return connectionClass;
    }

    public int getWizardType() {
        return wizardType;
    }

    public LocalObjectLight getNewConnection(){
        return action.getNewConnection();
    }

    public LocalObjectLight getConnectionParent() {
        return connectionParent;
    }
}
