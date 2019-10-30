package org.kuwaiba.web;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;
import javax.inject.Inject;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.web.authentication.AccessControl;
import org.kuwaiba.web.modules.welcome.WelcomeComponentFlow;


/**
 * The main view contains a simple label element and a template element.
 */
@Route("")
@PWA(name = "Project Base for Vaadin Flow with CDI", shortName = "Project Base")
public class IndexUI extends VerticalLayout  implements BeforeEnterObserver {

    /**
     * The reference to the back end bean
     */
    @Inject
    WebserviceBean wsBean;
    //contains valid user logged inside application
    private static AccessControl accessControl;
    
    public IndexUI() {
        
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (getAccessControl() == null || !getAccessControl().isUserSignedIn()) {
            //reroute to login screen
            event.forwardTo(LoginViewFlow.class);
        } else if (getAccessControl() != null && getAccessControl().isUserSignedIn()) {
            //reroute to Welcome screen
            event.forwardTo(WelcomeComponentFlow.class);
        }
    }

    public static AccessControl getAccessControl() {
        return accessControl;
    }
    
    public static void setAccessControl(AccessControl accessControl) {
        IndexUI.accessControl = accessControl;
    }
}