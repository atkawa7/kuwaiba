package com.neotropic.forms;

import com.neotropic.api.forms.ElementBuilder;
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
        
        try {
            String basepath = VaadinService.getCurrent()
                .getBaseDirectory().getAbsolutePath();
            
            Scanner in = new Scanner(new File(basepath + "/WEB-INF/SOF_V2.xml"));
            
            String line = "";
            
            while (in.hasNext()) { // Iterates each line in the file
                line += in.nextLine();
                // Do something with line
            }
            byte [] structure = line.getBytes();
            
            in.close();
            
            ElementBuilder formBuilder = new ElementBuilder();            
            formBuilder.build(structure);
            
//            ScriptRunner scriptRunner = new ScriptRunner(formBuilder.getElements(), formBuilder.getScript().getFunctions());
                        
            Window subWindow = new Window(formBuilder.getEvaluator().getValue(formBuilder.getRoot().getTitle()));
            subWindow.setModal(true);
            
            FormRenderer formRenderer = new FormRenderer(formBuilder);
            
            Panel pnlForm = new Panel();
            pnlForm.setContent(formRenderer);
            pnlForm.setSizeUndefined();
            subWindow.setContent(pnlForm);
            
            final VerticalLayout layout = new VerticalLayout();
            
            Button button = new Button(formBuilder.getEvaluator().getValue(formBuilder.getRoot().getTitle()));
            
            button.addClickListener(e -> {
                
                formRenderer.render();
                subWindow.setResizable(true);
                subWindow.center();
                subWindow.setSizeFull();
                UI.getCurrent().addWindow(subWindow);
                
//                scriptRunner.run(Constants.Function.GLOBAL);
            });
            layout.addComponents(button);
            
            setContent(layout);
            
        } catch (FileNotFoundException ex) {
            int i = 0;            
        }
    }

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MyUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }
}
