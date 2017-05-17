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
package org.inventory.bookmarks.actions;

/**
 * Action factory for the Bookmark Manager module
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class BookmarksActionFactory {
    /**
     * Singleton for the create bookmark action
     */
    private static NewBookmarkCategoryAction newBookmarkAction;
    /**
     * Singleton for the delete bookmark action
     */
    private static DeleteBookmarkCategoryAction deleteBookmarkAction;
    
    public static NewBookmarkCategoryAction getNewBookmarkAction() {
        if (newBookmarkAction == null)
            newBookmarkAction = NewBookmarkCategoryAction.getInstance();
        return newBookmarkAction;
    }
    
    public static DeleteBookmarkCategoryAction getDeleteBookmarkAction() {
        if (deleteBookmarkAction == null)
            deleteBookmarkAction = DeleteBookmarkCategoryAction.getInstance();
        return deleteBookmarkAction;
    }
}
