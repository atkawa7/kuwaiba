/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.neotropic.kuwaiba.modules.core.search;

import com.vaadin.flow.component.Component;

/**
 * Functional interface intended to be used to create the content that will be placed in the page when a search result 
 * is clicked.
 * @param <T> The type of the search result.
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public interface SearchResultCallback <T> {
    //TODO relocate this in integrate with the one in the service module
    /**
     * Given a search result, builds content to be displayed in the page.
     * @param searchResult The search result to be expanded.
     * @return The visual component that will show the detailed information about the search result.
     */
    public Component buildSearchResultDetailsPage(T searchResult);
}
