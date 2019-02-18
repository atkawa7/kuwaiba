/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neotropic.createdefaultdb.core;

/**
 *
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
public enum DataBaseContants {
    LABEL_ROOT("root")
    , PROPERTY_NAME("name")
    , NODE_PRIVILEGES("Privileges")
    , INDEX_SPECIAL_NODES("specialNodes")
    , NODE_OBJECT_ACTIVITY_LOG("ObjectActivityLog")
    , PROPERTY_CREATION_DATE("creationDate")
    , NODE_GENERAL_ACTIVITY_LOG("GeneralActivityLog")
    , NODE_GROUPS("Groups")
    , NODE_DUMMYROOT("DummyRoot")
    , PROPERTY_DISPLAY_NAME("displayName")
    , PROPERTY_ID("id")
    , PROPERTY_PASSWORD("password")
    , PROPERTY_FIRST_NAME("firstName")
    , PROPERTY_LAST_NAME("lastName")
    , PROPERTY_TYPE("type")
    , PROPERTY_ENABLED("enabled")
    , USER_TYPE_WEB_SERVICE("2")
    , USER_TYPE_SOUTHBOUND("3")
    , DEFAULT_ADMIN("admin")
    , DEFAULT_ADMIN_OID("1")
    , DEFAULT_ADMIN_PASSWORD("kuwaiba")
    , DEFAULT_ADMIN_FIRSTNAME("Jhon")
    , DEFAULT_ADMIN_LASTNAME("Doe")
    , DEFAULT_ADMIN_ENABLED("true")
    , DEFAULT_ADMIN_TYPE("kuwaiba")
    , DEFAULT_ADMIN_GROUPID("1")
    , DEFAULT_GROUP_DESCRIPTION("Default group")
    , GROUP_PROPERTY_DESCRIPTION("Default group")
    , DEFAULT_GROUP_NAME("DefaultGroup")
    , USER_TYPE_GUI("1")
    , INDEX_USERS("users")
    , PROPERTY_FEATURE_TOKEN("featureToken")
    , PROPERTY_ACCESS_LEVEL("accessLevel")
    , INDEX_GROUPS ("groups")
   
    ;

    private String value;

    private DataBaseContants(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
    
    public int getIntValue(){        
        return Integer.parseInt(value);
    }
    
    public boolean getBooleanValue(){        
        return Boolean.valueOf(value);
    }
}
