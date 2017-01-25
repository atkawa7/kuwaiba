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
package com.neotropic.inventory.modules.sdh.wizard;

import com.neotropic.inventory.modules.sdh.LocalSDHContainerLinkDefinition;
import com.neotropic.inventory.modules.sdh.LocalSDHPosition;
import com.neotropic.inventory.modules.sdh.SDHConfigurationObject;
import com.neotropic.inventory.modules.sdh.SDHModuleService;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalObjectLightList;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.ExplorablePanel;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.explorer.view.BeanTreeView;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 * This is the wizard to make SDH connections
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class SDHConnectionWizard {
    private CommunicationsStub com = CommunicationsStub.getInstance();
    

    public LocalObjectLight run(LocalObjectLight equipmentA, LocalObjectLight equipmentB) {        
        SDHConfigurationObject configObject = Lookup.getDefault().lookup(SDHConfigurationObject.class);
        WizardDescriptor wizardDescriptor;
        switch ((Connections)configObject.getProperty("connectionType")) { //There's a different set of steps depending on what we're gonna create
            default:
            case CONNECTION_TRANSPORTLINK:
                wizardDescriptor = new WizardDescriptor(new WizardDescriptor.Panel[] {
                    new ConnectionGeneralInfoStep((Connections)configObject.getProperty("connectionType")),
                    new ChooseConnectionEndpointsStep(equipmentA, equipmentB)});
                
                initWizardDescriptor(wizardDescriptor, new String[] { "Fill in the general information", "Choose the endpoints" });
                Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);

                dialog.setVisible(true);
                dialog.toFront();

                //The thread will be blocked either Cancel or Finish is clicked
                if (wizardDescriptor.getValue() == WizardDescriptor.FINISH_OPTION) {
                    LocalObjectLight sourcePort = (LocalObjectLight)wizardDescriptor.getProperty("sourcePort");
                    LocalObjectLight targetPort = (LocalObjectLight)wizardDescriptor.getProperty("targetPort");
                    LocalClassMetadataLight connectionType = (LocalClassMetadataLight)wizardDescriptor.getProperty("connectionType");
                    String connectionName = (String)wizardDescriptor.getProperty("connectionName");
                    LocalObjectLight newTransportLink = com.createSDHTransportLink(sourcePort, targetPort, connectionType.getClassName(), connectionName);
                    if (newTransportLink == null) {
                        NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
                        return null;
                    } else 
                        return newTransportLink;
                } else
                    return null;
            case CONNECTION_CONTAINERLINK:
                wizardDescriptor = new WizardDescriptor(new WizardDescriptor.Panel[] {
                    new ConnectionGeneralInfoStep((Connections)configObject.getProperty("connectionType")),
                    new ChooseRouteStep(equipmentA, equipmentB),
                    new ChooseContainerLinkResourcesStep()});
                
                initWizardDescriptor(wizardDescriptor, new String[] { "Fill in the general information", "Choose the route", "Choose positions" });
                dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);

                dialog.setVisible(true);
                dialog.toFront();
                //The thread will be blocked either Cancel or Finish is clicked
                if (wizardDescriptor.getValue() == WizardDescriptor.FINISH_OPTION) {
                    LocalClassMetadataLight connectionType = (LocalClassMetadataLight)wizardDescriptor.getProperty("connectionType");
                    String connectionName = (String)wizardDescriptor.getProperty("connectionName");
                    List<LocalSDHPosition> positions = (List<LocalSDHPosition>)wizardDescriptor.getProperty("positions");
                    LocalObjectLight newContainerLink = com.createSDHContainerLink(equipmentA, equipmentB, connectionType.getClassName(), positions, connectionName);
                    if (newContainerLink == null)
                        NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
                    else {
                        NotificationUtil.getInstance().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE, "Container successfully created");
                        return newContainerLink;
                    }
                }
                return null;
            case CONNECTION_TRIBUTARYLINK:
                wizardDescriptor = new WizardDescriptor(new WizardDescriptor.Panel[] {
                    new ConnectionGeneralInfoStep((Connections)configObject.getProperty("connectionType")),
                    new ChooseRouteStep(equipmentA, equipmentB),
                    new ChooseTributaryLinkResourcesStep(),
                    new ChooseConnectionEndpointsStep(equipmentA, equipmentB),
                    new ChooseServiceStep(SDHModuleService.CLASS_GENERICSDHSERVICE)});
                
                initWizardDescriptor(wizardDescriptor, new String[] { "Fill in the general information", "Choose the route", "Choose positions", "Choose the endpoints", "Choose a service (optional)" });
                dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);

                dialog.setVisible(true);
                dialog.toFront();
                //The thread will be blocked either Cancel or Finish is clicked
                if (wizardDescriptor.getValue() == WizardDescriptor.FINISH_OPTION) {
                    LocalClassMetadataLight connectionType = (LocalClassMetadataLight)wizardDescriptor.getProperty("connectionType");
                    String connectionName = (String)wizardDescriptor.getProperty("connectionName");
                    List<LocalSDHPosition> positions = (List<LocalSDHPosition>)wizardDescriptor.getProperty("positions");
                    LocalObjectLight sourcePort = (LocalObjectLight)wizardDescriptor.getProperty("sourcePort");
                    LocalObjectLight targetPort = (LocalObjectLight)wizardDescriptor.getProperty("targetPort");
                    
                    LocalObjectLight newTributaryLink = com.createSDHTributaryLink(sourcePort, targetPort, connectionType.getClassName(), positions, connectionName);
                    if (newTributaryLink == null)
                        NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
                    else {
                        LocalObjectLight service = (LocalObjectLight)wizardDescriptor.getProperty("service");
                        if (service != null) {
                            if (!com.associateObjectsToService(new String[] { newTributaryLink.getClassName() }, new Long[] { newTributaryLink.getOid()}, service.getClassName(), service.getOid()))
                                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.INFO_MESSAGE, com.getError());
                        }
                        NotificationUtil.getInstance().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE, "Tributary link successfully created");
                        return newTributaryLink;
                    }
                }
                return null;
        }  
    }
    
    public void initWizardDescriptor(WizardDescriptor wizardDescriptor, String[] labels) {
        //How the title of the panels should be displayed (by default it says something like "PANEL_NAME wizard STEPX of Y")
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
        //See WizardDescriptor.PROP_AUTO_WIZARD_STYLE documentation for a complete list of things you are enabling here
        wizardDescriptor.putProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE);
        //An image and the list of steps should be shown in a panel on the left side of the wizard?
        wizardDescriptor.putProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE);
        //Should the steps be numbered in the panel on the left side?
        wizardDescriptor.putProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE);
        //The list of steps on the left panel of the wizard
        wizardDescriptor.putProperty(WizardDescriptor.PROP_CONTENT_DATA, labels);
        wizardDescriptor.setTitle("SDH Connection Wizard");
    }
    
    private class ConnectionGeneralInfoStep implements WizardDescriptor.ValidatingPanel<WizardDescriptor> {
        private JComplexDialogPanel thePanel;

        public ConnectionGeneralInfoStep(Connections connection) {
            final JTextField txtConnectionName = new JTextField(20);                 
            txtConnectionName.setName("txtConnectionName"); //NOI18N
            final JComboBox<LocalClassMetadataLight> lstConnectionTypes;
            
            List<LocalClassMetadataLight> connectionClasses;
            
            switch (connection) {
                default:
                case CONNECTION_TRANSPORTLINK:
                    connectionClasses = com.getLightSubclasses("GenericSDHTransportLink", false, false);
                    break;
                case CONNECTION_CONTAINERLINK:
                    connectionClasses = com.getLightSubclasses("GenericSDHHighOrderContainerLink", false, false);
                    break;
                case CONNECTION_TRIBUTARYLINK:
                    connectionClasses = com.getLightSubclasses("GenericSDHTributaryLink", false, false);
                    break;
            }
            
            if (connectionClasses == null )
                lstConnectionTypes = new JComboBox<>();
            else
                lstConnectionTypes = new JComboBox<>(connectionClasses.toArray(new LocalClassMetadataLight[0]));
            
            lstConnectionTypes.setName("lstConnectionTypes"); //NOI18N
            
            thePanel = new JComplexDialogPanel(new String[] {"Connection name", "Connection type"}, new JComponent[] {txtConnectionName, lstConnectionTypes});
            thePanel.setName("General Information");
            //Shows what step we're in on the left panel of the wizard
            thePanel.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, 0);
        }
        
        @Override
        public Component getComponent() {
            return thePanel;
        }

        @Override
        public HelpCtx getHelp() {
            return null;
        }

        @Override
        public void readSettings(WizardDescriptor settings) {}

        @Override
        public void storeSettings(WizardDescriptor settings) {
            settings.putProperty("connectionName", ((JTextField)thePanel.getComponent("txtConnectionName")).getText());
            settings.putProperty("connectionType", ((JComboBox)thePanel.getComponent("lstConnectionTypes")).getSelectedItem());
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public void addChangeListener(ChangeListener l) {
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
        }
        
        @Override
        public void validate() throws WizardValidationException {
            if (((JTextField)thePanel.getComponent("txtConnectionName")).getText().trim().isEmpty())
                throw new WizardValidationException(thePanel.getComponent("txtConnectionName"), "The connection name can not be empty", null);
        }   
    }
    
    private class ChooseRouteStep implements WizardDescriptor.ValidatingPanel<WizardDescriptor>, ItemListener {
        private JPanel thePanel;
        private JComboBox<Route> lstRoutes;
        private JList<LocalObjectLight> lstRouteDetail;
        private JScrollPane pnlRouteDetailScroll;
        private LocalObjectLight endpointA;
        private LocalObjectLight endpointB;
        private LocalClassMetadataLight connectionType;
        private boolean valid;
        
        public ChooseRouteStep(LocalObjectLight endpointA, LocalObjectLight endpointB) {
            this.endpointA = endpointA;
            this.endpointB = endpointB;
        }
        
        @Override
        public void validate() throws WizardValidationException {
            if (lstRoutes == null || lstRoutes.getSelectedItem() == null)
                throw new WizardValidationException(thePanel, "No routes were found between these equipment", null);
        }

        @Override
        public Component getComponent() {
            return thePanel;
        }

        @Override
        public HelpCtx getHelp() {
            return null;
        }

        @Override
        public void readSettings(WizardDescriptor settings) {
            connectionType = (LocalClassMetadataLight)settings.getProperty("connectionType");
            initComponents();
        }

        @Override
        public void storeSettings(WizardDescriptor settings) {
            lstRoutes.removeItemListener(this);
            settings.putProperty("route", lstRoutes.getSelectedItem());
        }

        @Override
        public boolean isValid() {
            return valid;
        }

        @Override
        public void addChangeListener(ChangeListener l) {}

        @Override
        public void removeChangeListener(ChangeListener l) {}
        
        private void initComponents() {
            valid = true;
            List<LocalObjectLightList> routes;
            if (com.isSubclassOf(connectionType.getClassName(), SDHModuleService.CLASS_GENERICSDHHIGHORDERCONTAINERLINK) ||
                    com.isSubclassOf(connectionType.getClassName(), SDHModuleService.CLASS_GENERICSDHHIGHORDERTRIBUTARYLINK))
                routes = com.findRoutesUsingTransportLinks(endpointA, endpointB);
            else
                routes = com.findRoutesUsingContainerLinks(endpointA, endpointB);
            
            //The panel is created here, so it exists even in case of error
            thePanel = new JPanel(new BorderLayout());
            thePanel.setName("Choose a route");
            
            if (routes == null) {
                JOptionPane.showMessageDialog(null, com.getError(), "Error calculating routes", JOptionPane.ERROR_MESSAGE);
                valid = false;
                return;
            }
            
            if (routes.isEmpty()) {
                JOptionPane.showMessageDialog(null, "No routes were found between these equipment", "Route Calculation", JOptionPane.INFORMATION_MESSAGE);
                valid  = false;
                return;
            }
            
            lstRoutes = new JComboBox<>();
            lstRouteDetail = new JList<>();
            
            lstRoutes.addItemListener(this);
            
            pnlRouteDetailScroll = new JScrollPane(lstRouteDetail);
            
            int i = 1;
            for (LocalObjectLightList route : routes) {
                lstRoutes.addItem(new Route(String.format("Route %s", i), route));
                i ++;
            }
            
            if (!routes.isEmpty())
                lstRouteDetail.setSelectedIndex(0);
            
            thePanel.add(lstRoutes, BorderLayout.NORTH);
            thePanel.add(pnlRouteDetailScroll, BorderLayout.CENTER);
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            
            Route selectedRoute = (Route)e.getItem();
          
            DefaultListModel<LocalObjectLight> listModel = new DefaultListModel<>();
            
            for (LocalObjectLight aHop : selectedRoute.getHops())
                listModel.addElement(aHop);

            lstRouteDetail.setModel(listModel);
        }
        
        private final class Route {
            String name;
            List<LocalObjectLight> hops;
            int numberOfHops;

            public Route(String name, List<LocalObjectLight> hops) {
                this.name = name;
                this.hops = hops;
                this.numberOfHops = getNodes().size() - 1; //Ignores the first node, because it's the start node
            }

            public String getName() {
                return name;
            }

            public List<LocalObjectLight> getNodes() {
                List<LocalObjectLight> res = new ArrayList<>();
                for (LocalObjectLight hop : hops) {
                    if (com.isSubclassOf(hop.getClassName(), SDHModuleService.CLASS_GENERICEQUIPMENT))
                        res.add(hop);
                }
                return res;
            }
            

            public List<LocalObjectLight> getLinks() {
                List<LocalObjectLight> res = new ArrayList<>();
                for (LocalObjectLight hop : hops) {
                    if (com.isSubclassOf(hop.getClassName(), SDHModuleService.CLASS_GENERICLOGICALCONNECTION))
                        res.add(hop);
                }
                return res;
            }
            
            public List<LocalObjectLight> getHops() {
                return hops;
            }
            
            @Override
            public String toString() {
                return String.format("%s - %s %s", name, numberOfHops, (numberOfHops == 1 ? "hop" : "hops"));
            }
        }
    }
    
    private class ChooseContainerLinkResourcesStep implements WizardDescriptor.ValidatingPanel<WizardDescriptor>, MouseListener {
        private JPanel thePanel;
        private JList<HopDefinition> lstContainerDefinition;
        private JLabel lblInstructions;
        private LocalClassMetadataLight connectionType;
        private ChooseRouteStep.Route route;
        
        @Override
        public void validate() throws WizardValidationException {
            if (lstContainerDefinition.getModel().getSize() == 0)
                throw new WizardValidationException(thePanel, "The route can not be empty", null);
            
            for (int i = 0; i < lstContainerDefinition.getModel().getSize(); i++) {
                if (lstContainerDefinition.getModel().getElementAt(i).position == -1)
                    throw new WizardValidationException(thePanel, "You have to select position for every segment of the route", null);
            }
        }

        @Override
        public Component getComponent() {
           return thePanel;
        }

        @Override
        public HelpCtx getHelp() {
            return null;
        }

        @Override
        public void readSettings(WizardDescriptor settings) {
            connectionType = (LocalClassMetadataLight)settings.getProperty("connectionType");
            route = (ChooseRouteStep.Route)settings.getProperty("route");
            
            List<HopDefinition> containerDefinition = new ArrayList<>();
            
            for (LocalObjectLight aLink : route.getLinks()) 
                containerDefinition.add(new HopDefinition(aLink));
            
            lstContainerDefinition = new JList<>(containerDefinition.toArray(new HopDefinition[0]));
            lstContainerDefinition.addMouseListener(this);
            lblInstructions = new JLabel("Double click on a transport link to choose a position for this container");
            
            thePanel = new JPanel(new BorderLayout());
            
            thePanel.add(lblInstructions, BorderLayout.NORTH);
            thePanel.add(lstContainerDefinition, BorderLayout.CENTER);
        }

        @Override
        public void storeSettings(WizardDescriptor settings) {
            List<LocalSDHPosition> positions = new ArrayList<>();
            for (int i = 0; i < lstContainerDefinition.getModel().getSize(); i++) {
                HopDefinition aHop = lstContainerDefinition.getModel().getElementAt(i);
                positions.add(new LocalSDHPosition(aHop.getLink().getClassName(), aHop.getLink().getOid(), aHop.position));
            }
            settings.putProperty("positions", positions);
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public void addChangeListener(ChangeListener l) {}

        @Override
        public void removeChangeListener(ChangeListener l) {}

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) { //Only act upon a double-click event
                HopDefinition hop = lstContainerDefinition.getSelectedValue();
                List<LocalSDHContainerLinkDefinition> transportLinkStructure = com.getSDHTransportLinkStructure(hop.getLink().getClassName(), hop.getLink().getOid());
                
                if (transportLinkStructure == null) 
                    JOptionPane.showMessageDialog(null, com.getError(), "Error", JOptionPane.ERROR_MESSAGE);
                else {
                    JComboBox<AvailableTransportLinkPosition> lstAvailablePositions = new JComboBox<>(buildAvailablePositionsList(hop.getLink(), transportLinkStructure));
                    lstAvailablePositions.setName("lstAvailablePositions"); //NOI18N
                    JComplexDialogPanel pnlAvailablePositions = new JComplexDialogPanel(new String[] {"Available Positions"}, new JComponent[] {lstAvailablePositions});

                    if (JOptionPane.showConfirmDialog(null, pnlAvailablePositions, "Available Positions", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                        int selectedIndex = ((JComboBox)pnlAvailablePositions.getComponent("lstAvailablePositions")).getSelectedIndex();
                        int numberOfPositions = ((JComboBox)pnlAvailablePositions.getComponent("lstAvailablePositions")).getItemCount();
                        
                        //First we need to check if the selected container can fit into the transportlink, that is, if there are
                        //enough contiguous positions if the container is a concatenated one
                        try {
                            int numberOfPositionsToBeOccupied = SDHModuleService.calculateCapacity(connectionType.getClassName(), SDHModuleService.LinkType.TYPE_CONTAINERLINK);
                            
                            if (numberOfPositions - selectedIndex < numberOfPositionsToBeOccupied)
                                JOptionPane.showMessageDialog(null, "There are not enough positions to transport the concatenated container", "Error", JOptionPane.ERROR_MESSAGE);
                            else {
                                for (int i = selectedIndex; i < selectedIndex + numberOfPositionsToBeOccupied; i++) {
                                    AvailableTransportLinkPosition positionToBeOcuppied = (AvailableTransportLinkPosition)((JComboBox)pnlAvailablePositions.getComponent("lstAvailablePositions")).getItemAt(i);
                                    if (positionToBeOcuppied.container != null) {
                                        JOptionPane.showMessageDialog(null, "One of the positions to be assigned is already in use", "Error", JOptionPane.ERROR_MESSAGE);
                                        return;
                                    }
                                }
                                hop.position = ((AvailableTransportLinkPosition)((JComboBox)pnlAvailablePositions.getComponent("lstAvailablePositions")).getSelectedItem()).position;
                            }
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(null, "The ContainerLink class name does not allow to calculate the total number of concatenated positions", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {}

        @Override
        public void mouseReleased(MouseEvent e) {}

        @Override
        public void mouseEntered(MouseEvent e) {}

        @Override
        public void mouseExited(MouseEvent e) {}
        
        public AvailableTransportLinkPosition[] buildAvailablePositionsList(LocalObjectLight transportLink, 
                List<LocalSDHContainerLinkDefinition> transportLinkStructure) {
            try {
                int numberOfVC4 = SDHModuleService.calculateCapacity(transportLink.getClassName(), SDHModuleService.LinkType.TYPE_TRANSPORTLINK);
                AvailableTransportLinkPosition[] availablePositions = new AvailableTransportLinkPosition[numberOfVC4];
                
                //First, we fill the positions we know for sure that are being used
                for (LocalSDHContainerLinkDefinition aContainerDefinition : transportLinkStructure) {
                    int position = aContainerDefinition.getPositions().get(0).getPosition(); //This container definition has always only one position: The one used in this TransportLink
                    availablePositions[position - 1] = new AvailableTransportLinkPosition(position, aContainerDefinition.getContainer());
                    //A container might occupy more than one slot, if it's a concatenated circuit. Now, we will fill the adjacent which are also being used
                    try {
                        int numberOfAdjacentPositions = 0;
                        String adjacentPositions = aContainerDefinition.getContainer().getClassName().replace("VC4", "");
                        if (!adjacentPositions.isEmpty())
                            numberOfAdjacentPositions = Math.abs(Integer.valueOf(adjacentPositions)) - 1; //Minus one, because we've already filled the first position
                                                                                                          //Absolute value, because the concatenated containers class names are like "VC4-A_NUMBER"
                        for (int j = position; j < position + numberOfAdjacentPositions; j++)
                            availablePositions[j] = new AvailableTransportLinkPosition(j + 1, aContainerDefinition.getContainer());
                        
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(null, "The ContainerLink class name does not allow to calculate the total number of concatenated positions", "Error", JOptionPane.ERROR_MESSAGE);
                        return new AvailableTransportLinkPosition[0];
                    }
                }
                
                //Then we fill the rest (if any) with free slots
                for (int i = 1; i <= numberOfVC4; i++) {
                    if (availablePositions[i - 1] == null)
                        availablePositions[i - 1] = new AvailableTransportLinkPosition(i, null);
                }
                return availablePositions;
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "The TransportLink class name does not allow to calculate the total number of positions", "Error", JOptionPane.ERROR_MESSAGE);
                return new AvailableTransportLinkPosition[0];
            }
        }
    }
        
    private class ChooseTributaryLinkResourcesStep implements WizardDescriptor.ValidatingPanel<WizardDescriptor>, MouseListener {
        private JPanel thePanel;
        private JList<HopDefinition> lstContainerDefinition;
        private JLabel lblInstructions;
        private LocalClassMetadataLight connectionType;
        private ChooseRouteStep.Route route;
        
        @Override
        public void validate() throws WizardValidationException {
            if (lstContainerDefinition.getModel().getSize() == 0)
                throw new WizardValidationException(thePanel, "The route can not be empty", null);
            
            for (int i = 0; i < lstContainerDefinition.getModel().getSize(); i++) {
                if (lstContainerDefinition.getModel().getElementAt(i).position == -1)
                    throw new WizardValidationException(thePanel, "You have to select position for every segment of the route", null);
            }
        }

        @Override
        public Component getComponent() {
           return thePanel;
        }

        @Override
        public HelpCtx getHelp() {
            return null;
        }

        @Override
        public void readSettings(WizardDescriptor settings) {
            connectionType = (LocalClassMetadataLight)settings.getProperty("connectionType");
            route = (ChooseRouteStep.Route)settings.getProperty("route");
            
            List<HopDefinition> containerDefinition = new ArrayList<>();
            
            for (LocalObjectLight aLink : route.getLinks()) 
                containerDefinition.add(new HopDefinition(aLink));
            
            lstContainerDefinition = new JList<>(containerDefinition.toArray(new HopDefinition[0]));
            lstContainerDefinition.addMouseListener(this);
            lblInstructions = new JLabel("Double click on a container link to choose a position for this tributary link");
            
            thePanel = new JPanel(new BorderLayout());
            
            thePanel.add(lblInstructions, BorderLayout.NORTH);
            thePanel.add(lstContainerDefinition, BorderLayout.CENTER);
        }

        @Override
        public void storeSettings(WizardDescriptor settings) {
            List<LocalSDHPosition> positions = new ArrayList<>();
            for (int i = 0; i < lstContainerDefinition.getModel().getSize(); i++) {
                HopDefinition aHop = lstContainerDefinition.getModel().getElementAt(i);
                positions.add(new LocalSDHPosition(aHop.getLink().getClassName(), aHop.getLink().getOid(), aHop.position));
            }
            settings.putProperty("positions", positions);
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public void addChangeListener(ChangeListener l) {}

        @Override
        public void removeChangeListener(ChangeListener l) {}

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) { //Only act upon a double-click event
                HopDefinition hop = lstContainerDefinition.getSelectedValue();
                
                List<LocalSDHContainerLinkDefinition> structure;
                
                if (com.isSubclassOf(connectionType.getClassName(), SDHModuleService.CLASS_GENERICSDHHIGHORDERTRIBUTARYLINK))
                    structure = com.getSDHTransportLinkStructure(hop.getLink().getClassName(), hop.getLink().getOid());
                else
                    structure = com.getSDHContainerLinkStructure(hop.getLink().getClassName(), hop.getLink().getOid());
                
                if (structure == null) 
                    JOptionPane.showMessageDialog(null, com.getError(), "Error", JOptionPane.ERROR_MESSAGE);
                else {
                    JComboBox lstAvailablePositions;
                    if (com.isSubclassOf(connectionType.getClassName(), SDHModuleService.CLASS_GENERICSDHHIGHORDERTRIBUTARYLINK))
                        lstAvailablePositions = new JComboBox<>(buildAvailablePositionsListForTransportLinks(hop.getLink(), structure));
                    else
                        lstAvailablePositions = new JComboBox<>(buildAvailablePositionsListForContainers(hop.getLink(), structure));
                    
                    lstAvailablePositions.setName("lstAvailablePositions"); //NOI18N
                    JComplexDialogPanel pnlAvailablePositions = new JComplexDialogPanel(new String[] {"Available Positions"}, new JComponent[] {lstAvailablePositions});

                    if (JOptionPane.showConfirmDialog(null, pnlAvailablePositions, "Available Positions", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                        int selectedIndex = ((JComboBox)pnlAvailablePositions.getComponent("lstAvailablePositions")).getSelectedIndex();
                        int numberOfPositions = ((JComboBox)pnlAvailablePositions.getComponent("lstAvailablePositions")).getItemCount();
                        
                        //First we need to check if the selected tributary link fits into the contrainerlink, that is, if there are
                        //enough contiguous positions to carry the virtual circuit

                        int numberOfPositionsToBeOccupied;
                        switch (connectionType.getClassName().replace("TributaryLink", "")) { //NOI18N
                            case SDHModuleService.CLASS_VC4: //A VC4 occuoies only one position on a transport link
                            case SDHModuleService.CLASS_VC12:
                                numberOfPositionsToBeOccupied = 1;
                                break;
                            case SDHModuleService.CLASS_VC3:
                                numberOfPositionsToBeOccupied = 21;
                                break;
                            default:
                                JOptionPane.showMessageDialog(null, 
                                        "The selected connection type is not recognized as valid (VC3/VC12)", "Error", JOptionPane.ERROR_MESSAGE);
                                return;
                        }

                        if (numberOfPositions - selectedIndex < numberOfPositionsToBeOccupied)
                            JOptionPane.showMessageDialog(null, "There are not enough positions to transport this virtual circuit", "Error", JOptionPane.ERROR_MESSAGE);
                        else {
                            for (int i = selectedIndex; i < selectedIndex + numberOfPositionsToBeOccupied; i++) {
                                AbstractPosition positionToBeOcuppied = (AbstractPosition)((JComboBox)pnlAvailablePositions.getComponent("lstAvailablePositions")).getItemAt(i);
                                if (positionToBeOcuppied.container != null) {
                                    JOptionPane.showMessageDialog(null, "One of the positions to be assigned is already in use", "Error", JOptionPane.ERROR_MESSAGE);
                                    return;
                                }
                            }
                            hop.position = ((AbstractPosition)((JComboBox)pnlAvailablePositions.getComponent("lstAvailablePositions")).getSelectedItem()).position;
                        }
                    }
                }
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {}

        @Override
        public void mouseReleased(MouseEvent e) {}

        @Override
        public void mouseEntered(MouseEvent e) {}

        @Override
        public void mouseExited(MouseEvent e) {}
        
        public AvailableContainerLinkPosition[] buildAvailablePositionsListForContainers(LocalObjectLight containertLink, 
                List<LocalSDHContainerLinkDefinition> containertLinkStructure){
            try {
                int numberOfPositions;
                
                String containerSuffix = containertLink.getClassName().replace(SDHModuleService.CLASS_VC4, ""); //NOI18N
                if (containerSuffix.isEmpty())
                    numberOfPositions = 63;
                else
                    numberOfPositions = Math.abs(Integer.valueOf(containerSuffix)) * 63; 
                
                AvailableContainerLinkPosition[] availablePositions = new AvailableContainerLinkPosition[numberOfPositions];
                
                //First, we fill the positions we know for sure that are being used
                for (LocalSDHContainerLinkDefinition aContainerDefinition : containertLinkStructure) {
                    int position = aContainerDefinition.getPositions().get(0).getPosition(); //This container definition has always only one position: The one used in this TransportLink
                    availablePositions[position - 1] = new AvailableContainerLinkPosition(position, aContainerDefinition.getContainer());
                    //A container might occupy more than one slot, if it's a concatenated circuit. Now, we will fill the adjacent which are also being used
                        int numberOfAdjacentPositions ;
                        switch (aContainerDefinition.getContainer().getClassName()) {
                            case SDHModuleService.CLASS_VC12:
                                numberOfAdjacentPositions = 0;
                                break;
                            case SDHModuleService.CLASS_VC3:
                                numberOfAdjacentPositions = 20;
                                break;
                            default:
                                JOptionPane.showMessageDialog(null, "The ContainerLink class name does not allow to calculate the total number of concatenated positions", "Error", JOptionPane.ERROR_MESSAGE);
                                return new AvailableContainerLinkPosition[0];
                        }
                        
                        for (int j = position; j < position + numberOfAdjacentPositions; j++)
                            availablePositions[j] = new AvailableContainerLinkPosition(j + 1, aContainerDefinition.getContainer());                        
                }
                
                //Then we fill the rest (if any) with free slots
                for (int i = 1; i <= numberOfPositions; i++) {
                    if (availablePositions[i - 1] == null)
                        availablePositions[i - 1] = new AvailableContainerLinkPosition(i, null);
                }
                return availablePositions;
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "The ContainerLink class name does not allow to calculate the total number of positions", "Error", JOptionPane.ERROR_MESSAGE);
                return new AvailableContainerLinkPosition[0];
            }
        }
        
        public AvailableTransportLinkPosition[] buildAvailablePositionsListForTransportLinks(LocalObjectLight transportLink, 
                List<LocalSDHContainerLinkDefinition> transportLinkStructure) {
            try {
                int numberOfVC4 = SDHModuleService.calculateCapacity(transportLink.getClassName(), SDHModuleService.LinkType.TYPE_TRANSPORTLINK);
                AvailableTransportLinkPosition[] availablePositions = new AvailableTransportLinkPosition[numberOfVC4];
                
                //First, we fill the positions we know for sure that are being used
                for (LocalSDHContainerLinkDefinition aContainerDefinition : transportLinkStructure) {
                    int position = aContainerDefinition.getPositions().get(0).getPosition(); //This container definition has always only one position: The one used in this TransportLink
                    availablePositions[position - 1] = new AvailableTransportLinkPosition(position, aContainerDefinition.getContainer());
                    //A container might occupy more than one slot, if it's a concatenated circuit. Now, we will fill the adjacent which are also being used
                    try {
                        int numberOfAdjacentPositions = 0;
                        String adjacentPositions = aContainerDefinition.getContainer().getClassName().replace("VC4", "");
                        if (!adjacentPositions.isEmpty())
                            numberOfAdjacentPositions = Math.abs(Integer.valueOf(adjacentPositions)) - 1; //Minus one, because we've already filled the first position
                                                                                                          //Absolute value, because the concatenated containers class names are like "VC4-A_NUMBER"
                        for (int j = position; j < position + numberOfAdjacentPositions; j++)
                            availablePositions[j] = new AvailableTransportLinkPosition(j + 1, aContainerDefinition.getContainer());
                        
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(null, "The ContainerLink class name does not allow to calculate the total number of concatenated positions", "Error", JOptionPane.ERROR_MESSAGE);
                        return new AvailableTransportLinkPosition[0];
                    }
                }
                
                //Then we fill the rest (if any) with free slots
                for (int i = 1; i <= numberOfVC4; i++) {
                    if (availablePositions[i - 1] == null)
                        availablePositions[i - 1] = new AvailableTransportLinkPosition(i, null);
                }
                return availablePositions;
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "The TransportLink class name does not allow to calculate the total number of positions", "Error", JOptionPane.ERROR_MESSAGE);
                return new AvailableTransportLinkPosition[0];
            }
        }
        
    }
    
    private class ChooseConnectionEndpointsStep implements WizardDescriptor.ValidatingPanel<WizardDescriptor> {
        private ExplorablePanel pnlTreeASide;
        private ExplorablePanel pnlTreeBSide;
        private JPanel thePanel;
        private LocalObjectLight sourcePort, targetPort;

        public ChooseConnectionEndpointsStep(LocalObjectLight equipmentA, LocalObjectLight equipmentB) {
            thePanel = new JPanel();
            
            BeanTreeView treeASide = new BeanTreeView();
            BeanTreeView treeBSide = new BeanTreeView();
            
            pnlTreeASide = new ExplorablePanel();
            pnlTreeBSide = new ExplorablePanel();
            
            pnlTreeASide.getExplorerManager().setRootContext(new ObjectNode(equipmentA));
            pnlTreeBSide.getExplorerManager().setRootContext(new ObjectNode(equipmentB));
            
            pnlTreeASide.setViewportView(treeASide);
            pnlTreeBSide.setViewportView(treeBSide);
            
            thePanel.setLayout(new BorderLayout());
            thePanel.add(pnlTreeASide, BorderLayout.WEST);
            thePanel.add(pnlTreeBSide, BorderLayout.EAST);
            
            thePanel.setName("Select the endpoints");
            //Shows what step we're in on the left panel of the wizard
            thePanel.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, 1);
        }

        @Override
        public Component getComponent() {
            return thePanel;
        }

        @Override
        public HelpCtx getHelp() {
            return null;
        }

        @Override
        public void readSettings(WizardDescriptor settings) {
        }

        @Override
        public void storeSettings(WizardDescriptor settings) {
             settings.putProperty("sourcePort", sourcePort);
             settings.putProperty("targetPort", targetPort);
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public void addChangeListener(ChangeListener l) {
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
        }

        @Override
        public void validate() throws WizardValidationException {
            sourcePort = pnlTreeASide.getLookup().lookup(LocalObjectLight.class);
            if (sourcePort == null || !com.isSubclassOf(sourcePort.getClassName(), Constants.CLASS_GENERICPORT))
                throw new WizardValidationException(pnlTreeASide, "You have to select a source port on the left panel", null);
            
            targetPort = pnlTreeBSide.getLookup().lookup(LocalObjectLight.class);
            if (targetPort == null || !com.isSubclassOf(targetPort.getClassName(), Constants.CLASS_GENERICPORT))
                throw new WizardValidationException(pnlTreeASide, "You have to select a target port on the right panel", null);
        }
    }
    
    private class ChooseServiceStep implements WizardDescriptor.Panel<WizardDescriptor> {
        private JPanel thePanel;
        private JList<LocalObjectLight> lstServices;
        private String serviceClass;

        public ChooseServiceStep(String serviceClass) {
            this.serviceClass = serviceClass;
        }
        
        @Override
        public Component getComponent() {
            return thePanel;
        }

        @Override
        public HelpCtx getHelp() {
            return null;
        }

        @Override
        public void readSettings(WizardDescriptor settings) {
            thePanel = new JPanel(new BorderLayout());
            thePanel.setName("Select a service (optional)");
            
            List<LocalObjectLight> services = com.getObjectsOfClassLight(serviceClass);
            if (services == null)
                JOptionPane.showMessageDialog(null, com.getError(), "Error", JOptionPane.ERROR_MESSAGE);
            else {
                lstServices = new JList<>(services.toArray(new LocalObjectLight[0]));
                thePanel.add(lstServices);
            }
        }

        @Override
        public void storeSettings(WizardDescriptor settings) {
            if (lstServices != null)
                settings.putProperty("service", lstServices.getSelectedValue()); //NOI18N
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public void addChangeListener(ChangeListener l) {}

        @Override
        public void removeChangeListener(ChangeListener l) {}
    }
   
    /**
     * A class defining a hop in a possible route for a virtual circuit
     */
    public class HopDefinition {
        LocalObjectLight link;
        int position;

        public HopDefinition(LocalObjectLight link) {
            this.link = link;
            this.position = -1; //The default position is unset
        }

        public LocalObjectLight getLink() {
            return link;
        }

        public int getPosition() {
            return position;
        }
        
        @Override
        public String toString() {
            return link + " - " + (position == -1 ? "NA" : position); //NOI18N
        }
    }
    
    /**
     * Simple root class for all types of SDH positions. Subclasses will simply overwrite the method toString
     */
    public abstract class AbstractPosition {
        protected int position;
        protected LocalObjectLight container;

        public AbstractPosition(int position, LocalObjectLight container) {
            this.position = position;
            this.container = container;
        }
        
        @Override
        public abstract String toString();
    }
    
    /**
     * A class representing a timeslot in a TransportLink
     */
    public class AvailableTransportLinkPosition extends AbstractPosition{

        public AvailableTransportLinkPosition(int position, LocalObjectLight container) {
            super(position, container);
        }

        @Override
        public String toString() {
            return String.format("%s - %s", position, container == null ? "Free" : container.getName());
        }
    }
    
    /**
     * A class representing a timeslot in a ContainerLink
     */
    public class AvailableContainerLinkPosition extends AbstractPosition {
        
        public AvailableContainerLinkPosition(int position, LocalObjectLight container) {
            super(position, container);
        }            

        @Override
        public String toString() {
            return String.format("%s - %s", asKLM(), container == null ? "Free" : container.getName());
        }
        
        private String asKLM() {
            int k, l, m;
            
            if (position % 21 == 0) {
                k = position / 21;
                l = 7;
                m = 3;
            } else {
                k = (position / 21) + 1;
                if ((position % 21) % 3 == 0)
                    l = (position % 21) / 3;
                else 
                    l = (position % 21) / 3 + 1;
                
                if ((position % 21) % 3 == 0)
                    m = 3;
                else
                    m = (position % 21) % 3;
            }
            
            return String.format("%s [%s - %s - %s]", position, k, l, m);
        }
    }
    
    public enum Connections {
        CONNECTION_TRANSPORTLINK,
        CONNECTION_CONTAINERLINK,
        CONNECTION_TRIBUTARYLINK
    }
}
