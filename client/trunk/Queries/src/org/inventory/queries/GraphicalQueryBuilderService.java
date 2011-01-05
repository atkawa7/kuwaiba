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
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.interfaces.LocalAttributeMetadata;
import org.inventory.core.services.interfaces.LocalClassMetadata;
import org.inventory.core.services.interfaces.LocalClassMetadataLight;
import org.inventory.core.services.interfaces.LocalObjectLight;
import org.inventory.core.services.interfaces.NotificationUtil;
import org.inventory.queries.graphical.QueryEditorNodeWidget;
import org.inventory.queries.graphical.QueryEditorScene;

/**
 * This class will replace the old QueryBuilderService in next releases
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class GraphicalQueryBuilderService implements ActionListener{
    private QueryBuilderTopComponent qbtc;
    private CommunicationsStub com = CommunicationsStub.getInstance();

    public GraphicalQueryBuilderService(QueryBuilderTopComponent qbtc) {
        this.qbtc = qbtc;
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

    public void actionPerformed(ActionEvent e) {
        JCheckBox insideCheck = (JCheckBox)e.getSource();
        if (e.getID() == QueryEditorScene.SCENE_FILTERENABLED){
            QueryEditorNodeWidget newNode;
            if (insideCheck.getClientProperty("filterType").equals(LocalObjectLight.class)){
                newNode = (QueryEditorNodeWidget) qbtc.getQueryScene().addNode(com.getMetaForClass((String)insideCheck.getClientProperty("className"),false));
                newNode.build(null);
            }else{
                String newNodeId = ((Class)insideCheck.getClientProperty("filterType")).
                        getSimpleName()+"_"+new Random().nextInt(1000);
                newNode = (QueryEditorNodeWidget)qbtc.getQueryScene().addNode(newNodeId);
                newNode.build(newNodeId);
            }

            String edgeName = "Edge_"+new Random().nextInt(1000);
            qbtc.getQueryScene().addEdge(edgeName);
            qbtc.getQueryScene().setEdgeSource(edgeName, insideCheck.getClientProperty("attribute"));
            qbtc.getQueryScene().setEdgeTarget(edgeName, newNode.getDefaultPinId());

            newNode.setPreferredLocation(new Point(qbtc.getQueryScene().getView().getMousePosition().x + 200,
                        qbtc.getQueryScene().getView().getMousePosition().y));
            qbtc.getQueryScene().validate();
        }
    }
}