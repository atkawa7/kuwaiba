/*
 *  Copyright 2011 Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>.
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

package utils.tools;

import java.io.ByteArrayOutputStream;
import java.util.Set;
import javax.persistence.metamodel.EntityType;
import utils.BackupProvider;

/**
 * Simple implementation of the BackupProvider interface
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class BackupProviderImpl implements BackupProvider{

    @Override
    public void startBinaryBackup(Set<EntityType> entities, ByteArrayOutputStream outputStream) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void startTextBackup(Set<EntityType> entities, ByteArrayOutputStream outputStream) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
