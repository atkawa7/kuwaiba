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

package org.kuwaiba.web.modules.physicalcon.wizards;

import com.vaadin.server.Page;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.web.gui.actions.AbstractAction;
import org.kuwaiba.apis.web.gui.navigation.BasicIconGenerator;
import org.kuwaiba.apis.web.gui.navigation.nodes.AbstractNode;
import org.kuwaiba.apis.web.gui.navigation.nodes.ChildrenProvider;
import org.kuwaiba.apis.web.gui.navigation.trees.BasicTree;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.apis.web.gui.tools.Wizard;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadataLight;
import org.kuwaiba.services.persistence.util.Constants;

/**
 * A wizard that given two initial objects, guides the user through the creation of a physical connection (link or container)
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class NewPhysicalConnectionWizard extends Wizard {
    
    public NewPhysicalConnectionWizard(RemoteObjectLight rootASide, RemoteObjectLight rootBSide, WebserviceBean wsBean) {
        super(new GeneralInfoStep(rootASide, rootBSide, wsBean));
    }
    
    /**
     * The user must choose if he/she wants to create a link or a container and what template (if any) 
     * should be used and provide general information like the name of the new connection and what class 
     * and template should be used for the new object
     */
    public static class GeneralInfoStep extends FormLayout implements Step {
        /**
         * The name of the new connection
         */
        private TextField txtName;
        /**
         * If the connection is a container or a link
         */
        private ComboBox<ConnectionType> cmbConnectionType;
        /**
         * The connection type (the class the new connection will be spawned from)
         */
        private ComboBox<RemoteClassMetadataLight> cmbConnectionClass;
        /**
         * The list of available templates
         */
        private ComboBox<RemoteObjectLight> cmbTemplates;
        /**
         * Should the connection be created from a template
         */
        private CheckBox chkHasTemplate;
        /**
         * Own properties
         */
        private Properties properties;
        
        private WebserviceBean wsBean;
        
        public GeneralInfoStep(RemoteObjectLight rootASide, RemoteObjectLight rootBSide, WebserviceBean wsBean) {
            this.wsBean = wsBean;
            properties = new Properties();
            properties.put("title", "General Information");
            properties.put("rootASide", rootASide);
            properties.put("rootBSide", rootBSide);
            
            txtName = new TextField("Name");
            txtName.setRequiredIndicatorVisible(true);
            
            cmbConnectionType = new ComboBox<>("Connection Type", Arrays.asList(new ConnectionType(1, "Connect Using a Container"), 
                    new ConnectionType(2, "Connect Using a Link")));
            cmbConnectionType.setEmptySelectionAllowed(false);
            cmbConnectionType.setRequiredIndicatorVisible(true);
            cmbConnectionType.setEmptySelectionCaption("Select a connection type...");
            
            cmbConnectionType.addSelectionListener((newSelection) -> {
                try {
                    if (newSelection.getSelectedItem().isPresent()) {
                        if (newSelection.getSelectedItem().get().getType() == 1)
                            cmbConnectionClass.setItems(this.wsBean.getSubClassesLight(Constants.CLASS_GENERICPHYSICALCONTAINER, false, false, Page.getCurrent().getWebBrowser().getAddress(), 
                                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()));
                        else
                            cmbConnectionClass.setItems(this.wsBean.getSubClassesLight(Constants.CLASS_GENERICPHYSICALLINK, false, false, Page.getCurrent().getWebBrowser().getAddress(), 
                                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()));
                    }
                } catch (ServerSideException ex) {
                    Notifications.showError(ex.getLocalizedMessage());
                }
            });
            
            cmbConnectionClass = new ComboBox<>("Connection Class");
            cmbConnectionClass.setEmptySelectionAllowed(false);
            cmbConnectionClass.setRequiredIndicatorVisible(true);
            cmbConnectionClass.setEmptySelectionCaption("Select a connection class...");
            cmbConnectionClass.addSelectionListener((newSelection) -> {
                try {
                    if (newSelection.getSelectedItem().isPresent()) {
                        cmbTemplates.setItems(this.wsBean.getTemplatesForClass(newSelection.getSelectedItem().get().getClassName(), Page.getCurrent().getWebBrowser().getAddress(), 
                            ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()));
                    }
                } catch (ServerSideException ex) {
                    Notifications.showError(ex.getLocalizedMessage());
                }
            });
            
            cmbTemplates = new ComboBox<>("Template");
            cmbTemplates.setEnabled(false);
            
            chkHasTemplate = new CheckBox("Use Template");
            chkHasTemplate.addValueChangeListener((newSelection) -> {
                cmbTemplates.setEnabled(chkHasTemplate.getValue());
            });
            
            addComponents(txtName, cmbConnectionType, cmbConnectionClass, cmbTemplates, chkHasTemplate);
            setSizeFull();
        }

        @Override
        public Step next() throws InvalidArgumentException {
            if (txtName.getValue().trim().isEmpty() || !cmbConnectionType.getSelectedItem().isPresent() 
                    || !cmbConnectionClass.getSelectedItem().isPresent() || (chkHasTemplate.getValue() && !cmbTemplates.getSelectedItem().isPresent()))
                throw new InvalidArgumentException("Please check that all mandatory fields were filled in correctly");
            properties.put("name", txtName.getValue());
            properties.put("class", cmbConnectionClass.getSelectedItem().get().getClassName());
            
            properties.put("templateId", chkHasTemplate.getValue() ? cmbTemplates.getSelectedItem().get().getId() : "");
            
            if (cmbConnectionType.getSelectedItem().get().type == 1)
                return new SelectContainerEndpointsStep(properties, wsBean);
            else
                return new SelectLinkEndpointsStep(properties, wsBean);
        }

        @Override
        public boolean isFinal() {
            return false;
        }

        @Override
        public Properties getProperties() {
            return properties;
        }
        
        private class ConnectionType {
            private int type;
            private String displayName;

            public ConnectionType(int type, String displayName) {
                this.type = type;
                this.displayName = displayName;
            }

            public int getType() {
                return type;
            }
            
            @Override
            public String toString() {
                return displayName;
            }
        }
    }
    
    /**
     * Step to select the endpoints if the connection type selected in the past step was a container
     */
    public static class SelectContainerEndpointsStep extends HorizontalLayout implements Step {
        /**
         * The tree on the left side of the wizard
         */
        private BasicTree aSideTree;
        /**
         * The tree on the right side of the wizard
         */
        private BasicTree bSideTree;
        /**
         * Own properties
         */
        private Properties properties;
        
        private WebserviceBean wsBean;
        
        /**
         * Default constructor
         * @param properties The set of properties to configure the step. Here are required: 
         * <ul>
         * <li>rootASide: The RemoteObjectLight instance representing the aSide of the connection. Must be a port</li>
         * <li>rootBSide: The RemoteObjectLight instance representing the bSide of the connection. Must be a port</li>
         * <li>name: The name of the new container
         * <li>class: The class of the new container (a RemoteClassMetadataLight instance)</li>
         * <li>templateId: The id of the template to be used to create the object. -1 to create it without template</li>
         * </ul>
         * @param wsBean Reference to the backend bean
         */
        public SelectContainerEndpointsStep(Properties properties, WebserviceBean wsBean) {
            this.wsBean = wsBean;
            this.properties = properties;
            this.properties.put("title", "Select the Container Endpoints");
            
            RemoteSession session = (RemoteSession) UI.getCurrent().getSession().getAttribute("session");
            
            this.aSideTree = new BasicTree(
            new ChildrenProvider<RemoteObjectLight, RemoteObjectLight>() {
                    @Override
                    public List<RemoteObjectLight> getChildren(RemoteObjectLight c) {
                        try {
                            return wsBean.getObjectChildren(c.getClassName(), 
                                c.getId(), -1, Page.getCurrent().getWebBrowser().getAddress(),
                                    session.getSessionId());

                        } catch (ServerSideException ex) {
                            Notifications.showError(ex.getLocalizedMessage());
                            return Collections.EMPTY_LIST;
                        }
                    }
                }, new BasicIconGenerator(wsBean, session), 
                new AbstractNode<RemoteObjectLight>((RemoteObjectLight)properties.get("rootASide")) {
                    @Override
                    public AbstractAction[] getActions() { return new AbstractAction[0]; }

                    @Override
                    public void refresh(boolean recursive) { }
            });
            this.aSideTree.setSelectionMode(Grid.SelectionMode.SINGLE);
            this.aSideTree.setSizeUndefined();
            
            this.bSideTree = new BasicTree(
            new ChildrenProvider<RemoteObjectLight, RemoteObjectLight>() {
                    @Override
                    public List<RemoteObjectLight> getChildren(RemoteObjectLight c) {
                        try {
                            return wsBean.getObjectChildren(c.getClassName(), 
                                c.getId(), -1, Page.getCurrent().getWebBrowser().getAddress(),
                                    session.getSessionId());

                        } catch (ServerSideException ex) {
                            Notifications.showError(ex.getLocalizedMessage());
                            return Collections.EMPTY_LIST;
                        }
                    }
                }, new BasicIconGenerator(wsBean, session), 
                new AbstractNode<RemoteObjectLight>((RemoteObjectLight)properties.get("rootBSide")) {
                    @Override
                    public AbstractAction[] getActions() { return new AbstractAction[0]; }

                    @Override
                    public void refresh(boolean recursive) { }
            });
            this.bSideTree.setSelectionMode(Grid.SelectionMode.SINGLE);
            this.aSideTree.setSizeUndefined();
            
            this.addComponents(aSideTree, bSideTree);
            this.setSpacing(true);
            this.setWidth(100, Unit.PERCENTAGE);
        }

        @Override
        public Step next() throws InvalidArgumentException {
            if (aSideTree.getSelectedItems().isEmpty() || bSideTree.getSelectedItems().isEmpty())
                throw new InvalidArgumentException("You have to select both endpoints");
            
            RemoteSession session = (RemoteSession) UI.getCurrent().getSession().getAttribute("session");
            
            RemoteObjectLight selectedASide = (RemoteObjectLight)aSideTree.getSelectedItems().iterator().next().getObject();
            RemoteObjectLight selectedBSide = (RemoteObjectLight)bSideTree.getSelectedItems().iterator().next().getObject();
            try {
                if (wsBean.isSubclassOf(selectedASide.getClassName(),Constants.CLASS_GENERICPORT, Page.getCurrent().getWebBrowser().getAddress(),
                                session.getSessionId()) || wsBean.isSubclassOf(selectedBSide.getClassName(),Constants.CLASS_GENERICPORT, Page.getCurrent().getWebBrowser().getAddress(),
                                session.getSessionId()))
                    throw new InvalidArgumentException("Ports can not be endpoints to containers");
                else {
                    properties.put("aSide", selectedASide);
                    properties.put("bSide", selectedBSide);
                    
                    String newConnection = wsBean.createPhysicalConnection(selectedASide.getClassName(), selectedASide.getId(), selectedBSide.getClassName(), 
                            selectedBSide.getId(), properties.getProperty("name"), properties.getProperty("class"), 
                            (String)properties.get("templateId"),  Page.getCurrent().getWebBrowser().getAddress(),
                                session.getSessionId());
                    
                    properties.put("connection", new RemoteObjectLight(properties.getProperty("class"), newConnection, properties.getProperty("name")));
                    
                    return null;
                }
            } catch (ServerSideException ex) {
                throw new InvalidArgumentException(ex.getLocalizedMessage());
            }
        }

        @Override
        public boolean isFinal() {
            return true;
        }
        
        @Override
        public Properties getProperties() {
            return properties;
        }
    }
    
    /**
     * Step to select the endpoints if the connection type selected in the past step was a link
     */
    public static class SelectLinkEndpointsStep extends HorizontalLayout implements Step {
        /**
         * The tree on the left side of the wizard
         */
        private BasicTree aSideTree;
        /**
         * The tree on the right side of the wizard
         */
        private BasicTree bSideTree;
        /**
         * Own properties
         */
        private Properties properties;
        
        private WebserviceBean wsBean;
        
        /**
         * Default constructor
         * @param properties The set of properties to configure the step. Here are required: 
         * <ul>
         * <li>aSide: The RemoteObjectLight instance representing the aSide of the connection. Must be a port</li>
         * <li>bSide: The RemoteObjectLight instance representing the bSide of the connection. Must be a port</li>
         * <li>templateId: The id of the template to be used to create the object. -1 to create it without template</li>
         * </ul>
         * @param wsBean Reference to the backend bean
         */
        public SelectLinkEndpointsStep(Properties properties, WebserviceBean wsBean) {
            this.wsBean = wsBean;
            this.properties = properties;
            this.properties.put("title", "Select the Link Endpoints");
            
            RemoteSession session = (RemoteSession) UI.getCurrent().getSession().getAttribute("session");
            
            this.aSideTree = new BasicTree(
            new ChildrenProvider<RemoteObjectLight, RemoteObjectLight>() {
                    @Override
                    public List<RemoteObjectLight> getChildren(RemoteObjectLight c) {
                        try {
                            return wsBean.getObjectChildren(c.getClassName(), 
                                c.getId(), -1, Page.getCurrent().getWebBrowser().getAddress(),
                                    session.getSessionId());

                        } catch (ServerSideException ex) {
                            Notifications.showError(ex.getLocalizedMessage());
                            return Collections.EMPTY_LIST;
                        }
                    }
                }, new BasicIconGenerator(wsBean, session), 
                new AbstractNode<RemoteObjectLight>((RemoteObjectLight)properties.get("rootASide")) {
                    @Override
                    public AbstractAction[] getActions() { return new AbstractAction[0]; }

                    @Override
                    public void refresh(boolean recursive) { }
            });
            this.aSideTree.setSelectionMode(Grid.SelectionMode.SINGLE);
            
            this.bSideTree = new BasicTree(
            new ChildrenProvider<RemoteObjectLight, RemoteObjectLight>() {
                    @Override
                    public List<RemoteObjectLight> getChildren(RemoteObjectLight c) {
                        try {
                            return wsBean.getObjectChildren(c.getClassName(), 
                                c.getId(), -1, Page.getCurrent().getWebBrowser().getAddress(),
                                    session.getSessionId());

                        } catch (ServerSideException ex) {
                            Notifications.showError(ex.getLocalizedMessage());
                            return Collections.EMPTY_LIST;
                        }
                    }
                }, new BasicIconGenerator(wsBean, session), 
                new AbstractNode<RemoteObjectLight>((RemoteObjectLight)properties.get("rootBSide")) {
                    @Override
                    public AbstractAction[] getActions() { return new AbstractAction[0]; }

                    @Override
                    public void refresh(boolean recursive) { }
            });
            this.bSideTree.setSelectionMode(Grid.SelectionMode.SINGLE);
            
            this.addComponents(aSideTree, bSideTree);
            this.setSizeUndefined();
        }

        @Override
        public Step next() throws InvalidArgumentException {
            if (aSideTree.getSelectedItems().isEmpty() || bSideTree.getSelectedItems().isEmpty())
                throw new InvalidArgumentException("You have to select both endpoints");
            
            RemoteSession session = (RemoteSession) UI.getCurrent().getSession().getAttribute("session");
            
            RemoteObjectLight selectedASide = (RemoteObjectLight)aSideTree.getSelectedItems().iterator().next().getObject();
            RemoteObjectLight selectedBSide = (RemoteObjectLight)bSideTree.getSelectedItems().iterator().next().getObject();
            try {
                if (!wsBean.isSubclassOf(selectedASide.getClassName(),Constants.CLASS_GENERICPORT, Page.getCurrent().getWebBrowser().getAddress(),
                                session.getSessionId()) || !wsBean.isSubclassOf(selectedBSide.getClassName(),Constants.CLASS_GENERICPORT, Page.getCurrent().getWebBrowser().getAddress(),
                                session.getSessionId()))
                    throw new InvalidArgumentException("Only ports can be connected using links");
                else {
                    properties.put("aSide", selectedASide);
                    properties.put("bSide", selectedBSide);
                    
                    String newConnection = wsBean.createPhysicalConnection(selectedASide.getClassName(), selectedASide.getId(), selectedBSide.getClassName(), 
                            selectedBSide.getId(), properties.getProperty("name"), properties.getProperty("class"), 
                            (String)properties.get("templateId"),  Page.getCurrent().getWebBrowser().getAddress(),
                                session.getSessionId());
                    
                    properties.put("connection", new RemoteObjectLight(properties.getProperty("class"), newConnection, properties.getProperty("name")));
                    
                    return null;
                }
            } catch (ServerSideException ex) {
                throw new InvalidArgumentException(ex.getLocalizedMessage());
            }
        }

        @Override
        public boolean isFinal() {
            return true;
        }
        
        @Override
        public Properties getProperties() {
            return properties;
        }
    }
    
    
}
