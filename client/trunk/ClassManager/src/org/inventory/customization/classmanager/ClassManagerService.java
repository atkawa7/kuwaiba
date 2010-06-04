/*
 *  Copyright 2010 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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

package org.inventory.customization.classmanager;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.filechooser.FileFilter;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.interfaces.LocalClassMetadataLight;
import org.inventory.core.services.interfaces.NotificationUtil;

/**
 * This class provides the business logic to the associated TopComponent
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class ClassManagerService extends FileFilter{

    public final String jpeg = "jpeg";
    public final String jpg = "jpg";
    public final String gif = "gif";
    public final String png = "png";

    private ClassManagerTopComponent cmstc;

    ClassManagerService(ClassManagerTopComponent _cmstc) {
        this.cmstc = _cmstc;
    }

    public String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }

    public byte[] getByteArrayFromImage(File f){
        try {
            BufferedImage img = ImageIO.read(f);
            ByteArrayOutputStream bas = new ByteArrayOutputStream();
            ImageIO.write(img, "image", bas);
            return bas.toByteArray();
        } catch (IOException ex) {
            return null;
        }
    }

    @Override
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        String extension = getExtension(f);
        if (extension != null) {
                if(extension.equals(gif) || extension.equals(jpeg) || extension.equals(jpg) ||
                extension.equals(png)) {
                    return true;
            } else {
                return false;
            }
        }
        return false;
    }

    @Override
    public String getDescription() {
        return "Image Files";
    }

    void saveProperties(LocalClassMetadataLight modifiedClass, String displayName,
            String description, byte[] smallIcon, byte[] icon) {
        CommunicationsStub com = CommunicationsStub.getInstance();
        if (!displayName.equals(""))
            com.setClassPlainAttribute(modifiedClass.getId(), "displayName", displayName);
        if (!description.equals(""))
            com.setClassPlainAttribute(modifiedClass.getId(), "description", description);
        if (smallIcon != null)
            com.setClassIcon(modifiedClass.getId(), "smallIcon", smallIcon);
        if (icon != null)
            com.setClassIcon(modifiedClass.getId(), "icon", icon);
    }

    List<LocalClassMetadataLight> getAllMeta() {
        List<LocalClassMetadataLight> res = new ArrayList<LocalClassMetadataLight>();
        LocalClassMetadataLight[] allMeta = CommunicationsStub.getInstance().getAllLightMeta();
        if (allMeta == null){
            cmstc.getNotifier().showSimplePopup("Class List", NotificationUtil.ERROR, CommunicationsStub.getInstance().getError());
            return res;
        }

        for (LocalClassMetadataLight myLight : allMeta)
            if (!(myLight.getIsAbstract() || myLight.getClassName().equals(CommunicationsStub.getInstance().getRootClass())))
                res.add(myLight);
        return res;
    }
}
