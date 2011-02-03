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

package org.inventory.queries;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
import javax.swing.JCheckBox;
import javax.xml.stream.XMLStreamException;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalResultRecord;
import org.inventory.communications.core.queries.LocalQuery;
import org.inventory.communications.core.queries.LocalQueryLight;
import org.inventory.communications.core.queries.LocalTransientQuery;
import org.inventory.core.services.interfaces.LocalClassMetadata;
import org.inventory.core.services.interfaces.LocalClassMetadataLight;
import org.inventory.core.services.interfaces.LocalObjectLight;
import org.inventory.core.services.interfaces.NotificationUtil;
import org.inventory.queries.graphical.QueryEditorNodeWidget;
import org.inventory.queries.graphical.QueryEditorScene;
import org.inventory.queries.graphical.elements.ClassNodeWidget;

/**
 * This class will replace the old QueryBuilderService in next releases
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class GraphicalQueryBuilderService implements ActionListener{
    private QueryBuilderTopComponent qbtc;
    private CommunicationsStub com = CommunicationsStub.getInstance();
    /**
     * This one has the execution details
     */
    private LocalTransientQuery currentTransientQuery;
    /**
     * This one has the storing details
     */
    private LocalQuery localQuery;
    /**
     * Array containing the query properties set by using the "configure" button 
     * (name, description and share as public)
     */
    private Object[] queryProperties;
    /**
     * Offset to place the nodes horizontally
     **/
    private static int X_OFFSET = 50;
    /**
     * Offset to place the nodes vertically
     **/
    private static int Y_OFFSET = 50;

    public GraphicalQueryBuilderService(QueryBuilderTopComponent qbtc) {
        this.qbtc = qbtc;
        queryProperties = new Object[3];
        resetProperties();
    }

    public LocalClassMetadataLight[] getClassList(){
        LocalClassMetadataLight[] items = com.getAllLightMeta();
        if (items == null){
            qbtc.getNotifier().showSimplePopup("Query Builder", NotificationUtil.ERROR, com.getError());
            return new LocalClassMetadataLight[0];
        }
        return items;
    }

    public LocalClassMetadata getClassDetails(String className){
        LocalClassMetadata res= com.getMetaForClass(className, false);
        if (res == null)
            qbtc.getNotifier().showSimplePopup("Query Builder", NotificationUtil.ERROR, com.getError());
        return res;
    }

    public LocalResultRecord[] executeQuery(int page) {
        currentTransientQuery = qbtc.getQueryScene().getTransientQuery(qbtc.getQueryScene().getCurrentSearchedClass(),
                        qbtc.getChkAnd().isSelected()?LocalTransientQuery.CONNECTOR_AND:LocalTransientQuery.CONNECTOR_OR,
                        Integer.valueOf(qbtc.getTxtResultLimit().getText()), page, false);
        LocalResultRecord[] res = com.executeQuery(currentTransientQuery);
        if (res == null)
            qbtc.getNotifier().showSimplePopup("Query Execution", NotificationUtil.ERROR, com.getError());
        return res;
    }

    public LocalQueryLight[] getQueries(boolean showAll){
        LocalQueryLight[] res = com.getQueries(showAll);
        if (res == null){
            qbtc.getNotifier().showSimplePopup("Error", NotificationUtil.ERROR, com.getError());
            return null;
        }
        else return res;
    }

    public void saveQuery(){
        currentTransientQuery = qbtc.getQueryScene().getTransientQuery(qbtc.getQueryScene().getCurrentSearchedClass(),
                            qbtc.getChkAnd().isSelected()?LocalTransientQuery.CONNECTOR_AND:LocalTransientQuery.CONNECTOR_OR,
                            Integer.valueOf(qbtc.getTxtResultLimit().getText()), 0, false);

        if (localQuery == null){ //It's a new query
            
            if (com.createQuery((String)queryProperties[0], currentTransientQuery.toXML(), (String)queryProperties[1], (Boolean)queryProperties[2]) != null)
                qbtc.getNotifier().showSimplePopup("Sucess", NotificationUtil.INFO, "Query created successfully");
            else
                qbtc.getNotifier().showSimplePopup("Error", NotificationUtil.INFO, com.getError());
            /*
             * Only for debugging purposes
             try{
                FileOutputStream fos = new FileOutputStream("/home/zim/query.xml");
                fos.write(currentTransientQuery.toXML());
                fos.flush();
                fos.close();
                JOptionPane.showMessageDialog(qbtc, "Query Saved Successfully","Success",JOptionPane.INFORMATION_MESSAGE);
            }catch(IOException e){
                e.printStackTrace();
            }*/
        }else{ //It's an old query. An update is necessary
            localQuery.setName((String)queryProperties[0]);
            localQuery.setStructure(currentTransientQuery.toXML());
            localQuery.setDescription((String)queryProperties[1]);
            localQuery.setIsPublic((Boolean)queryProperties[2]);

            if (com.saveQuery(localQuery))
                qbtc.getNotifier().showSimplePopup("Sucess", NotificationUtil.INFO, "Query saved successfully");
            else
                qbtc.getNotifier().showSimplePopup("Error", NotificationUtil.INFO, com.getError());
        }
    }

    LocalTransientQuery getCurrentTransientQuery() {
        return currentTransientQuery;
    }

    LocalQuery getCurrentLocalQuery(){
        return localQuery;
    }

    Object[] getQueryProperties(){
        return queryProperties;
    }

    void setQueryProperties(Object[] newProperties){
        queryProperties = newProperties;
    }

    public void actionPerformed(ActionEvent e) {
        JCheckBox insideCheck = (JCheckBox)e.getSource();
        switch (e.getID()){
            case QueryEditorScene.SCENE_FILTERENABLED:
                QueryEditorNodeWidget newNode;
                if (insideCheck.getClientProperty("filterType").equals(LocalObjectLight.class)){
                    LocalClassMetadata myMetadata = com.getMetaForClass((String)insideCheck.getClientProperty("className"),false);
                    newNode = (ClassNodeWidget)qbtc.getQueryScene().findWidget(myMetadata);
                    if (newNode == null){
                        newNode = (QueryEditorNodeWidget) qbtc.getQueryScene().addNode(myMetadata);
                        newNode.build(null);
                        qbtc.getQueryScene().validate();
                    }
                    insideCheck.putClientProperty("related-node", myMetadata);
                }else{
                    String newNodeId = ((Class)insideCheck.getClientProperty("filterType")).
                            getSimpleName()+"_"+new Random().nextInt(10000);
                    newNode = (QueryEditorNodeWidget)qbtc.getQueryScene().addNode(newNodeId);
                    newNode.build(newNodeId);
                    insideCheck.putClientProperty("related-node", newNodeId);
                }

                String edgeName = "Edge_"+new Random().nextInt(1000);
                qbtc.getQueryScene().addEdge(edgeName);
                qbtc.getQueryScene().setEdgeSource(edgeName, insideCheck.getClientProperty("attribute"));
                qbtc.getQueryScene().setEdgeTarget(edgeName, newNode.getDefaultPinId());

                if (qbtc.getQueryScene().getView().getMousePosition() != null)
                    newNode.setPreferredLocation(new Point(qbtc.getQueryScene().getView().getMousePosition().x + 200,
                            qbtc.getQueryScene().getView().getMousePosition().y));
                
                qbtc.getQueryScene().validate();
                break;
            case QueryEditorScene.SCENE_FILTERDISABLED:
                ((QueryEditorScene)qbtc.getQueryScene()).removeAllRelatedNodes(insideCheck.getClientProperty("related-node"));
                insideCheck.putClientProperty("related-node",null);
                qbtc.getQueryScene().validate();
                break;
        }
    }

    /**
     * Renders a query extracted from the database
     * @param selectedQuery query to be rendered
     */
    public void renderQuery(LocalQueryLight selectedQuery) {
        localQuery = com.getQuery(selectedQuery.getId());
        if (localQuery == null){
            qbtc.getNotifier().showSimplePopup("Error", NotificationUtil.ERROR, com.getError());
            return;
        }
        try {
            LocalTransientQuery transientQuery = new LocalTransientQuery(localQuery);
            qbtc.getQueryScene().clear();
            ClassNodeWidget rootNode = renderClassNode(transientQuery);
            qbtc.getQueryScene().setCurrentSearchedClass(rootNode.getWrappedClass());
            qbtc.getQueryScene().organizeNodes(rootNode, X_OFFSET, Y_OFFSET);
            qbtc.getQueryScene().validate();
        } catch (XMLStreamException ex) {
            qbtc.getNotifier().showSimplePopup("Error", NotificationUtil.ERROR, "Error parsing XML file");
            return;
        }
        queryProperties[0] = localQuery.getName();
        queryProperties[1] = localQuery.getDescription();
        queryProperties[2] = localQuery.getIsPublic();
        
    }

    private ClassNodeWidget renderClassNode(LocalTransientQuery subQuery){
        LocalClassMetadata classMetadata = com.getMetaForClass(subQuery.getClassName(), false);
        if (classMetadata == null){
            qbtc.getNotifier().showSimplePopup("Error", NotificationUtil.ERROR, com.getError());
            return null;
        }
        ClassNodeWidget currentNode = ((ClassNodeWidget)qbtc.getQueryScene().addNode(classMetadata));
        currentNode.build(null);
        currentNode.setVisibleAttributes(subQuery.getVisibleAttributeNames());

        //Marking the scene to validate is necessary for the newlycreated node to be painted
        //providing the clientArea necessary to calculate locations of new nodes
        qbtc.getQueryScene().validate();

        for (LocalTransientQuery join : subQuery.getJoins()){
            if (join != null)
                renderClassNode(join);
        }
        currentNode.setFilteredAttributes(subQuery.getAttributeNames(), subQuery.getConditions());
        
        return currentNode;
    }

    public void resetLocalQuery(){
        this.localQuery = null;
        resetProperties();
    }

    private void resetProperties() {
        queryProperties[0] = "New Query "+ new Random().nextInt(10000);
        queryProperties[1] = "";
        queryProperties[2] = false; //By default the views are private
    }
}