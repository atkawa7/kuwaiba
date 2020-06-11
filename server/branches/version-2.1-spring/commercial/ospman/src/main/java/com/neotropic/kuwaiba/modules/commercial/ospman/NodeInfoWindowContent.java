/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.neotropic.kuwaiba.modules.commercial.ospman;

import com.neotropic.kuwaiba.modules.commercial.ospman.AbstractMapProvider.OSPNode;
import com.neotropic.kuwaiba.modules.commercial.ospman.dialogs.PhysicalConnectionsDialog;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.commands.OneArgCommand;

/**
 * Component with a set of tools to markers: like delete
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class NodeInfoWindowContent extends VerticalLayout {
    private OneArgCommand<OSPNode> cmdDeleteNode;
    
    public NodeInfoWindowContent(OSPNode ospNode, OspInfoWindowContainer container, TranslationService ts, BusinessEntityManager bem) {
        HorizontalLayout hlyTools = new HorizontalLayout();
        
        hlyTools.add(new Button(new Icon(VaadinIcon.PLUG), event -> {
            container.closeInfoWindow();
            PhysicalConnectionsDialog dialog = new PhysicalConnectionsDialog(ospNode.getBusinessObject(), ts, bem);
            dialog.open();
        }));
        hlyTools.add(new Button(new Icon(VaadinIcon.TRASH), event -> {
            container.closeInfoWindow();
            if (cmdDeleteNode != null)
                cmdDeleteNode.execute(ospNode);
        }));
        
        setSizeFull();
        setMargin(false);
        setPadding(false);
        
        add(hlyTools);
        setHorizontalComponentAlignment(Alignment.CENTER, hlyTools);
    }
    
    public void setDeleteNodeCommand(OneArgCommand<OSPNode> cmdDeleteNode) {
        this.cmdDeleteNode = cmdDeleteNode;
    }
}
