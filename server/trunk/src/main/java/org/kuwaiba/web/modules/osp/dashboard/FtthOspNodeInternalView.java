/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.kuwaiba.web.modules.osp.dashboard;

import com.vaadin.ui.Accordion;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.renderers.TextRenderer;
import com.vaadin.ui.themes.ValoTheme;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.openide.util.Exceptions;

/**
 * A internal FTTH osp node view
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class FtthOspNodeInternalView {
    private final WebserviceBean webserviceBean;
    private final RemoteSession remoteSession;
    private final RemoteObjectLight object;
    
    public static class ClassName {
        private static final String FIBER_SPLITTER = "FiberSplitter"; //NOI18N
        private static final String MANHOLE = "Manhole"; //NOI18N
        private static final String SPLICE_BOX = "SpliceBox"; //NOI18N
        private static final String OPTICAL_PORT = "OpticalPort"; //NOI18N //Is not internal view candidate
        private static final String OPTICAL_LINK = "OpticalLink"; //NOI18N //Is not internal view candidate
        private static final String WIRE_CONTAINER = "WireContainer"; //NOI18N
        private static final String JUMPER = "Jumper"; //NOI18N
        private static final String BUILDING = "Building"; //NOI18N
        private static final String CTO = "CTO"; //NOI18N
        private static final String ODF = "ODF"; //NOI18N
        private static final String OPTICAL_LINE_TERMINAL = "OpticalLineTerminal"; //NOI18N
        private static final String OLT_BOARD = "OLTBoard"; //NOI18N
                
        public static boolean containClassName(String className) {
            return getClassNames().contains(className);
        }
        
        public static List<String> getClassNames() {
            return Arrays.asList(FIBER_SPLITTER, MANHOLE, SPLICE_BOX, BUILDING, CTO, ODF, OPTICAL_LINE_TERMINAL, OLT_BOARD);
        }
    }
    public FtthOspNodeInternalView(WebserviceBean webserviceBean, RemoteSession remoteSession, RemoteObjectLight object) {
        this.webserviceBean = webserviceBean;
        this.remoteSession = remoteSession;
        this.object = object;

    }
    
    public boolean canBuildInternalView() {
        return ClassName.containClassName(object.getClassName());
    }
    
    private RemoteObjectLight getFiber(RemoteObjectLight opticalPort, boolean inputFiber) throws ServerSideException {
        String attrNameEndpoint = "endpointA"; //NOI18N
        if (inputFiber)
            attrNameEndpoint = "endpointB"; //NOI18N
        

        List<RemoteObjectLight> endpoints = webserviceBean.getSpecialAttribute(
            opticalPort.getClassName(), opticalPort.getId(), attrNameEndpoint, 
            remoteSession.getIpAddress(), remoteSession.getSessionId());

        for (RemoteObjectLight endpoint : endpoints) {
            if (ClassName.OPTICAL_LINK.equals(endpoint.getClassName()))
                return endpoint;
        }
        return null;
    }
    
    private List<RemoteObjectLight> getFibers(RemoteObjectLight device, boolean inputFiber) throws ServerSideException {
        String attrNameEndpoint = "endpointA"; //NOI18N
        if (inputFiber)
            attrNameEndpoint = "endpointB"; //NOI18N
        
        List<RemoteObjectLight> opticalPorts = webserviceBean.getChildrenOfClassLight(
            device.getId(), device.getClassName(), ClassName.OPTICAL_PORT, -1, 
            remoteSession.getIpAddress(), remoteSession.getSessionId());
        
        List<RemoteObjectLight> fibers = new ArrayList();
        for (RemoteObjectLight opticalPort : opticalPorts) {
            RemoteObjectLight fiber = getFiber(opticalPort, inputFiber);
            if (fiber != null)
                fibers.add(fiber);
        }
        return fibers;
    }
    
    private class Row {
        private RemoteObjectLight cableIn;
        private RemoteObjectLight fiberIn;
        private RemoteObjectLight cableOut;
        private RemoteObjectLight fiberOut;
        
        public Row() {
        }
        
        public Row(RemoteObjectLight cableIn, RemoteObjectLight fiberIn, RemoteObjectLight cableOut, RemoteObjectLight fiberOut) {
            this.cableIn = cableIn;
            this.fiberIn = fiberIn;
            this.cableOut = cableOut;
            this.fiberOut = fiberOut;
        }
        
        public RemoteObjectLight getCableIn() {
            return cableIn;
        }
        
        public void setCableIn(RemoteObjectLight cableIn) {
            this.cableIn = cableIn;
        }
        
        public RemoteObjectLight getFiberIn() {
            return fiberIn;
        }
        
        public void setFiberIn(RemoteObjectLight fiberIn) {
            this.fiberIn = fiberIn;
        }
        
        public RemoteObjectLight getCableOut() {
            return cableOut;
        }
        
        public void setCableOut(RemoteObjectLight cableOut) {
            this.cableOut = cableOut;
        }
        
        public RemoteObjectLight getFiberOut() {
            return fiberOut;
        }
        
        public void setFiberOut(RemoteObjectLight fiberOut) {
            this.fiberOut = fiberOut;
        }
    }
    
    private boolean hasFiber(List<Row> rows, RemoteObjectLight fiber) {
        if (fiber == null)
            return false;
        for (Row row : rows) {
            if (row.getFiberIn() != null && row.getFiberIn().getId().equals(fiber.getId()))
                return true;
            if (row.getFiberOut() != null && row.getFiberOut().getId().equals(fiber.getId()))
                return true;
        }
        return false;
    }
    
    private List<Row> getSplices(RemoteObjectLight rol) throws ServerSideException {
        final String ATTR_NAME_MIRROR = "mirror";
        List<RemoteObjectLight> ports = webserviceBean.getChildrenOfClassLight(rol.getId(), rol.getClassName(), 
            ClassName.OPTICAL_PORT, -1, remoteSession.getIpAddress(), remoteSession.getSessionId());
        List<Row> rows = new ArrayList();
        for (RemoteObjectLight port : ports) {
            Row row = new Row();
            
            RemoteObjectLight fiberIn = getFiber(port, true);
            RemoteObjectLight fiberOut = getFiber(port, false);
            if (fiberIn != null && !hasFiber(rows, fiberIn)) {
                RemoteObjectLight cableIn = getCable(fiberIn);
                
                row.setCableIn(cableIn);
                row.setFiberIn(fiberIn);
            }
            if (fiberOut != null && !hasFiber(rows, fiberOut)) {
                RemoteObjectLight cableOut = getCable(fiberOut);
                
                row.setCableOut(cableOut);
                row.setFiberOut(fiberOut);
            }
            
            List<RemoteObjectLight> mirrors = webserviceBean.getSpecialAttribute(port.getClassName(), port.getId(), ATTR_NAME_MIRROR, remoteSession.getIpAddress(), remoteSession.getSessionId());
            if (mirrors.size() == 1) {
                RemoteObjectLight mirrorPort = mirrors.get(0);
                
                RemoteObjectLight mirrorFiberIn = getFiber(mirrorPort, true);
                RemoteObjectLight mirrorFiberOut = getFiber(mirrorPort, false);
                if (mirrorFiberIn != null && !hasFiber(rows, mirrorFiberIn)) {
                    RemoteObjectLight cableIn = getCable(mirrorFiberIn);
                    
                    row.setCableIn(cableIn);
                    row.setFiberIn(mirrorFiberIn);
                }
                if (mirrorFiberOut != null && !hasFiber(rows, mirrorFiberOut)) {
                    RemoteObjectLight cableOut = getCable(mirrorFiberOut);
                    
                    row.setCableOut(cableOut);
                    row.setFiberOut(mirrorFiberOut);
                }
            }
            if (row.getCableIn() == null && 
                row.getFiberIn() == null &&
                row.getFiberOut() == null &&
                row.getCableOut() == null)
                continue;
            rows.add(row);
        }
        return rows;
    }
    
    private RemoteObjectLight getCable(RemoteObjectLight fiber) throws ServerSideException {
        List<RemoteObjectLight> parents = webserviceBean.getParents(
            fiber.getClassName(), fiber.getId(),
            remoteSession.getIpAddress(), remoteSession.getSessionId());
        for (RemoteObjectLight parent : parents) {
            if (ClassName.WIRE_CONTAINER.equals(parent.getClassName()) || ClassName.JUMPER.equals(parent.getClassName()))
                return parent;
        }
        return null;
    }
    
    private void buildInfoView(VerticalLayout lytParent, HashMap<String, RemoteObjectLight> cablesIn, HashMap<String, RemoteObjectLight> cablesOut) throws ServerSideException {
        VerticalLayout lyt = new VerticalLayout();
        if (ClassName.MANHOLE.equals(object.getClassName())) {
            Label lblCablesIn = new Label("Cables In");
            lblCablesIn.addStyleName(ValoTheme.LABEL_BOLD);
            lyt.addComponent(lblCablesIn);            
            for (String key : cablesIn.keySet())
                lyt.addComponent(new Label(cablesIn.get(key).getName()));
            
            Label lblCablesOut = new Label("Cables Out");
            lblCablesOut.addStyleName(ValoTheme.LABEL_BOLD);
            lyt.addComponent(lblCablesOut);            
            for (String key : cablesOut.keySet())
                lyt.addComponent(new Label(cablesOut.get(key).getName()));                        
        }
        if (ClassName.SPLICE_BOX.equals(object.getClassName()) || 
            ClassName.CTO.equals(object.getClassName())) {
            /*ClassName.ODF.equals(object.getClassName())
            ClassName.OLT_BOARD.equals(object.getClassName())) {*/
            
            List<Row> rows = getSplices(object);
            HashMap<String, RemoteObjectLight> spliceBoxCablesIn = new HashMap();
            HashMap<String, RemoteObjectLight> spliceBoxCablesOut = new HashMap();
            for (Row row : rows) {
                if (row.getCableIn() != null) {
                    spliceBoxCablesIn.put(row.getCableIn().getId(), row.getCableIn());
                    cablesIn.put(row.getCableIn().getId(), row.getCableIn());
                }
                if (row.getCableOut() != null) {
                    spliceBoxCablesOut.put(row.getCableOut().getId(), row.getCableOut());
                    cablesOut.put(row.getCableOut().getId(), row.getCableOut());
                }
            }
            Label lblCablesIn = new Label("Cables In");
            lblCablesIn.addStyleName(ValoTheme.LABEL_BOLD);
            lyt.addComponent(lblCablesIn);            
            for (String key : spliceBoxCablesIn.keySet())
                lyt.addComponent(new Label(spliceBoxCablesIn.get(key).getName()));
            
            Label lblCablesOut = new Label("Cables Out");
            lblCablesOut.addStyleName(ValoTheme.LABEL_BOLD);
            lyt.addComponent(lblCablesOut);            
            for (String key : spliceBoxCablesOut.keySet())
                lyt.addComponent(new Label(spliceBoxCablesOut.get(key).getName()));
            Label lblSplices = new Label("Splices");
            lblSplices.setStyleName(ValoTheme.LABEL_BOLD);
            lyt.addComponent(lblSplices);    
            Grid<Row> grd = new Grid();
            grd.addStyleName(ValoTheme.TABLE_BORDERLESS);
            grd.setWidth("100%");
            grd.setItems(rows);
            grd.addColumn(Row::getCableIn).setCaption("Cable In").setRenderer(row -> 
                {if (row != null) return row.getName(); else return null;}, new TextRenderer());
            grd.addColumn(Row::getFiberIn).setCaption("Fiber In").setRenderer(row -> 
                {if (row != null) return row.getName(); else return null;}, new TextRenderer());
            grd.addColumn(Row::getCableOut).setCaption("Cable Out").setRenderer(row -> 
                {if (row != null) return row.getName(); else return null;}, new TextRenderer());
            grd.addColumn(Row::getFiberOut).setCaption("Fiber Out").setRenderer(row -> 
                {if (row != null) return row.getName(); else return null;}, new TextRenderer());
            grd.addItemClickListener(event -> {
                Row row = event.getItem();
                if (row.getFiberOut() != null) {
                    try {
                        List<RemoteObjectLight> ports = webserviceBean.getSpecialAttribute(
                            row.getFiberOut().getClassName(), row.getFiberOut().getId(), "endpointA", //NOI18N
                            remoteSession.getIpAddress(), remoteSession.getSessionId());
                        if (ports.size() == 1 && ClassName.OPTICAL_PORT.equals(ports.get(0).getClassName())) {
                            Window wnd = new Window();
                            wnd.setWidth("30%");
                            wnd.setHeight("50%");
                            wnd.setContent(new FtthPhysicalPath(webserviceBean, remoteSession, row.getFiberOut(), ports.get(0)));
                            wnd.center();
                            wnd.setModal(true);
                            wnd.setResizable(true);
                            UI.getCurrent().addWindow(wnd);
                        }
                    } catch (ServerSideException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
            lyt.addComponent(grd);
        }
        if (ClassName.FIBER_SPLITTER.equals(object.getClassName())) {
            List<Row> rows = new ArrayList();
            List<RemoteObjectLight> fibersIn = getFibers(object, true);            
            for (RemoteObjectLight fiberIn : fibersIn) {
                
                HorizontalLayout lyt0 = new HorizontalLayout();
                HorizontalLayout lyt1 = new HorizontalLayout();
                
                Label lblCableIn = new Label("Cable In");
                lblCableIn.addStyleName(ValoTheme.LABEL_BOLD);
                lyt0.addComponent(lblCableIn);
                RemoteObjectLight cable = getCable(fiberIn);
                if (cable != null) {                    
                    lyt0.addComponent(new Label(cable.getName()));
                    cablesIn.put(cable.getId(), cable);
                }
                lyt.addComponent(lyt0);
                
                Label lblFiberIn = new Label("Fiber In");
                lblFiberIn.addStyleName(ValoTheme.LABEL_BOLD);
                lyt1.addComponent(lblFiberIn);
                lyt1.addComponent(new Label(fiberIn.getName()));
                
                lyt.addComponent(lyt1);
            }
            
            List<RemoteObjectLight> fibersOut = getFibers(object, false);
            for (RemoteObjectLight fiber : fibersOut) {
                Row row = new Row();
                row.setFiberOut(fiber);
                RemoteObjectLight cable = getCable(fiber);
                if (cable != null) {
                    row.setCableOut(cable);
                    cablesOut.put(cable.getId(), cable);
                }
                rows.add(row);
            }
            Grid<Row> grd = new Grid();
            grd.addStyleName(ValoTheme.TABLE_BORDERLESS);
            grd.setItems(rows);
            grd.addColumn(Row::getCableOut).setCaption("Cable Out").setRenderer(row -> row.getName(), new TextRenderer());
            grd.addColumn(Row::getFiberOut).setCaption("Fiber Out").setRenderer(row -> row.getName(), new TextRenderer());
            grd.addItemClickListener(event -> {
                Row row = event.getItem();
                if (row.getFiberOut() != null) {
                    try {
                        List<RemoteObjectLight> ports = webserviceBean.getSpecialAttribute(
                            row.getFiberOut().getClassName(), row.getFiberOut().getId(), "endpointA", //NOI18N
                            remoteSession.getIpAddress(), remoteSession.getSessionId());
                        if (ports.size() == 1 && ClassName.OPTICAL_PORT.equals(ports.get(0).getClassName())) {
                            Window wnd = new Window();
                            wnd.setWidth("30%");
                            wnd.setHeight("50%");
                            wnd.setContent(new FtthPhysicalPath(webserviceBean, remoteSession, row.getFiberOut(), ports.get(0)));
                            wnd.center();
                            wnd.setModal(true);
                            wnd.setResizable(true);
                            UI.getCurrent().addWindow(wnd);
                        }
                    } catch (ServerSideException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
            lyt.addComponent(grd);
        }
        lytParent.addComponentAsFirst(lyt);
    }
    
    
    public Component buildInternalView() {
        try {
            if (ClassName.containClassName(object.getClassName())) {
                HashMap<String, RemoteObjectLight> cablesIn = new HashMap();
                HashMap<String, RemoteObjectLight> cablesOut = new HashMap();
                
                List<RemoteObjectLight> children = webserviceBean.getObjectChildren(object.getClassName(), object.getId(), -1, remoteSession.getIpAddress(), remoteSession.getSessionId());
                HashMap<RemoteObjectLight, Component> nestedViews = new HashMap();
                for (RemoteObjectLight child : children) {
                    FtthOspNodeInternalView internalView = new FtthOspNodeInternalView(webserviceBean, remoteSession, child);
                    Component theInternalView = internalView.buildInternalViewRecursive(cablesIn, cablesOut);
                    if (theInternalView != null)
                        nestedViews.put(child, theInternalView);
                }
                VerticalLayout verticalLayout = new VerticalLayout();
                if(!nestedViews.isEmpty()) {
                    Accordion accordion = new Accordion();
                    for (RemoteObjectLight key : nestedViews.keySet()) {
                        accordion.addTab(nestedViews.get(key), String.format("%s [%s]", key.getName(), key.getClassName()));
                    }
                    verticalLayout.addComponent(accordion);
                }
                buildInfoView(verticalLayout, cablesIn, cablesOut);
                Label lblRoot = new Label(String.format("%s [%s]", object.getName(), object.getClassName()));
                lblRoot.addStyleName(ValoTheme.LABEL_LARGE);
                verticalLayout.addComponentAsFirst(lblRoot);
                return verticalLayout;
            }
            return null;
        } catch (ServerSideException ex) {
            Notifications.showError(ex.getMessage());
            return null;
        }
    }
    
    private Component buildInternalViewRecursive(HashMap<String, RemoteObjectLight> cablesIn, HashMap<String, RemoteObjectLight> cablesOut) {
        try {
            if (ClassName.containClassName(object.getClassName())) {
                VerticalLayout verticalLayout = new VerticalLayout();
                buildInfoView(verticalLayout, cablesIn, cablesOut);
                
                List<RemoteObjectLight> children = webserviceBean.getObjectChildren(object.getClassName(), object.getId(), -1, remoteSession.getIpAddress(), remoteSession.getSessionId());
                HashMap<RemoteObjectLight, Component> nestedViews = new HashMap();
                for (RemoteObjectLight child : children) {
                    FtthOspNodeInternalView internalView = new FtthOspNodeInternalView(webserviceBean, remoteSession, child);
                    Component theInternalView = internalView.buildInternalViewRecursive(cablesIn, cablesOut);
                    if (theInternalView != null)
                        nestedViews.put(child, theInternalView);
                }
                if(!nestedViews.isEmpty()) {
                    Accordion accordion = new Accordion();
                    for (RemoteObjectLight key : nestedViews.keySet()) {
                        accordion.addTab(nestedViews.get(key), String.format("%s [%s]", key.getName(), key.getClassName()));
                    }
                    verticalLayout.addComponent(accordion);
                }
                return verticalLayout;
            }
            return null;
        } catch (ServerSideException ex) {
            Notifications.showError(ex.getMessage());
            return null;
        }
    }
}
