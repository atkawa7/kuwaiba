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
    private LocalNode[] nodes;
    /**
     * Every possible edge in the view
     */
    private LocalEdge[] edges;
    /**
     * Every possible label in the view
     */
    private LocalLabel[] labels;
    
    /**
     * The view background
     */
    private Image background;
    /**
     * Type of view (DefaultView, RackView, etc)
     */
    private String viewClass;

    public LocalObjectView(byte[] viewStructure, byte[] _background, String viewClass) {
        this.background = Utils.getImageFromByteArray(_background);
        this.viewClass = viewClass;
        if (viewStructure == null){
            nodes = new LocalNode[0];
            edges = new LocalEdge[0];
            labels = new LocalLabel[0];
        }else{
            try {
                parseXML(viewStructure);
            } catch (XMLStreamException ex) {
                nodes = new LocalNode[0];
                edges = new LocalEdge[0];
                labels = new LocalLabel[0];
            }
        }
    }

    public LocalObjectView(LocalNode[] myNodes, LocalEdge[] myEdges,LocalLabel[] myLabels) {
        nodes = myNodes;
        edges = myEdges;
        labels = myLabels;
    }

    public LocalEdge[] getEdges() {
        return edges;
    }

    public LocalLabel[] getLabels() {
        return labels;
    }

    public LocalNode[] getNodes() {
        return nodes;
    }

    public Image getBackground() {
        return background;
    }

    public String getViewClass() {
        return this.viewClass;
    }
    /**
     * Parse the XML document using StAX. Thanks to Michael Galpin (http://www.ibm.com/developerworks/java/library/os-ag-renegade15/index.html)
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

        List<LocalNode> myNodes = new ArrayList<LocalNode>();
        List<LocalEdge> myEdges = new ArrayList<LocalEdge>();

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

                    myNodes.add(new LocalNode(lol, xCoordinate, yCoordinate));
                }else{
                    if (reader.getName().equals(qEdge)){
                        Long objectId = Long.valueOf(reader.getAttributeValue(null,"id"));
                        String className = reader.getAttributeValue(null,"class");
                        LocalObject container = CommunicationsStub.getInstance().getObjectInfo(className, objectId);
                        LocalEdge myLocalEdge = new LocalEdge(container,null);

                        for (LocalNode myNode : myNodes){

                            if (((Long)container.getAttribute("aSide")).equals(myNode.getObject().getOid())){ //NOI18N
                                myLocalEdge.setaSide(myNode);
                                break;
                            }else{
                                if (((Long)container.getAttribute("bSide")).equals(myNode.getObject().getOid())){ //NOI18N
                                   myLocalEdge.setbSide(myNode);
                                   break;
                                }
                            }
                        }

                        myEdges.add(myLocalEdge);
                    }
                }
            }
        }
        reader.close();
        nodes = myNodes.toArray(new LocalNode[0]);
        edges = myEdges.toArray(new LocalEdge[0]);
    }
}
