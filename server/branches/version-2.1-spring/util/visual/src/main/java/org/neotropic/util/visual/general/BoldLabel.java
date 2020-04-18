/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.neotropic.util.visual.general;

import com.vaadin.flow.component.html.Label;

/**
 * Custom  bold text label 
 * @author Orlando Paz Duarte {@literal <orlando.paz@kuwaiba.org>}
 */
public class BoldLabel extends Label {

    public BoldLabel() {
    }
    
    public BoldLabel(String text) {
        super(text);
        getStyle().set("font-weight", "bold");
    }
    
    
}
