/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.neotropic.vaadin.lienzo.demo;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.TreeDragMode;
import com.vaadin.ui.UI;

/**
 * 
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
@Theme("demo")
@Title("LienzoComponent Add-on Demo")
@SuppressWarnings("serial")
public class DemoUI extends UI
{

    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = DemoUI.class, widgetset = "com.neotropic.vaadin.lienzo.demo.DemoWidgetSet")
    public static class Servlet extends VaadinServlet {
    }
    
    @Override
    protected void init(VaadinRequest request) {
        HorizontalSplitPanel hSplitPanel = new HorizontalSplitPanel();
        
        Tree tree = new Tree();
        tree.addItem("Colombia");
        tree.addItem("Valle del Cauca");
        tree.addItem("Santiago de Cali");
        tree.addItem("Cauca");
        tree.addItem("Popayán");
        tree.addItem("Nariño");
        tree.addItem("Pasto");
        
        tree.setParent("Valle del Cauca", "Colombia");
        tree.setParent("Cauca", "Colombia");
        tree.setParent("Nariño", "Colombia");
        tree.setParent("Santiago de Cali", "Valle del Cauca");
        tree.setParent("Popayán", "Cauca");
        tree.setParent("Pasto", "Nariño");
        
        tree.expandItem("Colombia");
        tree.expandItem("Valle del Cauca");
        tree.expandItem("Cauca");
        tree.expandItem("Nariño");
        
        tree.setDragMode(TreeDragMode.NODE);
        
        LienzoDropWrapper lienzo = new LienzoDropWrapper();
                
        hSplitPanel.setFirstComponent(tree);
        hSplitPanel.setSecondComponent(lienzo);
        
        setContent(hSplitPanel);
    }
}
