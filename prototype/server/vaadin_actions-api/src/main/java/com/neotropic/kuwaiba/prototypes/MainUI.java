package com.neotropic.kuwaiba.prototypes;

import com.neotropic.kuwaiba.prototypes.view.GISView;
import com.neotropic.kuwaiba.prototypes.view.TreeView;
import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.UI;

/**
 * This UI is the application entry point. A UI may either represent a browser window 
 * (or tab) or some part of a html page where a Vaadin application is embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is intended to be 
 * overridden to add component to the user interface and initialize non-component functionality.
 */
@Theme("kuwaiba")
public class MainUI extends UI {

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        TreeView treeView = new TreeView();
        GISView gisView = new GISView();
        HorizontalSplitPanel pnlSplitMain = new HorizontalSplitPanel(treeView, gisView);
        pnlSplitMain.setSplitPosition(20);
        setContent(pnlSplitMain);
    }

    @WebServlet(urlPatterns = "/*", name = "MainUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MainUI.class, productionMode = false, widgetset = "com.neotropic.kuwaiba.prototypes.AppWidgetSet")
    public static class MainUIServlet extends VaadinServlet {
    }
}
