/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.createdefaultdb.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.Label;

/**
 * Main process to create a basic database, if database exist only added new
 * classes.
 *
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
public class DbManagement {

    private Index<Node> specialNodes;
    private final String adminPassword;
    private GraphDatabaseService graphDb;
    private final Label specialNodeLabel;
    private final boolean connectionStatus;

    /**
     * Default constructor
     *
     * @param databaseDirectory
     * @param adminPassword
     */
    public DbManagement(String databaseDirectory, String adminPassword) {
        this.specialNodeLabel = Label.label(DataBaseContants.INDEX_SPECIAL_NODES.getValue());
        this.connectionStatus = openConnection(databaseDirectory);
        this.adminPassword = adminPassword;
    }

    /**
     *
     * @param dbPathString
     * @return
     */
    private boolean openConnection(String dbPathString) {
        try {

            //set database path
            File dbFile = new File(dbPathString);
            graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(dbFile);

            if (dbFile.exists() && dbFile.canWrite() && graphDb != null) {
                System.out.println("Data base Connected");
                registerShutdownHook(graphDb);

                return true;
            } else if (!dbFile.exists() || !dbFile.canWrite() && graphDb == null) {
                System.out.println("Path " + dbFile.getAbsolutePath() + " does not exist or is not writeable");
                return false;
            }

        } catch (Exception e) {
            System.out.println("Error opening conection: " + e.getMessage());
        }
        return false;
    }

    /**
     * create or update neo4j database
     */
    public void createdefaultData() {
        System.out.println("Creating Special nodes ...");
        createSpecialNodes();

        System.out.println("\nCreating Dummy Root ...");
        createDummyRoot();

        System.out.println("\nCreating Groups Root Node ...");
        Node groups = createGroupsRootNode();

        System.out.println("\nCreating Activity Log Root Nodes ...");
        createActivityLogRootNodes();

        System.out.println("\nCreating Privilege Root Node ...");
        createPrivilegeRootNode();

        //create default group
        //create default user admin
        //add admin to group
        if (groups != null) {
            System.out.println("\nCreating default user");
            long userID = createUser(DataBaseContants.DEFAULT_ADMIN.getValue(),
                    this.adminPassword,
                    DataBaseContants.DEFAULT_ADMIN_FIRSTNAME.getValue(),
                    DataBaseContants.DEFAULT_ADMIN_LASTNAME.getValue(),
                    DataBaseContants.DEFAULT_ADMIN_ENABLED.getBooleanValue(),
                    DataBaseContants.USER_TYPE_GUI.getIntValue(),
                    new ArrayList<Privilege>(),
                    groups
            );
            System.out.println("User admin with id " + userID + " updated");
        }
    }

    /**
     * Registers a shutdown hook for the Neo4j instance so that it shuts down
     * nicely when the VM exits (even if you "Ctrl-C" the running application).
     *
     * @param graphDb
     */
    private static void registerShutdownHook(final GraphDatabaseService graphDb) {

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                graphDb.shutdown();
            }
        });
    }

    /**
     * Create or update Special Nodes
     */
    private void createSpecialNodes() {
        if (connectionStatus) {
            try (Transaction tx = graphDb.beginTx()) {

                specialNodes = graphDb.index().forNodes(DataBaseContants.INDEX_SPECIAL_NODES.getValue());

                tx.success();
            } catch (Exception ex) {
                System.out.println("Error create Special Nodes: " + ex);
            }

        } else {
            System.out.println("You must connect first");
        }
    }

    /**
     * Create or update Dummy Root Node
     */
    private void createDummyRoot() {
        if (connectionStatus) {
            try (Transaction tx = graphDb.beginTx()) {

                Node dummyRootNode = specialNodes.get(DataBaseContants.PROPERTY_NAME.getValue(), DataBaseContants.NODE_DUMMYROOT.getValue()).getSingle();

                if (dummyRootNode == null) {
                    Label label = Label.label(DataBaseContants.LABEL_ROOT.getValue());
                    dummyRootNode = graphDb.createNode(specialNodeLabel, label);

                    //set properties
                    dummyRootNode.setProperty(DataBaseContants.PROPERTY_NAME.getValue(), DataBaseContants.NODE_DUMMYROOT.getValue());
                    dummyRootNode.setProperty(DataBaseContants.PROPERTY_DISPLAY_NAME.getValue(), DataBaseContants.NODE_DUMMYROOT.getValue());
                    dummyRootNode.setProperty(DataBaseContants.PROPERTY_CREATION_DATE.getValue(), Calendar.getInstance().getTimeInMillis());

                    graphDb.index().forNodes(DataBaseContants.INDEX_SPECIAL_NODES.getValue()).putIfAbsent(dummyRootNode, DataBaseContants.PROPERTY_NAME.getValue(), DataBaseContants.NODE_DUMMYROOT.getValue());

                    tx.success();
                    System.out.println("Dummy Root created");
                } else {
                    System.out.println("Dummy root found ");
                }
            } catch (Exception ex) {
                System.out.println("Error create Dummy root: " + ex.getMessage()); //NOI18N
            }
        } else {
            System.out.println("You must connect first");
        }
    }

    /**
     * Create or update Groups Root Node
     *
     * @return
     */
    private Node createGroupsRootNode() {

        if (connectionStatus) {
            try (Transaction tx = graphDb.beginTx()) {

                Node groupRootNode = specialNodes.get(DataBaseContants.PROPERTY_NAME.getValue(), DataBaseContants.NODE_GROUPS.getValue()).getSingle();

                if (groupRootNode == null) {
                    Label label = Label.label(DataBaseContants.LABEL_ROOT.getValue());
                    groupRootNode = graphDb.createNode(specialNodeLabel, label);

                    //set properties
                    groupRootNode.setProperty(DataBaseContants.PROPERTY_NAME.getValue(), DataBaseContants.NODE_GROUPS.getValue());
                    groupRootNode.setProperty(DataBaseContants.PROPERTY_CREATION_DATE.getValue(), Calendar.getInstance().getTimeInMillis());

                    graphDb.index().forNodes(DataBaseContants.INDEX_SPECIAL_NODES.getValue()).putIfAbsent(groupRootNode, DataBaseContants.PROPERTY_NAME.getValue(), DataBaseContants.NODE_GROUPS.getValue());
                    tx.success();
                    System.out.println("Groups Root Node created");
                } else {
                    System.out.println("Groups Root Node found ");
                }
                return groupRootNode;
            } catch (Exception ex) {
                System.out.println("Error create Groups Root Node: " + ex.getMessage()); //NOI18N
            }
            return null;
        } else {
            System.out.println("You must connect first");
        }
        return null;
    }

    /**
     * Create or update Activity and Object Log Root nodes
     */
    private void createActivityLogRootNodes() {

        if (connectionStatus) {
            try (Transaction tx = graphDb.beginTx()) {

                Node generalActivityRootNode = specialNodes.get(DataBaseContants.PROPERTY_NAME.getValue(), DataBaseContants.NODE_GENERAL_ACTIVITY_LOG.getValue()).getSingle();
                Node objectActivityRootNode = specialNodes.get(DataBaseContants.PROPERTY_NAME.getValue(), DataBaseContants.NODE_OBJECT_ACTIVITY_LOG.getValue()).getSingle();

                Label label = Label.label(DataBaseContants.LABEL_ROOT.getValue());
                if (generalActivityRootNode == null) {
                    generalActivityRootNode = graphDb.createNode(specialNodeLabel, label);

                    //set properties
                    generalActivityRootNode.setProperty(DataBaseContants.PROPERTY_NAME.getValue(), DataBaseContants.NODE_GENERAL_ACTIVITY_LOG.getValue());
                    generalActivityRootNode.setProperty(DataBaseContants.PROPERTY_CREATION_DATE.getValue(), Calendar.getInstance().getTimeInMillis());
                    graphDb.index().forNodes(DataBaseContants.INDEX_SPECIAL_NODES.getValue()).putIfAbsent(generalActivityRootNode, DataBaseContants.PROPERTY_NAME.getValue(), DataBaseContants.NODE_GENERAL_ACTIVITY_LOG.getValue());
                } else {
                    System.out.println("Object Activity Log node found ");
                }

                if (objectActivityRootNode == null) {
                    objectActivityRootNode = graphDb.createNode(specialNodeLabel, label);

                    //set properties
                    objectActivityRootNode.setProperty(DataBaseContants.PROPERTY_NAME.getValue(), DataBaseContants.NODE_OBJECT_ACTIVITY_LOG.getValue());
                    objectActivityRootNode.setProperty(DataBaseContants.PROPERTY_CREATION_DATE.getValue(), Calendar.getInstance().getTimeInMillis());
                    graphDb.index().forNodes(DataBaseContants.INDEX_SPECIAL_NODES.getValue()).putIfAbsent(objectActivityRootNode, DataBaseContants.PROPERTY_NAME.getValue(), DataBaseContants.NODE_OBJECT_ACTIVITY_LOG.getValue());
                } else {
                    System.out.println("Activity Log Root Node found ");
                }

                tx.success();
                System.out.println("Activity and Object Log Root Nodes created"); //NOI18N
            } catch (Exception ex) {
                System.out.println("Error create Activity and Object Log Root Nodes: " + ex.getMessage()); //NOI18N
            }
        } else {
            System.out.println("You must connect first");
        }
    }

    /**
     * Create or update Root nodes
     */
    private void createPrivilegeRootNode() {

        if (connectionStatus) {
            try (Transaction tx = graphDb.beginTx()) {

                Node privilegeRootNode = specialNodes.get(DataBaseContants.PROPERTY_NAME.getValue(), DataBaseContants.NODE_PRIVILEGES.getValue()).getSingle();

                if (privilegeRootNode == null) {
                    Label label = Label.label(DataBaseContants.LABEL_ROOT.getValue());
                    privilegeRootNode = graphDb.createNode(label);
                    privilegeRootNode.setProperty(DataBaseContants.PROPERTY_NAME.getValue(), DataBaseContants.NODE_PRIVILEGES.getValue());
                    privilegeRootNode.setProperty(DataBaseContants.PROPERTY_CREATION_DATE.getValue(), Calendar.getInstance().getTimeInMillis());
                    graphDb.index().forNodes(DataBaseContants.INDEX_SPECIAL_NODES.getValue()).putIfAbsent(privilegeRootNode, DataBaseContants.PROPERTY_NAME.getValue(), DataBaseContants.NODE_PRIVILEGES.getValue());
                    tx.success();
                    System.out.println("Privilige Root Node created");
                } else {
                    System.out.println("Privilige Root Node found ");
                }

            } catch (Exception ex) {
                System.out.println("Error create Privilige Root Node: " + ex.getMessage()); //NOI18N
            }
        } else {
            System.out.println("You must connect first");
        }
    }

    /**
     * Create or update user 'admin'
     *
     * @param userName
     * @param password
     * @param firstName
     * @param lastName
     * @param enabled
     * @param type
     * @param privileges
     * @param defaultGroup
     * @return
     */
    public long createUser(String userName,
            String password,
            String firstName,
            String lastName,
            boolean enabled,
            int type,
            List<Privilege> privileges,
            Node defaultGroup
    ) {
        long storedUserID = 0;
        boolean validData = true;

        if (userName == null) {
            System.out.println("Error User name can not be null");
            validData = false;
        }

        if (userName.trim().isEmpty()) {
            System.out.println("Error User name can not be an empty string");
            validData = false;
        }

        if (!userName.matches("^[a-zA-Z0-9_.]*$")) {
            System.out.println("Error User name " + userName + " contains invalid characters");
            validData = false;
        }

        if (password == null) {
            System.out.println("Error Password can not be null");
            validData = false;
        }

        if (password.trim().isEmpty()) {
            System.out.println("Error Password can not be an empty string");
            validData = false;
        }

        if (defaultGroup == null) {
            System.out.println("Error Group can not be null");
            validData = false;
        }

        if (type != DataBaseContants.USER_TYPE_GUI.getIntValue() && type != DataBaseContants.USER_TYPE_WEB_SERVICE.getIntValue() && type != DataBaseContants.USER_TYPE_SOUTHBOUND.getIntValue()) {
            System.out.println("Error Invalid user type");
            validData = false;
        }
        if (validData) {
            try (Transaction tx = graphDb.beginTx()) {
                Index<Node> userIndex = graphDb.index().forNodes(DataBaseContants.INDEX_USERS.getValue());
                Index<Node> groupIndex = graphDb.index().forNodes(DataBaseContants.INDEX_GROUPS.getValue());

                Node storedUser = userIndex.get(DataBaseContants.PROPERTY_NAME.getValue(), userName).getSingle();

                if (storedUser != null) {
                    System.out.println("User name " + userName + " already exists");
                    storedUser.setProperty(DataBaseContants.PROPERTY_PASSWORD.getValue(), BCrypt.hashpw(password, BCrypt.gensalt()));
                    System.out.println("User password update");
                    storedUserID = storedUser.getId();
                } else {

                    Label label = Label.label(DataBaseContants.INDEX_USERS.getValue());
                    Node newUserNode = graphDb.createNode(label);

                    newUserNode.setProperty(DataBaseContants.PROPERTY_CREATION_DATE.getValue(), Calendar.getInstance().getTimeInMillis());
                    newUserNode.setProperty(DataBaseContants.PROPERTY_NAME.getValue(), userName);
                    newUserNode.setProperty(DataBaseContants.PROPERTY_PASSWORD.getValue(), BCrypt.hashpw(password, BCrypt.gensalt()));
                    newUserNode.setProperty(DataBaseContants.PROPERTY_FIRST_NAME.getValue(), firstName == null ? "" : firstName);
                    newUserNode.setProperty(DataBaseContants.PROPERTY_LAST_NAME.getValue(), lastName == null ? "" : lastName);
                    newUserNode.setProperty(DataBaseContants.PROPERTY_TYPE.getValue(), type);
                    newUserNode.setProperty(DataBaseContants.PROPERTY_ENABLED.getValue(), enabled);

                    Node defaultGroupNode = groupIndex.get(DataBaseContants.PROPERTY_ID.getValue(), defaultGroup).getSingle();

                    if (defaultGroupNode != null) {
                        System.out.println("default group found");

                        //add user to node
                        newUserNode.createRelationshipTo(defaultGroupNode, RelTypes.BELONGS_TO_GROUP);
                        System.out.println("Add user to default group");

                    } else {
                        System.out.println("Creating default group ...");
                        Label defaultGrouplabel = Label.label(DataBaseContants.DEFAULT_GROUP_NAME.getValue());
                        defaultGroupNode = graphDb.createNode(defaultGrouplabel);
                        defaultGroupNode.setProperty(DataBaseContants.GROUP_PROPERTY_DESCRIPTION.getValue(), DataBaseContants.DEFAULT_GROUP_DESCRIPTION.getValue());

                        //add user to node                        
                        newUserNode.createRelationshipTo(defaultGroupNode, RelTypes.BELONGS_TO_GROUP);
                        defaultGroup.createRelationshipTo(defaultGroupNode, RelTypes.GROUP);
                        System.out.println("Add user to default group");
                    }

                    if (privileges != null) {
                        for (Privilege privilege : privileges) {
                            Node privilegeNode = graphDb.createNode();
                            privilegeNode.setProperty(DataBaseContants.PROPERTY_FEATURE_TOKEN.getValue(), privilege.getFeatureToken());
                            privilegeNode.setProperty(DataBaseContants.PROPERTY_ACCESS_LEVEL.getValue(), privilege.getAccessLevel());
                            newUserNode.createRelationshipTo(privilegeNode, RelTypes.HAS_PRIVILEGE);
                        }
                    }

                    userIndex.putIfAbsent(newUserNode, DataBaseContants.PROPERTY_ID.getValue(), newUserNode.getId());
                    userIndex.putIfAbsent(newUserNode, DataBaseContants.PROPERTY_NAME.getValue(), userName);
                    storedUserID = newUserNode.getId();
                }
                tx.success();

            } catch (Exception ex) {
                System.err.println("Error creating default user " + ex);
            }
        }
        return storedUserID;
    }

    /**
     * shutdown data base
     */
    public void shutDown() {
        System.out.println();
        System.out.println("\nShutting down database ...");
        graphDb.shutdown();
        System.out.println("Database Shut down ");
    }
}
