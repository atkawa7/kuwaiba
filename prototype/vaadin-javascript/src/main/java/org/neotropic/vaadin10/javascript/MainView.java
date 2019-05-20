package org.neotropic.vaadin10.javascript;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

/**
 * The root view
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Route("")
@Theme(Lumo.class)
public class MainView extends VerticalLayout {

    public MainView() {
        //We will add a button and a link. They both do the same. It's just to demonstrate how to navigate between views.
        Button btnHello = new Button("Click me to see a nice Gantt Chart");
        setAlignItems(Alignment.CENTER);
        add(btnHello);
        btnHello.addClickListener((anEvent) -> {
            btnHello.getUI().ifPresent(ui -> ui.navigate("gantt"));
        });
        
        add(new RouterLink("You can also click here if you don't like buttons!", GanttView.class));
    }
}