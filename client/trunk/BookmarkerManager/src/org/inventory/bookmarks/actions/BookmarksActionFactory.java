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
    private static NewBookmarkAction newBookmarkAction;
    /**
     * Singleton for the delete bookmark action
     */
    private static DeleteBookmarkAction deleteBookmarkAction;
    /**
     * Singleton for the add item to bookmarkAction
     */
    private static AddItemToBookmarkAction addItemToBookmarkAction;
    /**
     * Singleton for the delete item from bookmark
     */
    private static DeleteItemFromBookmark deleteItemFromBookmark;
    
    public static NewBookmarkAction getNewBookmarkAction() {
        if (newBookmarkAction == null)
            newBookmarkAction = new NewBookmarkAction();
        return newBookmarkAction;
    }
    
    public static DeleteBookmarkAction getDeleteBookmarkAction() {
        if (deleteBookmarkAction == null)
            deleteBookmarkAction = new DeleteBookmarkAction();
        return deleteBookmarkAction;
    }
    
    public static AddItemToBookmarkAction getAddItemToBookmarkAction() {
        if (addItemToBookmarkAction == null)
            addItemToBookmarkAction = new AddItemToBookmarkAction();
        return addItemToBookmarkAction;
    }
    
    public static DeleteItemFromBookmark getDeleteItemFromBookmark() {
        if (deleteItemFromBookmark == null)
            deleteItemFromBookmark = new DeleteItemFromBookmark();
        return deleteItemFromBookmark;
    }
}
