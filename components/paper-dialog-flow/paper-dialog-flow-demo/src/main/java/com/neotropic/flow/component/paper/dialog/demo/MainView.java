package com.neotropic.flow.component.paper.dialog.demo;

import com.neotropic.flow.component.paper.dialog.PaperDialog;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;
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
     *
     * @param service The message service. Automatically injected Spring managed bean.
     */
    public MainView(@Autowired GreetService service) {
        Button btnOpenPaperDialog = new Button("Open Paper Dialog");
        btnOpenPaperDialog.setWidth("70%");
        
        Button btnClosePaperDialog = new Button("Close Paper Dialog");
        
        PaperDialog paperDialog = new PaperDialog();
        // Configuring the paper dialog to show below of the btnOpenPaperDialog
        paperDialog.setNoOverlap(true);
        paperDialog.setHorizontalAlign(PaperDialog.HorizontalAlign.LEFT);
        paperDialog.setVerticalAlign(PaperDialog.VerticalAlign.TOP);
        paperDialog.setMargin(false);        
        paperDialog.positionTarget(btnOpenPaperDialog);
        paperDialog.setWidth(btnOpenPaperDialog.getWidth());
        
        paperDialog.add(btnClosePaperDialog);
        // Set the dialog-confirm attribute to one of the paper dialog children
        paperDialog.dialogConfirm(btnClosePaperDialog);
        
        btnOpenPaperDialog.addClickListener(event -> paperDialog.open());
        
        add(btnOpenPaperDialog);
        add(paperDialog);
        add(new Label("label 1"));
        add(new Label("label 2"));
        add(new Label("label 3"));
        add(new Label("label 4"));
        add(new Label("label 5"));
        add(new Label("label 6"));
        add(new Label("label 7"));
        add(new Label("label 8"));
    }

}
