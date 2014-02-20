/*
 * Copyright (c) 2014 adrian.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    adrian - initial API and implementation and/or initial documentation
 */
package org.kuwaiba.apis.persistence.exceptions;

import java.util.logging.Level;

/**
 *
 * @author adrian
 */
public class NotAuthorizedException extends InventoryException{
    
    public NotAuthorizedException(String methodName, String userName) {
        super(userName+java.util.ResourceBundle.
                getBundle("org/kuwaiba/Bundle").getString("LBL_NOTALLOWED")+methodName, Level.INFO);
    }
}
