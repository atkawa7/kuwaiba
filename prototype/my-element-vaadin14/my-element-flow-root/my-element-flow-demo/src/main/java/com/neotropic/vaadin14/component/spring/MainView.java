package com.neotropic.vaadin14.component.spring;

import com.neotropic.vaadin14.component.DhtmlxGantt;
import com.neotropic.vaadin14.component.MyElement;
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
        
        MyElement myElement = new MyElement();
        myElement.setProp1("from Vaadin");
        add(myElement);
        
        DhtmlxGantt dhtmlxGantt = new DhtmlxGantt();
        add(dhtmlxGantt);
    }

}
