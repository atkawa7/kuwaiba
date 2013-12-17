/*
 *  Copyright 2010, 2011, 2012 Neotropic SAS <contact@neotropic.co>.
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
 *
 */

package org.inventory.views.topology;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.LocalStuffFactory;
import org.inventory.core.services.api.LocalObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.api.visual.LocalObjectView;
import org.inventory.core.services.api.visual.LocalObjectViewLight;
import org.inventory.views.topology.scene.ObjectConnectionWidget;
import org.inventory.views.topology.scene.ObjectNodeWidget;
import org.inventory.views.topology.scene.TopologyViewScene;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 * Contains the business logic for the associated TopComponent
 * @author Adrian Martinez <adrian.martinez@kuwaiba.org>
 */
public class TopologyViewService implements LookupListener {


    private TopologyViewScene scene;
    private LocalObjectView currentView;
    /**
     * Topology view Top Component
     */
    private TopologyViewTopComponent  tvtc;
    /**
     * Topology view id
     */
    private long tvId = 0;
    /**
     * communication stub
     */
    private CommunicationsStub com = CommunicationsStub.getInstance();
    /**
     * Array containing the query properties set by using the "configure" button
     * (name, description and share as public)
     */
    private Object[] viewProperties;
    private NotificationUtil nu = Lookup.getDefault().lookup(NotificationUtil.class);
    
    public TopologyViewService(TopologyViewScene scene, TopologyViewTopComponent tvtc) {
        this.tvtc = tvtc;
        this.scene = scene;
        viewProperties = new Object[2];
        resetProperties();
    }

    public long getTvId() {
        return tvId;
    }

    public void setTvId(long tvId) {
        this.tvId = tvId;
    }

    public Object[] getViewProperties() {
        return viewProperties;
    }

    public void setViewProperties(Object[] viewProperties) {
        this.viewProperties = viewProperties;
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Saves the view to a XML representation at server side
     */
    public void saveView(){
        byte[] viewStructure = tvtc.getScene().getAsXML();
        byte[] background = null;
        if(tvId == 0){
            tvId = com.createGeneralView(LocalObjectViewLight.TYPE_TOPOLOGY, (String)viewProperties[0], (String)viewProperties[1], viewStructure, background);
            if(tvId != -1)
                tvtc.getNotifier().showSimplePopup("Sucess", NotificationUtil.INFO, "Topology view created successfully");
            else
                tvtc.getNotifier().showSimplePopup("Error", NotificationUtil.INFO, com.getError());
        }
        else{
            if(com.updateGeneralView(tvId, (String)viewProperties[0], (String)viewProperties[1], viewStructure, background))
                tvtc.getNotifier().showSimplePopup("Sucess", NotificationUtil.INFO, "Topology view updated successfully");
            else
                tvtc.getNotifier().showSimplePopup("Error", NotificationUtil.INFO, com.getError());
        }
    }

    public LocalObjectViewLight[] getTopologyViews(){
        List<LocalObjectViewLight> res = com.getGeneralViews(LocalObjectViewLight.TYPE_TOPOLOGY);
        if(res == null){
            tvtc.getNotifier().showSimplePopup("Error", NotificationUtil.ERROR, com.getError());
            return null;
        }
        else{
            tvtc.toggleButtons(true);
            LocalObjectViewLight[] lov = new LocalObjectViewLight[res.size()];
            for (int i=0; i<res.size(); i++)
                lov[i] = res.get(i);
            return lov;
        }
    }
    /**
     * Loads a view from a XML representation at server side
     */
    public void loadTopologyView(LocalObjectViewLight selectedTopologyView){
        LocalObjectView localView = com.getGeneralView(selectedTopologyView.getId());
        tvId = localView.getId();
        viewProperties[0] = localView.getName();
        viewProperties[1] = localView.getDescription();
        if (localView == null){
            tvtc.getNotifier().showSimplePopup("Error", NotificationUtil.ERROR, com.getError());
            return;
        }else{
            tvtc.getScene().clear();
            try {
                parseXML(localView.getStructure());
            } catch (XMLStreamException ex) {
                System.out.println("An exception was thrown parsing the XML View: "+ex.getMessage());
            }
            tvtc.getScene().validate();
        }
    }

    public void deleteView(){
        if(com.deleteGeneralViews(new long[]{tvId}))
            tvtc.getNotifier().showSimplePopup("Success", NotificationUtil.INFO, "Saved view deleted successfully");
        else
            tvtc.getNotifier().showSimplePopup("Error", NotificationUtil.ERROR, com.getError());
    }

    private void resetProperties() {
        viewProperties[0] = "New topology view "+ new Random().nextInt(10000);
        viewProperties[1] = "";
    }

    /**
     * Parse the XML document using StAX. Thanks to <a href="http://www.ibm.com/developerworks/java/library/os-ag-renegade15/index.html">Michael Galpin</a>
     * for his ideas on this
     * @param structure
     * @throws XMLStreamException
     */
    public void parseXML(byte[] structure) throws XMLStreamException {
        //Here is where we use Woodstox as StAX provider
        Random randomGenerator = new Random();
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();

        QName qNode = new QName("node"); //NOI18N
        QName qEdge = new QName("edge"); //NOI18N
        QName qLabel = new QName("label"); //NOI18N
        QName qControlPoint = new QName("controlpoint"); //NOI18N
        QName qPolygon = new QName("polygon"); //NOI18N
        QName qIcon = new QName("icon"); //NOI18N

        ByteArrayInputStream bais = new ByteArrayInputStream(structure);
        XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);
       
        while (reader.hasNext()){
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT){
                if (reader.getName().equals(qNode)){
                    String objectClass = reader.getAttributeValue(null, "class");

                    int x = Double.valueOf(reader.getAttributeValue(null,"x")).intValue();
                    int y = Double.valueOf(reader.getAttributeValue(null,"y")).intValue();
                    Long objectId = Long.valueOf(reader.getElementText());

                    LocalObjectLight lol = CommunicationsStub.getInstance().
                            getObjectInfoLight(objectClass, objectId);
                    if (lol != null){
                        ObjectNodeWidget widget = (ObjectNodeWidget)scene.addNode(lol);
                        widget.setPreferredLocation(new Point(x, y));
                    }
                    else
                        currentView.setDirty(true);
                }else{
                    if (reader.getName().equals(qIcon)){
                            if(Integer.valueOf(reader.getAttributeValue(null,"type"))==1){
                                LocalObjectLight lol = LocalStuffFactory.createLocalObjectLight();
                                lol.setOid(Long.valueOf(reader.getAttributeValue(null,"id")));
                                int x = Double.valueOf(reader.getAttributeValue(null,"x")).intValue();
                                int y = Double.valueOf(reader.getAttributeValue(null,"y")).intValue();
                                lol.setName(scene.CLOUD_ICON + reader.getElementText());
                                Widget myCloud = scene.addNode(lol);
                                myCloud.setPreferredLocation(new Point(x, y));

                            }
                        }
                    else{
                        if (reader.getName().equals(qEdge)){
                            String edgeName = reader.getAttributeValue(null,"name");

                            Long aSide = Long.valueOf(reader.getAttributeValue(null,"aside"));
                            Long bSide = Long.valueOf(reader.getAttributeValue(null,"bside"));

                            if (edgeName != null){
                                LocalObjectLight aSideObject = LocalStuffFactory.createLocalObjectLight();
                                aSideObject.setOid(aSide);
                                Widget aSideWidget = scene.findWidget(aSideObject);

                                LocalObjectLight bSideObject = LocalStuffFactory.createLocalObjectLight();
                                bSideObject.setOid(bSide);
                                Widget bSideWidget = scene.findWidget(bSideObject);

                                if (aSideWidget == null || bSideWidget == null)
                                    currentView.setDirty(true);
                                else{
                                    ObjectConnectionWidget newEdge = (ObjectConnectionWidget)scene.addEdge(edgeName);
                                    scene.setEdgeSource(edgeName, aSideObject);
                                    scene.setEdgeTarget(edgeName, bSideObject);
                                    List<Point> localControlPoints = new ArrayList<Point>();
                                    while(true){
                                        reader.nextTag();
                                        if (reader.getName().equals(qControlPoint)){
                                            if (reader.getEventType() == XMLStreamConstants.START_ELEMENT){
                                                String cpx = reader.getAttributeValue(null, "x");
                                                String cpy = reader.getAttributeValue(null, "y");
                                                Point point = new Point();
                                                point.setLocation(Double.valueOf(cpx), Double.valueOf(cpy));
                                                localControlPoints.add(point);
                                            }
                                        }else{
                                            newEdge.setControlPoints(localControlPoints, false);
                                            break;
                                        }
                                    }
                                }
                            }else
                                currentView.setDirty(true);
                        }//hasta aqui edges
                        else{
                            if (reader.getName().equals(qLabel)){
                                int x = Double.valueOf(reader.getAttributeValue(null,"x")).intValue();
                                int y = Double.valueOf(reader.getAttributeValue(null,"y")).intValue();
                                Widget myLabel = scene.addNode(randomGenerator.nextInt(1000) +  scene.FREE_LABEL + reader.getElementText());
                                myLabel.setPreferredLocation(new Point(x,y));
                            }
                            else{
                                if (reader.getName().equals(qPolygon)) {
                                    Widget myPolygon = scene.addNode(randomGenerator.nextInt(1000) +  scene.FREE_FRAME + reader.getAttributeValue(null, "title"));
                                    Point p = new Point();
                                    p.setLocation(Double.valueOf(reader.getAttributeValue(null, "x")), Double.valueOf(reader.getAttributeValue(null, "y")));
                                    myPolygon.setPreferredLocation(p);
                                    Dimension d = new Dimension();
                                    d.setSize(Double.valueOf(reader.getAttributeValue(null, "w")), Double.valueOf(reader.getAttributeValue(null, "h")));
                                    Rectangle r = new Rectangle(d);
                                    myPolygon.setPreferredBounds(r);
                                    }
                                else{
                                    //An unknown discardable tag
                                }
                            }//end icons
                        }//end polygons
                    }//end else labels
                }//HATA AQUI
            }
        }
        reader.close();
        scene.validate();
        scene.repaint();
    }
}
