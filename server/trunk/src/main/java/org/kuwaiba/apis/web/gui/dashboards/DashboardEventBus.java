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

package org.kuwaiba.apis.web.gui.dashboards;

import java.util.ArrayList;
import java.util.List;

/**
 * In many cases, dashboard widgets need to exchange information between each other. This class manages the interactions between widgets.
 * With this implementation, the widgets are never aware of the other widgets, they just subscribe to the bus and start sending and receiving events, unlike 
 * the conventional observer pattern approach.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class DashboardEventBus {
    /**
     * The list of widgets that are able to listen for events
     */
    private List<DashboardEventListener> subscribers;

    public DashboardEventBus() {
        subscribers = new ArrayList<>();
    }
    
    /**
     * Adds a subscriber
     * @param subscriber the new subscriber 
     */
    public void addSubscriber(DashboardEventListener subscriber) {
        subscribers.add(subscriber);
    }
    
    /**
     * Removes a subscriber
     * @param subscriber the subscriber to be removed
     */
    public void removeSubscriber(DashboardEventListener subscriber) {
        subscribers.remove(subscriber);
    }
    
    /**
     * Removes all existing subscribers
     */
    public void clearSubscribers() {
        subscribers.clear();
    }
    
    /**
     * Notifies all the subscribers except the source (in case the source is also a subscriber) that an event has been generated
     * @param event 
     */
    public void notifySubscribers(DashboardEventListener.DashboardEvent event) {
        subscribers.forEach((subscriber) -> { 
            if(!subscriber.equals(event.getSource()))
                subscriber.eventReceived(event); 
        });
    }
}
