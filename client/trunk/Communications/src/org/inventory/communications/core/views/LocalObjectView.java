/*
 *  Copyright 2010 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.interfaces.LocalObject;
import org.inventory.core.services.interfaces.LocalObjectLight;
import org.inventory.core.services.utils.Utils;


/**
 * This class represents the elements inside a view as recorded in the database
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class LocalObjectView {
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
     * Type of view (DefaultView, RackView, etc)
     */
    private String viewClass;
    /**
     * Mark the current view as outdated
     */
    private boolean isDirty = false;

    public LocalObjectView(byte[] viewStructure, byte[] _background, String viewClass) {
        this.background = Utils.getImageFromByteArray(_background);
        this.viewClass = viewClass;
        nodes = new ArrayList<LocalNode>();
        edges = new ArrayList<LocalEdge>();
        labels = new ArrayList<LocalLabel>();
        if (viewStructure != null){
            try {
                parseXML(viewStructure);
            } catch (XMLStreamException ex) {
                System.out.println("An exception was thrown parsing the XML View: "+ex.getMessage());
            }
        }
    }

    public LocalObjectView(LocalNode[] myNodes, LocalEdge[] myEdges,LocalLabel[] myLabels) {
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

    public String getViewClass() {
        return this.viewClass;
    }
    /**
     * Parse the XML document using StAX. Thanks to <a href="http://www.ibm.com/developerworks/java/library/os-ag-renegade15/index.html">Michael Galpin</a>
     * for his ideas on this
     * @param structure
     * @throws XMLStreamException
     */
    private void parseXML(byte[] structure) throws XMLStreamException {
        //Here is where we use Woodstox as StAX provider
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        //QName qView = new QName("view");
        //QName qNodes = new QName("nodes");
        QName qNode = new QName("node");

        //QName qEdges = new QName("edges");
        QName qEdge = new QName("edge");
        //QName qLabels = new QName("labels");
        QName qLabel = new QName("label");
        ByteArrayInputStream bais = new ByteArrayInputStream(structure);
        XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);

        nodes.removeAll(nodes);
        edges.removeAll(edges);
       labels.removeAll(labels);

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
                        nodes.add(new LocalNode(lol, xCoordinate, yCoordinate));
                    else
                        isDirty = true;
                }else{
                    if (reader.getName().equals(qEdge)){
                        Long objectId = Long.valueOf(reader.getAttributeValue(null,"id"));
                        String className = reader.getAttributeValue(null,"class");
                        LocalObject container = CommunicationsStub.getInstance().getObjectInfo(className, objectId);
                        LocalEdge myLocalEdge = new LocalEdge(container,null);

                        for (LocalNode myNode : nodes){

                            if (((Long)container.getAttribute("nodeA")).equals(myNode.getObject().getOid())) //NOI18N
                                myLocalEdge.setaSide(myNode);
                            else{
                                if (((Long)container.getAttribute("nodeB")).equals(myNode.getObject().getOid())) //NOI18N
                                   myLocalEdge.setbSide(myNode);
                            }

                            if (myLocalEdge.getaSide() != null && myLocalEdge.getbSide() != null)
                                break;
                        }
                        if (myLocalEdge.getaSide() == null || myLocalEdge.getbSide() == null)
                            isDirty = true;
                        else{
                            myLocalEdge.setClassName(className);
                            edges.add(myLocalEdge);
                        }
                    }else{
                        if (reader.getName().equals(qLabel)){
                            //Unavailable by now
                        }
                    }
                }
            }
        }
        reader.close();
    }

    public boolean getIsDirty(){
        return this.isDirty;
    }

    public void setIsDirty(boolean value) {
        this.isDirty = value;
    }
}
