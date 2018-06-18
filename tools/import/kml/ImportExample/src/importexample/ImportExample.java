/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package importexample;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;




/**
 *
 * @author daniel
 */
public class ImportExample {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        File archivo = null;
        FileReader fr = null;
        
        FileWriter fichero = null;
        PrintWriter pw = null;
        
        BufferedReader br = null;
        List<Item> itemsManhole = new ArrayList();
        List<Item> itemsFocRoute = new ArrayList();
        //List<Item> itemsPoles = new ArrayList();

      try {
         // Apertura del fichero y creacion de BufferedReader para poder
         // hacer una lectura comoda (disponer del metodo readLine()).
         archivo = new File("doc.kml");
            fr = new FileReader(archivo);
            br = new BufferedReader(fr);
            boolean agregarItem = false;
            Item it = new Item();
            // Lectura del fichero
            String linea;
         while((linea=br.readLine())!=null)
         {
             ///////////////////////MANHOLDS//////////////////
                boolean nameManholds = linea.contains("<name>");
                boolean manhole = linea.contains("<name>Manhole");
                boolean longitudeManhole = linea.contains("longitude");
                boolean latitudeManhole = linea.contains("latitude");

                boolean finPlacemarkManhole = linea.contains("</Placemark>");
                
                boolean folderManhole = linea.contains("<Folder>");
                
                if(folderManhole) agregarItem=false;
                
                if (nameManholds && manhole) {
                    agregarItem = true;
                }
                
                if(nameManholds && agregarItem)
                {
                    it = new Item();
                    it.setName(obtenerValorEtiqueta(linea));
                }
                
                if(longitudeManhole && agregarItem)
                {
                    it.setLon(obtenerValorEtiqueta(linea));
                }
                
                if(latitudeManhole && agregarItem)
                {
                    it.setLat(obtenerValorEtiqueta(linea));
                }
                if(finPlacemarkManhole && agregarItem)
                {
                    itemsManhole.add(it);
                }
                /////////////////////////FIN MANHOLDS////////////
                
                
                
                
                
                               
                
            }
         
            
            Collections.sort(itemsManhole, new LongComparator());
            
            fichero = new FileWriter("prueba.csv");
            pw = new PrintWriter(fichero);
            int cont=0;
            int cont2=1;
            
            
            pw.println("Country~t~root~t~name~c~root~t~name~c~Angola~t~acronym~c~AO");
            for(int i =0; i< itemsManhole.size();i++ )
            {
                if (i==0)
                {
                    pw.println("Zone~t~Country~t~name~c~Angola~t~name~c~Zone"+0);
                }
                
                
                if(cont==100){
                    pw.println("Zone~t~Country~t~name~c~Angola~t~name~c~Zone"+cont2);
                    cont=0;
                    cont2++;
                }else{
                    pw.println("Manhole~t~Zone~t~name~c~Zone"+(cont2-1)+"~t~name~c~"+itemsManhole.get(i).getName()+"~t~longitude~c~"+itemsManhole.get(i).getLon()+"~t~latitude~c~"+itemsManhole.get(i).getLat()+"");
                }
                cont++;
            }
           
            System.out.println(itemsManhole.size());
      }
      catch(Exception e){
         e.printStackTrace();
      }finally{
         // En el finally cerramos el fichero, para asegurarnos
         // que se cierra tanto si todo va bien como si salta 
         // una excepcion.
         try{                    
            if( null != fr ){   
               fr.close();     
            }                  
         }catch (Exception e2){ 
            e2.printStackTrace();
         }
      }     
}
     
    public static String obtenerValorEtiqueta(String s)
    {
        return s.substring(s.indexOf(">")+1, s.lastIndexOf("<"));
    }
    
}
    

