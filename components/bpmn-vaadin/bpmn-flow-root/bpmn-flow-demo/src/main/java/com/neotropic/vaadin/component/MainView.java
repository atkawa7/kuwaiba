package com.neotropic.vaadin.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A sample Vaadin view class.
 * <p>
 * To implement a Vaadin view just extend any Vaadin component and
 * use @Route annotation to announce it in a URL as a Spring managed
 * bean.
 * Use the @PWA annotation make the application installable on phones,
 * tablets and some desktop browsers.
 * <p>
 * A new instance of this class is created for every new user and every
 * browser tab/window.
 */
@Route
@PWA(name = "Vaadin Application",
        shortName = "Vaadin App",
        description = "This is an example Vaadin application.",
        enableInstallPrompt = false)
@CssImport("./styles/shared-styles.css")
@CssImport(value = "./styles/vaadin-text-field-styles.css", themeFor = "vaadin-text-field")
public class MainView extends VerticalLayout {

    /**
     * Construct a new Vaadin view.
     * <p>
     * Build the initial UI state for the user accessing the application.
     */
    public MainView() {
        setSizeFull();
        BpmnViewer bpmnViewer = new BpmnViewer(
            "https://cdn.staticaly.com/gh/bpmn-io/bpmn-js-examples/dfceecba/starter/diagram.bpmn");
        BpmnModeler bpmnDiagram = new BpmnModeler(
            "https://cdn.staticaly.com/gh/bpmn-io/bpmn-js-examples/dfceecba/starter/diagram.bpmn", 
            true, true);
        BpmnModeler newBpmnDiagram = new BpmnModeler(true, true);
        
        Button btnDiagram = new Button("Export diagram");
        Button btnNewBpmnDiagram = new Button("Export diagram");
        
        btnDiagram.addClickListener(e -> {
            bpmnDiagram.exportDiagram(xml -> {
                TextArea textArea = new TextArea();
                textArea.setValue(xml);
                Notification notification = new Notification(textArea);
                notification.setDuration(2000);
                notification.open();
            });
        });
        btnNewBpmnDiagram.addClickListener(e -> {
            newBpmnDiagram.exportDiagram(xml -> {
                TextArea textArea = new TextArea();
                textArea.setValue(xml);
                Notification notification = new Notification(textArea);
                notification.setDuration(2000);
                notification.open();
            });
        });
        Tab tabBpmnViewer = new Tab("BPMN Viewer");
        Tab tabBpmnDiagram = new Tab("BPMN Modeler");
        Tab tabNewBpmnDiagram = new Tab("New BPMN Diagram");
        Tabs tabs = new Tabs(tabBpmnViewer, tabBpmnDiagram, tabNewBpmnDiagram);
        tabs.setSelectedTab(tabBpmnViewer);
        
        Div divBpmnViewer = new Div();
        divBpmnViewer.setSizeFull();
        divBpmnViewer.add(bpmnViewer);
                
        Div divBpmnDiagram = new Div();
        divBpmnDiagram.setSizeFull();
        divBpmnDiagram.add(btnDiagram);
        divBpmnDiagram.add(bpmnDiagram);
        
        Div divNewBpmnDiagram = new Div();
        divNewBpmnDiagram.setSizeFull();
        divNewBpmnDiagram.add(btnNewBpmnDiagram);
        divNewBpmnDiagram.add(newBpmnDiagram);
                
        Map<Tab, Component> mapTabs = new HashMap();
        mapTabs.put(tabBpmnViewer, divBpmnViewer);
        mapTabs.put(tabBpmnDiagram, divBpmnDiagram);
        mapTabs.put(tabNewBpmnDiagram, divNewBpmnDiagram);
        
        Div div = new Div();
        div.setSizeFull();
        div.add(divBpmnViewer);
        
        tabs.addSelectedChangeListener(event -> {
            div.removeAll();
            div.add(mapTabs.get(event.getSelectedTab()));
        });
        
        add(tabs);
        add(div);
    }

}
