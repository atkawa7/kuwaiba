/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kuwaiba.apis.forms.components.impl;

import com.vaadin.annotations.StyleSheet;
import com.vaadin.ui.CustomLayout;
import java.io.IOException;
import java.io.InputStream;

/**
 * Custom Layout that contains the printable template
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
@StyleSheet({"vaadin://css/printablevaadintemplate.css"})
public class PrintLayout extends CustomLayout {
    
    public PrintLayout(InputStream inputStream) throws IOException {
        
        super(inputStream);        
    }
}
