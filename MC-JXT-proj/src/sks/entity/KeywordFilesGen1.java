package sks.entity;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class KeywordFilesGen1 {
	
	public sks.entity.KeywordFiles[] Gen(sks.entity.FileKeywords[] fks1){
		Map<String, Vector<Integer>> keywordfile = new HashMap<String, Vector<Integer>>();
		
		for(int i=0; i<fks1.length; i++){
			for(int j=0; j<fks1[i].keywords.length; j++){
				Vector<Integer> vc = keywordfile.get(fks1[i].keywords[j]);
				if( vc == null){
					vc = new Vector<Integer>();
					vc.add(fks1[i].index);
					keywordfile.put(fks1[i].keywords[j], vc);
				}
				else{
					vc.add(fks1[i].index);
					keywordfile.put(fks1[i].keywords[j], vc);
				}
			}
		}
		int n=0;

		Set<String> keywordsSet = keywordfile.keySet();
		Object[] keywords = keywordsSet.toArray();
		sks.entity.KeywordFiles[] kfs1 = new sks.entity.KeywordFiles[300];
		for(int i=1700; i<2000; i++){
			System.out.println(keywords.length);

			kfs1[n] = new sks.entity.KeywordFiles();
			kfs1[n].keyword = (String) keywords[i];
			Vector<Integer> files = keywordfile.get(keywords[i]);
			kfs1[n].files = new int[files.size()];
			for(int j=0; j<files.size(); j++){
				kfs1[n].files[j] = files.get(j);
			}
			n++;
			System.out.println(n);

		}
		
		return kfs1;
	}
	
	public static void main(String[] args){
		
		try {
			
			ObjectInputStream in = new ObjectInputStream(new FileInputStream("FileKeywords.dat"));
			sks.entity.FileKeywords[] fks1 = (FileKeywords[]) in.readObject();
			in.close();

			KeywordFiles[] kfs1 = new KeywordFilesGen1().Gen(fks1);
			
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("KeywordFiles33.dat"));
			out.writeObject(kfs1);
			out.close();

			for(int i=0; i<kfs1.length; i++){
				System.out.print("\nKeyword: " + kfs1[i].keyword + " files: ");
				for(int j=0; j<kfs1[i].files.length; j++)
					System.out.print(kfs1[i].files[j] + " ");
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
