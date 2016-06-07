/*
 * Copyright (c) 2016 gir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    gir - initial API and implementation and/or initial documentation
 */
package com.neotropic.inventory.modules.sdh.wizard;

import com.neotropic.inventory.modules.sdh.LocalSDHContainerLinkDefinition;
import com.neotropic.inventory.modules.sdh.LocalSDHPosition;
import com.neotropic.inventory.modules.sdh.SDHConfigurationObject;
import com.neotropic.inventory.modules.sdh.SDHModuleService;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.GridLayout;
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
                    new ChooseRouteBasedOnTransportLinksStep(equipmentA, equipmentB),
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
                    new ChooseRouteBasedOnContainerLinksStep(equipmentA, equipmentB),
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
    
    private class ConnectionGeneralInfoStep implements WizardDescriptor.ValidatingPanel {
        private JComplexDialogPanel thePanel;

        public ConnectionGeneralInfoStep(Connections connection) {
            final JTextField txtConnectionName = new JTextField(20);                 
            txtConnectionName.setName("txtConnectionName"); //NOI18N
            final JComboBox<LocalClassMetadataLight> lstConnectionTypes;
            
            LocalClassMetadataLight[] connectionClasses;
            
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
                lstConnectionTypes = new JComboBox<>(connectionClasses);
            
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
        public void readSettings(Object settings) {}

        @Override
        public void storeSettings(Object settings) {
            ((WizardDescriptor)settings).putProperty("connectionName", ((JTextField)thePanel.getComponent("txtConnectionName")).getText());
            ((WizardDescriptor)settings).putProperty("connectionType", ((JComboBox)thePanel.getComponent("lstConnectionTypes")).getSelectedItem());
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
    
    private class ChooseRouteBasedOnTransportLinksStep implements WizardDescriptor.ValidatingPanel, ItemListener {
        private JPanel thePanel;
        private JComboBox<Route> lstRoutes;
        private JList<LocalObjectLight> lstRouteDetail;
        private JScrollPane pnlRouteDetailScroll;
        private LocalObjectLight endpointA;
        private LocalObjectLight endpointB;

        public ChooseRouteBasedOnTransportLinksStep(LocalObjectLight endpointA, LocalObjectLight endpointB) {
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
        public void readSettings(Object settings) {
            initComponents();
        }

        @Override
        public void storeSettings(Object settings) {
            lstRoutes.removeItemListener(this);
            ((WizardDescriptor)settings).putProperty("route", lstRoutes.getSelectedItem());
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public void addChangeListener(ChangeListener l) {}

        @Override
        public void removeChangeListener(ChangeListener l) {}
        
        private void initComponents() {
            List<LocalObjectLightList> routes = com.findRoutesUsingTransportLinks(endpointA, endpointB);
            if (routes == null) {
                JOptionPane.showMessageDialog(null, com.getError(), "Error calculating routes", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (routes.isEmpty()) {
                JOptionPane.showMessageDialog(null, "No routes were found between these equipment", "Route Calculation", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            thePanel = new JPanel(new BorderLayout());
            thePanel.setName("Choose a route");
            
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
                    if (com.isSubclassOf(hop.getClassName(), SDHModuleService.CLASS_GENERICSDHTRANSPORTLINK))
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
    
    private class ChooseContainerLinkResourcesStep implements WizardDescriptor.ValidatingPanel, MouseListener {
        private JPanel thePanel;
        private JList<HopDefinition> lstContainerDefinition;
        private JLabel lblInstructions;
        private LocalClassMetadataLight connectionType;
        private ChooseRouteBasedOnTransportLinksStep.Route route;
        
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
        public void readSettings(Object settings) {
            connectionType = (LocalClassMetadataLight)((WizardDescriptor)settings).getProperty("connectionType");
            route = (ChooseRouteBasedOnTransportLinksStep.Route)((WizardDescriptor)settings).getProperty("route");
            
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
        public void storeSettings(Object settings) {
            List<LocalSDHPosition> positions = new ArrayList<>();
            for (int i = 0; i < lstContainerDefinition.getModel().getSize(); i++) {
                HopDefinition aHop = lstContainerDefinition.getModel().getElementAt(i);
                positions.add(new LocalSDHPosition(aHop.transportLink.getClassName(), aHop.transportLink.getOid(), aHop.position));
            }
            ((WizardDescriptor)settings).putProperty("positions", positions);
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
                List<LocalSDHContainerLinkDefinition> transportLinkStructure = com.getSDHTransportLinkStructure(hop.transportLink.getClassName(), hop.transportLink.getOid());
                
                if (transportLinkStructure == null) 
                    JOptionPane.showMessageDialog(null, com.getError(), "Error", JOptionPane.ERROR_MESSAGE);
                else {
                    JComboBox<AvailablePosition> lstAvailablePositions = new JComboBox<>(buildAvailablePositionsList(hop.transportLink, transportLinkStructure));
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
                                    AvailablePosition positionToBeOcuppied = (AvailablePosition)((JComboBox)pnlAvailablePositions.getComponent("lstAvailablePositions")).getItemAt(i);
                                    if (positionToBeOcuppied.container != null) {
                                        JOptionPane.showMessageDialog(null, "One of the positions to be assigned is already in use", "Error", JOptionPane.ERROR_MESSAGE);
                                        return;
                                    }
                                }
                                hop.position = ((AvailablePosition)((JComboBox)pnlAvailablePositions.getComponent("lstAvailablePositions")).getSelectedItem()).position;
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
        
        public AvailablePosition[] buildAvailablePositionsList(LocalObjectLight transportLink, 
                List<LocalSDHContainerLinkDefinition> transportLinkStructure){
            try {
                int numberOfVC4 = SDHModuleService.calculateCapacity(transportLink.getClassName(), SDHModuleService.LinkType.TYPE_TRANSPORTLINK);
                AvailablePosition[] availablePositions = new AvailablePosition[numberOfVC4];
                
                //First, we fill the positions we know for sure that are being used
                for (LocalSDHContainerLinkDefinition aContainerDefinition : transportLinkStructure) {
                    int position = aContainerDefinition.getPositions().get(0).getPosition(); //This container definition has always only one position: The one used in this TransportLink
                    availablePositions[position - 1] = new AvailablePosition(position, aContainerDefinition.getContainer());
                    //A container might occupy more than one slot, if it's a concatenated circuit. Now, we will fill the adjacent which are also being used
                    try {
                        int numberOfAdjacentPositions = 0;
                        String adjacentPositions = aContainerDefinition.getContainer().getClassName().replace("VC4", "");
                        if (!adjacentPositions.isEmpty())
                            numberOfAdjacentPositions = Math.abs(Integer.valueOf(adjacentPositions)) - 1; //Minus one, because we've already filled the first position
                                                                                                          //Absolute value, because the concatenated containers class names are like "VC4-A_NUMBER"
                        for (int j = position; j < position + numberOfAdjacentPositions; j++)
                            availablePositions[j] = new AvailablePosition(j + 1, aContainerDefinition.getContainer());
                        
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(null, "The ContainerLink class name does not allow to calculate the total number of concatenated positions", "Error", JOptionPane.ERROR_MESSAGE);
                        return new AvailablePosition[0];
                    }
                }
                
                //Then we fill the rest (if any) with free slots
                for (int i = 1; i <= numberOfVC4; i++) {
                    if (availablePositions[i - 1] == null)
                        availablePositions[i - 1] = new AvailablePosition(i, null);
                }
                return availablePositions;
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "The TransportLink class name does not allow to calculate the total number of positions", "Error", JOptionPane.ERROR_MESSAGE);
                return new AvailablePosition[0];
            }
        }
        
        private class HopDefinition {
            LocalObjectLight transportLink;
            int position;

            public HopDefinition(LocalObjectLight transportLink) {
                this.transportLink = transportLink;
                this.position = -1; //The default position is unset
            }
            
            @Override
            public String toString() {
                return transportLink + " - " + (position == -1 ? "NA" : position); //NOI18N
            }
        }
        
        private class AvailablePosition {
            private int position;
            private LocalObjectLight container;

            public AvailablePosition(int position, LocalObjectLight container) {
                this.position = position;
                this.container = container;
            }            
            
            @Override
            public String toString() {
                return String.format("%s - %s", position, container == null ? "Free" : container.getName());
            }
        }
    }
    
    private class ChooseRouteBasedOnContainerLinksStep implements WizardDescriptor.ValidatingPanel, ItemListener {
        private JPanel thePanel;
        private JComboBox<Route> lstRoutes;
        private JList<LocalObjectLight> lstRouteDetail;
        private JScrollPane pnlRouteDetailScroll;
        private LocalObjectLight endpointA;
        private LocalObjectLight endpointB;

        public ChooseRouteBasedOnContainerLinksStep(LocalObjectLight endpointA, LocalObjectLight endpointB) {
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
        public void readSettings(Object settings) {
            initComponents();
        }

        @Override
        public void storeSettings(Object settings) {
            lstRoutes.removeItemListener(this);
            ((WizardDescriptor)settings).putProperty("route", lstRoutes.getSelectedItem());
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public void addChangeListener(ChangeListener l) {}

        @Override
        public void removeChangeListener(ChangeListener l) {}
        
        private void initComponents() {
            List<LocalObjectLightList> routes = com.findRoutesUsingContainerLinks(endpointA, endpointB);
            if (routes == null) {
                JOptionPane.showMessageDialog(null, com.getError(), "Error calculating routes", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (routes.isEmpty()) {
                JOptionPane.showMessageDialog(null, "There are not high order containers connecting these equipment", "Route Calculation", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            thePanel = new JPanel(new BorderLayout());
            thePanel.setName("Choose a route");
            
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
                    if (CommunicationsStub.getInstance().isSubclassOf(hop.getClassName(), SDHModuleService.CLASS_GENERICEQUIPMENT))
                        res.add(hop);
                }
                return res;
            }
            

            public List<LocalObjectLight> getLinks() {
                List<LocalObjectLight> res = new ArrayList<>();
                for (LocalObjectLight hop : hops) {
                    if (CommunicationsStub.getInstance().isSubclassOf(hop.getClassName(), SDHModuleService.CLASS_GENERICSDHCONTAINERLINK))
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
    
    private class ChooseTributaryLinkResourcesStep implements WizardDescriptor.ValidatingPanel, MouseListener {
        private JPanel thePanel;
        private JList<HopDefinition> lstContainerDefinition;
        private JLabel lblInstructions;
        private LocalClassMetadataLight connectionType;
        private ChooseRouteBasedOnContainerLinksStep.Route route;
        
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
        public void readSettings(Object settings) {
            connectionType = (LocalClassMetadataLight)((WizardDescriptor)settings).getProperty("connectionType");
            route = (ChooseRouteBasedOnContainerLinksStep.Route)((WizardDescriptor)settings).getProperty("route");
            
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
        public void storeSettings(Object settings) {
            List<LocalSDHPosition> positions = new ArrayList<>();
            for (int i = 0; i < lstContainerDefinition.getModel().getSize(); i++) {
                HopDefinition aHop = lstContainerDefinition.getModel().getElementAt(i);
                positions.add(new LocalSDHPosition(aHop.containerLink.getClassName(), aHop.containerLink.getOid(), aHop.position));
            }
            ((WizardDescriptor)settings).putProperty("positions", positions);
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
                List<LocalSDHContainerLinkDefinition> transportLinkStructure = com.getSDHContainerLinkStructure(hop.containerLink.getClassName(), hop.containerLink.getOid());
                
                if (transportLinkStructure == null) 
                    JOptionPane.showMessageDialog(null, com.getError(), "Error", JOptionPane.ERROR_MESSAGE);
                else {
                    JComboBox<AvailablePosition> lstAvailablePositions = new JComboBox<>(buildAvailablePositionsList(hop.containerLink, transportLinkStructure));
                    lstAvailablePositions.setName("lstAvailablePositions"); //NOI18N
                    JComplexDialogPanel pnlAvailablePositions = new JComplexDialogPanel(new String[] {"Available Positions"}, new JComponent[] {lstAvailablePositions});

                    if (JOptionPane.showConfirmDialog(null, pnlAvailablePositions, "Available Positions", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                        int selectedIndex = ((JComboBox)pnlAvailablePositions.getComponent("lstAvailablePositions")).getSelectedIndex();
                        int numberOfPositions = ((JComboBox)pnlAvailablePositions.getComponent("lstAvailablePositions")).getItemCount();
                        
                        //First we need to check if the selected tributary link fits into the contrainerlink, that is, if there are
                        //enough contiguous positions to carry the virtual circuit

                        int numberOfPositionsToBeOccupied;
                        switch (connectionType.getClassName().replace("TributaryLink", "")) { //NOI18N
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
                                AvailablePosition positionToBeOcuppied = (AvailablePosition)((JComboBox)pnlAvailablePositions.getComponent("lstAvailablePositions")).getItemAt(i);
                                if (positionToBeOcuppied.container != null) {
                                    JOptionPane.showMessageDialog(null, "One of the positions to be assigned is already in use", "Error", JOptionPane.ERROR_MESSAGE);
                                    return;
                                }
                            }
                            hop.position = ((AvailablePosition)((JComboBox)pnlAvailablePositions.getComponent("lstAvailablePositions")).getSelectedItem()).position;
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
        
        public AvailablePosition[] buildAvailablePositionsList(LocalObjectLight containertLink, 
                List<LocalSDHContainerLinkDefinition> containertLinkStructure){
            try {
                int numberOfPositions;
                
                String containerSuffix = containertLink.getClassName().replace(SDHModuleService.CLASS_VC4, ""); //NOI18N
                if (containerSuffix.isEmpty())
                    numberOfPositions = 63;
                else
                    numberOfPositions = Math.abs(Integer.valueOf(containerSuffix)) * 63; 
                
                AvailablePosition[] availablePositions = new AvailablePosition[numberOfPositions];
                
                //First, we fill the positions we know for sure that are being used
                for (LocalSDHContainerLinkDefinition aContainerDefinition : containertLinkStructure) {
                    int position = aContainerDefinition.getPositions().get(0).getPosition(); //This container definition has always only one position: The one used in this TransportLink
                    availablePositions[position - 1] = new AvailablePosition(position, aContainerDefinition.getContainer());
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
                                return new AvailablePosition[0];
                        }
                        
                        for (int j = position; j < position + numberOfAdjacentPositions; j++)
                            availablePositions[j] = new AvailablePosition(j + 1, aContainerDefinition.getContainer());                        
                }
                
                //Then we fill the rest (if any) with free slots
                for (int i = 1; i <= numberOfPositions; i++) {
                    if (availablePositions[i - 1] == null)
                        availablePositions[i - 1] = new AvailablePosition(i, null);
                }
                return availablePositions;
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "The ContainerLink class name does not allow to calculate the total number of positions", "Error", JOptionPane.ERROR_MESSAGE);
                return new AvailablePosition[0];
            }
        }
        
        private class HopDefinition {
            LocalObjectLight containerLink;
            int position;

            public HopDefinition(LocalObjectLight containerLink) {
                this.containerLink = containerLink;
                this.position = -1; //The default position is unset
            }
            
            @Override
            public String toString() {
                return containerLink + " - " + (position == -1 ? "NA" : position); //NOI18N
            }
        }
        
        private class AvailablePosition {
            private int position;
            private LocalObjectLight container;

            public AvailablePosition(int position, LocalObjectLight container) {
                this.position = position;
                this.container = container;
            }            
            
            @Override
            public String toString() {
                return String.format("%s - %s", position, container == null ? "Free" : container.getName());
            }
        }
    }
    
    private class ChooseConnectionEndpointsStep implements WizardDescriptor.ValidatingPanel {
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
            
            pnlTreeASide.add(treeASide);
            pnlTreeBSide.add(treeBSide);
            
            pnlTreeASide.getExplorerManager().setRootContext(new ObjectNode(equipmentA));
            pnlTreeBSide.getExplorerManager().setRootContext(new ObjectNode(equipmentB));
            
            JScrollPane pnlScrollTreeASide = new JScrollPane();
            JScrollPane pnlScrollTreeBSide = new JScrollPane();
            
            pnlScrollTreeASide.setViewportView(pnlTreeASide);
            pnlScrollTreeBSide.setViewportView(pnlTreeBSide);
            
            GridLayout layout = new GridLayout(1, 2);
            thePanel.setLayout(layout);
            thePanel.add(pnlScrollTreeASide);
            thePanel.add(pnlScrollTreeBSide);
            
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
        public void readSettings(Object settings) {
        }

        @Override
        public void storeSettings(Object settings) {
             ((WizardDescriptor)settings).putProperty("sourcePort", sourcePort);
             ((WizardDescriptor)settings).putProperty("targetPort", targetPort);
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
    
    private class ChooseServiceStep implements WizardDescriptor.Panel{
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
        public void readSettings(Object settings) {
            thePanel = new JPanel(new BorderLayout());
            thePanel.setName("Select a service (optional)");
            
            LocalObjectLight[] services = com.getObjectsOfClassLight(serviceClass);
            if (services == null)
                JOptionPane.showMessageDialog(null, com.getError(), "Error", JOptionPane.ERROR_MESSAGE);
            else {
                lstServices = new JList<>(services);
                thePanel.add(lstServices);
            }
        }

        @Override
        public void storeSettings(Object settings) {
            if (lstServices != null)
                ((WizardDescriptor)settings).putProperty("service", lstServices.getSelectedValue()); //NOI18N
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
    
    public enum Connections {
        CONNECTION_TRANSPORTLINK,
        CONNECTION_CONTAINERLINK,
        CONNECTION_TRIBUTARYLINK
    }
}
