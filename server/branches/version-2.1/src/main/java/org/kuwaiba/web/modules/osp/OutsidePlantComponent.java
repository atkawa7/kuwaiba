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
package org.kuwaiba.web.modules.osp;

//import com.neotropic.vaadin14.component.MxGraph;
//import com.neotropic.vaadin14.component.MxGraphClickEdgeEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dnd.DropEvent;
import com.vaadin.flow.component.dnd.DropTarget;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.grid.ItemDoubleClickEvent;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.grid.contextmenu.GridMenuItem;
import com.vaadin.flow.component.grid.dnd.GridDragEndEvent;
import com.vaadin.flow.component.grid.dnd.GridDragStartEvent;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResourceRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.inject.Inject;
import org.kuwaiba.apis.web.gui.modules.AbstractTopComponent;
import org.kuwaiba.apis.web.gui.resources.ResourceFactory;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.services.persistence.util.Constants;
import org.kuwaiba.web.KuwaibaConst;
import org.kuwaiba.web.MainLayout;
/**
 * Main window of the Outside Plant module
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Route(value = KuwaibaConst.PAGE_OSP, layout = MainLayout.class)
@PageTitle(KuwaibaConst.TITLE_OSP)
public class OutsidePlantComponent extends AbstractTopComponent implements BeforeEnterObserver {
    /**
     * The name of the view
     */
    public static String ROUTE_VALUE = "osp";
    /**
     * Reference to the backend bean
     */
    @Inject
    private WebserviceBean webserviceBean;
    
    public OutsidePlantComponent() {
//        init();
    }
    
    public void init() {
        final RemoteSession remoteSession = UI.getCurrent().getSession().getAttribute(RemoteSession.class);
        
        TreeGrid<RemoteObjectLight> treeGrid = new TreeGrid();
//        treeGrid.addHierarchyColumn(RemoteObjectLight::toString);
        Renderer<RemoteObjectLight> renderer = TemplateRenderer.<RemoteObjectLight> of("<vaadin-grid-tree-toggle "
                        + "leaf='[[item.leaf]]' expanded='{{expanded}}' level='[[level]]'>"
                        + "<img width=\"32px\" height=\"32px\" src='" + "[[item.icon]]" + "' alt='oe!'>&nbsp;&nbsp;" 
                        + "[[item.name]]"
                        + "</vaadin-grid-tree-toggle>")
        .withProperty("leaf", item -> !treeGrid.getDataCommunicator().hasChildren(item))
        .withProperty("icon", icon -> "icons/icon.png")
        .withProperty("name", value -> String.valueOf(value.toString()));
        treeGrid.addColumn(renderer);
//        treeGrid.addHierarchyColumn(item -> {
//            HorizontalLayout horizontalLayout = new HorizontalLayout();
//            horizontalLayout.add(VaadinIcon.VAADIN_H.create());
//            horizontalLayout.add(new Label(item.toString()));
//            horizontalLayout.setSizeFull();
//            return horizontalLayout;
//        });
//        treeGrid.addComponentColumn(item -> {
//            HorizontalLayout horizontalLayout = new HorizontalLayout();
//            horizontalLayout.add(VaadinIcon.VAADIN_H.create());
//            horizontalLayout.add(new Label(item.toString()));
//            horizontalLayout.setSizeFull();
//            return horizontalLayout;
//        });
        HierarchicalDataProvider hierarchicalDataProvider = new AbstractBackEndHierarchicalDataProvider<RemoteObjectLight, Void>() {
            @Override
            public int getChildCount(HierarchicalQuery<RemoteObjectLight, Void> query) {
                RemoteObjectLight parent = query.getParent();
                if (parent == null) {
                    try {
                        List<RemoteObjectLight> children = webserviceBean.getObjectChildren(Constants.NODE_DUMMYROOT, "-1", -1, remoteSession.getIpAddress(), remoteSession.getSessionId());
                        return children.size();
                    } catch (ServerSideException ex) {
                        return 0;
                    }
                } else {
                    try {
                        List<RemoteObjectLight> children = webserviceBean.getObjectChildren(parent.getClassName(), parent.getId(), -1, remoteSession.getIpAddress(), remoteSession.getSessionId());
                        return children.size();
                    } catch (ServerSideException ex) {
                        return 0;
                    }
                }
            }

            @Override
            public boolean hasChildren(RemoteObjectLight parent) {
                return true;
//                if (parent == null) {
//                    try {
//                        List<RemoteObjectLight> children = webserviceBean.getObjectChildren(Constants.NODE_DUMMYROOT, "-1", -1, remoteSession.getIpAddress(), remoteSession.getSessionId());
//                        return !children.isEmpty();
//                    } catch (ServerSideException ex) {
//                        return false;
//                    }
//                } else {
//                    try {
//                        List<RemoteObjectLight> children = webserviceBean.getObjectChildren(parent.getClassName(), parent.getId(), -1, remoteSession.getIpAddress(), remoteSession.getSessionId());
//                        return !children.isEmpty();
//                    } catch (ServerSideException ex) {
//                        return false;
//                    }
//                }
            }

            @Override
            protected Stream<RemoteObjectLight> fetchChildrenFromBackEnd(HierarchicalQuery<RemoteObjectLight, Void> query) {
                RemoteObjectLight parent = query.getParent();
                if (parent == null) {
                    try {
                        List<RemoteObjectLight> children = webserviceBean.getObjectChildren(Constants.NODE_DUMMYROOT, "-1", -1, remoteSession.getIpAddress(), remoteSession.getSessionId());
                        return children.stream();
                    } catch (ServerSideException ex) {
                        return null;
                    }
                } else {
                    try {
                        List<RemoteObjectLight> children = webserviceBean.getObjectChildren(parent.getClassName(), parent.getId(), -1, remoteSession.getIpAddress(), remoteSession.getSessionId());
                        return children.stream();
                    } catch (ServerSideException ex) {
                        return null;
                    }
                }
            }
        };
        treeGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS);
        treeGrid.setDataProvider(hierarchicalDataProvider);
        
        treeGrid.addItemClickListener(new ComponentEventListener<ItemClickEvent<RemoteObjectLight>>() {
            @Override
            public void onComponentEvent(ItemClickEvent<RemoteObjectLight> event) {
                Notification.show("Click " + event.getItem().toString());
            }
        });
        treeGrid.addItemDoubleClickListener(new ComponentEventListener<ItemDoubleClickEvent<RemoteObjectLight>>() {
            @Override
            public void onComponentEvent(ItemDoubleClickEvent<RemoteObjectLight> event) {
                Notification.show("Double Click " + event.getItem().toString());
            }
        });
        GridContextMenu<RemoteObjectLight> contextMenu = new GridContextMenu<>(treeGrid);
        GridMenuItem<RemoteObjectLight> menuItem1 = contextMenu.addItem("Menu Item 1");
        menuItem1.getSubMenu().addItem("Menu Item 1.1").getSubMenu().addItem("Menu Item 1.1.1", new ComponentEventListener<GridContextMenu.GridContextMenuItemClickEvent<RemoteObjectLight>>() {
            @Override
            public void onComponentEvent(GridContextMenu.GridContextMenuItemClickEvent<RemoteObjectLight> event) {
                Optional<RemoteObjectLight> item = event.getItem();
                if (!item.isPresent()) {
                    // no selected row
                    return;
                }
                Notification.show("Context Menu Item Click " + event.getItem().get().toString());
            }
        });
        
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.getStyle().set("background", "blue");
        
        final List<RemoteObjectLight> draggedItems = new ArrayList();
        
        treeGrid.addDragStartListener(new ComponentEventListener<GridDragStartEvent<RemoteObjectLight>>() {
            @Override
            public void onComponentEvent(GridDragStartEvent<RemoteObjectLight> event) { 
                draggedItems.clear();
                draggedItems.addAll(event.getDraggedItems());
                int i = 0;
            }
        });
        treeGrid.addDragEndListener(new ComponentEventListener<GridDragEndEvent<RemoteObjectLight>> () {
            @Override
            public void onComponentEvent(GridDragEndEvent<RemoteObjectLight> event) {                
                draggedItems.clear();
                int i = 0;                
            }
        });
//        DropTarget dropTarget = DropTarget;
        DropTarget<VerticalLayout> dropTarget = DropTarget.create(verticalLayout);
        dropTarget.addDropListener(new ComponentEventListener<DropEvent<VerticalLayout>>() {
            @Override
            public void onComponentEvent(DropEvent<VerticalLayout> event) {
                Notification.show("Drop " + draggedItems.get(0).toString());
                draggedItems.clear();
                RemoteObjectLight[] rols = treeGrid.getSelectedItems().toArray(new RemoteObjectLight[0]);
                int i = 0;
            }
        });
////        DragEndEvent
////        DropTarget DropTarget = DropTarget.create();
//        DropTarget<>
//        treeGrid.addDropListener(listener)
        treeGrid.setRowsDraggable(true);
////        treeGrid.setDropMode(GridDropMode.);
        horizontalLayout.add(treeGrid);
////        MxGraph myElement = new MxGraph();
////        myElement.getElement().setAttribute("width", "200px");
////        myElement.getElement().setAttribute("height", "200px");
////        myElement.setProp1("from Vaadin");
////        myElement.setGrid("mx-graph/images/grid.gif");
////        myElement.addClickEdgeListener(new ComponentEventListener<MxGraphClickEdgeEvent>() {
////            @Override
////            public void onComponentEvent(MxGraphClickEdgeEvent t) {
////                Notification.show("mxgraph click edge");
////            }
////        });
//        add(myElement);
////        verticalLayout.add(myElement);
        horizontalLayout.add(verticalLayout);
        
        treeGrid.setWidth("40%");
        verticalLayout.setWidth("60%");
        
        horizontalLayout.setSizeFull();
                        
        add(horizontalLayout);
        setSizeFull();
        
        String uri1 = StreamResourceRegistry.getURI(ResourceFactory.getInstance().getClassIcon("Building", webserviceBean)).toString();
        String uri2 = StreamResourceRegistry.getURI(ResourceFactory.getInstance().getClassSmallIcon("Building", webserviceBean)).toString();
        System.out.println(">>> " + uri1);
        System.out.println(">>> " + uri2);
//        InventoryObjectNode inventoryObjectNode = new InventoryObjectNode(new RemoteObjectLight("className", "oid", "name"));
//        BasicIconGenerator basicIconGenerator = new BasicIconGenerator();
//        basicIconGenerator.apply(inventoryObjectNode);
//        new Image("");
    }
        
    @Override
    public void registerComponents() {
    }

    @Override
    public void unregisterComponents() {
    }

    @Override
    public void beforeEnter(BeforeEnterEvent bee) {
        init();
        
    }

}
