package sks.entity;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class KeywordFilesGen4 {

    public KeywordFiles[] Gen(FileKeywords[] fks2){
        Map<String, Vector<Integer>> keywordfile = new HashMap<String, Vector<Integer>>();

        for(int i=0; i<fks2.length; i++){
            for(int j=0; j<fks2[i].keywords.length; j++){
                Vector<Integer> vc = keywordfile.get(fks2[i].keywords[j]);
                if( vc == null){
                    vc = new Vector<Integer>();
                    vc.add(fks2[i].index);
                    keywordfile.put(fks2[i].keywords[j], vc);
                }
                else{
                    vc.add(fks2[i].index);
                    keywordfile.put(fks2[i].keywords[j], vc);
                }
            }
        }

        Set<String> keywordsSet = keywordfile.keySet();
        Object[] keywords = keywordsSet.toArray();
        KeywordFiles[] kfs2 = new KeywordFiles[keywords.length];
        for(int i=0; i<keywords.length; i++){
            kfs2[i] = new KeywordFiles();
            kfs2[i].keyword = (String) keywords[i];
            Vector<Integer> files = keywordfile.get(keywords[i]);
            kfs2[i].files = new int[files.size()];
            for(int j=0; j<files.size(); j++)
                kfs2[i].files[j] = files.get(j);
        }

        return kfs2;
    }

    public static void main(String[] args){

        try {

            ObjectInputStream in = new ObjectInputStream(new FileInputStream("FileKeywords3.dat"));
            FileKeywords[] fks2 = (FileKeywords[]) in.readObject();
            in.close();

            KeywordFiles[] kfs2 = new KeywordFilesGen2().Gen(fks2);

            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("KeywordFiles3.dat"));
            out.writeObject(kfs2);
            out.close();

            for(int i=0; i<kfs2.length; i++){
                System.out.print("\nKeyword: " + kfs2[i].keyword + " files: ");
                for(int j=0; j<kfs2[i].files.length; j++)
                    System.out.print(kfs2[i].files[j] + " ");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
