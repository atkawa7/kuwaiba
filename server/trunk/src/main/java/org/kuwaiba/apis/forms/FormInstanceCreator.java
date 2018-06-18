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
package org.kuwaiba.apis.forms;

import com.vaadin.server.Page;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import org.kuwaiba.apis.forms.elements.AbstractElementField;
import org.kuwaiba.apis.forms.elements.AbstractFormInstanceCreator;
import org.kuwaiba.apis.forms.elements.Constants;
import org.kuwaiba.apis.forms.elements.FormStructure;
import org.kuwaiba.apis.forms.elements.XMLUtil;
import org.kuwaiba.apis.web.gui.util.NotificationsUtil;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadata;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadataLight;
import org.kuwaiba.beans.WebserviceBean;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class FormInstanceCreator extends AbstractFormInstanceCreator {
    private final WebserviceBean wsBean;
    private final RemoteSession session;

    public FormInstanceCreator(FormStructure formStructure, WebserviceBean wsBean, RemoteSession session) {
        super(formStructure);
        this.wsBean = wsBean;
        this.session = session;
    }

    @Override
    protected void addRemoteObjectLight(XMLEventWriter xmlew, XMLEventFactory xmlef, AbstractElementField element) throws XMLStreamException {
        if (element.getValue() instanceof RemoteObjectLight) {
                        
            RemoteObjectLight remoteObjectLight = (RemoteObjectLight) element.getValue();
                        
            try {
                RemoteClassMetadata classInfo = wsBean.getClass(remoteObjectLight.getClassName(), Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId());
                
                XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.OBJECT_ID, String.valueOf(remoteObjectLight.getId()));
                XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.OBJECT_NAME, remoteObjectLight.getName());
                XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.CLASS_ID, String.valueOf(classInfo.getId()));
                
            } catch (ServerSideException ex) {
                NotificationsUtil.showError(ex.getMessage());
            }
        }
    }

    @Override
    protected void addClassInfoLight(XMLEventWriter xmlew, XMLEventFactory xmlef, AbstractElementField element) throws XMLStreamException {
        if (element.getValue() instanceof RemoteClassMetadataLight) {
            
            RemoteClassMetadataLight classInfoLight = (RemoteClassMetadataLight) element.getValue();
            
            XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.CLASS_ID, String.valueOf(classInfoLight.getId()));
            XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.CLASS_NAME, classInfoLight.getClassName());
        }
    }
    
}
