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
import com.neotropic.kuwaiba.modules.commercial.ospman.AbstractMapProvider.OSPNode;
import com.neotropic.kuwaiba.modules.commercial.ospman.OspToolRegisterEvents.EdgeAddedEvent;
import com.neotropic.kuwaiba.modules.commercial.ospman.OspToolset.ToolHand;
import com.neotropic.kuwaiba.modules.commercial.ospman.OspToolset.ToolMarker;
import com.neotropic.kuwaiba.modules.commercial.ospman.OspToolset.ToolPolygon;
import com.neotropic.kuwaiba.modules.commercial.ospman.OspToolset.ToolPolyline;
import org.neotropic.kuwaiba.modules.core.navigation.commands.Command;
import com.neotropic.kuwaiba.modules.commercial.ospman.OspToolRegisterEvents.EdgeInfoWindowRequestEvent;
import com.neotropic.kuwaiba.modules.commercial.ospman.OspToolRegisterEvents.EdgePathChangedEvent;
import com.neotropic.kuwaiba.modules.commercial.ospman.OspToolRegisterEvents.MapCenterChangedEvent;
import com.neotropic.kuwaiba.modules.commercial.ospman.OspToolRegisterEvents.NodeAddedEvent;
import com.neotropic.kuwaiba.modules.commercial.ospman.OspToolRegisterEvents.NodeInfoWindowRequestEvent;
import com.neotropic.kuwaiba.modules.commercial.ospman.OspToolRegisterEvents.NodePositionChangedEvent;
import com.neotropic.kuwaiba.modules.commercial.ospman.OspToolset.ToolBounceMarker;
import com.neotropic.kuwaiba.modules.commercial.ospman.OspToolset.ToolChangeMapCenter;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import java.util.Arrays;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.commands.FourArgsCommand;
import org.neotropic.kuwaiba.modules.core.navigation.commands.OneArgCommand;
import org.neotropic.kuwaiba.modules.core.navigation.commands.TwoArgsCommand;
import org.neotropic.util.visual.tools.ToolRegister;

/**
 * Component with a set of tools available to work in an outside plant canvas
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class OutsidePlantTools extends HorizontalLayout {
    private BusinessObjectLight tmpObject;
    private final TranslationService translationService;
    private final OutsidePlantSearch outsidePlantSearch;
    private final List<Button> buttons;
    
    private Command cmdBack;
    
    private Command cmdSaveOspView;
    private Command cmdDeleteOspView;
    private Command cmdOspViewChanged;
    
    private TwoArgsCommand<BusinessObjectLight, GeoCoordinate> cmdAddMarker;
    private FourArgsCommand<BusinessObjectLight, BusinessObjectLight, List<GeoCoordinate>, Command> cmdAddPolyline;
    
    private OneArgCommand<OSPNode> cmdDeleteMarker;
    private OneArgCommand<OSPEdge> cmdDeletePolyline;
    
    public OutsidePlantTools(AbstractMapProvider mapProvider, ToolRegister toolRegister, 
                MetadataEntityManager mem, ApplicationEntityManager aem, BusinessEntityManager bem, TranslationService translationService) {
        
        this.translationService = translationService;
        ToolHand toolHand = new ToolHand();
        ToolMarker toolMarker = new ToolMarker();
        ToolPolygon toolPolygon = new ToolPolygon();
        ToolPolyline toolPolyline = new ToolPolyline();
        ToolBounceMarker toolBounceMarker = new ToolBounceMarker();
        ToolChangeMapCenter toolChangeMapCenter = new ToolChangeMapCenter();
        
        toolRegister.getTools().addAll(Arrays.asList(
            toolHand, toolMarker, toolPolygon, toolPolyline, toolBounceMarker, toolChangeMapCenter));
        
        getElement().getStyle().set("background-color", "#fff"); //NOI18N
        getElement().getStyle().set("left", "2%"); //NOI18N
        getElement().getStyle().set("position", "absolute"); //NOI18N
        getElement().getStyle().set("top", "8%"); //NOI18N
        getElement().getStyle().set("z-index", "5"); //NOI18N
        Button btnBack = new Button(new Icon(VaadinIcon.ARROW_LEFT), 
            event -> executeCommand(cmdBack));
        Button btnSave = new Button(new Icon(VaadinIcon.SAFE), 
            event -> executeCommand(cmdSaveOspView));
        Button btnDelete = new Button(new Icon(VaadinIcon.TRASH), 
            event -> executeCommand(cmdDeleteOspView));
        
        Button btnHand = new Button(new Icon(VaadinIcon.HAND));
        Button btnPolygon = new Button(new Icon(VaadinIcon.STAR_O));
        Button btnPolyline = new Button(new Icon(VaadinIcon.SPARK_LINE));
        
        this.buttons = Arrays.asList(btnHand, btnPolygon, btnPolyline);

        btnHand.addClickListener(event -> {
            enabledButtons(btnHand);
            toolRegister.setTool(toolHand);
        });
        
        btnPolygon.addClickListener(event -> {
            enabledButtons(btnPolygon);
            toolRegister.setTool(toolPolygon);
        });
        
        btnPolyline.addClickListener(event -> {
            enabledButtons(btnPolyline);
            toolRegister.setTool(toolPolyline);
        });
        
        outsidePlantSearch = new OutsidePlantSearch(bem, this.translationService, mapProvider);
        
        outsidePlantSearch.addNewListener(event -> {
            tmpObject = event.getObject();
            toolRegister.setTool(toolMarker);
            enabledButtons(btnHand);
        });
        outsidePlantSearch.addSelectionListener(event -> {
            toolBounceMarker.setBounceNode(event.getOspNode());
            toolRegister.setTool(toolBounceMarker);
        });
        
        toolRegister.addListener(event -> {
            /**
             * If a marker-complete event is fired the marker tool was active
             */
            if (event instanceof NodeAddedEvent) {
                NodeAddedEvent theEvent = (NodeAddedEvent) event;
                if (cmdAddMarker != null)
                    cmdAddMarker.execute(tmpObject, theEvent.getPosition());
                
                toolRegister.setTool(toolHand);
            } else if (event instanceof EdgeAddedEvent) {
                EdgeAddedEvent theEvent = (EdgeAddedEvent) event;
                if (cmdAddPolyline != null) {
                    cmdAddPolyline.execute(
                        theEvent.getSource().getBusinessObject(),
                        theEvent.getTarget().getBusinessObject(),
                        theEvent.getPath(), 
                        theEvent.getDeleteDummyEdgeCommand()
                    );
                }
                //Enables the polyline tool again
                toolRegister.setTool(toolPolyline);
            }
            else if (event instanceof NodePositionChangedEvent)
                executeCommand(cmdOspViewChanged);
            else if (event instanceof EdgePathChangedEvent)
                executeCommand(cmdOspViewChanged);
            else if (event instanceof MapCenterChangedEvent)
                executeCommand(cmdOspViewChanged);                
            else if (mapProvider instanceof OspInfoWindowContainer) {
                OspInfoWindowContainer container = (OspInfoWindowContainer) mapProvider;
                
                if (event instanceof NodeInfoWindowRequestEvent) {
                    NodeInfoWindowRequestEvent theEvent = (NodeInfoWindowRequestEvent) event;
                    NodeInfoWindowContent content = new NodeInfoWindowContent(theEvent.getOspNode(), container, translationService, mem, aem, bem);
                    content.setDeleteNodeCommand(cmdDeleteMarker);
                    container.setInfoWindowContent(content);
                    container.openInfoWindow(theEvent.getOspNode().getBusinessObject());
                    
                } else if (event instanceof EdgeInfoWindowRequestEvent) {
                    EdgeInfoWindowRequestEvent theEvent = (EdgeInfoWindowRequestEvent) event;
                    EdgeInfoWindowContent content = new EdgeInfoWindowContent(theEvent.getOspEdge(), container);
                    content.setDeleteEdgeCommand(cmdDeletePolyline);
                    container.setInfoWindowContent(content);
                    container.setInfoWindowPosition(theEvent.getPosition());
                    container.openInfoWindow();
                }
            }
        });
        setMargin(false);
        setPadding(false);
        setSpacing(false);
        
        add(btnBack, btnSave, btnDelete, outsidePlantSearch, btnHand, btnPolygon, btnPolyline);
        
        enabledButtons(btnHand);
        toolRegister.setTool(toolHand);
    }
    
    private void enabledButtons(Button disableButton) {
        for (Button enableButton : buttons)
            enableButton.setEnabled(true);
        disableButton.setEnabled(false);
    }
        
    public void setBackCommand(Command cmdBack) {
        this.cmdBack = cmdBack;
    }
    
    public void setSaveOspViewCommand(Command cmdSaveOspView) {
        this.cmdSaveOspView = cmdSaveOspView;
    }
    
    public void setDeleteOspViewCommand(Command cmdDeleteOspView) {
        this.cmdDeleteOspView = cmdDeleteOspView;
    }
    
    public void setAddmarkerCommand(TwoArgsCommand<BusinessObjectLight, GeoCoordinate> cmdAddMarker) {
        this.cmdAddMarker = cmdAddMarker;
    }
    
    public void setAddPolylineCommand(FourArgsCommand<BusinessObjectLight, BusinessObjectLight, List<GeoCoordinate>, Command> cmdAddPolyline) {
        this.cmdAddPolyline = cmdAddPolyline;
    }
    
    public void setOspViewChangedCommand(Command cmdViewChanged) {
        this.cmdOspViewChanged = cmdViewChanged;
    }
    
    public void setDeleteMarkerCommand(OneArgCommand<OSPNode> cmdDeleteMarker) {
        this.cmdDeleteMarker = cmdDeleteMarker;
    }
    
    public void setDeletePolylineCommand(OneArgCommand<OSPEdge> cmdDeletePolyline) {
        this.cmdDeletePolyline = cmdDeletePolyline;
    }
    //Helpers
    private void executeCommand(Command command) {
        if (command != null)
            command.execute();
    }
}
