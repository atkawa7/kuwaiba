/*
 * Copyright (c) 2016 gir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    gir - initial API and implementation and/or initial documentation
 */
package org.inventory.queries.nodes;

import java.util.Collections;
import org.inventory.communications.core.queries.LocalResultRecord;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author gir
 */
public class QueryResultRootNode extends AbstractNode {
    
    public QueryResultRootNode(Children children) {
        super(children);
    }
    
    public static class QueryResultRootChildren extends Children.Keys<LocalResultRecord> {

        public QueryResultRootChildren(LocalResultRecord[] resultRecords) {
            setKeys(resultRecords);
        }
        
        @Override
        public void removeNotify() {
            setKeys(Collections.EMPTY_SET);
        }
        
        @Override
        protected Node[] createNodes(LocalResultRecord key) {
            return new Node[] { new QueryResultRecordNode(key) };
        }
        
    }
}
