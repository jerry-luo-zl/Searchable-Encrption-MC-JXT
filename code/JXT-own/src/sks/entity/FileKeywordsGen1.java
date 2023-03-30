package sks.entity;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileKeywordsGen1 {
	
	public FileKeywords Gen(int index, String[] keywords){
		FileKeywords fk = new FileKeywords();
		fk.index = index;
		fk.keywords = keywords;
		
		return fk;
	}
	
	public static void main(String args[]){
		FileKeywordsGen1 fkg1 = new FileKeywordsGen1();
		FileKeywords[] fks1 = new FileKeywords[222];
		// 创建 reader

		

		try (BufferedReader br = Files.newBufferedReader(Paths.get("FileKeywords.data"))) {
			// CSV文件的分隔符
			String DELIMITER = ",";
			// 按行读取
			String line;
			int i=0;
			while ((line = br.readLine()) != null) {
				// 分割
				String[] columns = line.split(DELIMITER);
				// 打印行
				System.out.println("User["+ String.join(", ", columns) +"]");
				i++;
				fks1[i-1] = fkg1.Gen(i-1, columns);


			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}



		try {
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("FileKeywords5.dat"));
			out.writeObject(fks1);
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
