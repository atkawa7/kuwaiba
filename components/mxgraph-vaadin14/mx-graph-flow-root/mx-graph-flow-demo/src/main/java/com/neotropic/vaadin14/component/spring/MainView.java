package com.neotropic.vaadin14.component.spring;

import com.neotropic.vaadin14.component.MxGraph;
import com.neotropic.vaadin14.component.MxGraphCell;
import com.neotropic.vaadin14.component.MxGraphClickEdgeEvent;
import com.neotropic.vaadin14.component.PaperSlider;
import com.neotropic.vaadin14.component.PaperSliderValueChangeEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;

@Route
@PWA(name = "Project Base for Vaadin Flow with Spring", shortName = "Project Base")
public class MainView extends VerticalLayout {

    public MainView(@Autowired MessageBean bean) {

        PaperSlider paperSlider = new PaperSlider();
        paperSlider.setValue(5);
        paperSlider.addValueChangeListener(new ComponentEventListener<PaperSliderValueChangeEvent>() {
            @Override
            public void onComponentEvent(PaperSliderValueChangeEvent t) {
                Notification.show("Paper slider value change; value = " + paperSlider.getValue());
            }
        });
        add(paperSlider);
        
        Button button = new Button("Get paper slider value",
                e -> Notification.show("value = " + paperSlider.getValue()));
        add(button);
        
        MxGraph myElement = new MxGraph();
        myElement.setProp1("from Vaadin");
        myElement.addClickEdgeListener(new ComponentEventListener<MxGraphClickEdgeEvent>() {
            @Override
            public void onComponentEvent(MxGraphClickEdgeEvent t) {
                Notification.show("mxgraph click edge");
            }
        });
        
        Button addButton = new Button("Add Cell"); // (3)

        addButton.addClickListener(click -> {
     // (1)
     MxGraphCell mxGraphCell = new MxGraphCell();
     
     myElement.addCell(mxGraphCell);
     }
  );
        add(myElement);
        add(addButton);
    }

}
