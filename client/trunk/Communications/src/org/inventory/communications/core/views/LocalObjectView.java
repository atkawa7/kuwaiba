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

import com.sun.xml.internal.stream.XMLInputFactoryImpl;
import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.interfaces.LocalObjectLight;
import org.inventory.core.services.utils.Utils;
import org.inventory.webservice.ViewInfo;
import org.openide.util.Exceptions;


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

    public LocalObjectView(ViewInfo remoteView) {
        this.background = Utils.getImageFromByteArray(remoteView.getBackground());
        if (remoteView.getStructure() == null){
            nodes = new LocalNode[0];
            edges = new LocalEdge[0];
            labels = new LocalLabel[0];
        }else  try {
            parseXML(remoteView.getStructure());
        } catch (XMLStreamException ex) {
            nodes = new LocalNode[0];
            edges = new LocalEdge[0];
            labels = new LocalLabel[0];
        }
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

    /**
     * Parse the XML document using StAX. Thanks to Michael Galpin (http://www.ibm.com/developerworks/java/library/os-ag-renegade15/index.html)
     * for his ideas on this
     * @param structure
     * @throws XMLStreamException
     */
    private void parseXML(byte[] structure) throws XMLStreamException {
        //Here is where we use Woodstox as StAX provider
        XMLInputFactory inputFactory = XMLInputFactoryImpl.newInstance();
        //QName qView = new QName("view");
        //QName qNodes = new QName("nodes");
        QName qNode = new QName("node");
        QName qX = new QName("x");
        QName qY = new QName("y");
        //QName qEdges = new QName("edges");
        QName qEdge = new QName("edge");
        //QName qLabels = new QName("labels");
        QName qLabel = new QName("label");
        ByteArrayInputStream bais = new ByteArrayInputStream(structure);
        XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);

        List<LocalNode> myNodes = new ArrayList<LocalNode>();

        while (reader.hasNext()){
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT){
                if (reader.getName().equals(qNode))
                    myNodes.add(new LocalNode((LocalObjectLight) CommunicationsStub.getInstance().getObjectInfo("RootObject", Long.valueOf(reader.getElementText()), null),
                            Integer.valueOf(reader.getAttributeValue(null,qX.getLocalPart())),
                            Integer.valueOf(reader.getAttributeValue(null, qY.getLocalPart()))));
            }
        }
        reader.close();
    }
}
