/**
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.inventory.modules.sdh.actions;

import com.neotropic.inventory.modules.sdh.LocalSDHContainerLinkDefinition;
import com.neotropic.inventory.modules.sdh.SDHModuleService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Utils;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * A simple top component that shows the structure of a container or a transport link
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class SDHLinkStructureTopComponent extends TopComponent implements ExplorerManager.Provider {
    private ExplorerManager em;
    /**
     * The structure to be rendered
     */
    private List<LocalSDHContainerLinkDefinition> structure;
    /**
     * The max number of timeslots in the container or transport link (STM1 = 1, STM256 = 256, VC4-4 = 63x4, etc)
     */
    private int maxCapacity;
    /**
     * Should the timeslots be segmented and how? for example, a VC4 has 3 segments of 21 timeslots
     */
    private int segments;
    /**
     * Main scroll panel
     */
    private JScrollPane pnlScrollMain;
    /**
     * The list containing the structure
     */
    private JList<StructureElement> lstComponents;
    
    public SDHLinkStructureTopComponent(LocalObjectLight sdhLink, List<LocalSDHContainerLinkDefinition> structure, int maxCapacity, int segments) {
        em = new ExplorerManager();
        this.structure = structure;
        this.maxCapacity = maxCapacity;
        this.segments = segments;
        setDisplayName(sdhLink.toString());
        initComponents();
        associateLookup(ExplorerUtils.createLookup(em, getActionMap()));
    }

    @Override
    public int getPersistenceType() {
        return PERSISTENCE_NEVER;
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        Mode myMode = WindowManager.getDefault().findMode("properties");
        myMode.dockInto(this);
        
        DefaultListModel<StructureElement> listModel = new DefaultListModel<>();
        
        //First we fill all the possible positions with empty slots, then we set those actually occupied
        for (int i = 0; i < maxCapacity; i++)
            listModel.addElement(new StructureElement(null));
        
        for (LocalSDHContainerLinkDefinition aContainerDefinition : structure) {
            int containerCapacity = SDHModuleService.calculateCapacity(aContainerDefinition.getContainer().getClassName(), SDHModuleService.LinkType.TYPE_CONTAINERLINK);
            int initPosition = aContainerDefinition.getPositions().get(0).getPosition() - 1; //Minus one because the index of the list is 0-based, while the actual timeslot number 
            //First, let's fill the initial position
            StructureElement currentContainer = new StructureElement(aContainerDefinition);
            listModel.setElementAt(currentContainer, initPosition);
            //Now the adjacent
            for (int i = initPosition + 1; i < initPosition + containerCapacity; i++)
                listModel.setElementAt(new StructureElement(aContainerDefinition), i);
        }
        
        lstComponents = new JList<>(listModel);
        lstComponents.setCellRenderer(new CustomCellRenderer());

        pnlScrollMain = new JScrollPane(lstComponents);
       
        add(pnlScrollMain);
    }
    
    @Override
    public ExplorerManager getExplorerManager() {        
        return em;
    }

    private static class StructureElement {
        LocalSDHContainerLinkDefinition containerDefinition;
        public static final ImageIcon freeIcon = new ImageIcon(Utils.createRectangleIcon(Color.GREEN, 10, 10));
        public static final ImageIcon structuredIcon = new ImageIcon(Utils.createRectangleIcon(Color.BLUE, 10, 10));
        public static final ImageIcon tributaryIcon = new ImageIcon(Utils.createRectangleIcon(Color.ORANGE, 10, 10));
        
        public StructureElement(LocalSDHContainerLinkDefinition containerDefinition) {
            this.containerDefinition = containerDefinition;
        }
        
        public ImageIcon getIcon() {
            if (containerDefinition == null)
                return freeIcon;
            if (containerDefinition.isStructured())
                return structuredIcon;
            else
                return tributaryIcon;
        }
        
        @Override
        public String toString() {
            if (containerDefinition == null)
                return "Free";
            return containerDefinition.getContainer().toString();
        }
    }
    
    private class CustomCellRenderer extends JLabel implements ListCellRenderer<StructureElement> {

        public CustomCellRenderer() {
            setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            setOpaque(true);        
        }
        

        @Override
        public Component getListCellRendererComponent(JList<? extends StructureElement> list, StructureElement value, int index, boolean isSelected, boolean cellHasFocus) {
            setIcon(value.getIcon());
            setText(value.toString());
            
            if (isSelected)
                setBackground(UIManager.getColor("Button.focus"));
            else
                setBackground(Color.WHITE);
            
            return this;
        }
    }
}
