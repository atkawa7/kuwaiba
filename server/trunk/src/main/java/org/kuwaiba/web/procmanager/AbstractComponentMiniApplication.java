/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>
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
 */
package org.kuwaiba.web.procmanager;

import com.vaadin.ui.Component;
import java.util.Properties;
import org.kuwaiba.apis.web.gui.miniapps.AbstractMiniApplication;
import org.kuwaiba.beans.WebserviceBean;

/**
 * A Component Mini Application is a graphical element to be added to a Vaadin
 * Graphical Application
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public abstract class AbstractComponentMiniApplication extends AbstractMiniApplication<Component, Component> {
    private WebserviceBean webserviceBean;
    
    public AbstractComponentMiniApplication(Properties inputParameters) {
        super(inputParameters);
    }
    
    public WebserviceBean getWebserviceBean() {
        return webserviceBean;
    }
    
    public void setWebserviceBean(WebserviceBean webserviceBean) {
        this.webserviceBean = webserviceBean;                
    }
    
    public Properties getInputParameters() {
        return inputParameters;
    }
    
    public void setInputParameters(Properties inputParameters) {
        this.inputParameters = inputParameters;        
    }
    
    public Properties getOutputParameters() {
        return miniApplicationData;
    }
    
    public void setOutputParameters(Properties outputParameters) {
        this.miniApplicationData = outputParameters;
    }
}
