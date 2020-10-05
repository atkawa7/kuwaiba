package com.neotropic.vaadin14.component.spring;

import com.neotropic.flow.component.aceeditor.AceEditor;
import com.neotropic.flow.component.aceeditor.AceMode;
import com.neotropic.flow.component.aceeditor.AceTheme;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

/**
 * Main for demo view
 *  @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 */
@Route(value = "")
public class AceView extends VerticalLayout {

    boolean readonly = false;
    String currentValue = "";
    public AceView() {
        
     AceEditor aceEditor = new AceEditor();
     aceEditor.setMode(AceMode.groovy);
     VerticalLayout lytEditor = new VerticalLayout(aceEditor);
     lytEditor.setHeight("500px");
     add(lytEditor);    
     
     aceEditor.addAceEditorValueChangedListener(clickListener -> { 
        currentValue = aceEditor.getValue();
         System.err.println("New Value: " + currentValue);
     });
     add( new HorizontalLayout(new Button("Set mode Java", evt -> {
        aceEditor.setMode(AceMode.java);
     }),
     new Button("Set mode Groovy", evt -> {
        aceEditor.setMode(AceMode.groovy);
     }),
     new Button("Set Theme TextMate", evt -> {
        aceEditor.setTheme(AceTheme.textmate);
     }),
     new Button("Set Theme Eclipse", evt -> {
        aceEditor.setTheme(AceTheme.eclipse);
     }),
     new Button("Set Theme chaos", evt -> {
        aceEditor.setTheme(AceTheme.chaos);
     })));
     
     add( new HorizontalLayout(new Button("Toogle Read Only", evt -> {
        toogleReadOnly();
        aceEditor.setReadonly(readonly);
     }), new Button("Show Value on Server", evt -> {
        new Notification("NEW VALUE => " + aceEditor.getValue(), 3000).open();
     })));
    }
    
    public void toogleReadOnly() {
        readonly = ! readonly;
    }

}
