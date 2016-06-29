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

import java.lang.reflect.InvocationTargetException;
import org.inventory.communications.core.queries.LocalResultRecord;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;

/**
 *
 * @author gir
 */
public class QueryResultRecordNode extends AbstractNode {
    /**
     * A single query result record
     */
    private LocalResultRecord record;
    
    public QueryResultRecordNode(LocalResultRecord record) {
        super(Children.LEAF);
        this.record = record;
    }
    
    @Override
    public String getDisplayName() {
        return record.getObject() == null ? "asddsada" : record.getObject().toString();
    }

    @Override
    protected Sheet createSheet() {
        Sheet aSheet = Sheet.createDefault();
        Sheet.Set properties = Sheet.createPropertiesSet();
        int i = 0;
        for (final String column : record.getExtraColumns()) {
            PropertySupport.ReadOnly<String> aTableColumn = new PropertySupport.ReadOnly<String>(String.valueOf(i), String.class, String.valueOf(i), String.valueOf(i)) {
                
                @Override
                public String getValue() throws IllegalAccessException, InvocationTargetException {
                    return column;
                }
            };
            properties.put(aTableColumn);
            i ++;
        }
        aSheet.put(properties);
        return aSheet;
    }
    
    
}
