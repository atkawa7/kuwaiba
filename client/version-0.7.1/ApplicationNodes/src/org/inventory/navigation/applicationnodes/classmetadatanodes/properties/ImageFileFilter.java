/*
 *  Copyright 2010-2015 Neotropic SAS <contact@neotropic.co>
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
 */
package org.inventory.navigation.applicationnodes.classmetadatanodes.properties;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 * Image Filter
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class ImageFileFilter extends FileFilter {

    public final String jpeg = "jpeg";
    public final String jpg = "jpg";
    public final String gif = "gif";
    public final String png = "png";

    public ImageFileFilter() {
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
    
    @Override
    public boolean accept(File f) {
        if (f.isDirectory()) 
            return true;

        String extension = getExtension(f);
        if (extension != null) {
            if(extension.equals(gif) || extension.equals(jpeg) || extension.equals(jpg) ||
                extension.equals(png)) 
                    return true;
            else 
                return false;
        }
        return false;
    }

    @Override
    public String getDescription() {
        return "Image Files";
    }
}
