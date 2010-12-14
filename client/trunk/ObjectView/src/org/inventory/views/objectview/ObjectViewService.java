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

package org.inventory.views.objectview;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.views.LocalEdge;
import org.inventory.communications.core.views.LocalObjectView;
import org.inventory.core.services.interfaces.LocalObject;
import org.inventory.core.services.interfaces.LocalObjectLight;
import org.inventory.core.services.interfaces.NotificationUtil;
import org.inventory.core.services.utils.Utils;
import org.inventory.views.objectview.scene.ObjectConnectionWidget;
import org.inventory.views.objectview.scene.ObjectNodeWidget;
import org.inventory.views.objectview.scene.ViewBuilder;
import org.inventory.views.objectview.scene.ViewScene;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;

/**
 * Contains the business logic for the associated TopComponent
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class ObjectViewService implements LookupListener{
    
    private ObjectViewTopComponent vrtc;
    private Lookup.Result selectedNodes;
    private CommunicationsStub com;
    private ViewBuilder viewBuilder;

    public ObjectViewService(ObjectViewTopComponent _vrtc){
        this.vrtc = _vrtc;
        this.com = CommunicationsStub.getInstance();
    }

    /**
     * Add this instance as listener for the selected nodes in the NavigationTree.
     * Should be called when the TopComponent is opened
     */
    public void initializeLookListener(){
        selectedNodes = Utilities.actionsGlobalContext().lookupResult(LocalObjectLight.class);
        selectedNodes.addLookupListener(this);
    }

    /**
     * Removes this instance as listener for the selected nodes in the NavigationTree.
     * Should be called when the TopComponent is closed
     */
    public void terminateLookupListener(){
        selectedNodes.removeLookupListener(this);
    }

    /**
     * Updates the view when a new object is selected
     * @param ev
     */
    public void resultChanged(LookupEvent ev) {
        Lookup.Result lookupResult = (Lookup.Result)ev.getSource();
        if(lookupResult.allInstances().size() == 1){

           //Don't update if the same object is selected
           LocalObjectLight myObject = (LocalObjectLight)lookupResult.allInstances().iterator().next();
           if (myObject.equals(vrtc.getScene().getCurrentObject()))
                return;
           
           //Check if the view is still unsaved
           vrtc.checkForUnsavedView(false);

           vrtc.setHtmlDisplayName(null); //Clear the displayname in case it was set to another value

            //We clean the scene...
           vrtc.getScene().getNodesLayer().removeChildren();
           vrtc.getScene().getEdgesLayer().removeChildren();
           vrtc.getScene().getBackgroundLayer().removeChildren();
           vrtc.getScene().getInteractionLayer().removeChildren();
           vrtc.getScene().getLabelsLayer().removeChildren();

           //If the selected node is the root
           if (myObject.getOid() == null){
               vrtc.setDisplayName(null);
               vrtc.setHtmlDisplayName(null);
               return;
           }

           vrtc.getScene().setCurrentObject(myObject);
           
           LocalObjectView defaultView = com.getObjectDefaultView(myObject.getOid(),myObject.getClassName());
           if(defaultView == null){
               List<LocalObjectLight> myChildren = com.getObjectChildren(myObject.getOid(), com.getMetaForClass(myObject.getClassName(),false).getOid());
               List<LocalObject> myConnections = com.getChildrenOfClass(myObject.getOid(), LocalEdge.CLASS_GENERICCONNECTION);
               //TODO: Change for a ViewFactory
               viewBuilder = new ViewBuilder(null, vrtc.getScene());
               viewBuilder.buildDefaultView(myChildren, myConnections);
           }
           else{
               viewBuilder = new ViewBuilder(defaultView, vrtc.getScene());
               viewBuilder.buildView();
               if (viewBuilder.getMyView().getIsDirty()){
                   vrtc.getNotifier().showSimplePopup("View changes", NotificationUtil.WARNING, "Some elements in the view has been deleted since the last time it was opened. They were removed");
                   vrtc.getScene().fireChangeEvent(new ActionEvent(this, ViewScene.SCENE_CHANGETOSAVE, "Removing old objects"));
                   viewBuilder.getMyView().setIsDirty(false);
               }
           }

           vrtc.getScene().validate();
           vrtc.getScene().repaint();
           vrtc.setDisplayName(myObject.getDisplayname() + " ["+myObject.getClassName()+"]");
        }else{
            if(!lookupResult.allInstances().isEmpty())
                vrtc.getNotifier().showStatusMessage("More than one object selected. No view available", false);
        }
    }

    /**
     * Adds a background (removing the old one if existing) to the view
     */
    public void addBackground() {
        JFileChooser fChooser = new JFileChooser();
        fChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fChooser.setFileFilter(Utils.getImageFileFilter());
        if (fChooser.showOpenDialog(vrtc.getScene().getView()) == JFileChooser.APPROVE_OPTION){
            Image myBackgroundImage = Toolkit.getDefaultToolkit().createImage(fChooser.getSelectedFile().getAbsolutePath());
            if (myBackgroundImage == null)
                 vrtc.getNotifier().showSimplePopup("Image load", NotificationUtil.ERROR, "Error loading image. Please try another");
            else{
                vrtc.getScene().setBackgroundImage(myBackgroundImage);
                vrtc.getScene().validate();
                vrtc.getScene().fireChangeEvent(new ActionEvent(this, ViewScene.SCENE_CHANGE, "Add Background"));
            }
        }
    }

    /**
     * Removes the current background
     */
    public void removeBackground() {
        vrtc.getScene().getBackgroundLayer().removeChildren();
        vrtc.getScene().fireChangeEvent(new ActionEvent(this, ViewScene.SCENE_CHANGE, "Remove Background"));
    }

    /**
     * Saves the view to a XML representation at server side
     */
    public void saveView() {
        byte[] viewStructure = vrtc.getScene().getAsXML();
        if (!com.saveView(vrtc.getScene().getCurrentObject().getOid(),
                 vrtc.getScene().getCurrentObject().getClassName(), 
                "DefaultView", vrtc.getScene().getBackgroundImage(), viewStructure)) //NOI18N
            vrtc.getNotifier().showSimplePopup("Object View", NotificationUtil.ERROR, com.getError());
        else
            vrtc.setHtmlDisplayName(vrtc.getDisplayName());
    }

    public void refreshView() {
        List<LocalObjectLight> childrenNodes = com.getObjectChildren(vrtc.getScene().getCurrentObject().getOid(),
                com.getMetaForClass(vrtc.getScene().getCurrentObject().getClassName(), false).getOid());
        List<LocalObject> childrenEdges = com.getChildrenOfClass(vrtc.getScene().getCurrentObject().getOid(),
                LocalEdge.CLASS_GENERICCONNECTION);
        
        List<LocalObjectLight> currentNodes = new ArrayList<LocalObjectLight>();
        List<LocalObject> currentEdges = new ArrayList<LocalObject>();

        for (Widget widget : vrtc.getScene().getNodesLayer().getChildren())
            currentNodes.add(((ObjectNodeWidget)widget).getObject());

        for (Widget widget : vrtc.getScene().getEdgesLayer().getChildren())
            currentEdges.add(((ObjectConnectionWidget)widget).getObject());

        Object[] nodesIntersection = Utils.inverseIntersection(childrenNodes, currentNodes);
        Object[] edgesIntersection = Utils.inverseIntersection(childrenEdges, currentEdges);
        
        viewBuilder.refreshView((List<LocalObjectLight>)nodesIntersection[0], (List<LocalObject>)edgesIntersection[0],
                (List<LocalObjectLight>)nodesIntersection[1], (List<LocalObject>)edgesIntersection[1]);
        vrtc.getScene().validate();
        vrtc.getScene().repaint();
        if (!((List)nodesIntersection[0]).isEmpty() || !((List)nodesIntersection[1]).isEmpty()
                || !((List)edgesIntersection[0]).isEmpty() || !((List)edgesIntersection[1]).isEmpty())
            vrtc.getScene().fireChangeEvent(new ActionEvent(this, ViewScene.SCENE_CHANGE, "Refresh result"));
    }
}
