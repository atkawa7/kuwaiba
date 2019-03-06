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
package com.neotropic.prototype.ssh;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.neotropic.prototype.ssh.parsers.DefaultParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Main class to launch the test connector of SSH with JSCH library
 *  @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class TestJschConnector {
    
    /**
     * The ssh user
     */
    private static final String USER = "";
    /**
     * The ssh pasword
     */
    private static final String PWD = "";
    /**
     * Default host ipAddr
     */
    private static final String IPADRR = "127.0.0.1";
    /**
     * Default port for ssh
     */
    private static final int PORT = 22;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
       
        TestJschConnector x = new  TestJschConnector();
        x.connect();
    }

    private void connect(){
        JSch sshShell = new JSch();
        Session session = null;
        ChannelExec channel =  null;
        try {
            session = sshShell.getSession(USER, IPADRR, PORT);
            session.setPassword(PWD);
            //Enable to -not recommended- disable host key checking
            
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(10000); //Connection timeout
            channel = (ChannelExec) session.openChannel("exec");

            channel.setCommand("sh script_test"); //NOI18N
            channel.connect();
            DefaultParser defaultParser = new DefaultParser();
            defaultParser.parse(readCommandExecutionResult(channel));   
            
        } catch (JSchException ex) {
                Logger.getLogger(TestJschConnector.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (session != null)
                session.disconnect();
            if (channel != null)
                channel.disconnect();
        }
    
    }
    
    /**
     * Reads the channel's input stream into a string.
     * @param channel The session's channel.
     * @return The string with the result of the command execution.
     * @throws InvalidArgumentException if there was an error executing the command or reading its result.
     */
    private String readCommandExecutionResult (ChannelExec channel) {
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(channel.getInputStream()))) {
            String result = buffer.lines().collect(Collectors.joining("\n"));
            return channel.getExitStatus() == 0 ? result : null;
        } catch (IOException ex) {
            Logger.getLogger(TestJschConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
