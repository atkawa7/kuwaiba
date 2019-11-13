/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kuwaiba.web.procmanager;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import javax.inject.Inject;
import org.kuwaiba.apis.web.gui.modules.AbstractTopComponent;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.web.KuwaibaConst;
import org.kuwaiba.web.MainLayout;

/**
 *
 * @author johnyortega
 */
@Route(value = KuwaibaConst.PAGE_PROCESS_MANAGER, layout = MainLayout.class)
@PageTitle(KuwaibaConst.TITLE_PROCESS_MANAGER)
public class ProcessManagerComponent extends AbstractTopComponent {
    /**
     * Reference to the backend bean
     */
    @Inject
    private WebserviceBean webserviceBean;
    
    public ProcessManagerComponent() {
    }
    
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        setSizeFull();
        add(new TimelineView());
    }

    @Override
    public void registerComponents() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void unregisterComponents() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
