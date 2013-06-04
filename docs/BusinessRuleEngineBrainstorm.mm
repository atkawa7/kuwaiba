<map version="0.9.0">
<!-- To view this file, download free mind mapping software FreeMind from http://freemind.sourceforge.net -->
<node CREATED="1358975769296" ID="ID_1981870306" MODIFIED="1362612029129" TEXT="Business Rules Engine">
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
<node CREATED="1362612032772" HGAP="29" ID="ID_387864091" MODIFIED="1362612066651" POSITION="right" TEXT="Is Red Hat&apos;s DROOLS and option?" VSHIFT="53">
<node CREATED="1362614799168" ID="ID_1019282117" MODIFIED="1362614820920" TEXT="Knowledge Representation and Reasoning">
<node CREATED="1362663224550" ID="ID_1919496715" MODIFIED="1362663234453" TEXT="Most basic approach to IA"/>
<node CREATED="1362663235867" ID="ID_1096664242" MODIFIED="1362664170946" TEXT="Deals with how the knowled is represented in a structured way and how to use that knowledge to take decisions"/>
</node>
<node CREATED="1362664287882" ID="ID_1752651071" MODIFIED="1362665764563" TEXT="Rule Engines and Production Rule Systems">
<node CREATED="1362664328803" ID="ID_372757827" MODIFIED="1362664345484" TEXT="A RE delivers KRR functionality to a developer">
<node CREATED="1362664346603" ID="ID_1682267869" MODIFIED="1362664533102" TEXT="Ontology: The knowledge model representation. OWL, java classes, DB records"/>
<node CREATED="1362664349322" ID="ID_1316380867" MODIFIED="1362664552391" TEXT="Rules: The reasoning, the thinking">
<node CREATED="1362665238741" ID="ID_461641298" MODIFIED="1362665272867" TEXT="Production Rules: Two-part rules (IF something THEN something)"/>
<node CREATED="1362665460390" ID="ID_1275272243" MODIFIED="1362665556203" TEXT="If changes in the data trigger actions, that&apos;s called data driven reasoning"/>
<node CREATED="1362674482050" ID="ID_242579064" MODIFIED="1362674505490" TEXT="Rules are stored in what is known as &quot;Production Memory&quot;"/>
</node>
<node CREATED="1362664354542" ID="ID_1333821241" MODIFIED="1362664619560" TEXT="Data: Inputs">
<node CREATED="1362674508245" ID="ID_1032346602" MODIFIED="1362674517434" TEXT="Facts are stored in what is known as &quot;Working Memory&quot;"/>
</node>
</node>
<node CREATED="1362675060796" ID="ID_1902239547" MODIFIED="1362675099002" TEXT="More than a PRS, DROOLS is a Hybrid RS, since it also uses backward chaining (since version 5)"/>
</node>
<node CREATED="1362665711246" ID="ID_457252900" MODIFIED="1362665760262" TEXT="DROOLS use (and extend) the Rete algorithm"/>
<node CREATED="1362674676674" ID="ID_1683540883" MODIFIED="1362674689733" TEXT="Backward chaining = derivation queries"/>
<node CREATED="1362675400918" FOLDED="true" ID="ID_1802711412" MODIFIED="1364229916775" TEXT="Common questions">
<node CREATED="1362675406427" ID="ID_1146334766" MODIFIED="1362675411285" TEXT="When to use a RE">
<node CREATED="1362680049812" ID="ID_387174508" MODIFIED="1362680068774" TEXT="When traditional programming doesn&apos;t seem to be enough"/>
<node CREATED="1362680069190" ID="ID_177723462" MODIFIED="1362680099544" TEXT="When the rules change often"/>
<node CREATED="1362680111589" ID="ID_1743334072" MODIFIED="1362680139364" TEXT="Domain experts are non-technical"/>
<node CREATED="1362680198272" ID="ID_326774057" MODIFIED="1362680249755" TEXT="When the problem is too complex"/>
</node>
<node CREATED="1362675411610" ID="ID_945938460" MODIFIED="1362675445891" TEXT="What the advantage over a common harcoded if...then approach">
<node CREATED="1362679407662" ID="ID_1155616979" MODIFIED="1362679413928" TEXT="Declarative Programming: Rule engines allow you to say &quot;What to do&quot;, not &quot;How to do it&quot;."/>
<node CREATED="1362679510343" ID="ID_520537469" MODIFIED="1362679516483" TEXT="Logic and Data Separation"/>
<node CREATED="1362679560690" ID="ID_1889953338" MODIFIED="1362679562693" TEXT="Speed and Scalability"/>
<node CREATED="1362679579020" ID="ID_619973953" MODIFIED="1362679592532" TEXT="Centralization of Knowledge (Single Point of Truth, Knowledge base)"/>
<node CREATED="1362679701509" ID="ID_94659394" MODIFIED="1362679703921" TEXT="Tool Integration "/>
<node CREATED="1362679751370" ID="ID_1779804220" MODIFIED="1362679753622" TEXT="Understandable Rules "/>
</node>
</node>
<node CREATED="1364229917980" ID="ID_363119339" MODIFIED="1364229926700" TEXT="User Guide">
<node CREATED="1364229932271" ID="ID_949219909" MODIFIED="1364229967651" TEXT="Object type (i.e. Router, WorkOrder) + constraints = Pattern"/>
<node CREATED="1364230575950" ID="ID_487145108" MODIFIED="1364230594502" TEXT="DRL = Drools Rule Language"/>
<node CREATED="1364233305619" ID="ID_983557559" MODIFIED="1364233354535" TEXT="Rules are not called directly, the existing set of rules is executed and the rules to be applied are matched depending on the input"/>
<node CREATED="1364233951525" ID="ID_1024548282" MODIFIED="1364233970574" TEXT="Cross Product = the result of a joins"/>
</node>
</node>
</node>
</map>
