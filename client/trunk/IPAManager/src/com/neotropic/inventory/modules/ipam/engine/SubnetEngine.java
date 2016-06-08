/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neotropic.inventory.modules.ipam.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Make all the validations an calculate the possible subnets for IPv4 and IPv6 Addresses
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class SubnetEngine {
    
    private String ipAddress;
    private int maskBits;
    private int numberOfHosts;
    private Map<Integer, List<String>> mapSubnets;
    private List<String> subnets;
    
    public SubnetEngine() {
        ipAddress = "";
        mapSubnets = new HashMap();
        subnets = new ArrayList<>();
    }
    
    public void calculateSubnets(String ipCIDR){
        String[] ipCIDRsplited = ipCIDR.split("/");
              
        ipAddress = ipCIDRsplited[0];
        maskBits = Integer.parseInt(ipCIDRsplited[1]);
        
        calculateNumberOfHosts();
        List<List<String>> binaryIPSegments = parseToBinary(ipAddress, 8);
        
        int segmentId=1;
        int segementPos=0;
        
        for (List<String> segment : binaryIPSegments) {
            List<String> listOf = new ArrayList<String>();
            String subnetSegment = "";
            String netSegment = "";
            for (String bit : segment) {
                if(segementPos >= maskBits)
                    subnetSegment += bit;
                else
                    netSegment += bit;
                segementPos++;
            }
            if(!netSegment.isEmpty() && subnetSegment.isEmpty()){
                listOf.add(Integer.toString(Integer.parseInt(netSegment,2)));
                mapSubnets.put(segmentId, listOf);
            }
            
            if(!subnetSegment.isEmpty()){
                if(!netSegment.isEmpty() && Integer.parseInt(netSegment) == 0) //in case the of lower bits in front of the mask
                    subnetSegment="0";
                for (String segement : complement(netSegment, segmentCalculation(subnetSegment))) 
                    listOf.add(Integer.toString(Integer.parseInt(segement,2)));
                mapSubnets.put(segmentId, listOf);
            }
            segmentId++;
        }
        
        for (Map.Entry e : mapSubnets.entrySet()) {
            System.out.println(e.getKey() + " " + e.getValue());
        }
        createSubnets(1, "", "");
    }
    
    private void createSubnets(int i, String netheader, String subnetBit){
        if(!subnetBit.isEmpty() && mapSubnets.containsKey(i))
            netheader += subnetBit+".";
        
        if(!mapSubnets.containsKey(i)){
            subnets.add(netheader+subnetBit);
            return;
        }
        List<String> list = mapSubnets.get(i);
        i++;
        for (String bit : list) 
            createSubnets(i, netheader, bit);
    }
    
    private List<String> segmentCalculation(String subnetSegment){
        List<String> segments = new ArrayList<String>();
        int bits = subnetSegment.length();
        int x = Integer.parseInt(subnetSegment, 2);
        while(true){
            String segment = "";
            int diference = 0;
            if(Integer.toString(x, 2).length()>bits)
                break;
            if(Integer.toString(x, 2).length()<bits)
                diference = bits - Integer.toString(x, 2).length();
            for (int i = 0; i < diference; i++) 
                segment += "0";
           segment += Integer.toString(x, 2);
           segments.add(segment);
           x++;
        }
        return segments;
    }
    
    /**
     * For example in the IP 123.35.140.0/22 the 0 in third segment(140) is the 
     * first bit for subnetig so after calculate the possible subnet values 
     * we got in binary 00, 01, 10, 11 the complement means 
     * 14 + 00(0 in decimal) = 140 
     * 14 + 01(1 in decimal) = 141
     * 14 + 10(2 in decimal) = 142 
     * 14 + 11(3 in decimal) = 143
     * @param segment in this case it is 14
     * @param complement all th e possible values for the subnet.
     * @return a list of all the possible subnet combinations for every segment
     */
    private List<String> complement(String segment, List<String> complement){
        List<String> complements = new ArrayList<String>();
        String first = segment;
        for (String bits : complement) 
            complements.add(first+bits);
        return complements;
    }
    
    private List<List<String>> parseToBinary(String address, int bitSize){
        List<String> binaryAddress = new ArrayList<String>();
        List<String> singleSegment = new ArrayList<String>();
        List<List<String>> segments = new ArrayList<List<String>>();
        
        String[] splitedAddress = address.split("\\.");
        for (String splitedAddres : splitedAddress) {
            String segmen = Integer.toString(Integer.parseInt(splitedAddres), 2);
            if(segmen.length() < bitSize){
                for (int j = 0; j < bitSize-segmen.length(); j++) 
                    binaryAddress.add("0");
            }
            for (int j = 0; j < segmen.length(); j++) 
                binaryAddress.add(""+segmen.charAt(j));
        }
        
        for(int i=0; i < binaryAddress.size();i++){
            if(i%bitSize==0 && i!=0){
                segments.add(singleSegment);
                singleSegment = new ArrayList<String>();
            }
            singleSegment.add(binaryAddress.get(i));
        }
        segments.add(singleSegment);
        
        return segments;
    }
        
    public int calculateNumberOfHosts(){
        int n = 32-maskBits;
        return (int)(Math.pow(2, n)-2);
    }
    
    public int calculateNumberOfHostsIpV6(){
        int n = 128-maskBits;
        return (int)(Math.pow(2, n));
    }
    
    private String[] completeIPv6(String ip){
        String[] shortIPAddress = ip.split(":");
        String[] ipa = {"0000", "0000", "0000", "0000", "0000", "0000", "0000", "0000"};
        boolean flag = false;
        for (int g = 0; g<shortIPAddress.length;g++){ 
            if(shortIPAddress[g].isEmpty()){
                flag = true;
                break;
            }
            ipa[g] = shortIPAddress[g];
        }
        
        if(flag){
            int l = 7;
            for (int g = shortIPAddress.length - 1; g > 0 ;g--){ 
                if(shortIPAddress[g].isEmpty())
                    break;
                ipa[l] = shortIPAddress[g];
                l--;
            }
        }
        return ipa;
    }
    
    private List<List<String>> parseToBinaryIpv6(String[] ip){
        List<List<String>> binaryIPAddress = new ArrayList<>();
        for (String segment : ip) {
            List<String> segments = new ArrayList<>();
            for(int i=1; i<=segment.length();i++){
                if(segment.length()==3)
                    segment = "0" + segment;
                if (segment.length() == 2) 
                    segment = "00" +segment;
                if (segment.length() == 1) 
                    segment = "000" + segment;
                String h = Integer.toString(Integer.parseInt(segment.substring(i-1, i),16),2);
                while(h.length()<4)
                    h="0"+h;
                segments.add(h);
            }
            binaryIPAddress.add(segments);
        }
        return binaryIPAddress;
    }
    
    public List<String> calculateSubnetsIpv6(String ipCIDR){
        String[] splitedCIDR = ipCIDR.split("/");
        maskBits = Integer.parseInt(splitedCIDR[1]);
        String ipv6 = splitedCIDR[0];
        List<List<String>> ip = parseToBinaryIpv6(completeIPv6(ipv6));
        List<String> segmentos = new ArrayList<>();
        List<List<String>> subnets = new ArrayList<>();
        int i = 0;
        boolean flag = false;
        String netPart = "";
        String maskPart ="";
        for (List<String> segments : ip) {
            segmentos = new ArrayList<>();
            for (String segment : segments) {
                maskPart = "";
                for (int k =0; k < segment.length(); k++) {
                    if(i==maskBits){
                        maskPart = segment.substring(k);
                        netPart = segment.substring(0, k);
                        flag = true;
                        break;
                    }
                    i++;
                }        
                if(flag)
                    break;
                segmentos.add(segment);
            } 
            subnets.add(segmentos);
            if(flag)
                break;
        }
        if(maskPart.length()==4 && !maskPart.contentEquals("0000"))
            System.out.println("esto no se puede ome");
        List<String> calculation = segmentCalculation(maskPart);
        List<String> complement = complement(netPart, calculation);
        return createIPv6(subnets, complement);
    }
    
    public String ipv6AsString(String[] ip){
        String ipv6 = "";
        for (String segment : ip) 
            ipv6 += segment + ":";
        return ipv6.substring(0, ipv6.length()-1);
    }
    
    public List<Integer> segmentAnIP(String ip) {
        String segments[] = null;
        List<Integer>  theSegments= new ArrayList<>();
        if(ip.contains("."))
            segments = ip.split("\\.");
        else if(ip.contains(":")){
            ip = ipv6AsString(completeIPv6(ip));
            segments = ip.split(":");
            for (String segment : segments) 
                theSegments.add(Integer.parseInt(segment,16));
            return theSegments;
        }
        for (String segment : segments) 
           theSegments.add(Integer.parseInt(segment));
        return theSegments;
    }
    
    /**
     * calculate if a given networkIp for a subnet is inside of another subnet 
     * taking as parameters the broadcastIp an de networkIp
     * @param netwrokIp the network IP
     * @param broadcastIp the broadcast IP
     * @param ip the possible Ip
     * @return true if it contained, false if not
     */
    public boolean itContains(String netwrokIp, String broadcastIp, String ip){
        List<Integer> binaryNetworkIp = segmentAnIP(netwrokIp);
        List<Integer> binarybroadcastIp = segmentAnIP(broadcastIp);
        List<Integer> binaryIp = segmentAnIP(ip);
        for (int i = 0; i < binaryIp.size(); i++) {
            if(Objects.equals(binaryIp.get(i), binaryNetworkIp.get(i)))
                    continue;
            else if((binaryIp.get(i) > binaryNetworkIp.get(i)) && (binaryIp.get(i) < binarybroadcastIp.get(i)))
                    return true;
            else 
                return false;
        }
        return false;
    }
    
    private List<String> createIPv6(List<List<String>> segments, List<String> complements){
        String ip = "";
        String[] nipAddress = {"0000", "0000", "0000", "0000", "0000", "0000", "0000", "0000"};
        String[] bipAddress = {"ffff", "ffff", "ffff", "ffff", "ffff", "ffff", "ffff", "ffff"};
        boolean flag = true;
        List<String> partialSubnets =  new ArrayList<>();
        List<String> subnets =  new ArrayList<>();
        
        for (List<String> segment : segments) {
            if(segment.size()>0){
                for (String bits : segment) {
                    ip += Integer.toString(Integer.parseInt(bits, 2),16);
                    if(segment.size()<4)
                        flag = false;
                }
                if(flag)
                    ip += ":";
            }
        }
        if(ip.length()>0){
        if((ip.substring(ip.length()-2, ip.length()-1)).equals(":"))
                ip = ip.substring(0, ip.length()-1);
        }
        
        for (String string : complements)
            partialSubnets.add(ip+Integer.toString(Integer.parseInt(string, 2),16));               
        
        int size = 0;
        String networkip = partialSubnets.get(0);
        String broadcastip = partialSubnets.get(partialSubnets.size()-1);
        size = partialSubnets.size();
        String[] partialNetworkSplited = networkip.split(":");
        String[] partialBroadcastSplited = broadcastip.split(":");
        String n = "";
        String b = "";
        for(int i = 0; i< partialNetworkSplited.length; i++){
            n = partialNetworkSplited[i];
            b = partialBroadcastSplited[i];
            while(n.length()<4){
                n += "0";
                b += "f";
            }
            nipAddress[i] = n;
            bipAddress[i] = b;
        }
        String subnet = "";
        for(int i = 0; i<nipAddress.length; i++)
            subnet += nipAddress[i]+":";
        subnets.add(subnet.substring(0, subnet.length()-1));
        subnet = "";
        for(int i = 0; i<bipAddress.length; i++)
            subnet += bipAddress[i]+":";
        subnets.add(subnet.substring(0, subnet.length()-1));
        return subnets;
    }
    
    public static boolean isIPAddress(String ipAddress){
        String ipv4Regex = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])(\\/([0-9]|[1-2][0-9]|3[0-2]))$";
        String ipv6Regex = "^s*((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:)))(%.+)?s*";
        Pattern ipv4Pattern = Pattern.compile(ipv4Regex);
        Pattern ipv6Pattern = Pattern.compile(ipv6Regex);
        Matcher ipv4 = ipv4Pattern.matcher(ipAddress);
        Matcher ipv6 = ipv6Pattern.matcher(ipAddress);
        return ipv4.matches() || ipv6.matches();
    }
    
    public static boolean isHostname(String hostname){
        String hostnameRegex = "^(([a-zA-Z]|[a-zA-Z][a-zA-Z0-9-]*[a-zA-Z0-9]).)*([A-Za-z]|[A-Za-z][A-Za-z0-9-]*[A-Za-z0-9])$";
        Pattern hostnamePattern = Pattern.compile(hostnameRegex);
        Matcher mhostname = hostnamePattern.matcher(hostname);
        return mhostname.matches();
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getNumberOfHosts() {
        return numberOfHosts;
    }

    public void setNumberOfHosts(int numberOfHosts) {
        this.numberOfHosts = numberOfHosts;
    }

    public List<String> getSubnets() {
        return subnets;
    }
    
}
