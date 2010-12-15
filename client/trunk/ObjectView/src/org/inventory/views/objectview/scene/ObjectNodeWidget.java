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

package org.inventory.views.objectview.scene;

import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.interfaces.LocalObjectLight;
import org.inventory.communications.core.views.LocalNode;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.widget.general.IconNodeWidget;
import org.openide.util.ImageUtilities;

/**
 * This widget represents a node (as in the navigation tree)
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class ObjectNodeWidget extends IconNodeWidget implements ActionListener{
    private LocalObjectLight object;

    public ObjectNodeWidget(ViewScene scene, LocalNode node){
        super(scene);
        this.object = node.getObject();
        setLabel(node.getObject().getDisplayname());
        getLabelWidget().setFont(new Font(Font.SANS_SERIF,Font.BOLD,12));
        Image myIcon = CommunicationsStub.getInstance().getMetaForClass(node.getObject().getClassName(), false).getIcon();
        if(myIcon == null)
            myIcon = ImageUtilities.loadImage("org/inventory/views/objectview/res/default_32.png");
        setImage(myIcon);
        getActions().addAction(scene.createSelectAction());
        scene.getMoveAction().addActionListener(this);
        getActions().addAction(scene.getMoveAction());
        createActions(ViewScene.ACTION_CONNECT).addAction(ActionFactory.createConnectAction(scene.getEdgesLayer(), scene.getConnectionProvider()));
        getActions().addAction(ActionFactory.createInplaceEditorAction(new LabelInplaceTextEditor()));
    }

    /**
     * Returns the wrapped business object
     * @return
     */
    public LocalObjectLight getObject(){
        return this.object;
    }

    public void actionPerformed(ActionEvent e) {
        ((ViewScene)getScene()).fireChangeEvent(e);
    }
}