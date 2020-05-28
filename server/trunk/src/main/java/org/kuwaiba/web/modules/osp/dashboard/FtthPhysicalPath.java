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

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Grid;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.renderers.HtmlRenderer;
import java.util.ArrayList;
import java.util.List;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;

/**
 * The ftth physical path is a grid with a column that list the path
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class FtthPhysicalPath extends VerticalLayout {
    private final WebserviceBean webserviceBean;
    private final RemoteSession remoteSession;
    private final RemoteObjectLight port;
    private final RemoteObjectLight fiber;
    
    public FtthPhysicalPath(WebserviceBean webserviceBean, RemoteSession remoteSession, RemoteObjectLight fiber, RemoteObjectLight port) {
        this.webserviceBean = webserviceBean;
        this.remoteSession = remoteSession;
        this.port = port;
        this.fiber = fiber;
        init();
    }
    
    public static class ClassName {
        private static final String FIBER_SPLITTER = "FiberSplitter"; //NOI18N
        private static final String MANHOLE = "Manhole"; //NOI18N
        private static final String SPLICE_BOX = "SpliceBox"; //NOI18N}
        private static final String OPTICAL_LINK = "OpticalLink"; //NOI18N //Is not internal view candidate
        private static final String CTO = "CTO"; //NOI18N
        private static final String ODF = "ODF"; //NOI18N
        private static final String OPTICAL_LINE_TERMINAL = "OpticalLineTerminal"; //NOI18N
        private static final String WIRE_CONTAINER = "WireContainer"; //NOI18N
        private static final String JUMPER = "Jumper"; //NOI18N
    }
    
    private class Row {
        private final RemoteObjectLight child;
        private final RemoteObjectLight parent;

        public Row(RemoteObjectLight child, RemoteObjectLight parent) {
            this.child = child;
            this.parent = parent;
        }

        public RemoteObjectLight getChild() {
            return child;
        }

        public RemoteObjectLight getParent() {
            return parent;
        }
    }
    
    private class Item {
        private final Row row;
        
        public Item(Row row) {
            this.row = row;
        }
        public Row getRow() {
            return row;
        }
    }
    
    private void init() {
        try {
            setSizeFull();
            Grid<Item> grd = new Grid();
            List<RemoteObjectLight> path = webserviceBean.getPhysicalPath(port.getClassName(), port.getId(),
                    remoteSession.getIpAddress(), remoteSession.getSessionId());
            List<RemoteObjectLight> thePath = new ArrayList();
            if (fiber != null)
                thePath.add(fiber);
                                                
            for (RemoteObjectLight item : path) {
                if (ClassName.OPTICAL_LINK.equals(item.getClassName())) {
                    thePath.add(item);
                }
            }
            List<Item> rows = new ArrayList();
            for (int i = 0; i < thePath.size(); i++) {
                RemoteObjectLight item = thePath.get(i);
                List<RemoteObjectLight> parents = webserviceBean.getParents(item.getClassName(), item.getId(), remoteSession.getIpAddress(), remoteSession.getSessionId());
                RemoteObjectLight parent = null;
                for (RemoteObjectLight p : parents) {
                    if (ClassName.WIRE_CONTAINER.equals(p.getClassName()) || ClassName.JUMPER.equals(p.getClassName())) {
                        parent = p;
                        break;
                    }
                }
                if (i > 0) {
                    List<RemoteObjectLight> endpointsB = webserviceBean.getSpecialAttribute(item.getClassName(), item.getId(), "endpointB", remoteSession.getIpAddress(), remoteSession.getSessionId());
                    if (endpointsB.size() == 1) {
                        RemoteObjectLight endpointB = endpointsB.get(0);
                        List<RemoteObjectLight> theParents = webserviceBean.getParents(endpointB.getClassName(), endpointB.getId(), remoteSession.getIpAddress(), remoteSession.getSessionId());
                        for (RemoteObjectLight theP : theParents) {
                            if (ClassName.FIBER_SPLITTER.equals(theP.getClassName()) || 
                                ClassName.SPLICE_BOX.equals(theP.getClassName()) ||
                                ClassName.CTO.equals(theP.getClassName()) ||
                                ClassName.ODF.equals(theP.getClassName()) ||
                                ClassName.OPTICAL_LINE_TERMINAL.equals(theP.getClassName())) {

                                rows.add(new Item(new Row(endpointB, theP)));
                                break;
                            }
                        }
                    }
                }
                rows.add(new Item(new Row(item, parent)));
                
                List<RemoteObjectLight> endpointsA = webserviceBean.getSpecialAttribute(item.getClassName(), item.getId(), "endpointA", remoteSession.getIpAddress(), remoteSession.getSessionId());
                if (endpointsA.size() == 1) {
                    RemoteObjectLight endpointA = endpointsA.get(0);
                    List<RemoteObjectLight> theParents = webserviceBean.getParents(endpointA.getClassName(), endpointA.getId(), remoteSession.getIpAddress(), remoteSession.getSessionId());
                    for (RemoteObjectLight theP : theParents) {
                        if (ClassName.FIBER_SPLITTER.equals(theP.getClassName()) || 
                            ClassName.SPLICE_BOX.equals(theP.getClassName()) ||
                            ClassName.CTO.equals(theP.getClassName()) ||
                            ClassName.ODF.equals(theP.getClassName()) ||
                            ClassName.OPTICAL_LINE_TERMINAL.equals(theP.getClassName())) {

                            rows.add(new Item(new Row(endpointA, theP)));
                            break;
                        }
                    }
                }
            }
            grd.setItems(rows);
            grd.setSizeFull();
            grd.addColumn(Item::getRow).setCaption("Physical Path").setRenderer(item -> {
                StringBuilder builder = new StringBuilder();
                builder.append("<span class=\"v-icon\" style=\"font-family: ");
                builder.append(VaadinIcons.STOP.getFontFamily());
                builder.append(";color:black");
                builder.append("\">&#x");
                
                if (item.getParent() != null) {
                    if (ClassName.WIRE_CONTAINER.equals(item.getParent().getClassName()) || ClassName.JUMPER.equals(item.getParent().getClassName()))
                        builder.append(Integer.toHexString(VaadinIcons.BULLSEYE.getCodepoint()));                    
                    else if (ClassName.FIBER_SPLITTER.equals(item.getParent().getClassName()))
                        builder.append(Integer.toHexString(VaadinIcons.CONNECT.getCodepoint()));
                    else if (ClassName.OPTICAL_LINE_TERMINAL.equals(item.getParent().getClassName()))
                        builder.append(Integer.toHexString(VaadinIcons.ABACUS.getCodepoint()));
                    else if (ClassName.ODF.equals(item.getParent().getClassName()))
                        builder.append(Integer.toHexString(VaadinIcons.HARDDRIVE.getCodepoint()));
                    else if (ClassName.SPLICE_BOX.equals(item.getParent().getClassName()))
                        builder.append(Integer.toHexString(VaadinIcons.SLIDERS.getCodepoint()));
                    else if (ClassName.CTO.equals(item.getParent().getClassName()))
                        builder.append(Integer.toHexString(VaadinIcons.SLIDER.getCodepoint()));
                    else
                        builder.append(Integer.toHexString(VaadinIcons.STOP.getCodepoint()));
                } else {
                    builder.append(Integer.toHexString(VaadinIcons.STOP.getCodepoint()));
                }
                builder.append("; ");
                builder.append(item.getParent() != null ? item.getParent().getName() : "");
                builder.append(" (");
                builder.append(item.getChild() != null ? item.getChild().getName() : "");
                builder.append(")");
                builder.append("</span>");
                
                return builder.toString();}, 
                new HtmlRenderer());
            addComponent(grd);
            grd.addItemClickListener(event -> {
                RemoteObjectLight parent = event.getItem().getRow().getParent();
                if (ClassName.FIBER_SPLITTER.equals(parent.getClassName()) || 
                    ClassName.SPLICE_BOX.equals(parent.getClassName()) ||
                    ClassName.CTO.equals(parent.getClassName()) ||
                    ClassName.ODF.equals(parent.getClassName()) ||
                    ClassName.OPTICAL_LINE_TERMINAL.equals(parent.getClassName())) {
                        Window window = new Window();
                        FtthOspNodeInternalView ftthOspNodeInternalView = new FtthOspNodeInternalView(webserviceBean, remoteSession, parent);
                        if (ftthOspNodeInternalView.canBuildInternalView()) {
                            window.setModal(true);
//                            window.setSizeFull();
                            window.setContent(ftthOspNodeInternalView.buildInternalView());
                            UI.getCurrent().addWindow(window);
                        }
                }
            });
            setComponentAlignment(grd, Alignment.MIDDLE_CENTER);
        } catch (ServerSideException ex) {
            Notifications.showError(ex.getMessage());
        }
    }
}
