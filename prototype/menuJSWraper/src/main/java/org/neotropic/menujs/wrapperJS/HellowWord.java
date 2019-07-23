package org.neotropic.menujs.wrapperJS;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;



@Route("hello")
public class HellowWord extends VerticalLayout {

    public HellowWord() {
        Label label = new Label("HOLA MUNDO");
        add(label);
    }
}
