package sks.entity;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class FileKeywordsGen3 {

    public FileKeywords Gen(int index, String[] keywords){
        FileKeywords fk = new FileKeywords();
        fk.index = index;
        fk.keywords = keywords;

        return fk;
    }

    public static void main(String args[]){
        FileKeywordsGen1 fkg1 = new FileKeywordsGen1();

        FileKeywords[] fks1 = new FileKeywords[7];

        String[] str1= {"10", "12"};
        String[] str2 = {"12", "13", "14", "15"};
        String[] str3 = {"14", "15"};
        String[] str4 = {"10", "12", "13"};
        String[] str5 = {"11", "13"};
        String[] str6 = {"12", "13"};
        String[] str7 = {"11", "13"};

        fks1[0] = fkg1.Gen(1, str1);
        fks1[1] = fkg1.Gen(2, str2);
        fks1[2] = fkg1.Gen(3, str3);
        fks1[3] = fkg1.Gen(4, str4);
        fks1[4] = fkg1.Gen(5, str5);
        fks1[5] = fkg1.Gen(6, str6);
        fks1[6] = fkg1.Gen(7, str7);

        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("FileKeywords3.dat"));
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
