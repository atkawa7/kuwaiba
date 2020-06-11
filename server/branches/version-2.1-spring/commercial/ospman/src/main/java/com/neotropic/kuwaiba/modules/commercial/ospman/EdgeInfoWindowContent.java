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

import com.neotropic.kuwaiba.modules.commercial.ospman.AbstractMapProvider.OSPEdge;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.neotropic.kuwaiba.modules.core.navigation.commands.OneArgCommand;

/**
 * Component with a set of tools to polylines: like edit, delete
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class EdgeInfoWindowContent extends VerticalLayout {
    private OneArgCommand<OSPEdge> cmdDeleteEdge;
    
    public EdgeInfoWindowContent(OSPEdge ospEdges, OspInfoWindowContainer container) {
        HorizontalLayout hlyTools = new HorizontalLayout();
        
        hlyTools.add(new Button(new Icon(VaadinIcon.TRASH), event -> {
            if (cmdDeleteEdge != null)
                cmdDeleteEdge.execute(ospEdges);
        }));
        
        setSizeFull();
        setMargin(false);
        setPadding(false);
        add(hlyTools);
        setHorizontalComponentAlignment(Alignment.CENTER, hlyTools);
    }
    
    public void setDeleteEdgeCommand(OneArgCommand<OSPEdge> cmdDeleteEdge) {
        this.cmdDeleteEdge = cmdDeleteEdge;
    }
}
