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
import org.inventory.communications.util.Utils;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.views.objectview.scene.ViewBuilder;
import org.inventory.views.objectview.scene.ViewScene;
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
    private ViewScene scene;

    public ObjectViewService(ViewScene scene, ObjectViewTopComponent vrtc){
        this.vrtc = vrtc;
        this.scene = scene;
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
           if (myObject.equals(scene.getCurrentObject()))
               return;
           
           //Check if the view is still unsaved
           vrtc.checkForUnsavedView(false);

           vrtc.setHtmlDisplayName(null); //Clear the displayname in case it was set to another value

            //We clean the scene...
           scene.clear();

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
       scene.setCurrentObject(myObject);
       List<LocalObjectViewLight> views = com.getObjectRelatedViews(myObject.getOid(),myObject.getClassName());
       
       if(views.isEmpty()){ //There are no saved views
           List<LocalObjectLight> myChildren = com.getObjectChildren(myObject.getOid(), com.getMetaForClass(myObject.getClassName(),false).getOid());
           List<LocalObject> myConnections = com.getChildrenOfClass(myObject.getOid(),myObject.getClassName(), Constants.CLASS_GENERICCONNECTION);
           //TODO: Change for a ViewFactory
           viewBuilder = new ViewBuilder(null, scene);
           viewBuilder.buildDefaultView(myChildren, myConnections);
           scene.setCurrentView(null);
       }else{
           LocalObjectView defaultView = com.getObjectRelatedView(myObject.getOid(),myObject.getClassName(), views.get(0).getId());
           scene.setCurrentView(defaultView);
           viewBuilder = new ViewBuilder(defaultView, scene);
           scene.clear();
           viewBuilder.buildView();
           if (defaultView.isDirty()){
               vrtc.getNotifier().showSimplePopup("Information", NotificationUtil.WARNING_MESSAGE, "Some elements in the view has been deleted since the last time it was opened. They were removed");
               scene.fireChangeEvent(new ActionEvent(this, ViewScene.SCENE_CHANGETOSAVE, "Removing old objects"));
               defaultView.setDirty(false);
           }
       }
       scene.setSceneFont(vrtc.getCurrentFont());
       scene.setSceneForegroundColor(vrtc.getCurrentColor());
       scene.validate();
       vrtc.setDisplayName(myObject.toString());
    }

    public void disableView(){
       vrtc.setDisplayName(null);
       vrtc.setHtmlDisplayName(null);
       scene.clear();
       vrtc.toggleButtons(false);
       scene.setCurrentObject(null);
    }

    /**
     * Adds a background (removing the old one if existing) to the view
     */
    public void addBackground() {
        JFileChooser fChooser = new JFileChooser();
        fChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fChooser.setFileFilter(Utils.getImageFileFilter());
        if (fChooser.showOpenDialog(scene.getView()) == JFileChooser.APPROVE_OPTION){
            Image myBackgroundImage;
            try {
                myBackgroundImage = ImageIO.read(new File(fChooser.getSelectedFile().getAbsolutePath()));
                scene.setBackgroundImage(myBackgroundImage);
                scene.fireChangeEvent(new ActionEvent(this, ViewScene.SCENE_CHANGE, "Add Background"));
            } catch (IOException ex) {
                vrtc.getNotifier().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, ex.getMessage());
            }
        }
    }  

    /**
     * Saves the view to a XML representation at server side
     */
    public void saveView() {
        byte[] viewStructure = scene.getAsXML();
        if (scene.getCurrentView() == null){
            long viewId = com.createObjectRelatedView(scene.getCurrentObject().getOid(),
                    scene.getCurrentObject().getClassName(), null, null,0, viewStructure, scene.getBackgroundImage());
            if (viewId != -1){ //NOI18N
                scene.setCurrentView(new LocalObjectViewLight(viewId, null, null,0));
                vrtc.setHtmlDisplayName(vrtc.getDisplayName());
            }
            else{
                vrtc.getNotifier().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
            }
        }else{
            if (!com.updateObjectRelatedView(scene.getCurrentObject().getOid(),
                     scene.getCurrentObject().getClassName(), scene.getCurrentView().getId(),
                    null, null,viewStructure, scene.getBackgroundImage()))
                vrtc.getNotifier().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
            else
                vrtc.setHtmlDisplayName(vrtc.getDisplayName());
        }
    }

    public void refreshView() {
        List<LocalObjectLight> childrenNodes = com.getObjectChildren(scene.getCurrentObject().getOid(),
                com.getMetaForClass(scene.getCurrentObject().getClassName(), false).getOid());
        List<LocalObject> childrenEdges = com.getChildrenOfClass(scene.getCurrentObject().getOid(),
                scene.getCurrentObject().getClassName(),Constants.CLASS_GENERICCONNECTION);

        Collection[] nodesIntersection = Utils.inverseIntersection(childrenNodes, scene.getNodes());
        Collection[] edgesIntersection = Utils.inverseIntersection(childrenEdges, scene.getEdges());
        
        viewBuilder.refreshView((Collection<LocalObjectLight>)nodesIntersection[0], (Collection<LocalObjectLight>)edgesIntersection[0],
                (Collection<LocalObjectLight>)nodesIntersection[1], (Collection<LocalObjectLight>)edgesIntersection[1]);
        scene.setSceneFont(vrtc.getCurrentFont());
        scene.setSceneForegroundColor(vrtc.getCurrentColor());
        scene.validate();
        if (!nodesIntersection[0].isEmpty() || !nodesIntersection[1].isEmpty()
                || !edgesIntersection[0].isEmpty() || !edgesIntersection[1].isEmpty())
            scene.fireChangeEvent(new ActionEvent(this, ViewScene.SCENE_CHANGE, "Refresh result"));
    }
}
