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

package org.inventory.core.services.utils;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

/**
 * Class with utility methods
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class Utils {
    public static Image getImageFromByteArray(byte[] bytes){
        try {
            InputStream in = new ByteArrayInputStream(bytes);
            BufferedImage bf = ImageIO.read(in);
            return bf;
        } catch (IOException ex) {
            return null;
        }
    }

    public static byte[] getByteArrayFromImage(File f,String format){
        try {
            BufferedImage img = ImageIO.read(f);
            ByteArrayOutputStream bas = new ByteArrayOutputStream();
            ImageIO.write(img, format, bas);
            return bas.toByteArray();
        } catch (IOException ex) {
            return null;
        }
    }
}
