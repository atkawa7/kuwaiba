/*
 * Copyright (c) 2018 adrian.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    adrian - initial API and implementation and/or initial documentation
 */
package org.inventory.core.visual.decorators;

import java.awt.BasicStroke;

/**
 * A stroke to represent a dot line
 * @author Adrian Martinez <adrian.martinez@neotropic.co>
 */
public class DotLineStroke extends BasicStroke{
 
    public DotLineStroke(int width) {
        super(width, BasicStroke.CAP_BUTT, 
                    BasicStroke.JOIN_BEVEL, 0, new float[] { 4, 5 }, 0);
    }
}
