package sks.entity;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class FileKeywordsGen2 {
	
	public FileKeywords Gen(int index, String[] keywords){
		FileKeywords fk = new FileKeywords();
		fk.index = index;
		fk.keywords = keywords;
		
		return fk;
	}
	
	public static void main(String args[]){
		FileKeywordsGen2 fkg2 = new FileKeywordsGen2();
		
		FileKeywords[] fks2 = new FileKeywords[7];
		
		String[] str1= {"6", "7", "8", "9"};
		String[] str2 = {};
		String[] str3 = {"6", "7"};
		String[] str4 = { "6"};
		String[] str5 = { "6", "9"};
		String[] str6 = {"7", "9"};
		String[] str7 = {"7", "8", "9"};
		
		fks2[0] = fkg2.Gen(1, str1);
		fks2[1] = fkg2.Gen(2, str2);
		fks2[2] = fkg2.Gen(3, str3);
		fks2[3] = fkg2.Gen(4, str4);
		fks2[4] = fkg2.Gen(5, str5);
		fks2[5] = fkg2.Gen(6, str6);
		fks2[6] = fkg2.Gen(7, str7);
		
		try {
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("FileKeywords2.dat"));
			out.writeObject(fks2);
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
