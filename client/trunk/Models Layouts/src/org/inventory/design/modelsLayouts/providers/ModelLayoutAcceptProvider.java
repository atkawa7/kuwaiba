/**
 *  Copyright 2010-2017, Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.inventory.design.modelsLayouts.providers;

import java.awt.Point;
import java.awt.datatransfer.Transferable;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.inventory.design.modelsLayouts.model.LabelShape;
import org.inventory.design.modelsLayouts.model.RectangleShape;
import org.inventory.design.modelsLayouts.model.Shape;
import org.inventory.design.modelsLayouts.scene.ModelLayoutScene;
import org.inventory.design.modelsLayouts.scene.widgets.LabelShapeWidget;
import org.inventory.design.modelsLayouts.scene.widgets.RectangleShapeWidget;
import org.netbeans.api.visual.action.AcceptProvider;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.widget.Widget;

/**
 * Drop. Accept provider to add shapes from palette
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ModelLayoutAcceptProvider implements AcceptProvider {

    @Override
    public ConnectorState isAcceptable(Widget widget, Point point, Transferable t) {
        if (!t.isDataFlavorSupported(Shape.DATA_FLAVOR))
            return ConnectorState.REJECT;
        return ConnectorState.ACCEPT;
    }

    @Override
    public void accept(Widget widget, Point point, Transferable t) { 
        JTextField txtName = new JTextField(20);
        txtName.setName("txtName");
        JCheckBox chkEquipment = new JCheckBox();
        chkEquipment.setName("chkEquipment");
        
        JComplexDialogPanel pnlShape = new JComplexDialogPanel(
            new String[] {"Shape name", "Is the shape an equipment?"}, 
            new JComponent[] {txtName, chkEquipment});
        if (JOptionPane.showConfirmDialog(null, pnlShape, "New Shape", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            
            String shapeName = txtName.getText();
            Boolean isEquipment = chkEquipment.isSelected();
            
            ModelLayoutScene scene ;
            if (widget.getScene() instanceof ModelLayoutScene) {
                scene = (ModelLayoutScene) widget.getScene();
                
                for (Shape node : scene.getNodes()) {
                    if (!shapeName.isEmpty() && shapeName.equals(node.getName())) {
                        NotificationUtil.getInstance().showSimplePopup("Warning", 
                            NotificationUtil.WARNING_MESSAGE, "There cannot be two figures with the same name");
                    }
                }
                try {  
                    Widget newWidget = null;
                    Shape shape = (Shape) t.getTransferData(Shape.DATA_FLAVOR);

                    Shape newShape = null;
                    if (shape instanceof LabelShape)
                        newShape = new LabelShape();
                    else
                        newShape = new RectangleShape();
                    newShape.setName(shapeName);
                    newShape.setIsEquipment(isEquipment);

                    if (widget instanceof ModelLayoutScene) {
                        newShape.setParent(null);
                        newWidget = scene.addNode(newShape);
                    }
                    else {
                        Object parent = scene.findObject(widget);

                        if (parent != null && parent instanceof Shape) {
                            newShape.setParent((Shape) parent);
                            newWidget = scene.addNode(newShape);
                        }
                    }
                    if (newWidget != null) {
                        newWidget.setPreferredLocation(point);
                        scene.repaint();

                        newShape.setX(point.x);
                        newShape.setY(point.y);
                        if (newShape instanceof LabelShape)
                            ((LabelShapeWidget) newWidget).fixLookup();
                        if (newShape instanceof RectangleShape)
                            ((RectangleShapeWidget) newWidget).fixLookup();
                    }
                } catch (Exception ex) {            
                }
            }
        }
    }
}
