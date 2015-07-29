/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package clustering;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import weka.core.Instances;
import weka.clusterers.EM;

/**
 *
 * @author biswajit
 */


public class Clustering {

    /**
     * @param args the command line arguments
     */
    
    static final int keysize = 15;
    static double rating[] = new double [419];
    
    static final String[] keys = new String []
    {
        "Shooting Modes",
        "price",
        "Optical Sensor Resolution (in MegaPixel)",
        "Image Display Resolution",
        "Sensor Type",
        "LCD Screen Size",
        "Weight",
        "Type",
        "Lens Type",
        "Auto Focus",
        "Manual Focus",
        "HDMI",
        "Optical Zoom",
        "Digital Zoom",
        "Viewfinder"
    };
    
    static final boolean [] numeric = new boolean []
    {
        false,
        true,
        true,
        true,
        false,
        true,
        true,
        false,
        false,
        false,
        false,
        false,
        true,
        true,
        false
    };
    
    
    static final boolean [] yesno = new boolean []
    {
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        true,
        true,
        true,
        false,
        false,
        true
    };
    
    static final boolean [] svalue = new boolean []
    {
        true,
        false,
        false,
        false,
        true,
        false,
        false,
        true,
        true,
        false,
        false,
        false,
        false,
        false,
        true
    };
    
    
    static class Camera
    {
        int numerics[] = new int[keysize];
        String yesno[] = new String[keysize];
        Vector<String> string = new Vector<>();
        
        Camera()
        {
            for(int i=0; i<numerics.length; i++)
            {
                numerics[i] = 0;
                yesno[i] = "no";
            }
        }
    }
    
    public static String normalize(String inp)
    {
        String temp = inp.toLowerCase();
        temp = temp.replaceAll("[^a-z]", "");
        return temp;
    }
    
    
    public static int getnumeric(String inp)
    {
        inp = inp.replaceAll(",", "");
        String temp[] = inp.split("[^0-9]+");
        int max = 0;
        for(String str: temp)
        {
            try{
//                max = Math.max(max, Integer.parseInt(str));
                return Integer.parseInt(str);
            }
            catch(Exception e)
            {}
        }
        
        return max;
    }
    
    static HashSet<String> stopwords;
    
    public static void loadwords() throws Exception
    {
        stopwords = new HashSet<>();
        Scanner fin = new Scanner(new FileInputStream("stopwords.txt"));
        
        while(fin.hasNext())
        {
            stopwords.add(fin.next());
        }
    }
    
    public static boolean stopword(String inp) throws Exception
    {
        if(stopwords == null) loadwords();
        return stopwords.contains(inp);
    }
    
    public static Vector<String> getstring(String inp) throws Exception
    {
        inp = inp.toLowerCase();
        String temp[] = inp.split("[^a-z]");
        
        Vector<String> ret = new Vector<>();
        for(String word: temp)
            if(word.length() > 2 && !stopword(word))
                ret.add(word);
        
        return ret;
    }
    
    public static int getindex(String attr)
    {
        for(int i = 0; i<keysize; i++) if(attr.equals(keys[i])) return i;
        return -1;
    }
    
    static HashMap<String, Integer> wordset = new HashMap<>();
    
    public static void insertword(String word)
    {
        if(wordset.containsKey(word)) wordset.put(word, wordset.get(word)+1);
        else wordset.put(word, 1);
    }
    
    public static double getrating(double x1, double x2)
    {
        double y = -0.04470596863*x1*x1 + 0.1567469854*x1*x2 - 0.145246816*x2*x2 + 0.1928919585*x1 - 0.1805866208*x2 + 4.001260316;
        return y;
    }
    
    public static void main(String[] args) throws Exception{
        // TODO code application logic here
        
        Camera arr[] = new Camera[419];
        Scanner sent = new Scanner(new FileInputStream("../sentiment_edited.txt"));
        double sqerr = 0.0;
        int cnt = 0;
        
        for(int ind=1; ind<419; ind++)
        {
            double pos, neg;
            pos = sent.nextDouble();
            neg = sent.nextDouble();
            
            File fxml = new File("../cams/cams_"+ind+".xml");
            System.out.println("../cams/cams_"+ind+".xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fxml);
            
            arr[ind] = new Camera();
            rating[ind] = 0.0;
            
            doc.getDocumentElement().normalize();

            NodeList specs = doc.getElementsByTagName("specsKey");
            for(int i=0; i<specs.getLength(); i++)
            {
                Element spec = (Element) specs.item(i);
                String feature = spec.getAttributes().item(0).getNodeValue();
                String value = spec.getTextContent();
                
                if(feature.equals("ratingValue"))
                {
                    System.out.print(getrating(pos,neg)+" ");
                    try{
                        rating[ind] = Double.parseDouble(value);
                        sqerr += (getrating(pos,neg)-rating[ind])*(getrating(pos,neg)-rating[ind]);
                        cnt ++;
                        System.out.println(rating[ind]);
                    }
                    catch(Exception e)
                    {
                        if(Math.abs(pos) > 0.0001 || Math.abs(neg) > 0.0001) rating[ind] = getrating(pos,neg);
                        else rating[ind] = 0;
                        System.out.println(0);
                    }
                }
                
                int find = getindex(feature);
                if(find < 0) continue;
                if(numeric[find]) arr[ind].numerics[find] = getnumeric(value);
                if(yesno[find]) arr[ind].yesno[find] = "yes";
                if(svalue[find]) arr[ind].string.addAll(getstring(value));
                
            }
            
//            for(int i=0; i<keysize; i++)
//            {
//                System.out.print(keys[i]+" : ");
//                if(numeric[i]) System.out.println(arr[ind].numerics[i]);
//                else if(yesno[i]) System.out.println(arr[ind].yesno[i]);
//            }
            
            for(String word: arr[ind].string) insertword(word);
        }
        
        
        PrintWriter rateout = new PrintWriter("ratings.txt");
        for(int i=1;i<419; i++) rateout.println(rating[i]);
        rateout.close();
        
        System.out.println(Math.sqrt(sqerr/(double)cnt));
        
        
        PrintWriter fout = new PrintWriter("data.arff");
        fout.println("@RELATION camera");
        
        HashSet<String> currset = new HashSet<>();
        HashSet<String> newset = new HashSet<>();
        
        for(int i = 0; i<keysize; i++)
        {
            if(numeric[i])
            {
                fout.println("@ATTRIBUTE "+normalize(keys[i])+" NUMERIC");
                currset.add(normalize(keys[i]));
            }
            else if(yesno[i])
            {
                fout.println("@ATTRIBUTE "+normalize(keys[i])+" {yes, no}");
                currset.add(normalize(keys[i]));
            }
        }
        
        for(String word : wordset.keySet())
        {
            if(!currset.contains(word))
            {
                fout.println("@ATTRIBUTE "+word+" {yes,no}");
                currset.add(word);
                newset.add(word);
            }
        }
        
        
        fout.println("@DATA");
        for(int ind = 1; ind < 419; ind ++)
        {
            for(int i = 0; i<keysize; i++)
            {
                if(numeric[i]) fout.print(arr[ind].numerics[i]+",");
                else if(yesno[i]) fout.print(arr[ind].yesno[i]+",");
            }
            
            for(String word : newset)
            {
                if(arr[ind].string.contains(word)) fout.print("yes,");
                else fout.print("no,");
            }
            
            fout.println();
        }
        
        fout.close();
        
        
        BufferedReader fin = new BufferedReader (new FileReader("data.arff"));
        Instances data = new Instances(fin);
        fin.close();
        
        for(int numclusters = 3; numclusters<=10; numclusters+=1)
        {
            Vector<Integer> classes [] = new Vector [numclusters];
            for(int i=0; i<numclusters; i++) classes[i] = new Vector<>();

            EM clusterer = new EM();
            clusterer.setNumClusters(numclusters);
            clusterer.setMaxIterations(10);
            clusterer.buildClusterer(data);
            
//            System.out.println(clusterer);
            
            for(int i=1; i<419; i++) classes[clusterer.clusterInstance(data.instance(i-1))].add(i);

            fout = new PrintWriter(new FileOutputStream("numcluster"+numclusters+".txt"));

            for(int i=0; i<numclusters; i++)
            {
               int sorted [] = new int[419];
               int size = 0;
               for(int elem : classes[i])
               {
                   sorted[size] = elem;
                   size++;
               }
               
               for(int a = 0; a<size; a++)
                   for(int b = size-1; b>a; b--)
                       if(rating[sorted[b]] > rating[sorted[b-1]])
                       {
                            int temp = sorted[b];
                            sorted[b] = sorted[b-1];
                            sorted[b-1] = temp;
                       }
               
               for(int a = 0; a<size; a++) fout.print(sorted[a]+" ");
               fout.println();
            }

            fout.close();
        }
        
    }
    
}
