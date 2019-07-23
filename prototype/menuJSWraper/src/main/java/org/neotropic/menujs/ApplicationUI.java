package org.neotropic.menujs;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import org.neotropic.menujs.wrapperJS.MenuItem;
import org.neotropic.menujs.wrapperJS.MenuView;
import org.neotropic.menujs.wrapperJS.SmartMenuWrapper;

import java.util.ArrayList;
import java.util.List;

@Route("")

public class ApplicationUI extends VerticalLayout {

    protected ApplicationUI() {
        //We will add a button and a link. They both do the same. It's just to demonstrate how to navigate between views.
        Button btnHello = new Button("Go to menu view");
        setAlignItems(Alignment.CENTER);
        add(btnHello);
        btnHello.addClickListener((anEvent) -> {
            btnHello.getUI().ifPresent(ui -> ui.navigate("Menu"));
        });


    }



}