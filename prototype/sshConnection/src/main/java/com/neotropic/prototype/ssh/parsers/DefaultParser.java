/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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
package com.neotropic.prototype.ssh.parsers;

import com.neotropic.prototype.ssh.entities.DefaultEntity;
import java.util.ArrayList;
import java.util.List;

/**
 * Default parser to process the data got it from ssh
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class DefaultParser {
    
    public void parse(String input){
        String[] lines = input.split("\n");
        ParsingState state = ParsingState.START;

        List<DefaultEntity> linesEntities = new ArrayList<>();
        
        for (String line : lines) {
            state = ParsingState.PROCESSING;
            linesEntities.add(new DefaultEntity(line));
        }        
        state = ParsingState.END;
        System.out.println("Number of lines proceesed: " + linesEntities.size());
        for(int i = 0; i< linesEntities.size(); i++)
            System.out.println(linesEntities.get(i).getLine() + " - line " + i + ") ");
    }
    
    /**
     * The possible states of the parsing process
     */
    private enum ParsingState {
        /**
         * The default state
         */
        START, 
        /**
         * Processing the input section
         */
        PROCESSING,
        /**
         * After the empty line after listing all the lines of the input
         */
        END
    }
}
