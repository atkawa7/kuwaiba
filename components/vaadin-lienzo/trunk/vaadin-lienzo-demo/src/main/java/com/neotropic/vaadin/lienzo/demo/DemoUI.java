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

import com.neotropic.vaadin.lienzo.demo.model.SampleBusinessObject;
import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.TreeData;
import com.vaadin.server.SerializableFunction;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.ui.dnd.EffectAllowed;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.TreeGrid;
import com.vaadin.ui.UI;
import com.vaadin.ui.components.grid.TreeGridDragSource;

/**
 * 
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
@Theme("valo")
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
        hSplitPanel.setSplitPosition(25, Unit.PERCENTAGE);
        
        TreeGrid<SampleBusinessObject> tree = new TreeGrid();
        TreeData<SampleBusinessObject> treeData = new TreeData<>();
        
        SampleBusinessObject rootObject = new SampleBusinessObject(1, "node 1");
        treeData.addRootItems(rootObject);
        treeData.addItem(rootObject, new SampleBusinessObject(2, "node 1.1"));
        treeData.addItem(rootObject, new SampleBusinessObject(3, "node 1.2"));
        
        tree.setSizeFull();
        tree.setTreeData(treeData);
        
        tree.expand(rootObject);
        
        tree.addColumn(SampleBusinessObject::toString).setCaption("Name");
        
        TreeGridDragSource<SampleBusinessObject> dragSource = new TreeGridDragSource<>(tree);
        dragSource.setEffectAllowed(EffectAllowed.MOVE);
        dragSource.setDragDataGenerator(SampleBusinessObject.DATA_TYPE, new SerializableFunction<SampleBusinessObject, String>() {
            @Override
            public String apply(SampleBusinessObject t) { //Now we serialize the object to be transferred
                return t.serialize();
            }
        });
        
        LienzoDropWrapper lienzo = new LienzoDropWrapper();
                
        
        hSplitPanel.setFirstComponent(tree);
        hSplitPanel.setSecondComponent(lienzo);
        
        setContent(hSplitPanel);
    }
}
