package com.neotropic.forms;

import com.neotropic.api.forms.ElementBuilder;
import com.neotropic.web.components.TreeWrapper;
import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

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
        String [] formFiles = {"formcustomerorder", "formequipmentselector"};
        
        String basepath = VaadinService.getCurrent()
            .getBaseDirectory().getAbsolutePath();
        
        VerticalLayout verticalLayout = new VerticalLayout();
        setContent(verticalLayout);
        
////        TreeWrapper treeWrapper = new TreeWrapper();
////        setContent(treeWrapper.getTree());
                
        for (String formFile : formFiles) {
            try {
                Scanner in = new Scanner(new File(basepath + "/WEB-INF/" + formFile + ".xml"));

                String line = "";

                while (in.hasNext())
                    line += in.nextLine();
                    
                byte [] structure = line.getBytes();

                in.close();

                ElementBuilder formBuilder = new ElementBuilder();            
                formBuilder.build(structure);

                Window subWindow = new Window(formBuilder.getEvaluator().getValue(formBuilder.getRoot().getTitle()));
                subWindow.setModal(true);

                FormRenderer formRenderer = new FormRenderer(formBuilder);

                Panel pnlForm = new Panel();
                pnlForm.setContent(formRenderer);
                pnlForm.setSizeUndefined();
                subWindow.setContent(pnlForm);
                
                Button button = new Button(formBuilder.getEvaluator().getValue(formBuilder.getRoot().getTitle()));
                
                button.addClickListener(e -> {

                    formRenderer.render();
                    subWindow.setResizable(true);
                    subWindow.center();
                    subWindow.setSizeFull();
                    UI.getCurrent().addWindow(subWindow);
                });
                verticalLayout.addComponents(button);
                                
            } catch (FileNotFoundException ex) {
            }
        }
    }

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MyUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }
}
