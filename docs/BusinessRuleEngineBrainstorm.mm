<map version="0.9.0">
<!-- To view this file, download free mind mapping software FreeMind from http://freemind.sourceforge.net -->
<node CREATED="1358975769296" ID="ID_1981870306" MODIFIED="1358989411029" TEXT="Business Rules Engine">
<node CREATED="1358976079742" ID="ID_1491468321" MODIFIED="1358976093092" POSITION="right" TEXT="What should it provide?">
<node CREATED="1358979009515" ID="ID_1415309885" MODIFIED="1358979040294" TEXT="A way to check for some conditions before performing an action"/>
<node CREATED="1358980113031" ID="ID_298746220" MODIFIED="1358984270506" TEXT="Provide a way execute Triggered Actions"/>
</node>
<node CREATED="1358976093788" ID="ID_599575948" MODIFIED="1358976106105" POSITION="right" TEXT="When is it executed?">
<node CREATED="1358984281862" ID="ID_500891371" MODIFIED="1358984303606" TEXT="When a method is called, since the whole API is functional-oriented"/>
</node>
<node CREATED="1358976107271" ID="ID_1543424778" MODIFIED="1358976112793" POSITION="right" TEXT="How is it executed?">
<node CREATED="1358981002352" ID="ID_714472267" MODIFIED="1358981015039" TEXT="1. User performs an action"/>
<node CREATED="1358981018097" ID="ID_447914331" MODIFIED="1358981448064" TEXT="2. The method that implements the action, calls the BRE to check if there&apos;s a BR associated to it. It must provide the applicable business rule types (e.g.: NEW_OBJECT, UPDATE_ATTRIBUTE) and a set of predefined arguments as inputs to the rule. It must return a code indicating the result of checking the condition (e.g.: ACCEPT, QUEUE, REJECT, ERROR)"/>
<node CREATED="1358981461034" ID="ID_1891725485" MODIFIED="1358981607354" TEXT="3. The business rule checks if there are triggered actions related to it and call them, passing the result of evaluating the condition and the initial parameters for it to execute the task"/>
<node CREATED="1358984057013" ID="ID_1648757861" MODIFIED="1358984227445" TEXT="4. The method that called the BRE should take the value returned and either a) Continue with the execution b) Stop the execution c) Put the action in a queue to be performed later"/>
</node>
<node CREATED="1358976278364" ID="ID_1542986950" MODIFIED="1358976280775" POSITION="left" TEXT="Rules">
<node CREATED="1358976123259" HGAP="50" ID="ID_942948458" MODIFIED="1359038684127" TEXT="What kind of rules would it support?" VSHIFT="1">
<node CREATED="1358976140706" ID="ID_270908649" MODIFIED="1358976155739" TEXT="User privileges can be managed as  BR">
<node CREATED="1359036417939" ID="ID_1907564611" MODIFIED="1359036488030" TEXT="Access to funtionalities described by string tokens (like query-module) - Should the methods be grouped by module?">
<icon BUILTIN="help"/>
</node>
<node CREATED="1359036496351" ID="ID_1359015532" MODIFIED="1359036517069" TEXT="Can a user modify attributes of an object or a given set of objects?"/>
<node CREATED="1359036517627" ID="ID_1336628242" MODIFIED="1359039264860" TEXT="Can a user move/delete objects?"/>
</node>
<node CREATED="1358976156820" ID="ID_40991082" MODIFIED="1358984606234" TEXT="Updating an attribute">
<node CREATED="1358984607674" ID="ID_1282771496" MODIFIED="1358984623405" TEXT="Check against a given regular expression"/>
<node CREATED="1358985438828" ID="ID_1092102846" MODIFIED="1358985501714" TEXT="Check next steps in a state machine"/>
<node CREATED="1358985601481" ID="ID_1672073046" MODIFIED="1359037143907" TEXT="Check for thresholds in a numeric field (also applicable to date fields)"/>
</node>
<node CREATED="1358976166542" ID="ID_1686849779" MODIFIED="1358976200697" TEXT="Creating an object">
<node CREATED="1358986896150" ID="ID_1083947452" MODIFIED="1358986953535" TEXT="If any of the parents match a given condition (attribute value)"/>
<node CREATED="1359037275650" ID="ID_945773619" MODIFIED="1359037380618" TEXT="Create only if another instance in the db matches a given condition (e.g.: create a building if the contract X has the attribute &quot;signed&quot; set to &quot;true&quot;) "/>
<node CREATED="1359037510710" ID="ID_1655353713" MODIFIED="1359037530555" TEXT="Create only if the workflow platform says it&apos;s possible"/>
</node>
<node CREATED="1358976174469" ID="ID_1543276090" MODIFIED="1358976212724" TEXT="Deleting object"/>
<node CREATED="1358976178672" ID="ID_1978970087" MODIFIED="1358976192225" TEXT="Moving and object"/>
<node CREATED="1358976223787" ID="ID_1713458381" MODIFIED="1358976233237" TEXT="Related to defined state machines"/>
<node CREATED="1358976238575" ID="ID_322365891" MODIFIED="1358976250890" TEXT="Related to workflow platform interactions">
<node CREATED="1358976252175" ID="ID_693148546" MODIFIED="1358976269245" TEXT="While such interaction is performed, actions might be queued"/>
</node>
<node CREATED="1358985679035" ID="ID_1252602783" MODIFIED="1358985708856" TEXT="Creating a relationship between two elements"/>
</node>
<node CREATED="1358976295242" ID="ID_581032138" MODIFIED="1359039559675" TEXT="How can they be described and stored?">
<node CREATED="1358976302785" ID="ID_539627460" MODIFIED="1358976304478" TEXT="XML"/>
<node CREATED="1358976304896" ID="ID_1723906091" MODIFIED="1358976343079" TEXT="Arrays/Matrixes">
<icon BUILTIN="ksmiletris"/>
</node>
<node CREATED="1358976318946" ID="ID_1146906382" MODIFIED="1358976322278" TEXT="Natural language"/>
</node>
</node>
<node CREATED="1358976371387" ID="ID_660441700" MODIFIED="1358984344210" POSITION="left" TEXT="Misc">
<node CREATED="1358976398620" ID="ID_76092504" MODIFIED="1358976409984" TEXT="Support Rulesets to organize BRs"/>
</node>
<node CREATED="1358980895798" ID="ID_872349713" MODIFIED="1358980964380" POSITION="left" TEXT="Triggered Actions: Actions executed when a business rule is matched"/>
</node>
</map>
