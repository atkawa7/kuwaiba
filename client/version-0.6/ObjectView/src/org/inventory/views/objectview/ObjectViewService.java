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

package org.inventory.views.objectview;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObject;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.views.LocalObjectView;
import org.inventory.communications.core.views.LocalObjectViewLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.Utils;
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
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ObjectViewService implements LookupListener{
    
    private ObjectViewTopComponent vrtc;
    private Lookup.Result<LocalObjectLight> selectedNodes;
    private CommunicationsStub com;
    private ViewBuilder viewBuilder;

    public ObjectViewService(ObjectViewTopComponent vrtc){
        this.vrtc = vrtc;
        this.com = CommunicationsStub.getInstance();
    }

    /**
     * Add this instance as listener for the selected nodes in the NavigationTree.
     * Should be called when the TopComponent is opened
     */
    public void initializeLookupListener(){
        selectedNodes = Utilities.actionsGlobalContext().lookupResult(LocalObjectLight.class);
        selectedNodes.addLookupListener(this);
        if (selectedNodes.allInstances().size() == 1) //There's a node already selected
            loadView(selectedNodes.allInstances().iterator().next());
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
    @Override
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
           vrtc.getScene().clear();

           if (myObject.getOid() != -1){ //Other nodes than the root one
               if(!com.getMetaForClass(myObject.getClassName(), false).isViewable()){
                   vrtc.getNotifier().showStatusMessage("This object doesn't have any view", false);
                   disableView();
                   return;
               }
           }
           loadView(myObject);
        }else{
            if(!lookupResult.allInstances().isEmpty()){
               vrtc.getNotifier().showStatusMessage("More than one object selected. No view available", false);
               vrtc.toggleButtons(false);
            }
        }
    }

    private void loadView(LocalObjectLight myObject){
       //If the selected node is the root
       if (myObject.getOid() == -1){
           disableView();
           return;
       }

       vrtc.toggleButtons(true);

       vrtc.getScene().setCurrentObject(myObject);

       List<LocalObjectViewLight> views = com.getObjectRelatedViews(myObject.getOid(),myObject.getClassName());
       
       if(views.isEmpty()){ //There are no saved views
           List<LocalObjectLight> myChildren = com.getObjectChildren(myObject.getOid(), com.getMetaForClass(myObject.getClassName(),false).getOid());
           List<LocalObject> myConnections = com.getChildrenOfClass(myObject.getOid(),myObject.getClassName(), Constants.CLASS_GENERICCONNECTION);
           //TODO: Change for a ViewFactory
           viewBuilder = new ViewBuilder(null, vrtc.getScene());
           viewBuilder.buildDefaultView(myChildren, myConnections);
           vrtc.getScene().setCurrentView(null);
       }
       else{
           LocalObjectView defaultView = com.getObjectRelatedView(myObject.getOid(),myObject.getClassName(), views.get(0).getId());
           vrtc.getScene().setCurrentView(defaultView);
           viewBuilder = new ViewBuilder(defaultView, vrtc.getScene());
           vrtc.getScene().clear();
           viewBuilder.buildView();
           if (defaultView.isDirty()){
               vrtc.getNotifier().showSimplePopup("View changes", NotificationUtil.WARNING, "Some elements in the view has been deleted since the last time it was opened. They were removed");
               vrtc.getScene().fireChangeEvent(new ActionEvent(this, ViewScene.SCENE_CHANGETOSAVE, "Removing old objects"));
               defaultView.setDirty(false);
           }
       }
       for (Widget node : vrtc.getScene().getNodesLayer().getChildren()){
           ((ObjectNodeWidget)node).getLabelWidget().setFont(vrtc.getCurrentFont());
           ((ObjectNodeWidget)node).getLabelWidget().setForeground(vrtc.getCurrentColor());
       }
       vrtc.getScene().validate();
       vrtc.getScene().repaint();
       vrtc.setDisplayName(myObject.getName() + " ["+myObject.getClassName()+"]");
    }

    private void disableView(){
       vrtc.setDisplayName(null);
       vrtc.setHtmlDisplayName(null);
       vrtc.getScene().clear();
       vrtc.toggleButtons(false);
       vrtc.getScene().validate();
       vrtc.getScene().setCurrentObject(null);
    }

    /**
     * Adds a background (removing the old one if existing) to the view
     */
    public void addBackground() {
        JFileChooser fChooser = new JFileChooser();
        fChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fChooser.setFileFilter(Utils.getImageFileFilter());
        if (fChooser.showOpenDialog(vrtc.getScene().getView()) == JFileChooser.APPROVE_OPTION){
            Image myBackgroundImage;
            try {
                myBackgroundImage = ImageIO.read(new File(fChooser.getSelectedFile().getAbsolutePath()));
                vrtc.getScene().setBackgroundImage(myBackgroundImage);
                vrtc.getScene().fireChangeEvent(new ActionEvent(this, ViewScene.SCENE_CHANGE, "Add Background"));
            } catch (IOException ex) {
                vrtc.getNotifier().showSimplePopup("Image load", NotificationUtil.ERROR, ex.getMessage());
            }
        }
    }  

    /**
     * Saves the view to a XML representation at server side
     */
    public void saveView() {
        byte[] viewStructure = vrtc.getScene().getAsXML();
        if (vrtc.getScene().getCurrentView() == null){
            long viewId = com.createObjectRelatedView(vrtc.getScene().getCurrentObject().getOid(),
                    vrtc.getScene().getCurrentObject().getClassName(), null, null,0, viewStructure, vrtc.getScene().getBackgroundImage());
            if (viewId != -1){ //NOI18N
                vrtc.getScene().setCurrentView(new LocalObjectViewLight(viewId, null, null,0));
                vrtc.setHtmlDisplayName(vrtc.getDisplayName());
            }
            else{
                vrtc.getNotifier().showSimplePopup("Object View", NotificationUtil.ERROR, com.getError());
            }
        }else{
            if (!com.updateObjectRelatedView(vrtc.getScene().getCurrentObject().getOid(),
                     vrtc.getScene().getCurrentObject().getClassName(), vrtc.getScene().getCurrentView().getId(),
                    null, null,viewStructure, vrtc.getScene().getBackgroundImage())) //NOI18N
                vrtc.getNotifier().showSimplePopup("Object View", NotificationUtil.ERROR, com.getError());
            else
                vrtc.setHtmlDisplayName(vrtc.getDisplayName());
        }
    }

    public void refreshView() {
        List<LocalObjectLight> childrenNodes = com.getObjectChildren(vrtc.getScene().getCurrentObject().getOid(),
                com.getMetaForClass(vrtc.getScene().getCurrentObject().getClassName(), false).getOid());
        List<LocalObject> childrenEdges = com.getChildrenOfClass(vrtc.getScene().getCurrentObject().getOid(),
                vrtc.getScene().getCurrentObject().getClassName(),Constants.CLASS_GENERICCONNECTION);

        Collection[] nodesIntersection = Utils.inverseIntersection(childrenNodes, vrtc.getScene().getNodes());
        Collection[] edgesIntersection = Utils.inverseIntersection(childrenEdges, vrtc.getScene().getEdges());
        
        viewBuilder.refreshView((Collection<LocalObjectLight>)nodesIntersection[0], (Collection<LocalObjectLight>)edgesIntersection[0],
                (Collection<LocalObjectLight>)nodesIntersection[1], (Collection<LocalObjectLight>)edgesIntersection[1]);
        vrtc.getScene().validate();
        vrtc.getScene().repaint();
        if (!nodesIntersection[0].isEmpty() || !nodesIntersection[1].isEmpty()
                || !edgesIntersection[0].isEmpty() || !edgesIntersection[1].isEmpty())
            vrtc.getScene().fireChangeEvent(new ActionEvent(this, ViewScene.SCENE_CHANGE, "Refresh result"));
    }
}
