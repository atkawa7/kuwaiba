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
package org.kuwaiba.web.modules.containment;

import com.google.common.eventbus.EventBus;
import com.vaadin.event.Action;
import com.vaadin.event.Transferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
//import com.vaadin.ui.Table;
import com.vaadin.ui.Tree;
//import com.vaadin.ui.Tree.TreeDragMode;
import com.vaadin.ui.VerticalLayout;
//import com.vaadin.ui.themes.Reindeer;
import com.vaadin.ui.themes.ValoTheme;
import java.util.ArrayList;
import java.util.List;
import org.kuwaiba.apis.web.gui.actions.AbstractAction;
import org.kuwaiba.apis.web.gui.modules.AbstractTopComponent;
import org.kuwaiba.apis.web.gui.nodes.containment.ContainmentNode;
import org.kuwaiba.beans.WebserviceBeanLocal;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.metadata.ClassInfoLight;
import org.kuwaiba.web.custom.tree.DynamicTree;

/**
 * The main component of the Containtment Manager module.
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ContainmentManagerComponent extends AbstractTopComponent {
        
    public ContainmentManagerComponent(EventBus eventBus, WebserviceBeanLocal wsBean, RemoteSession session) {
        super(wsBean, eventBus, session);
        
        VerticalLayout compositionRoot = new VerticalLayout();
        compositionRoot.setSizeFull();
        
        Label lblInfo = new Label(
                "Drag and drop from the right list the classes "
                        + "you want to be possibly children "
                        + "of any of the classes in the left tree.");
        
        compositionRoot.addComponent(lblInfo);
        compositionRoot.setExpandRatio(lblInfo, 0.05f);
        lblInfo.setSizeFull();
        
        HorizontalSplitPanel content = new HorizontalSplitPanel();
        
        try {
            List<ClassInfoLight> allMeta = wsBean.getAllClassesLight(false, 
                    Page.getCurrent().getWebBrowser().getAddress(), 
                    session.getSessionId());
            
            List<ClassInfoLight> treeModel = new ArrayList();
            List<ClassInfoLight> listModel = new ArrayList();
            
            for (ClassInfoLight item : allMeta) {
                listModel.add(item);
                if (!item.isAbstract())
                    treeModel.add(item);
            }
            DynamicTree tree = new DynamicTree(null, this);
            
//            tree.setDragMode(TreeDragMode.NONE);
//            
//            ClassInfoLight navtreeRootClass = new ClassInfoLight();
//            navtreeRootClass.setDisplayName("Navigation Tree Root");
//            navtreeRootClass.setId(-1);
//            
//            ContainmentNode navtreeRootNode = new ContainmentNode(navtreeRootClass);
//            navtreeRootNode.setDisplayName(navtreeRootClass.getDisplayName());
//            navtreeRootNode.setTree(tree);
//            navtreeRootNode.getTree().setItemIcon(navtreeRootNode, 
//                    new ThemeResource("img/mod_containtment_icon_flag_red.png"));
//            
//            for (ClassInfoLight item : treeModel) {
//                ContainmentNode containmentNode = new ContainmentNode(item);
//                containmentNode.setTree(tree);
//                containmentNode.getTree().setItemIcon(containmentNode, 
//                        new ThemeResource("img/mod_containtment_icon_flag_green.png"));
//            }
//            
//            Table table = new Table();
//            table.setSizeFull();
//            table.setVisible(true);
//            table.addContainerProperty("class", ContainmentNode.class, null);
//            
//            for (ClassInfoLight item: listModel) {
//                table.addItem(new Object[]{new ContainmentNode(item)}, item.getId());
//            }
//            
//            table.setPageLength(table.size());
//            table.setSelectable(true);
//            table.setImmediate(true);
//            table.setDragMode(Table.TableDragMode.ROW);
//            table.setColumnHeaderMode(Table.ColumnHeaderMode.HIDDEN);
//            table.addStyleName(Reindeer.TABLE_BORDERLESS) ;
//            table.addStyleName(ValoTheme.TABLE_NO_STRIPES);
//            table.addStyleName(ValoTheme.TABLE_NO_HORIZONTAL_LINES);
//                                    
//            tree.setDropHandler(new DropHandler() {
//
//                public void drop(DragAndDropEvent event) {
//                    Transferable t = event.getTransferable();
//                    
//                    Tree.TreeTargetDetails target = 
//                            (Tree.TreeTargetDetails) event.getTargetDetails();
//                    
//                    Object sourceItemId = t.getData("itemId");
//                    Object targetItemId = target.getItemIdOver();
//                    
//                    if (!tree.areChildrenAllowed(targetItemId))
//                        return;
//
//                    if (event.getTransferable().getSourceComponent() instanceof Table) {
//                        try {
//                            ContainmentNode child = (ContainmentNode) 
//                                    table.getItem(sourceItemId)
//                                            .getItemProperty("class").getValue();
//                            ContainmentNode parent = (ContainmentNode) targetItemId;
//                                                        
//                            long[] children = new long[]{((ClassInfoLight) child.getObject()).getId()};
//                            
//                            wsBean.addPossibleChildren(((ClassInfoLight) parent.getObject()).getId(), 
//                                    children, 
//                                    Page.getCurrent().getWebBrowser().getAddress(), 
//                                    session.getSessionId());
//                            
//                            child.setTree(tree);
//                            child.getTree().setChildrenAllowed(child, false);
//                            child.getTree().setParent(child, parent);
//                            child.getTree().setItemIcon(child, 
//                                    new ThemeResource("img/mod_containtment_icon_flag_black.png"));
//                                                        
//                            Notification.show("Operation completed successfully", 
//                                    Notification.Type.TRAY_NOTIFICATION);
//                        } catch (ServerSideException ex) {
//                            Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
//                        }
//                    }
//                }
//
//                public AcceptCriterion getAcceptCriterion() {
//                    return AcceptAll.get();
//                }
//            });
//            tree.addActionHandler(new Action.Handler() {
//
//                @Override
//                public Action[] getActions(Object target, Object sender) {
//                    if (target instanceof ContainmentNode)
//                        return ((ContainmentNode) target).getActions();
//                    return null;
//                }
//
//                @Override
//                public void handleAction(Action action, Object sender, Object target) {
//                    ((AbstractAction) action).actionPerformed(sender, target);
//                }
//            });
                        
            content.setFirstComponent(tree);
            //content.setSecondComponent(table);
        } catch (ServerSideException ex) {
            Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
        compositionRoot.addComponent(content);
        compositionRoot.setExpandRatio(content, 0.95f);
        content.setSizeFull();
        
        setCompositionRoot(compositionRoot);
        
        this.setSizeFull();
    }

    @Override
    public void registerComponents() {}

    @Override
    public void unregisterComponents() {}
    
}
