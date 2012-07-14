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

package org.inventory.communications.core.views;

import java.awt.Image;
import java.awt.Point;
import java.io.ByteArrayInputStream;
//import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.api.LocalObject;
import org.inventory.core.services.api.LocalObjectLight;
import org.inventory.core.services.api.visual.LocalEdge;
import org.inventory.core.services.api.visual.LocalLabel;
import org.inventory.core.services.api.visual.LocalNode;
import org.inventory.core.services.api.visual.LocalObjectView;
import org.inventory.core.services.utils.Utils;
import org.openide.util.lookup.ServiceProvider;


/**
 * This class represents the elements inside a view as recorded in the database
 * @author Charles Edward Bedon Cortazar <charles.bedon@@kuwaiba.org>
 */
@ServiceProvider(service=LocalObjectView.class)
public class LocalObjectViewImpl  implements LocalObjectView {
    /**
     * Every possible node in the view
     */
    private List<LocalNode> nodes;
    /**
     * Every possible edge in the view
     */
    private List<LocalEdge> edges;
    /**
     * Every possible label in the view
     */
    private List<LocalLabel> labels;
    
    /**
     * The view background
     */
    private Image background;
    /**
     * Type of view 
     */
    private int viewType;
    /**
     * Mark the current view as outdated
     */
    private boolean dirty = false;

    public LocalObjectViewImpl() {    
        nodes = new ArrayList<LocalNode>();
        edges = new ArrayList<LocalEdge>();
        labels = new ArrayList<LocalLabel>();
    }


    public LocalObjectViewImpl(byte[] viewStructure, byte[] _background, int viewType) {
        this();
        this.background = Utils.getImageFromByteArray(_background);
        this.viewType = viewType;
        
        if (viewStructure != null){
            /* Comment this out for debugging purposes
            try{
                FileOutputStream fos = new FileOutputStream("/home/zim/out.xml");
                fos.write(viewStructure);
                fos.close();
            }catch(Exception e){}*/
             
            try {
                parseXML(viewStructure);
            } catch (XMLStreamException ex) {
                System.out.println("An exception was thrown parsing the XML View: "+ex.getMessage());
            }
        }
    }

    public LocalObjectViewImpl(LocalNode[] myNodes, LocalEdge[] myEdges,LocalLabel[] myLabels) {
        nodes = Arrays.asList(myNodes);
        edges = Arrays.asList(myEdges);
        labels = Arrays.asList(myLabels);
    }

    public List<LocalEdge> getEdges() {
        return edges;
    }

    public List<LocalLabel> getLabels() {
        return labels;
    }

    public List<LocalNode> getNodes() {
        return nodes;
    }

    public Image getBackground() {
        return background;
    }

    public int getViewType() {
        return viewType;
    }

    /**
     * Parse the XML document using StAX. Thanks to <a href="http://www.ibm.com/developerworks/java/library/os-ag-renegade15/index.html">Michael Galpin</a>
     * for his ideas on this
     * @param structure
     * @throws XMLStreamException
     */
    public final void parseXML(byte[] structure) throws XMLStreamException {
        //Here is where we use Woodstox as StAX provider
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();

        QName qNode = new QName("node"); //NOI18N
        QName qEdge = new QName("edge"); //NOI18N
        QName qLabel = new QName("label"); //NOI18N
        QName qControlPoint = new QName("controlpoint"); //NOI18N

        ByteArrayInputStream bais = new ByteArrayInputStream(structure);
        XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);

        nodes.clear();
        edges.clear();
        labels.clear();

        while (reader.hasNext()){
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT){
                if (reader.getName().equals(qNode)){
                    String objectClass = reader.getAttributeValue(null, "class");
                    
                    int xCoordinate = Double.valueOf(reader.getAttributeValue(null,"x")).intValue();
                    int yCoordinate = Double.valueOf(reader.getAttributeValue(null,"y")).intValue();
                    Long objectId = Long.valueOf(reader.getElementText());
                    
                    LocalObjectLight lol = CommunicationsStub.getInstance().
                            getObjectInfoLight(objectClass, objectId);
                    if (lol != null)
                        nodes.add(new LocalNodeImpl(lol, xCoordinate, yCoordinate));
                    else
                        dirty = true;
                }else{
                    if (reader.getName().equals(qEdge)){
                        Long objectId = Long.valueOf(reader.getAttributeValue(null,"id"));
                        Long aSide = Long.valueOf(reader.getAttributeValue(null,"aside"));
                        Long bSide = Long.valueOf(reader.getAttributeValue(null,"bside"));

                        String className = reader.getAttributeValue(null,"class");
                        LocalObject container = CommunicationsStub.getInstance().getObjectInfo(className, objectId);
                        if (container != null){
                            LocalEdgeImpl myLocalEdge = new LocalEdgeImpl(container,null);

                            for (LocalNode myNode : nodes){

                                if (aSide.equals(myNode.getObject().getOid())) //NOI18N
                                    myLocalEdge.setaSide(myNode);
                                else{
                                    if (bSide.equals(myNode.getObject().getOid())) //NOI18N
                                       myLocalEdge.setbSide(myNode);
                                }

                                if (myLocalEdge.getaSide() != null && myLocalEdge.getbSide() != null)
                                    break;
                            }
                            if (myLocalEdge.getaSide() == null || myLocalEdge.getbSide() == null)
                                dirty = true;
                            else{
                                edges.add(myLocalEdge);
                                while(true){
                                    reader.nextTag();
                                    if (reader.getName().equals(qControlPoint)){
                                        if (reader.getEventType() == XMLStreamConstants.START_ELEMENT)
                                            edges.get(edges.size() -1).getControlPoints().
                                                    add(new Point(Double.valueOf(reader.getAttributeValue(null,"x")).intValue(), //NOI18N
                                                    Double.valueOf(reader.getAttributeValue(null,"y")).intValue()));             //NOI18N
                                    }else break;
                                }
                            }
                        }else
                            dirty = true;
                    }else{
                        if (reader.getName().equals(qLabel)){
                            //Unavailable for now
                        }
                        else{
                            //An unknown discardable tag
                        }
                    }
                }
            }
        }
        reader.close();
    }

    public boolean isDirty(){
        return this.dirty;
    }

    public void setDirty(boolean value) {
        this.dirty = value;
    }
}
