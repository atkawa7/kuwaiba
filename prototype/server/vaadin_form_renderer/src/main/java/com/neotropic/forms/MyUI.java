package com.neotropic.forms;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import java.io.File;

/**
 * This UI is the application entry point. A UI may either represent a browser window 
 * (or tab) or some part of an HTML page where a Vaadin application is embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is intended to be 
 * overridden to add component to the user interface and initialize non-component functionality.
 */
@Theme("defaultformstheme")
public class MyUI extends UI {

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        String path = Variable.FORM_RESOURCE_STRUCTURES;
                
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        
        VerticalLayout verticalLayout = new VerticalLayout();
        setContent(verticalLayout);
                        
        for (File file : listOfFiles) {
            Button button = new Button(file.getName());

            button.addClickListener(e -> {
                                
                FormDisplayer.getInstance().display(file, true);
            });
            verticalLayout.addComponents(button);
        }
        
        path = Variable.FORM_RESOURCE_INSTANCES;
        folder = new File(path);
        listOfFiles = folder.listFiles();
        
        for (File file : listOfFiles) {
            Button button = new Button(file.getName());

            button.addClickListener(e -> {
                                
                FormInstanceDisplayer.getInstance().display(file, true);
            });
            verticalLayout.addComponents(button);
        }
        
        ScriptQueryManager.getInstance().loadScriptQueryFiles();
    }

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MyUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }
}
