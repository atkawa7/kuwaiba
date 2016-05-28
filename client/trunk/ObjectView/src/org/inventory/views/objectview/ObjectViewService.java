/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.inventory.views.objectview;

import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.visual.scene.PhysicalConnectionProvider;
import org.inventory.views.objectview.scene.AbstractViewBuilder;
import org.inventory.views.objectview.scene.ChildrenViewBuilder;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;

/**
 * Implements the logic necessary to control what's shown in the associated TC
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ObjectViewService implements LookupListener {
    private ObjectViewTopComponent vrtc;
    private Lookup.Result<LocalObjectLight> selectedNodes;
    private CommunicationsStub com;
    private AbstractViewBuilder viewBuilder;
    private LocalObjectLight currentObject;

    public ObjectViewService(ObjectViewTopComponent vrtc) {
        this.vrtc = vrtc;
        this.com = CommunicationsStub.getInstance();
        viewBuilder = new ChildrenViewBuilder(this);
    }
    
    /**
     * Add this instance as listener for the selected nodes in the NavigationTree.
     * Should be called when the TopComponent is opened
     */
    public void initializeListeners(){
        selectedNodes = Utilities.actionsGlobalContext().lookupResult(LocalObjectLight.class);
        selectedNodes.addLookupListener(this);
        if (selectedNodes.allInstances().size() == 1) //There's a node already selected
            resultChanged(new LookupEvent(selectedNodes));
    }

    /**
     * Removes this instance as listener for the selected nodes in the NavigationTree.
     * Should be called when the TopComponent is closed
     */
    public void terminateListeners(){
        selectedNodes.removeLookupListener(this);
        viewBuilder.getScene().removeAllListeners();
    }  

    @Override
    public void resultChanged(LookupEvent ev) {
        Lookup.Result lookupResult = (Lookup.Result)ev.getSource();
        if(lookupResult.allInstances().size() == 1){

           //Don't update if the same object is selected
           LocalObjectLight myObject = (LocalObjectLight)lookupResult.allInstances().iterator().next();
           if (myObject.equals(currentObject))
               return;
           
           //Check if the view is still unsaved
           vrtc.checkForUnsavedView(false);
           
           setCurrentObject(myObject);

           vrtc.setHtmlDisplayName(null); //Clear the displayname in case it was set to another value

            //We clean the scene...
           viewBuilder.getScene().clear();
           
           if(!com.getMetaForClass(myObject.getClassName(), false).isViewable()) {
                vrtc.getNotifier().showStatusMessage("This object doesn't have any view", false);
                disableView();
                return;
           }
           
           //If the current view type does not support the selected object, fallback to the default view
           if (!viewBuilder.supportsClass(myObject.getClassName())){
               vrtc.getNotifier().showSimplePopup("Warning", NotificationUtil.WARNING_MESSAGE, 
                    String.format("Class %s does not support %s", currentObject.getClassName(), viewBuilder.getName()));
               vrtc.selectView (0);
           }
           else{
                try{
                    viewBuilder.buildView(myObject);
                }catch (IllegalArgumentException ex){
                    vrtc.getNotifier().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, ex.getMessage());
                    viewBuilder.getScene().clear();
                    disableView();
                    return;
                }
                vrtc.setDisplayName(myObject.toString());
                viewBuilder.getScene().setSceneFont(vrtc.getCurrentFont());
                viewBuilder.getScene().setSceneForegroundColor(vrtc.getCurrentColor());
                viewBuilder.getScene().validate();
                vrtc.toggleButtons(true);
                
           }
        }
    }
    
    public void disableView(){
       vrtc.setDisplayName(null);
       vrtc.setHtmlDisplayName(null);
       viewBuilder.getScene().clear();
       vrtc.toggleButtons(false);
       setCurrentObject(null);
    }

    public LocalObjectLight getCurrentObject() {
        return currentObject;
    }
    
    private void setCurrentObject(LocalObjectLight currentObject) {
        this.currentObject = currentObject;
        if (viewBuilder.getScene().supportsConnections())
            ((PhysicalConnectionProvider)viewBuilder.getScene().getConnectProvider()).setCurrentParentObject(currentObject);
    }

    public ObjectViewTopComponent getComponent(){
        return vrtc;
    }

    public AbstractViewBuilder getViewBuilder() {
        return viewBuilder;
    }

    public void setViewBuilder(AbstractViewBuilder viewBuilder) {
        //If the current view type does not support the selected object, fallback to the default view
        if (!viewBuilder.supportsClass(currentObject.getClassName())){
            vrtc.getNotifier().showSimplePopup("Warning", NotificationUtil.WARNING_MESSAGE, 
                    String.format("Class %s does not support %s", currentObject.getClassName(), viewBuilder.getName()));
            vrtc.selectView(0);
        }else{ 
            this.viewBuilder = viewBuilder;
            try{
                viewBuilder.buildView(currentObject);
            }catch (IllegalArgumentException ex){
                vrtc.getNotifier().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, ex.getMessage());
                viewBuilder.getScene().clear();
                disableView();
            }
        }
    }
}