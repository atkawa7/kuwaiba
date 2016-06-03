/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neotropic.inventory.modules.ipam.nodes.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author adrian
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
        subnets = new ArrayList<String>();
    }
    
    public void calculateSubnets(String ipCIDR){
        String[] ipCIDRsplited = ipCIDR.split("/");
        
        if(!isIPAddress(ipCIDR))
            throw new IllegalArgumentException("Is not a valid ip address");
       
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
        
    public void calculateNumberOfHosts(){
        int n = 32-maskBits;
        numberOfHosts = (int)(Math.pow(2, n)-2);
    }
    
    public boolean isIPAddress(String ipAddress){
        String ipv4Regex = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])(\\/([0-9]|[1-2][0-9]|3[0-2]))$";
        String ipv6Regex = "^s*((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:)))(%.+)?s*(\\/(d|dd|1[0-1]d|12[0-8]))$";
        Pattern ipv4Pattern = Pattern.compile(ipv4Regex);
        Pattern ipv6Pattern = Pattern.compile(ipv6Regex);
        Matcher ipv4 = ipv4Pattern.matcher(ipAddress);
        Matcher ipv6 = ipv6Pattern.matcher(ipAddress);
        return ipv4.matches() || ipv6.matches();
    }
    
    public boolean isHostname(String hostname){
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
