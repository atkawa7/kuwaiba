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

import com.neotropic.vaadin.lienzo.LienzoComponent;
import com.neotropic.vaadin.lienzo.client.core.shape.LienzoNode;
import com.neotropic.vaadin.lienzo.client.events.LienzoMouseOverListener;
import com.neotropic.vaadin.lienzo.client.events.LienzoNodeClickListener;
import com.neotropic.vaadin.lienzo.client.events.LienzoNodeDblClickListener;
import com.neotropic.vaadin.lienzo.client.events.LienzoNodeRightClickListener;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.Notification;

/**
 * 
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class LienzoDropWrapper extends DragAndDropWrapper {
    private final LienzoComponent lienzoComponent;
    private LienzoNode lienzoNode = null;
    
    private final DropHandler dropHandler = new DropHandler() {

        @Override
        public void drop(DragAndDropEvent event) {
            String url = "http://localhost:8080/vaadin-lienzo-demo/VAADIN/themes/demo/images/node.png";
            lienzoNode = new LienzoNode();
            lienzoNode.setUrlIcon(url);
            lienzoNode.setCaption("Node Id = " + lienzoNode.getId());
        }

        @Override
        public AcceptCriterion getAcceptCriterion() {
            return AcceptAll.get();
        }
    };
    
    public LienzoDropWrapper() {
        lienzoComponent = new LienzoComponent();
        setCompositionRoot(lienzoComponent);
        setDropHandler(dropHandler);
        lienzoComponent.addLienzoMouseOverListener(lienzoMouseOverListener);
        lienzoComponent.addLienzoNodeClickListener(lienzoNodeClickListener);
        lienzoComponent.addLienzoNodeDblClickListener(lienzoNodeDblClickListener);
        lienzoComponent.addLienzoNodeRightClickListener(lienzoNodeRightClickListener);
    }
    
    LienzoMouseOverListener lienzoMouseOverListener = new LienzoMouseOverListener() {

        @Override
        public void lienzoMouseOver(int x, int y) {
            if (lienzoNode != null) {
                lienzoNode.setX(x);
                lienzoNode.setY(y);
                lienzoNode.setWidth(32);
                lienzoNode.setHeight(32);
                lienzoComponent.addLienzoNode(lienzoNode);
                lienzoNode = null;
            }
        }
    };
    
    LienzoNodeClickListener lienzoNodeClickListener = new LienzoNodeClickListener() {

        @Override
        public void lienzoNodeClicked(long id) {
            //Notification.show("Node clicked", Notification.Type.ERROR_MESSAGE);
        }
    };
    
    LienzoNodeRightClickListener lienzoNodeRightClickListener = new LienzoNodeRightClickListener() {

        @Override
        public void lienzoNodeRightClicked(long id) {
            Notification.show("Node Right Clicked", Notification.Type.ERROR_MESSAGE);
        }
    };
    
    LienzoNodeDblClickListener lienzoNodeDblClickListener = new LienzoNodeDblClickListener() {

        @Override
        public void lienzoNodeDoubleClicked(long id) {
            Notification.show("Node Double Clicked", Notification.Type.ERROR_MESSAGE);
        }
    };
}
