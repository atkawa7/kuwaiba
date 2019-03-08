/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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

package org.kuwaiba.apis.web.gui.views;

/**
 * A node that represents a comment or a label in a view.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class CommentViewNode extends AbstractViewNode<String>{
    /**
     * A longer text with the body of the comment.
     */
    private String details;
    
    /**
     * Default constructor
     * @param title The title of the comment. This will be used as identifier of the node, so there can't be duplicated titles in comments of a view.
     * @param details A longer text with the body of the comment.
     */
    public CommentViewNode(String title, String details) {
        super(title);
        this.details = details;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
