/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.neotropic.util.visual.slider;

import com.neotropic.flow.component.mxgraph.MxGraph;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;

/**
 * Custom slider control for mxgraph zoom
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>} 
 */
public class SliderMxGraphZoom extends PaperSlider {

    public SliderMxGraphZoom(MxGraph mxGraph) {
        setMax(300);
        setMin(30);
        setStep(10);
        setValue(100);
        addValueChangeListener(listener-> {
            double value = new Double(listener.getValue()) / 100;
            mxGraph.setScale(value);
        });
        addListener(SliderValueChangingEvent.class, listener -> {
            double value = new Double(getImmediateValue()) / 100;
            mxGraph.setScale(value);
        });
    }
    
    
}
