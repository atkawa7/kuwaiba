/**
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
package org.kuwaiba.web.modules.ltmanager;

import com.vaadin.cdi.CDIView;
import com.vaadin.data.HasValue;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import org.kuwaiba.apis.web.gui.modules.AbstractTopComponent;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadataLight;
import org.kuwaiba.services.persistence.util.Constants;
import org.kuwaiba.web.IndexUI;
import org.kuwaiba.web.modules.ltmanager.dashboard.ListTypeManagerDashboard;

/**
 * The main component of the List Manager module.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@CDIView("ltmanager")
public class ListTypeManagerComponent extends AbstractTopComponent {
    /**
     * View identifier
     */
    public static String VIEW_NAME = "ltmanager";
    /**
     * Text field to filter the types
     */
    private TextField txtListTypeFilter;
    /**
     * The table with the results
     */
    private Grid<RemoteClassMetadataLight> tblListTypes;
    /**
     * Layout for all the graphic components on the left side
     */
    private VerticalLayout lytLeftPanel;
    /**
     * The backend bean
     */
    @Inject
    private WebserviceBean wsBean;
    
    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        HorizontalSplitPanel pnlMain = new HorizontalSplitPanel();
        pnlMain.setSplitPosition(33, Unit.PERCENTAGE);
        MenuBar mnuMain = ((IndexUI)getUI()).getMainMenu();

        addComponent(mnuMain);
        addComponent(pnlMain);
        setExpandRatio(mnuMain, 0.5f);
        setExpandRatio(pnlMain, 9.5f);
        setSizeFull();
        
        
        try {
            List<RemoteClassMetadataLight> currentListTypes = wsBean.
                    getSubClassesLight(Constants.CLASS_GENERICOBJECTLIST, false, false, 
                            Page.getCurrent().getWebBrowser().getAddress(), 
                            ((RemoteSession) getSession().getAttribute("session")).getSessionId());
            
            txtListTypeFilter = new TextField();
            txtListTypeFilter.setPlaceholder("Search...");
            txtListTypeFilter.addValueChangeListener(this::onTxtFilterChange);
            txtListTypeFilter.setSizeFull();
            
            tblListTypes = new Grid<>();
            tblListTypes.setItems(currentListTypes);
            tblListTypes.addColumn(RemoteClassMetadataLight::toString).setCaption("Name");
            tblListTypes.setSizeFull();
            tblListTypes.setSelectionMode(Grid.SelectionMode.SINGLE);
            
            tblListTypes.addSelectionListener((selectionEvent) -> {
                if (!selectionEvent.getAllSelectedItems().isEmpty()) {
                    Optional<RemoteClassMetadataLight> selectedListType = selectionEvent.getFirstSelectedItem();
                    pnlMain.setSecondComponent(new ListTypeManagerDashboard(selectedListType.get(), wsBean));
                }
            });
            

            FormLayout lytFilter = new FormLayout(txtListTypeFilter);
            lytFilter.setMargin(true);

            lytLeftPanel = new VerticalLayout(lytFilter, tblListTypes);
            lytLeftPanel.setExpandRatio(lytFilter, 1);
            lytLeftPanel.setExpandRatio(tblListTypes, 9);
            lytLeftPanel.setSizeFull();
            pnlMain.setFirstComponent(lytLeftPanel);
            pnlMain.setSplitPosition(20, Unit.PERCENTAGE);
        } catch (ServerSideException ex) {
            Notifications.showError(ex.getMessage());
        }
    }
    
//    public ListTypeManagerComponent(EventBus eventBus, WebserviceBean wsBean, RemoteSession session) {
//        //super(wsBean, eventBus, session);
//    }
    
    private void onTxtFilterChange(HasValue.ValueChangeEvent<String> event) {
        ListDataProvider<RemoteClassMetadataLight> dataProvider = (ListDataProvider<RemoteClassMetadataLight>) tblListTypes.getDataProvider();
        dataProvider.setFilter((source) -> {
            String filterAsLowerCase = event.getValue().toLowerCase();
            return source.getClassName().toLowerCase().contains(filterAsLowerCase) || source.getClassName().toLowerCase().contains(filterAsLowerCase);
        });
    }

    @Override
    public void registerComponents() {
        //listTypesTree.register();
    }

    @Override
    public void unregisterComponents() {
        //listTypesTree.unregister();
    }
    
}
