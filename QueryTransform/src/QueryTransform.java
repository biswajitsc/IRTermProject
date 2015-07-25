
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author anurag
 */
public class QueryTransform {
    
    private static String [] units = {  "rs",
                                        "inch",
                                        "mm",
                                        "cm",
                                        "mp",
                                        "mexapixel",
                                        "fps",
                                        "sec",
                                        "dots",
                                        "g",
                                        "p",
                                        "x"
                                    };
   
    
    private static boolean isNumber(String str) {
        try {
            double n = Double.parseDouble(str);
        } catch(NumberFormatException ex) {
            return false;
        }
        return true;
    }
    
    private static int isSize(String token) {
        for (int i = 1; i < token.length() - 1; ++i) {
            if (token.charAt(i) == 'x') {
                if(isNumber(token.substring(0, i)) && isNumber(token.substring(i + 1, token.length()))) {
                    return i;
                }
            }
        }
        return -1;
    }
    
    private static int roundOf(int x) {
        int p10 = 1, n = x;
        while (p10 < x) {
            p10 *= 10;
        }
        if (p10 > 100) {
            p10 /= 100;
           n = p10 * (n / p10);
        }
        return n;
    }
    
    private static HashSet <String> getQueryTerms(String query) {
        query = query.toLowerCase();
        ArrayList <String> terms = new ArrayList <>();
        String [] tokens = query.split(" ");
        boolean [] used = new boolean[tokens.length];
        
        Arrays.fill(used, false);
        
        // Step 1. Identify numerical values with units. 
        // For e.g 12 mm, 12mm. Exclude these terms from consideration in the following steps.
        
        String [] newTokens = new String[tokens.length];
        int ptr = 0;
        
        for (int i = 0; i < tokens.length - 1; ++i) {
            terms.add(tokens[i + 1] + ":" + tokens[i]);
            terms.add(tokens[i] + ":" + tokens[i + 1]);
        }
        
        for (int i = 0; i < tokens.length; ++i) if (!used[i]) {
            
            if (tokens[i].equals("rs")) {
                if (i + 1 < tokens.length && isNumber(tokens[i + 1])) {
                    int cost = roundOf((int)Double.parseDouble(tokens[i + 1]));
                    terms.add("rs:" + cost);
                    if (i > 0) {
                        terms.add(tokens[i - 1] + ":" + cost);
                    }
                    if (i + 2 < tokens.length) {
                        terms.add(tokens[i + 2] + ":" + cost);   
                    }
                    used[i] = true;
                    used[i + 1] = true;
                    continue;
                }
            }
            
            if (tokens[i].startsWith("rs") && tokens[i].length() > 2) {
                if (isNumber(tokens[i].substring(2, tokens[i].length()))) {
                    int cost = roundOf((int)Double.parseDouble(tokens[i].substring(2, tokens[i].length())));
                    terms.add("rs:" + cost);
                    if (i > 0) {
                        terms.add(tokens[i - 1] + ":" + cost);
                    }
                    if (i + 1 < tokens.length) {
                        terms.add(tokens[i + 1] + ":" + cost);   
                    }
                    used[i] = true;
                    continue;
                }
            }
            
            // check for type 12mm
            for (int j = 0; j < units.length; ++j) if (!units[j].equals("x") && units[j].length() < tokens[i].length()) {
                String prefix = tokens[i].substring(0, tokens[i].length() - units[j].length());
                if (tokens[i].endsWith(units[j]) && isNumber(prefix)) {
                    int val = roundOf((int)Double.parseDouble(prefix));
                    used[i] = true;
                    terms.add(units[j] + ":" + val);
                    if (i > 0) {
                        terms.add(tokens[i - 1] + ":" + val);
                    }
                    if (i + 1 < tokens.length) {
                        terms.add(tokens[i + 1] + ":" + val);
                    }
                    break;
                }
            }
                
            if (used[i]) {
                continue;
            }
            
            // type 12 mm
            if (isNumber(tokens[i])) {
                int val = roundOf((int)Double.parseDouble(tokens[i]));
                if (i + 1 < tokens.length) {
                    for (int j = 0; j < units.length; ++j) if (!units[j].equals("x")) {
                        if (tokens[i + 1].equals(units[j])) {
                            used[i] = true;
                            used[i + 1] = true;
                            terms.add(units[j] + ":" + val);
                            if (i > 0) {
                                terms.add(tokens[i - 1] + ":" + val);
                            }

                            if (i + 2 < tokens.length) {
                                terms.add(tokens[i + 2] + ":" + val);
                            }
                            break;
                        }
                    }
                }
            }
        }
        
        // try for 100 X 200 and 100 x
        
        for (int i = 0; i < tokens.length; ++i) if (!used[i]) {
            // type 100x200
            int p = isSize(tokens[i]);
            if (p >= 0) {
                int length = roundOf((int)Double.parseDouble(tokens[i].substring(0, p)));
                int breadth = roundOf((int)Double.parseDouble(tokens[i].substring(p + 1, tokens[i].length())));
                used[i] = true;
                terms.add(length + "x" + breadth);
                if (i > 0) {
                    terms.add(tokens[i - 1] + ":" +length + "x" + breadth);
                }

                if (i + 1 < tokens.length) {
                    terms.add(tokens[i + 1] + ":" +length + "x" + breadth);
                }

                continue;
            }
            
            // type 100 x 200
            if (isNumber(tokens[i])) {
                if (i + 2 < tokens.length) {
                    if (tokens[i + 1].equals("x") && isNumber(tokens[i + 2])) {
                        int length = roundOf((int)Double.parseDouble(tokens[i]));
                        int breadth = roundOf((int)Double.parseDouble(tokens[i + 2]));
                        used[i] = true;
                        used[i + 1] = true;
                        used[i + 2] = true;
                        terms.add(length + "x" + breadth);
                        if (i > 0) {
                            terms.add(tokens[i - 1] + ":" + length + "x" + breadth);
                        }

                        if (i + 3 < tokens.length) {
                            terms.add(tokens[i + 3] + ":" +length + "x" + breadth);
                        }
                        continue;
                    }
                }
                
                if (i + 1 < tokens.length) {
                    if (tokens[i + 1].equals("x")) {
                        used[i] = true;
                        used[i + 1] = true;
                        int val = roundOf((int)Double.parseDouble(tokens[i]));
                        terms.add("x:" + val);
                        if (i > 0) {
                            terms.add(tokens[i - 1] + ":" + val);
                        }

                        if (i + 2 < tokens.length) {
                            terms.add(tokens[i + 2] + ":" + val);   
                        }
                        continue;
                    }
                }
            }
            
            if (tokens[i].endsWith("x") && tokens[i].length() > 1) {
                String prefix = tokens[i].substring(0, tokens[i].length() - 1);
                if (isNumber(prefix)) {
                    int val = roundOf((int)Double.parseDouble(prefix));
                    terms.add("x:" + val);
                    if (i > 0) {
                        terms.add(tokens[i - 1] + ":" + val);
                    }

                    if (i + 1 < tokens.length) {
                        terms.add(tokens[i + 1] + ":" + val);   
                    }
                    continue;
                }
            }
        }
        
        for (int i = 0; i < tokens.length; ++i) if (!used[i]) {
            terms.add(tokens[i]);
        }
        
        ArrayList<String> uniqueTerms = new ArrayList<>();
        
        HashSet <String> hash = new HashSet<>();
        
//        System.err.println("Generated query terms :");
        for (String term : terms) {
            if (!hash.contains(term)){
                hash.add(term);
//                System.err.println(term);
            }
        }
        return hash;
    }
    
    static final double alpha = 0.8;
    
    static class DocScore implements Comparator<DocScore>, Comparable<DocScore>{
        int docId;
        double score;

        public DocScore() {
        }
        
        public DocScore(int docId, double score) {
            this.docId = docId;
            this.score = score;
        }
        
        @Override
        public int compareTo(DocScore o) {
            if (this.score < o.score) return 1;
            if (this.score > o.score) return -1;
            return 0;
        }

        @Override
        public int compare(DocScore t, DocScore t1) {
            if (t.score < t1.score) return 1;
            if (t.score > t1.score) return -1;
            return 0;
        }
    }
    
    static final int NUMRESULTS = 20;
    static final int NUMDOCS = 418;
    
    private static void calcResult(String query) {
        HashSet <String> terms = getQueryTerms(query);
        
        for (String term : terms) {
            System.err.println(term);
        }
        
        ArrayList <DocScore> scores = new ArrayList <>();
        ArrayList <Double> rating = new ArrayList <>();
        
        {
            String file = "rating.txt";
            try {
                Scanner sc = new Scanner(new FileInputStream(file));
                while (sc.hasNext()) {
                    String line = sc.nextLine();
                    rating.add(Double.parseDouble(line));
                }
            } catch(Exception ex) {
                System.err.println("Unable to open rating.txt");
                ex.printStackTrace();
            }
        }
        
        for (int id = 1; id <= NUMDOCS; ++id) {
            String file = "Documents/Document_" + id + ".txt";
            try {
                Scanner sc = new Scanner(new FileInputStream(file));
                String line;
                String [] tokens;
                HashSet <String> first = new HashSet <String>();
                HashSet <String> second = new HashSet <String>();
                HashSet <String> third = new HashSet <String>();
                
                line = sc.nextLine();
                tokens = line.split(" ");
                for (String token : tokens)
                    first.add(token);
                
                line = sc.nextLine();
                tokens = line.split(" ");
                for (String token : tokens)
                    second.add(token);
                
                line = sc.nextLine();
                tokens = line.split(" ");
                for (String token : tokens)
                    third.add(token);
                
                double curRating = 0.0;
                if (id <= rating.size()) {
                    curRating = rating.get(id - 1);
                    curRating /= 5.0;
                    if (curRating == 0.0) {
                        curRating = 0.5;
                    }
                }
                
                double score = 0;
                for (String term : terms) {
                    if (first.contains(term)) {
                        score += 3.0 * alpha + (1.0 - alpha) * curRating;
                    } else if (second.contains(term)) {
                        score += 2.0 * alpha + (1.0 - alpha) * curRating;;
                    } else if (third.contains(term)) {
                        score += 1.0 * alpha + (1.0 - alpha) * curRating;;
                    }
                }
                
                if (score > 1) System.err.println(id + " " + curRating + " " + score);
                scores.add(new DocScore(id, score));
                
                sc.close();
                
            } catch (FileNotFoundException ex) {
                System.err.println("Could not open file " + file);
                continue;
            }
        }
        Collections.sort(scores);
        
        ArrayList <Integer> ranking = new ArrayList<>();
//        ArrayList <Integer> cur = new ArrayList<>();
//        int prevScore = -1;
//        for (DocScore o : scores) {
//            System.err.println(o.docId + " -> " + o.score + " -> " + o.finalScore);
//            if (o.score == prevScore) {
//                cur.add(o.docId);
//            } else {
////                Collections.shuffle(cur, new Random(System.currentTimeMillis()));
//                for (int docId : cur) {
//                    ranking.add(docId);
////                    System.err.println("Adding " + docId);
//                }
//                    
//                cur.clear();
//                cur.add(o.docId);
//                prevScore = o.score;
//            }
//        }
//        Collections.shuffle(cur, new Random(System.currentTimeMillis()));
//        for (int docId : cur) {
//            ranking.add(docId);
//    //      System.err.println("Adding " + docId);
//        }
        
        for (DocScore o : scores) {
            System.err.println(o.docId + " score -> " + o.score);
            ranking.add(o.docId);
        }
        
        for (int docId : ranking) {
            System.out.println(docId);
        }
        
        
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("PastResults.txt", true)));
            for (int i = 0; i < 20; ++i) {
                out.print(ranking.get(i) + " ");
            }
            out.println("");
            out.close();
        } catch (IOException e) {
            //exception handling left as an exercise for the reader
        }
        
    }
    
//dimension 100X200 length 100mm 100 mm zoom 100 x rs 500
    public static void main(String[] args) {
        if (args.length > 0) {
            String query = args[0];
            for (int i = 1; i < args.length; ++i) {
                query = query + " " + args[i];
            }
            calcResult(query);
        }
    }
}
