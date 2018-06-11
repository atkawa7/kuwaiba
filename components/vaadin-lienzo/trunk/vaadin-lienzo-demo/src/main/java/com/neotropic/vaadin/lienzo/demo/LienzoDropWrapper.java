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
import com.neotropic.vaadin.lienzo.client.core.shape.SrvEdgeWidget;

import com.neotropic.vaadin.lienzo.client.events.LienzoMouseOverListener;
import com.neotropic.vaadin.lienzo.client.events.NodeWidgetClickListener;
import com.neotropic.vaadin.lienzo.client.events.NodeWidgetDblClickListener;
import com.neotropic.vaadin.lienzo.client.events.NodeWidgetRightClickListener;
import com.neotropic.vaadin.lienzo.client.core.shape.SrvNodeWidget;
import com.neotropic.vaadin.lienzo.client.core.shape.SrvFrameWidget;
import com.neotropic.vaadin.lienzo.client.events.EdgeWidgetAddListener;
import com.neotropic.vaadin.lienzo.client.events.EdgeWidgetClickListener;
import com.neotropic.vaadin.lienzo.client.events.EdgeWidgetDblClickListener;
import com.neotropic.vaadin.lienzo.client.events.EdgeWidgetRightClickListener;
import com.neotropic.vaadin.lienzo.client.events.EdgeWidgetUpdateListener;
import com.neotropic.vaadin.lienzo.client.events.FrameWidgetClickListener;
import com.neotropic.vaadin.lienzo.client.events.FrameWidgetDblClickListener;
import com.neotropic.vaadin.lienzo.client.events.FrameWidgetRightClickListener;
import com.neotropic.vaadin.lienzo.client.events.FrameWidgetUpdateListener;
import com.neotropic.vaadin.lienzo.client.events.NodeWidgetUpdateListener;
import com.neotropic.vaadin.lienzo.demo.model.SampleBusinessObject;
import com.vaadin.shared.ui.dnd.DropEffect;
import com.vaadin.ui.Panel;
import com.vaadin.ui.dnd.DropTargetExtension;
import com.vaadin.ui.dnd.event.DropEvent;
import com.vaadin.ui.dnd.event.DropListener;
import java.util.Optional;

/**
 * A wrapper UI component used to contain the Lienzo component. It basically manages the events.
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public final class LienzoDropWrapper extends Panel {
    private final LienzoComponent lienzoComponent;
    private SrvNodeWidget srvNodeWidget = null;
      
    public LienzoDropWrapper() {
        
        lienzoComponent = new LienzoComponent();        
        DropTargetExtension<LienzoComponent> dropTarget = new DropTargetExtension<>(lienzoComponent);
        dropTarget.setDropEffect(DropEffect.MOVE);
        
        dropTarget.addDropListener(new DropListener<LienzoComponent>() {
            @Override
            public void drop(DropEvent<LienzoComponent> event) {
                Optional<String> dataTransferData = event.getDataTransferData(SampleBusinessObject.DATA_TYPE); //Only get this type of data. Note that the type of the data to be trasferred is set in the drag source
                
                if (dataTransferData != null) {
                    SampleBusinessObject droppedObject = SampleBusinessObject.deserialize(dataTransferData.get());
                    String url = "http://localhost:8080/vaadin-lienzo-demo/VAADIN/themes/demo/images/node.png";
                    srvNodeWidget = new SrvNodeWidget();
                    srvNodeWidget.setUrlIcon(url);
                    srvNodeWidget.setCaption(droppedObject.toString());
                }
            }
        });
        
        String url = "http://localhost:8080/vaadin-lienzo-demo/VAADIN/themes/demo/images/background.png";        
        lienzoComponent.addBackground(url, 0, 0);
                
        SrvFrameWidget frame = new SrvFrameWidget("Frame", 400, 100, 300, 300);
        lienzoComponent.addFrameWidget(frame);
        
        String cloudUrlIcon = "http://localhost:8080/vaadin-lienzo-demo/VAADIN/themes/demo/images/cloud-big.png";
        SrvNodeWidget cloudNode = new SrvNodeWidget();
        cloudNode.setCaption("Cloud");
        cloudNode.setUrlIcon(cloudUrlIcon);
        cloudNode.setX(100);
        cloudNode.setY(100);
        cloudNode.setWidth(60);
        cloudNode.setHeight(34);
        lienzoComponent.addNodeWidget(cloudNode);
        
        lienzoComponent.addLienzoMouseOverListener(lienzoMouseOverListener);
        
        lienzoComponent.addNodeWidgetClickListener(nodeWidgetClickListener);
        lienzoComponent.addNodeWidgetDblClickListener(nodeWidgetDblClickListener);
        lienzoComponent.addNodeWidgetRightClickListener(nodeWidgetRightClickListener);
        lienzoComponent.addNodeWidgetUpdateListener(nodeWidgetUpdateListener);
        
        lienzoComponent.addFrameWidgetClickListener(frameWidgetClickListener);
        lienzoComponent.addFrameWidgetDblClickListener(frameWidgetDblClickListener);
        lienzoComponent.addFrameWidgetRightClickListener(frameWidgetRightClickListener);
        lienzoComponent.addFrameWidgetUpdateListener(frameWidgetUpdateListener);
        
        lienzoComponent.addEdgeWidgetAddListener(edgeWidgetAddListener);
        lienzoComponent.addEdgeWidgetClickListener(edgeWidgetClickListener);
        lienzoComponent.addEdgeWidgetDblClickListener(edgeWidgetDblClickListener);
        lienzoComponent.addEdgeWidgetRightClickListener(edgeWidgetRigthClickListener);
        lienzoComponent.addEdgeWidgetUpdateListener(edgeWidgetUpdateListener);
        
        lienzoComponent.setEnableConnectionTool(true);
        
        setSizeFull();
        setContent(lienzoComponent);
    }
    
    public LienzoComponent getLienzoComponent() {
        return lienzoComponent;
    }
    
    LienzoMouseOverListener lienzoMouseOverListener = new LienzoMouseOverListener() {

        @Override
        public void lienzoMouseOver(int x, int y) {
            if (srvNodeWidget != null) {
                srvNodeWidget.setX(x);
                srvNodeWidget.setY(y);
                srvNodeWidget.setWidth(32);
                srvNodeWidget.setHeight(32);
                lienzoComponent.addNodeWidget(srvNodeWidget);
                srvNodeWidget = null;
            }
        }
    };
    
    NodeWidgetClickListener nodeWidgetClickListener = new NodeWidgetClickListener() {

        @Override
        public void nodeWidgetClicked(long id) {
            SrvNodeWidget srvNode = lienzoComponent.getNodeWidget(id);
            srvNode.setCaption("id = " + id + " Clicked");
            lienzoComponent.updateNodeWidget(id);
        }
    };
    
    NodeWidgetRightClickListener nodeWidgetRightClickListener = new NodeWidgetRightClickListener() {

        @Override
        public void nodeWidgetRightClicked(long id) {
            /*
            SrvNodeWidget srvNode = lienzoComponent.getNodeWidget(id);
            srvNode.setCaption("id = " + id + " Right Clicked");
            lienzoComponent.updateNodeWidget(id);
            */
            lienzoComponent.removeNodeWidget(id);
        }
    };
    
    NodeWidgetDblClickListener nodeWidgetDblClickListener = new NodeWidgetDblClickListener() {

        @Override
        public void nodeWidgetDoubleClicked(long id) {
            SrvNodeWidget srvNode = lienzoComponent.getNodeWidget(id);
            srvNode.setCaption("id = " + id + " Double Clicked");
            lienzoComponent.updateNodeWidget(id);
        }
    };
    
    NodeWidgetUpdateListener nodeWidgetUpdateListener = new NodeWidgetUpdateListener() {

        @Override
        public void nodeWidgetUpdated(SrvNodeWidget clntNode) {
            clntNode.setCaption("id = " +  clntNode.getId() + " Updated");
            lienzoComponent.updateNodeWidget(clntNode.getId());
        }
    };
    
    private FrameWidgetClickListener frameWidgetClickListener = new FrameWidgetClickListener() {

        @Override
        public void frameWidgetClicked(long id) {
            SrvFrameWidget srvFrame = lienzoComponent.getFrameWidget(id);
            srvFrame.setCaption("id = " + id + " Click");
            lienzoComponent.updateFrameWidget(srvFrame);
        }
    };
    
    private FrameWidgetDblClickListener frameWidgetDblClickListener = new FrameWidgetDblClickListener() {

        @Override
        public void frameWidgetDblClicked(long id) {
            SrvFrameWidget srvFrame = lienzoComponent.getFrameWidget(id);
            srvFrame.setCaption("id = " + id + " Double click");
            lienzoComponent.updateFrameWidget(srvFrame);
        }
    };
    
    private FrameWidgetRightClickListener frameWidgetRightClickListener = new FrameWidgetRightClickListener() {

        @Override
        public void frameWidgetRightClicked(long id) {
            /*
            SrvFrameWidget srvFrame = lienzoComponent.getFrameWidget(id);
            srvFrame.setCaption("id = " + id + " Right click");
            lienzoComponent.updateFrameWidget(srvFrame);
            */
            lienzoComponent.removeFrameWidget(id);
        }
    };
    
    private FrameWidgetUpdateListener frameWidgetUpdateListener = new FrameWidgetUpdateListener() {

        @Override
        public void frameWidgetUpdated(SrvFrameWidget clntFrameWidget) {
            clntFrameWidget.setCaption("id = " + clntFrameWidget.getId() + " Updated");
            lienzoComponent.updateFrameWidget(clntFrameWidget);
        }
    };
    
    private EdgeWidgetAddListener edgeWidgetAddListener = new EdgeWidgetAddListener() {

        @Override
        public void edgeWidetAdded(SrvEdgeWidget clntNewEdge) {
            if (clntNewEdge.getId() == 0)
                clntNewEdge.setColor("RED");
            if (clntNewEdge.getId() == 2)
                clntNewEdge.setColor("GREEN");
            if (clntNewEdge.getId() == 4)
                clntNewEdge.setColor("BLUE");
                
            clntNewEdge.setCaption("Edge id = " + clntNewEdge.getId());
            lienzoComponent.addEdgeWidget(clntNewEdge);                            
        }
    };
    
    private EdgeWidgetClickListener edgeWidgetClickListener = new EdgeWidgetClickListener() {

        @Override
        public void edgeWidgetClicked(long id) {
            SrvEdgeWidget srvEdge = lienzoComponent.getEdge(id);
            srvEdge.setCaption("id = " + srvEdge.getId() + " Clicked");
            lienzoComponent.updateEdgeWidget(srvEdge.getId());
        }
    };
    
    private EdgeWidgetDblClickListener edgeWidgetDblClickListener = new EdgeWidgetDblClickListener() {

        @Override
        public void edgeWidgetDblClicked(long id) {
            SrvEdgeWidget srvEdge = lienzoComponent.getEdge(id);
            srvEdge.setCaption("id = " + srvEdge.getId() + " Double Clicked");
            lienzoComponent.updateEdgeWidget(srvEdge.getId());
        }
    };
    
    private EdgeWidgetRightClickListener edgeWidgetRigthClickListener = new EdgeWidgetRightClickListener() {

        @Override
        public void edgeWidgetRightClicked(long id) {
            /*
            SrvEdgeWidget srvEdge = lienzoComponent.getEdge(id);
            srvEdge.setCaption("id = " + srvEdge.getId() + " Right Clicked");
            lienzoComponent.updateEdgeWidget(srvEdge.getId());
            */
            lienzoComponent.removeEdgeWidget(id);
        }
    };
    
    private EdgeWidgetUpdateListener edgeWidgetUpdateListener = new EdgeWidgetUpdateListener() {

        @Override
        public void edgeWidgetUpdated(SrvEdgeWidget clntEdge) {
            SrvEdgeWidget srvEdge = lienzoComponent.getEdge(clntEdge.getId());
            srvEdge.setCaption("id = " + srvEdge.getId() + " Updated");
            lienzoComponent.updateEdgeWidget(srvEdge.getId());
        }
    };
}
