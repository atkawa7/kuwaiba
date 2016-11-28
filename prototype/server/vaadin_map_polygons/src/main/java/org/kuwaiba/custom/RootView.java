/*
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
package org.kuwaiba.custom;

import com.google.common.eventbus.EventBus;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;

/**
 * This view organize the interaction between other containers components
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
@SuppressWarnings("serial")
public class RootView extends CustomComponent {
    
    public RootView(VaadinSession session) {
//        session.setAttribute(ConnectionButton.NAME, true);
//        session.setAttribute(MarkerButton.NAME, true);
//        session.setAttribute(MeasureButton.NAME, true);
        
        EventBus eventBus = new EventBus();
        
//        VerticalLayout hLayout = new VerticalLayout();
        HorizontalLayout hLayout = new HorizontalLayout();
        
        TreeView treeView = new TreeView(eventBus);
        
        GISView gisView = new GISView(eventBus, session);
        
        PropertyView propertyView = new PropertyView(eventBus);
        
        eventBus.register(treeView); // subscribers
        eventBus.register(propertyView); // subscribers
        
        hLayout.addComponent(treeView);
        hLayout.addComponent(gisView);
        hLayout.addComponent(propertyView);
        /*
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
            XMLEventWriter xmlew;
            xmlew = xmlof.createXMLEventWriter(baos);
            XMLEventFactory xmlef = XMLEventFactory.newInstance();
            QName qnameHierarchy = new QName("hierarchy");
            xmlew.add(xmlef.createStartElement(qnameHierarchy, null, null));
            xmlew.add(xmlef.createEndElement(qnameHierarchy, null));
            xmlew.close();
            
            File file = new File("/data/files/same.txt");            
            // Write
            
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(baos.toByteArray());
            fos.close();
            
            // Read
            
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] bytes = new byte[(int) file.length()];
            fileInputStream.read(bytes);
            fileInputStream.close();
            
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        */
        setCompositionRoot(hLayout);        
    }
}
