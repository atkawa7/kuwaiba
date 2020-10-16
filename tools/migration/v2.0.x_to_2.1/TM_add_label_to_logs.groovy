/**
  *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *       https://apache.org/licenses/LICENSE-2.0.txt
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */

/**
  * Kuwaiba Version: 2.1
  * 
  * name: add_label_to_logs
  * Description: Sets generalActivityLogsLabel and objectActivityLogsLabel. And deletes specialNodes with name Groups, GeneralActivityLog, ObjectActivityLog
  * commitOnExecute: true
  * parameters: none
  * 
  * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
  */
def taskResult = TaskResult.newInstance();
try {
    def tx = connectionHandler.beginTx()
    def setGeneralActivityLogsLabelQueryBuilder = new StringBuilder();
    setGeneralActivityLogsLabelQueryBuilder.append("MATCH (:specialNodes{name:'GeneralActivityLog'})<-[:CHILD_OF_SPECIAL]-(n1)"); //NOI18N
    setGeneralActivityLogsLabelQueryBuilder.append("SET n1:generalActivityLogs"); //NOI18N
    
    def setObjectActivityLogsLabelQueryBuilder = new StringBuilder();
    setObjectActivityLogsLabelQueryBuilder.append("MATCH (:specialNodes{name:'ObjectActivityLog'})<-[:CHILD_OF_SPECIAL]-(n2)"); //NOI18N
    setObjectActivityLogsLabelQueryBuilder.append("SET n2:objectActivityLogs"); //NOI18N
    
    def deleteSomeSpecialNodesQueryBuilder = new StringBuilder();
    deleteSomeSpecialNodesQueryBuilder.append("MATCH (n:specialNodes)"); //NOI18N
    deleteSomeSpecialNodesQueryBuilder.append("WHERE n.name IN ['Groups', 'GeneralActivityLog', 'ObjectActivityLog']"); //NOI18N
    deleteSomeSpecialNodesQueryBuilder.append("DETACH DELETE n"); //NOI18N
    
    connectionHandler.execute(setGeneralActivityLogsLabelQueryBuilder.toString());
    connectionHandler.execute(setObjectActivityLogsLabelQueryBuilder.toString());
    connectionHandler.execute(deleteSomeSpecialNodesQueryBuilder.toString());
    
    tx.success();
    taskResult.getMessages().add(TaskResult.createInformationMessage("Set generalActivityLogsLabel"));
    taskResult.getMessages().add(TaskResult.createInformationMessage("Set objectActivityLogsLabel"));
    taskResult.getMessages().add(TaskResult.createInformationMessage("Deleted specialNodes with name Groups, GeneralActivityLog, ObjectActivityLog"));
} catch(Exception ex) {
    taskResult.getMessages().add(TaskResult.createErrorMessage(String.format("Unexpected error: %s", ex.getMessage())));
}
return taskResult;