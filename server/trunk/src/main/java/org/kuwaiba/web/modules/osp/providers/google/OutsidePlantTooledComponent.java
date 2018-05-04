/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.web.modules.osp.providers.google;

import com.vaadin.event.FieldEvents;
import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Notification;
import de.steinwedel.messagebox.MessageBox;
import java.util.List;
import org.kuwaiba.apis.web.gui.actions.AbstractAction;
import org.kuwaiba.apis.web.gui.modules.EmbeddableComponent;
import org.kuwaiba.apis.web.gui.modules.TopComponent;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.web.custom.core.AbstractTooledComponent;
import org.kuwaiba.web.modules.osp.OutsidePlantComponent;
import org.kuwaiba.web.modules.osp.providers.google.tool.actions.ActionsFactory;
import org.kuwaiba.web.modules.osp.providers.google.tool.actions.DeleteOspViewAction;
import org.kuwaiba.web.modules.osp.providers.google.tool.actions.FilterByAction;
import org.kuwaiba.web.modules.osp.providers.google.tool.actions.NewOspViewAction;
import org.kuwaiba.web.modules.osp.providers.google.tool.actions.OpenOspViewAction;
import org.kuwaiba.web.modules.osp.providers.google.tool.actions.SaveOspViewAction;

/**
 * The tooled component that wrapped the DragAndDropWrapper GoogleMapWrapper
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class OutsidePlantTooledComponent extends AbstractTooledComponent implements EmbeddableComponent {
    public static final String ACTION_CAPTION_DELETE = "Delete";
    public static final String ACTION_CAPTION_FILTER = "Filter";
    public static final String ACTION_CAPTION_NEW = "New";
    public static final String ACTION_CAPTION_OPEN = "Open";
    public static final String ACTION_CAPTION_SAVE = "Save";
    
    public static final String ACTION_ICON_DELETE = "img/mod_osp_icon_delete.png";    
    public static final String ACTION_ICON_FILTER = "img/mod_osp_icon_filter.png";
    public static final String ACTION_ICON_NEW = "img/mod_osp_icon_add.png";
    public static final String ACTION_ICON_OPEN = "img/mod_osp_icon_open.png";
    public static final String ACTION_ICON_SAVE = "img/mod_osp_icon_save.png";
        
    public static final String BUTTON_CAPTION_NODE_LABELS = "Show/hide node labels";
    public static final String BUTTON_CAPTION_CONN_LABELS = "Show/hide connection labels";
    public static final String BUTTON_CAPTION_SELECT = "Select";
    public static final String BUTTON_CAPTION_CONNECT = "Connect";
    public static final String BUTTON_CAPTION_NEW_CONTAINER = "Connect Using a Container";
    public static final String BUTTON_CAPTION_NEW_LINK = "Connect Using a Link";
    public static final String BUTTON_CAPTION_POLYGON = "Draw polygon";
    public static final String BUTTON_CAPTION_MEASURE = "Measure distance";
    public static final String BUTTON_CAPTION_CLEAN = "Clean";
    public static final String BUTTON_CAPTION_SEARCH = "Search";

    public static final String BUTTON_ICON_NODE_LABELS = "img/mod_osp_icon_hide_node_labels.png";
    public static final String BUTTON_ICON_CONN_LABELS = "img/mod_osp_icon_hide_connection_labels.png";
    public static final String BUTTON_ICON_SELECT = "img/mod_osp_icon_select.png";
    public static final String BUTTON_ICON_CONNECT = "img/mod_osp_icon_connect.png";
    public static final String BUTTON_ICON_NEW_CONTAINER = "img/mod_osp_new_container.png";
    public static final String BUTTON_ICON_NEW_LINK = "img/mod_osp_new_link.png";
    public static final String BUTTON_ICON_POLYGON = "img/mod_osp_icon_polygon.png";
    public static final String BUTTON_ICON_MEASURE = "img/mod_osp_icon_measure.png";
    public static final String BUTTON_ICON_CLEAN = "img/mod_osp_icon_clean.png";
    public static final String BUTTON_ICON_SEARCH = "img/mod_osp_icon_search.png";
    
    private Button btnNew;
    private Button btnOpen;
    private Button btnSave;
    private Button btnDelete;
    private Button btnNodeLabels;
    private Button btnConnLabels;
    private Button btnSelect;
////    private Button btnConnect;
    private Button btnNewContainer;
    private Button btnNewLink;
    private Button btnPolygon;
    private Button btnMeasure;
    private Button btnFilter;
    private Button btnClean;
    private ComboBox cboSearch;
    private Button btnSearch;  
    
    private final ToolBarSize toolBarSize = AbstractTooledComponent.ToolBarSize.NORMAL;
    
    private final TopComponent parentComponent;
        
    public OutsidePlantTooledComponent(TopComponent parentComponent) {
        super(new AbstractAction[0], 
                AbstractTooledComponent.TOOLBAR_ORIENTATION_HORIZONTAL, 
                AbstractTooledComponent.ToolBarSize.NORMAL);
        
        this.parentComponent = parentComponent;
                        
        createButtonNew();
        createButtonOpen();
        createButtonSave();
        createButtonDelete();
        createButtonShowNodeLabels();
        createButtonShowConnectionLabels();
        createButtonSelect();
////        createButtonConnect();
        createButtonNewContainer();
        createButtonNewLink();
        createButtonPolygon();
        createButtonMeasure();
        createButtonFilter();
        createButtonClean();
        createButtonComboBoxSearch();
        createButtonSearch();
        
        setSizeFull();
    }
    
    private void setButtonSelected(Button button) {
        
        if (button.getStyleName().contains("selected"))
            button.setStyleName("selected", false);
        else
            button.setStyleName("selected", true);
    }
    
    private void setButtonSelected(Button button, boolean selected) {
        button.setStyleName("selected", selected);
    }
    
    private Button createButton(AbstractAction action) {
        Button button = new Button();
        if (action != null) {
            button.setIcon(action.getIcon());
            button.setDescription(action.getCaption());
        }
        button.setStyleName("v-button-icon-only");
        button.setWidth(toolBarSize.size() + 2 * TOOLBAR_HORIZONTAL_PADDING, Unit.PIXELS);
        button.setHeight(toolBarSize.size() + 2 * TOOLBAR_VERTICAL_PADDING, Unit.PIXELS);
        
        ((AbstractOrderedLayout) getToolbarLayout()).addComponent(button);
        
        return button;
    }
    
    private void createButtonNew() {
        NewOspViewAction createNewOspViewAction = ActionsFactory.createNewOspViewAction(ACTION_CAPTION_NEW, ACTION_ICON_NEW);
        
        btnNew = createButton(createNewOspViewAction);
        btnNew.addClickListener((Button.ClickEvent event) -> {
            OutsidePlantComponent ospComponent = (OutsidePlantComponent) parentComponent;
            createNewOspViewAction.actionPerformed(ospComponent, ospComponent.getGoogleMapWrapper());
        });
    }
    
    private void createButtonOpen() {
        OpenOspViewAction createNewOspViewAction = ActionsFactory.createOpenOspViewAction(
            ACTION_CAPTION_OPEN, ACTION_ICON_OPEN);
        
        btnOpen = createButton(createNewOspViewAction);
        btnOpen.addClickListener((Button.ClickEvent event) -> {
            OutsidePlantComponent ospComponent = (OutsidePlantComponent) parentComponent;
            createNewOspViewAction.actionPerformed(ospComponent, ospComponent.getGoogleMapWrapper());
        });
    }
    
    private void createButtonSave() {
        SaveOspViewAction saveOspViewAction = ActionsFactory.createSaveOspViewAction(
            ACTION_CAPTION_SAVE, ACTION_ICON_SAVE);
        
        btnSave = createButton(saveOspViewAction);
        btnSave.addClickListener((Button.ClickEvent event) -> {
            OutsidePlantComponent ospComponent = (OutsidePlantComponent) parentComponent;
            saveOspViewAction.actionPerformed(ospComponent, ospComponent.getGoogleMapWrapper());
        });
    }
    
    private void createButtonDelete() {
        DeleteOspViewAction deleteOspViewAction = ActionsFactory.createDeleteOspViewAction(
            ACTION_CAPTION_DELETE, ACTION_ICON_DELETE);
        
        btnDelete = createButton(deleteOspViewAction);
        btnDelete.addClickListener((Button.ClickEvent event) -> {
            
            OutsidePlantComponent ospComponent = (OutsidePlantComponent) parentComponent;
            deleteOspViewAction.actionPerformed(ospComponent, ospComponent.getGoogleMapWrapper());
        });
    }
    
    private void createButtonShowNodeLabels() {
        btnNodeLabels = createButton(null);
        btnNodeLabels.setDescription(BUTTON_CAPTION_NODE_LABELS);
        btnNodeLabels.setIcon(new ThemeResource(BUTTON_ICON_NODE_LABELS));
        
        btnNodeLabels.addClickListener((Button.ClickEvent event) -> {
            setButtonSelected(btnNodeLabels);
//            ((OutsidePlantComponent) parentComponent).getGoogleMapWrapper()
//                    .getMap().enableShowNodeLabelsTool();
        });
    }
    
    private void createButtonShowConnectionLabels() {
        btnConnLabels = createButton(null);
        btnConnLabels.setDescription(BUTTON_CAPTION_CONN_LABELS);
        btnConnLabels.setIcon(new ThemeResource(BUTTON_ICON_CONN_LABELS));
        
        btnConnLabels.addClickListener((Button.ClickEvent event) -> {
            setButtonSelected(btnConnLabels);
//            ((OutsidePlantComponent) parentComponent).getGoogleMapWrapper()
//                    .getMap().enableShowConnectionLabelsTool();
        });
    }
    
    private void createButtonSelect() {
        btnSelect = createButton(null);
        btnSelect.setIcon(new ThemeResource(BUTTON_ICON_SELECT));
        btnSelect.setDescription(BUTTON_CAPTION_SELECT);
        btnSelect.addClickListener((Button.ClickEvent event) -> {
            setButtonSelected(btnSelect, true);            
////            setButtonSelected(btnConnect, false);
            setButtonSelected(btnNewContainer, false);
            setButtonSelected(btnNewLink, false);
            setButtonSelected(btnPolygon, false);
            setButtonSelected(btnMeasure, false);
            
//            ((OutsidePlantComponent) parentComponent).getGoogleMapWrapper()
//                    .getMap().enableSelectTool();
        });
    }
    
////    private void createButtonConnect() {
////        btnConnect = createButton(null);
////        btnConnect.setIcon(new ThemeResource(BUTTON_ICON_CONNECT));
////        btnConnect.setDescription(BUTTON_CAPTION_CONNECT);
////        btnConnect.addClickListener((Button.ClickEvent event) -> {
////            setButtonSelected(btnSelect, false);
////            setButtonSelected(btnConnect, true);
////            setButtonSelected(btnPolygon, false);
////            setButtonSelected(btnMeasure, false);
////            
////            ((OutsidePlantComponent) parentComponent).getGoogleMapWrapper()
////                    .getMap().enableConnectionTool();
////        });
////    }
    
    private void createButtonNewContainer() {
        btnNewContainer = createButton(null);
        btnNewContainer.setIcon(new ThemeResource(BUTTON_ICON_NEW_CONTAINER));
        btnNewContainer.setDescription(BUTTON_CAPTION_NEW_CONTAINER);
        btnNewContainer.addClickListener((Button.ClickEvent event) -> {
            setButtonSelected(btnSelect, false);
            setButtonSelected(btnNewContainer, true);
            setButtonSelected(btnNewLink, false);
            setButtonSelected(btnPolygon, false);
            setButtonSelected(btnMeasure, false);
            
//            ((OutsidePlantComponent) parentComponent).getGoogleMapWrapper()
//                    .getMap().enableConnectionTool(true);
        });
    }
    
    private void createButtonNewLink() {
        btnNewLink = createButton(null);
        btnNewLink.setIcon(new ThemeResource(BUTTON_ICON_NEW_LINK));
        btnNewLink.setDescription(BUTTON_CAPTION_NEW_LINK);
        btnNewLink.addClickListener((Button.ClickEvent event) -> {
            setButtonSelected(btnSelect, false);
            setButtonSelected(btnNewContainer, false);
            setButtonSelected(btnNewLink, true);
            setButtonSelected(btnPolygon, false);
            setButtonSelected(btnMeasure, false);
            
//            ((OutsidePlantComponent) parentComponent).getGoogleMapWrapper()
//                    .getMap().enableConnectionTool(false);
        });
    }
    
    private void createButtonPolygon() {
        btnPolygon = createButton(null);
        btnPolygon.setIcon(new ThemeResource(BUTTON_ICON_POLYGON));
        btnPolygon.setDescription(BUTTON_CAPTION_POLYGON);
        btnPolygon.addClickListener((Button.ClickEvent event) -> {
            setButtonSelected(btnSelect, false);
////            setButtonSelected(btnConnect, false);            
            setButtonSelected(btnNewContainer, false);
            setButtonSelected(btnNewLink, false);
            setButtonSelected(btnPolygon, true);
            setButtonSelected(btnMeasure, false);
            
//            ((OutsidePlantComponent) parentComponent).getGoogleMapWrapper()
//                    .getMap().enablePolygonTool();
        });
    }
    
    private void createButtonMeasure() {
        btnMeasure = createButton(null);
        btnMeasure.setIcon(new ThemeResource(BUTTON_ICON_MEASURE));
        btnMeasure.setDescription(BUTTON_CAPTION_MEASURE);
        btnMeasure.addClickListener((Button.ClickEvent event) -> {
            CustomGoogleMap map = ((OutsidePlantComponent) parentComponent).getGoogleMapWrapper().getMap();
                        
////            setButtonSelected(btnConnect, false);
            setButtonSelected(btnNewContainer, false);
            setButtonSelected(btnNewLink, false);
            setButtonSelected(btnPolygon, false);
            
//            setButtonSelected(btnSelect, map.getMeasureDistance());
//            setButtonSelected(btnMeasure, !map.getMeasureDistance());
//            
//            map.enableMeasureTool();
        });
    }
    
    private void createButtonFilter() {
        FilterByAction filteByAction = ActionsFactory.createFilterByAction(
            ACTION_CAPTION_FILTER, ACTION_ICON_FILTER);
        
        btnFilter = createButton(filteByAction);
        
        btnFilter.addClickListener((Button.ClickEvent event) -> {
            OutsidePlantComponent ospComponent = (OutsidePlantComponent) parentComponent;
            filteByAction.actionPerformed(ospComponent, ospComponent.getGoogleMapWrapper());
        });
    }
    
    private void createButtonClean() {
        btnClean = createButton(null);
        btnClean.setIcon(new ThemeResource(BUTTON_ICON_CLEAN));
        btnClean.setDescription(BUTTON_CAPTION_CLEAN);
        
        btnClean.addClickListener((Button.ClickEvent event) -> {
                                    
            MessageBox mbClean = MessageBox.createQuestion()
                .withCaption("Confirmation")
                .withMessage("Delete all elements in the current view")
                .withOkButton(() -> {
                    GoogleMapWrapper mapWrapper = ((OutsidePlantComponent) parentComponent)
                        .getGoogleMapWrapper();
                    
                    CustomGoogleMap map = ((OutsidePlantComponent) parentComponent)
                        .getGoogleMapWrapper().getMap();
                    
//                    if (mapWrapper.getCurrentView() != null) {
//                        try {
//                            map.enableCleanTool();
//                            
//                            parentComponent.getWsBean().updateGeneralView(
//                                    mapWrapper.getCurrentView().getId(),
//                                    mapWrapper.getCurrentView().getName(),
//                                    mapWrapper.getCurrentView().getDescription(),
//                                    map.getAsXML(),
//                                    null,
//                                    Page.getCurrent().getWebBrowser().getAddress(),
//                                    parentComponent.getApplicationSession().getSessionId());
//                            
//                            Notification.show("The view was cleaned", Notification.Type.TRAY_NOTIFICATION);
//                        } catch (ServerSideException ex) {
//                            Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
//                        }
//                    } else {
//                        map.enableCleanTool();
//                        Notification.show("The view was cleaned", Notification.Type.TRAY_NOTIFICATION);
//                    }
                })
                .withCancelButton();
            
            mbClean.open();
        });
    }
    
    private void createButtonComboBoxSearch() {
//        cboSearch = new ComboBox();
//        cboSearch.setDescription("Search");
//        cboSearch.addFocusListener((FieldEvents.FocusEvent event) -> {
//            cboSearch.setValue(null);
//            CustomGoogleMap map = ((OutsidePlantComponent) parentComponent)
//                .getGoogleMapWrapper().getMap();
//            
//            List<Object> elements = map.getVisbleNodesAndConnections();
//            cboSearch.removeAllItems();
//            cboSearch.addItem(null);
//            cboSearch.addItems(elements);
//        });
//        cboSearch.setNullSelectionAllowed(true);
//        cboSearch.setValue(null);
//        cboSearch.setNullSelectionItemId(null);
//        //TODO: deafult size
//        cboSearch.setHeight(toolBarSize.size() + 2 * TOOLBAR_VERTICAL_PADDING, Unit.PIXELS);
//                
//        ((AbstractOrderedLayout) getToolbarLayout()).addComponent(cboSearch);
    }
    
    private void createButtonSearch() {
        btnSearch = createButton(null);
        btnSearch.setIcon(new ThemeResource(BUTTON_ICON_SEARCH));
        btnSearch.setDescription(BUTTON_CAPTION_SEARCH);
//        btnSearch.addClickListener((Button.ClickEvent event) -> {
//            ((OutsidePlantComponent) parentComponent)
//                    .getGoogleMapWrapper().getMap().moveMapToOverlay(cboSearch.getValue());
//        });
    }
    
    public void enableTools(boolean enable) {
        btnNew.setEnabled(true);
        btnOpen.setEnabled(true);
        btnSave.setEnabled(enable);
        btnDelete.setEnabled(enable);
        
        btnNodeLabels.setEnabled(enable); // are there nodes? enable
        setButtonSelected(btnNodeLabels, true);
        
        btnConnLabels.setEnabled(enable); // are there connections? enable
        setButtonSelected(btnConnLabels, true);
        
        btnSelect.setEnabled(enable);
        setButtonSelected(btnSelect, true);
        
////        btnConnect.setEnabled(enable);
        btnNewContainer.setEnabled(enable);
        btnNewLink.setEnabled(enable);
        btnPolygon.setEnabled(enable);
        btnMeasure.setEnabled(enable);
        btnFilter.setEnabled(enable);
        btnClean.setEnabled(enable);
        cboSearch.setEnabled(enable);
        btnSearch.setEnabled(enable);
    }
        
    public void register() {
        if (parentComponent != null)
            parentComponent.getEventBus().register(this);
    }
    
    public void unregister() {
        if (parentComponent != null)
            parentComponent.getEventBus().unregister(this);
    }

    @Override
    public TopComponent getTopComponent() {
        return parentComponent;
    }
}