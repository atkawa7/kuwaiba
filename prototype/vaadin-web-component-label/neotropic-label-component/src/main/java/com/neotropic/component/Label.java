package com.neotropic.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JavaScript;

@JavaScript("frontend://src/js/main.js")
@Tag("neotropic-label")
public class Label extends Component {

    public Label(String text) {
        getElement().setAttribute("text", text);
    }
}
