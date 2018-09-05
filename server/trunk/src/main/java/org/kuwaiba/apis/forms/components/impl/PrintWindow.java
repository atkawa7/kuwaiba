/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kuwaiba.apis.forms.components.impl;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Scanner;
import org.kuwaiba.apis.forms.FormRenderer;
import org.kuwaiba.apis.forms.elements.AbstractElement;
import org.kuwaiba.apis.forms.elements.AbstractElementField;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteArtifact;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteArtifactDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteProcessInstance;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.util.i18n.I18N;
import org.kuwaiba.web.procmanager.FormArtifactRenderer;

/**
 * Window to show a preview and confirm the print action
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class PrintWindow extends Window {
    private final String templatePath;
    private final RemoteArtifactDefinition remoteArtifactDefinition;
    private final RemoteArtifact remoteArtifact;
    private final WebserviceBean webserviceBean;
    private final RemoteSession remoteSession;
    private final RemoteProcessInstance remoteProcessInstance;
    
    public PrintWindow(RemoteArtifactDefinition remoteArtifactDefinition, RemoteArtifact remoteArtifact, WebserviceBean webserviceBean, RemoteSession remoteSession, RemoteProcessInstance remoteProcessInstance, String templatePath) {
        this.remoteArtifactDefinition = remoteArtifactDefinition;
        this.remoteArtifact = remoteArtifact;
        this.webserviceBean = webserviceBean;
        this.remoteSession = remoteSession;
        this.remoteProcessInstance = remoteProcessInstance;
        
        this.templatePath = templatePath;
        init();        
    }    
    
    private void init() {
        GridLayout gridLayout = new GridLayout();
        gridLayout.setMargin(true);
        gridLayout.setSpacing(true);
        gridLayout.setColumns(2);
        gridLayout.setRows(2);
        
        Panel pnlPreview = new Panel();
        pnlPreview.setHeight("360px");
        pnlPreview.setWidth("1024px");
        pnlPreview.setContent(getPrintLayout());
                
        Button btnCancel = new Button(I18N.gm("cancel"), VaadinIcons.CLOSE);
        
        Button btnPrint = new Button(I18N.gm("print"), VaadinIcons.PRINT);
        
        btnPrint.addStyleName(ValoTheme.BUTTON_PRIMARY);
        btnPrint.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_RIGHT);
                
        btnCancel.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                PrintWindow.this.close();
            }
        });
        
        btnPrint.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                String divId = "divdiv";
                
                JavaScript.getCurrent().execute(""
                    .concat("var divToPrint = document.getElementById('" + divId + "').innerHTML;")
                    .concat("var popupWin = window.open('', '_blank', 'width=300,height=300');")
                    .concat("popupWin.document.open();")
                    .concat("popupWin.document.write('<!DOCTYPE html><html><head><title></title><link rel=\"stylesheet\" type=\"text/css\" href=\"./VAADIN/css/printabletemplate.css\"></head><body onload=\"window.print()\">' + ").concat("divToPrint").concat(" + '</body></html>');")
                    .concat("popupWin.document.close();")
                );
            }
        });
                
        gridLayout.addComponent(pnlPreview, 0, 0, 1, 0);
        gridLayout.addComponent(btnCancel);
        gridLayout.addComponent(btnPrint);
        
        gridLayout.setComponentAlignment(pnlPreview, Alignment.MIDDLE_CENTER);
        gridLayout.setComponentAlignment(btnCancel, Alignment.MIDDLE_RIGHT);
        gridLayout.setComponentAlignment(btnPrint, Alignment.MIDDLE_LEFT);
                
        setContent(gridLayout); 
        setModal(true);
        center();
    }
    
    private PrintLayout getPrintLayout() {
        
        PrintLayout printLayout = null;
        try {
            /*
            File file = new File(templatePath);
            printLayout = new PrintLayout(new FileInputStream(file));
            */
            File file = new File(templatePath);
            
            byte[] byteTemplate = getFileAsByteArray(file);
            String stringTemplate = new String(byteTemplate);
            
            FormArtifactRenderer formArtifactRenderer = new FormArtifactRenderer(
                remoteArtifactDefinition, 
                remoteArtifact, webserviceBean, 
                remoteSession, 
                remoteProcessInstance);
            
            FormRenderer formRenderer = (FormRenderer) formArtifactRenderer.renderArtifact();
            
            List<AbstractElement> elements = formRenderer.getFormStructure().getElements();
            for (AbstractElement element : elements) {
                
                if (element instanceof AbstractElementField) {
                    AbstractElementField elementField = (AbstractElementField) element;
                    
                    if (elementField.getId() != null) {
                        String id = element.getId();
                        
                        String value = "";
                        
                        if (elementField.getValue() != null) {
                            if (elementField.getValue() instanceof RemoteObjectLight) {
                                
                                value = ((RemoteObjectLight) elementField.getValue()).getName();
                            }
                            else {
                                
                                value = elementField.getValue().toString();
                            }
                        }
                        stringTemplate = stringTemplate.replace("${" + id + "}", value);
                    }
                }
            }
            String processEnginePath = String.valueOf(PersistenceService.getInstance().getApplicationEntityManager().getConfiguration().get("processEnginePath"));
                        
            PrintWriter templateInstance = new PrintWriter(processEnginePath + "/temp/processengine.tmp");
            templateInstance.println(stringTemplate);
            templateInstance.close();
                                                                        
            printLayout = new PrintLayout(new FileInputStream(new File(processEnginePath + "/temp/processengine.tmp")));
        } catch (FileNotFoundException ex) {
            //TODO:
        } catch (IOException ex) {
            //TODO:
        }
        return printLayout;        
    }
    
    public static byte[] getFileAsByteArray(File file) {
        try {
            Scanner in = new Scanner(file);

            String line = "";

            while (in.hasNext())
                line += in.nextLine();

            byte [] structure = line.getBytes();

            in.close();

            return structure;

        } catch (FileNotFoundException ex) {

            return null;
        }
    }
                    
}
