# Neotropic SAS 2017
# Kuwaiba Open Network Inventory - Web Service Stub Generator v0.4
# This script generates the Java client side stubs necessary to consume the platform's web service. Use -h flag to see the full list of options

# Defaults
url="http://localhost:8080/kuwaiba/KuwaibaService?wsdl"
destination="src"

while getopts u:cd:h option
do
    case "${option}"
    in
        u) url=${OPTARG};;
        c) clean=true;;
        d) destination=${OPTARG};;
        h) echo $'Kuwaiba Open Network Inventory - Web Service Stub Generator v0.4\n -u WSDL URL. Default: http://localhost:8080/kuwaiba/KuwaibaService?wsdl\n -c Clean the destination directory before parsing the WSDL\n -d Destination directory. Default: src';exit;;
    esac
done

if ! type wsimport >/dev/null; then
    echo "wsimport command not found. This command is in the bin folder of your Java installation. Check if your PATH is correctly pointing there."
    exit 1;
fi

if [ -z ${clean+x} ]
then
    echo "Cleaning ${destination}/org/inventory/communications/wsclient..."
    rm -rf "${destination}/org/inventory/communications/wsclient"
fi

wsimport -d $destination -p org.inventory.communications.wsclient -Xnocompile $url -b kuwaibaAsync.xml 

if [ $? -eq 0 ]
then
    #Here we patch the TransientQuery class with the missing setters that wsimport does not generate
    echo "Patching TransientQuery class..."
    setters="public void setAttributeNames(List<String> attributeNames) {
        this.attributeNames = attributeNames;
    }

    public void setAttributeValues(List<String> attributeValues) {
        this.attributeValues = attributeValues;
    }

    public void setConditions(List<Integer> conditions) {
        this.conditions = conditions;
    }

    public void setJoins(List<TransientQuery> joins) {
        this.joins = joins;
    }

    public void setVisibleAttributeNames(List<String> visibleAttributeNames) {
        this.visibleAttributeNames = visibleAttributeNames;
    }"

    head -n -2  "${destination}/org/inventory/communications/wsclient/TransientQuery.java" > temp.txt

    echo "${setters}" >> temp.txt
    echo $'\n}' >> temp.txt
    mv temp.txt "${destination}/org/inventory/communications/wsclient/TransientQuery.java"
    echo "Patching done."
else
    echo wsimport failed. Aborting any further actions.
fi

