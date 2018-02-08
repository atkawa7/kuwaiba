/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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

package org.inventory.core.visual.export.filters;

import java.io.File;
import java.io.IOException;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.inventory.core.visual.scene.AbstractScene;
import org.netbeans.api.visual.export.SceneExporter;

/**
 * Exports to PNG/JPG
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ImageFilter extends SceneExportFilter {

    //Singleton
    private static ImageFilter self;
    private JComplexDialogPanel myPanel;

    public static ImageFilter getInstance(){
        if (self == null)
            self = new ImageFilter();
        return self;
    }
    
    @Override
    public String getDisplayName() {
        return "Image - PNG/JPG";
    }

    @Override
    public String getExtension() {
        if (myPanel != null){
            if ((SceneExporter.ImageType)((JComboBox)myPanel.getComponent("cmbFormat")).
                    getSelectedItem() == SceneExporter.ImageType.JPG)
                return ".jpg";
        }
        return ".png"; //NOI18N
    }

    @Override
    public void export(AbstractScene exportable, String fileName) throws IOException {
        SceneExporter.createImage(exportable,
                new File(fileName),
                myPanel == null ? SceneExporter.ImageType.PNG : (SceneExporter.ImageType)((JComboBox)myPanel.getComponent("cmbFormat")).getSelectedItem(),
                myPanel == null ? SceneExporter.ZoomType.ACTUAL_SIZE : (SceneExporter.ZoomType)((JComboBox)myPanel.getComponent("cmbZoom")).getSelectedItem(),
                myPanel == null ? false : (Boolean)((JComboBox)myPanel.getComponent("cmbVisibleAreaOnly")).getSelectedItem(),
                false,
                100,
                0,  //Not used
                0); //Not used
        JOptionPane.showMessageDialog(null, I18N.gm("view_export_successfully"), I18N.gm("success"), JOptionPane.INFORMATION_MESSAGE);
    }
    
    @Override
    public JPanel getExportSettingsPanel() {
        if (myPanel == null){
            JComboBox<SceneExporter.ImageType> cmbFormat = new JComboBox<>(new SceneExporter.ImageType[]{SceneExporter.ImageType.PNG, SceneExporter.ImageType.JPG});
            cmbFormat.setName("cmbFormat");
            
            JComboBox<SceneExporter.ZoomType> cmbZoom = new JComboBox<>(new SceneExporter.ZoomType[]{SceneExporter.ZoomType.ACTUAL_SIZE, SceneExporter.ZoomType.CURRENT_ZOOM_LEVEL});
            cmbZoom.setName("cmbZoom");
            
            JComboBox<Boolean> cmbVisibleAreaOnly = new JComboBox<>(new Boolean[]{false, true});
            cmbVisibleAreaOnly.setName("cmbVisibleAreaOnly");
            
            myPanel = new JComplexDialogPanel(new String[]{I18N.gm("format"), I18N.gm("zoom"), I18N.gm("visible_read_only")}, 
                    new JComponent[]{cmbFormat, cmbZoom, cmbVisibleAreaOnly});
        }
        return myPanel;
    }
}