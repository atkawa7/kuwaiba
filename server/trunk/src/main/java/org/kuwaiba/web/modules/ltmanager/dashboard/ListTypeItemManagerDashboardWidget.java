/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.kuwaiba.web.modules.ltmanager.dashboard;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Page;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.util.ArrayList;
import java.util.List;
import org.kuwaiba.apis.web.gui.dashboards.AbstractDashboardWidget;
import org.kuwaiba.apis.web.gui.events.OperationResultListener;
import org.kuwaiba.apis.web.gui.properties.PropertyFactory;
import org.kuwaiba.apis.web.gui.properties.PropertySheet;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadataLight;
import org.kuwaiba.web.modules.ltmanager.actions.AddListTypeItemWindow;

/**
 * A dashboard widget that allows to manage the list type items associated to a given list type
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ListTypeItemManagerDashboardWidget extends AbstractDashboardWidget {
    /**
     * The list type associated to this widget
     */
    private RemoteClassMetadataLight listType;
    /**
     * Table containing the related list type items
     */
    private ListTypeItemsControlTable tblListTypeItems;
    /**
     * The property sheet that allows to edit a list type item properties
     */
    private PropertySheet propertySheet;
    /**
     * Reference to the ws bean
     */
    private WebserviceBean wsBean;
    
    public ListTypeItemManagerDashboardWidget(RemoteClassMetadataLight listType, WebserviceBean wsBean) {
        super(String.format("List Type Items for %s", listType.getClassName()));
        this.wsBean = wsBean;
        this.listType = listType;
        this.createContent();
        this.setSizeFull();
    }
    
    @Override
    public void createCover() { }  //Not used

    @Override
    public void createContent() { 
        HorizontalLayout lytContent = new HorizontalLayout();
        lytContent.setMargin(true);
        try {
            List<RemoteObjectLight> listTypeItems = wsBean.getListTypeItems(listType.getClassName(), Page.getCurrent().getWebBrowser().getAddress(), 
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
            this.tblListTypeItems = new ListTypeItemsControlTable(listTypeItems);
            lytContent.addComponent(this.tblListTypeItems);
            this.propertySheet = new PropertySheet(new ArrayList<>(), "");
            lytContent.addComponent(this.propertySheet);
            
        } catch (ServerSideException ex) {
            Notifications.showError(ex.getMessage());
        }
        this.contentComponent = lytContent;
        addComponent(contentComponent);
    }
    
    /**
     * Updates the property sheet depending on the selection
     */
    public void updatePropertySheet() {
        if (!this.tblListTypeItems.getLstListTypeItems().getSelectedItems().isEmpty()) {
            try {
                
                RemoteObjectLight selectedItem = this.tblListTypeItems.getLstListTypeItems().getSelectedItems().iterator().next();
                
                this.propertySheet.setCaption(String.format("Properties in %s", selectedItem));
                this.propertySheet.setItems(PropertyFactory.propertiesFromRemoteObject(selectedItem, wsBean));
                
                
            } catch (ServerSideException ex) {
                this.propertySheet.clear();
                Notifications.showError(ex.getMessage());
            }
        } else
            this.propertySheet.clear();
    }
    
    /**
     * The combination of a table containing the list type items and a few buttons with options 
     */
    private class ListTypeItemsControlTable extends VerticalLayout {
        /**
         * A button to add new list type items
         */
        private Button btnAddListTypeItem;
        /**
         * A button to see what objects refer to the selected list type item
         */
        private Button btnDeleteListTypeItem;
        /**
         * A button to delete the selected list type item
         */
        private Button btnSeeListTypeItemUses;
        /**
         * The list with the actual list type items
         */
        private Grid<RemoteObjectLight> lstListTypeItems;
        
        public ListTypeItemsControlTable(List<RemoteObjectLight> listTypeItems) {
            HorizontalLayout lytButtons = new HorizontalLayout();
            btnAddListTypeItem = new Button("Add", (event) -> {
                AddListTypeItemWindow wdwAddListTypeItem = new AddListTypeItemWindow(listType, wsBean, new OperationResultListener() {
                    @Override
                    public void doIt() {
                        refreshListTypeItemsList();
                    }
                });
                
                getUI().addWindow(wdwAddListTypeItem);
            });
            btnAddListTypeItem.setWidth(100, Unit.PERCENTAGE);
            btnAddListTypeItem.setIcon(VaadinIcons.INSERT);
            
            btnSeeListTypeItemUses = new Button("See Uses", (event) -> {
                try {
                    if (lstListTypeItems.getSelectedItems().isEmpty())
                        Notifications.showError("You need to select an item first");
                    else {
                        RemoteObjectLight selectedItem = lstListTypeItems.getSelectedItems().iterator().next();
                        List<RemoteObjectLight> listTypeItemUses = wsBean.getListTypeItemUses(selectedItem.getClassName(), selectedItem.getId(), -1, Page.getCurrent().getWebBrowser().getAddress(), 
                                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
                        if (listTypeItemUses.isEmpty()) 
                            Notifications.showInfo("This list type item is not used by any inventory object");
                        else {
                            Window wdwListTypeItemUses = new Window(String.format("Objects using %s", selectedItem));
                            Grid<RemoteObjectLight> tblListTypeItemUses = new Grid<>();
                            tblListTypeItemUses.setItems(listTypeItemUses);
                            tblListTypeItemUses.addColumn(RemoteObjectLight::getName).setCaption("Name");
                            tblListTypeItemUses.addColumn(RemoteObjectLight::getClassName).setCaption("Type");
                            tblListTypeItemUses.setWidth(100, Unit.PERCENTAGE);
                            
                            wdwListTypeItemUses.setContent(tblListTypeItemUses);
                            wdwListTypeItemUses.center();
                            wdwListTypeItemUses.setWidth(20, Unit.PERCENTAGE);
                            wdwListTypeItemUses.setModal(true);
                            UI.getCurrent().addWindow(wdwListTypeItemUses);
                        }
                    }
                } catch (ServerSideException ex) {
                    Notifications.showError(ex.getLocalizedMessage());
                }
            });
            btnSeeListTypeItemUses.setWidth(100, Unit.PERCENTAGE);
            btnSeeListTypeItemUses.setIcon(VaadinIcons.ARROW_CIRCLE_RIGHT);
            
            btnDeleteListTypeItem = new Button("Delete", (event) -> {
                if (lstListTypeItems.getSelectedItems().isEmpty())
                    Notifications.showError("You need to select an item first");
                else {
                    RemoteObjectLight selectedItem = lstListTypeItems.getSelectedItems().iterator().next();
                    try {
                        wsBean.deleteListTypeItem(selectedItem.getClassName(), selectedItem.getId(), 
                                false, Page.getCurrent().getWebBrowser().getAddress(), 
                        ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
                        refreshListTypeItemsList();
                    } catch (ServerSideException ex) {
                        Notifications.showError(ex.getMessage());
                    }
                }
            });
            btnDeleteListTypeItem.setWidth(100, Unit.PERCENTAGE);
            btnDeleteListTypeItem.setIcon(VaadinIcons.CLOSE);
            
            lytButtons.setWidth(100, Unit.PERCENTAGE);
            lytButtons.setSpacing(false);
            lytButtons.setSizeFull();
            lytButtons.addComponents(btnAddListTypeItem, btnSeeListTypeItemUses, btnDeleteListTypeItem);
            
            lstListTypeItems = new Grid<>();
            lstListTypeItems.setSelectionMode(Grid.SelectionMode.SINGLE);
            lstListTypeItems.setItems(listTypeItems);
            lstListTypeItems.addColumn(RemoteObjectLight::getName).setCaption("Items in this List Type");
            
            lstListTypeItems.addSelectionListener((e) -> {
                updatePropertySheet();
            });
            
            setSpacing(false);
            setSizeUndefined();
            addComponents(lstListTypeItems, lytButtons);
        }
        
        public void refreshListTypeItemsList() {
            try {
                List<RemoteObjectLight> listTypeItems = wsBean.getListTypeItems(listType.getClassName(), Page.getCurrent().getWebBrowser().getAddress(), 
                        ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());

                lstListTypeItems.setItems(listTypeItems);

            } catch (ServerSideException ex) {
                Notifications.showError(ex.getMessage());
            }
        }

        public Grid<RemoteObjectLight> getLstListTypeItems() {
            return lstListTypeItems;
        }
    }
}
