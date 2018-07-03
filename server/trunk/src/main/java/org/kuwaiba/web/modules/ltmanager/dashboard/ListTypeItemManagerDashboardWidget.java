/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>
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
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.util.List;
import org.kuwaiba.apis.web.gui.dashboards.AbstractDashboardWidget;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadataLight;

/**
 * A dashboard widget that allows to manage the list type items associated to a given list type
 * @author Charles Bedon <charles.bedon@kuwaiba.org>
 */
public class ListTypeItemManagerDashboardWidget extends AbstractDashboardWidget {
    /**
     * The list type associated to this widget
     */
    private RemoteClassMetadataLight listType;
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
        try {
            List<RemoteObjectLight> listTypeItems = wsBean.getListTypeItems(listType.getClassName(), Page.getCurrent().getWebBrowser().getAddress(), 
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
            
            lytContent.addComponent(new ListTypeItemsControlTable(listTypeItems));
            
        } catch (ServerSideException ex) {
            Notifications.showError(ex.getMessage());
        }
        this.contentComponent = lytContent;
        addComponent(contentComponent);
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
                Window wdwAddListTypeItem = new Window("New List Type Item");
                
                TextField txtName = new TextField("Name");
                txtName.setRequiredIndicatorVisible(true);
                txtName.setSizeFull();
                
                
                TextField txtDisplayName = new TextField("Display Name");
                txtDisplayName.setSizeFull();
                
                Button btnOK = new Button("OK", (e) -> {
                    try {
                        wsBean.createListTypeItem(listType.getClassName(), txtName.getValue(), 
                                txtDisplayName.getValue(), Page.getCurrent().getWebBrowser().getAddress(), 
                            ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
                        refreshListTypeItemsList();
                    } catch (ServerSideException ex) {
                        Notifications.showError(ex.getMessage());
                    }
                    wdwAddListTypeItem.close();
                });
                
                btnOK.setEnabled(false);
                txtName.addValueChangeListener((e) -> {
                    btnOK.setEnabled(!txtName.isEmpty());
                });
                
                Button btnCancel = new Button("Cancel", (e) -> {
                    wdwAddListTypeItem.close();
                });
 
                wdwAddListTypeItem.setModal(true);
                wdwAddListTypeItem.setWidth(10, Unit.PERCENTAGE);
                wdwAddListTypeItem.center();
                
                FormLayout lytTextFields = new FormLayout(txtName, txtDisplayName);
                HorizontalLayout lytMoreButtons = new HorizontalLayout(btnOK, btnCancel);
                VerticalLayout lytMain = new VerticalLayout(lytTextFields, lytMoreButtons);
                lytMain.setComponentAlignment(lytMoreButtons, Alignment.TOP_RIGHT);
                
                wdwAddListTypeItem.setContent(lytMain);
                
                getUI().addWindow(wdwAddListTypeItem);
            });
            btnAddListTypeItem.setWidth(100, Unit.PERCENTAGE);
            btnAddListTypeItem.setIcon(VaadinIcons.INSERT);
            
            btnSeeListTypeItemUses = new Button("See Uses", (event) -> {
                Notifications.showError("Not Implemented Yet");
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
    }
}
