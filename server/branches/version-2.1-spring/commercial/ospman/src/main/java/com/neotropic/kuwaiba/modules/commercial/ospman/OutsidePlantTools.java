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
import com.neotropic.kuwaiba.modules.commercial.ospman.commands.CommandAddMarker;
import com.neotropic.kuwaiba.modules.commercial.ospman.commands.CommandAddConnection;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.server.Command;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.tools.Tool;
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
    private Command cmdSave;
    private Command cmdDelete;
    private CommandAddMarker cmdAddMarker;
    private CommandAddConnection cmdAddPolyline;
    private Command cmdRemoveMarker;
    private Command cmdRemovePolyline;
    
    public OutsidePlantTools(BusinessEntityManager bem, 
        TranslationService translationService, 
        AbstractMapProvider mapProvider, ToolRegister toolRegister) {
        
        this.translationService = translationService;
        Tool toolHand = new Tool("hand", null, null); //NOI18N
        Tool toolMarker = new Tool("marker", null, null); //NOI18N
        Tool toolPolygon = new Tool("polygon", null, null); //NOI18N
        Tool toolPolyline = new Tool("polyline", null, null); //NOI18N
        toolRegister.getTools().addAll(Arrays.asList(
            toolHand, toolMarker, toolPolygon, toolPolyline));
        
        getElement().getStyle().set("background-color", "#fff"); //NOI18N
        getElement().getStyle().set("left", "2%"); //NOI18N
        getElement().getStyle().set("position", "absolute"); //NOI18N
        getElement().getStyle().set("top", "8%"); //NOI18N
        getElement().getStyle().set("z-index", "5"); //NOI18N
        Button btnBack = new Button(new Icon(VaadinIcon.ARROW_LEFT), 
            event -> executeCommand(cmdBack));
        Button btnSave = new Button(new Icon(VaadinIcon.SAFE), 
            event -> executeCommand(cmdSave));
        Button btnDelete = new Button(new Icon(VaadinIcon.TRASH), 
            event -> executeCommand(cmdDelete));
        
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
            Properties properties = new Properties();
            properties.put("animate-marker", event.getOspNode()); //NOI18N
            properties.put("set-map-center", event.getOspNode().getLocation()); //NOI18N
            mapProvider.reload(properties);
        });
        
        toolRegister.addListener(event -> {
            /**
             * If a marker-complete event is fired the marker tool was active
             */
            if ("add-marker".equals(event.getId())) { //NOI18N
                if (cmdAddMarker != null) {
                    cmdAddMarker.setBussinesObject(tmpObject);
                    cmdAddMarker.setLat((double) event.getProperties().get("lat")); //NOI18N
                    cmdAddMarker.setLng((double) event.getProperties().get("lng")); //NOI18N
                    cmdAddMarker.execute();
                }
                toolRegister.setTool(toolHand);
            } else if ("add-polyline".equals(event.getId())) { //NOI18N
                if (cmdAddPolyline != null) {
                    cmdAddPolyline.setSource(((OSPNode) 
                        event.getProperties().get("source")).getBusinessObject()); //NOI18N
                    cmdAddPolyline.setTarget(((OSPNode) 
                        event.getProperties().get("target")).getBusinessObject()); //NOI18N
                    cmdAddPolyline.setPath((List) event.getProperties().get("path")); //NOI18N
                    cmdAddPolyline.execute();
                }
                //Enables the polyline tool again
                toolRegister.setTool(toolPolyline);
            } else if ("remove-marker".equals(event.getId())) {
                
            } else if ("remove-polyline".equals(event.getId())) {
                
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
    
    public void setSaveCommand(Command cmdSave) {
        this.cmdSave = cmdSave;
    }
    
    public void setDeleteCommand(Command cmdDelete) {
        this.cmdDelete = cmdDelete;
    }
    
    public void setAddmarkerCommand(CommandAddMarker cmdAddMarker) {
        this.cmdAddMarker = cmdAddMarker;
    }
    
    public void setAddPolylineCommand(CommandAddConnection cmdAddPolyline) {
        this.cmdAddPolyline = cmdAddPolyline;
    }
    //Helpers
    private void executeCommand(Command command) {
        if (command != null)
            command.execute();
    }
}
