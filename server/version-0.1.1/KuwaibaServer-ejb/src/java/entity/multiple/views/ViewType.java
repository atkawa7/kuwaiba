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

package entity.multiple.views;

import core.annotations.Administrative;
import entity.core.AdministrativeItem;
import java.io.Serializable;
import javax.persistence.Entity;

/**
 * Instances of this class are the different types of view available for a given class.
 * The default view is a  Explorer View
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
@Administrative
public class ViewType extends AdministrativeItem implements Serializable {
    protected String filter; //This one tells what elements should be included into the view
}
