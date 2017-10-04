package com.company;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/* * Java Program read a text file in multiple way.
* This program demonstrate how you can use FileReader,
* * BufferedReader, and Scanner to read text file,
* * along with newer utility methods added in JDK 7
* * and 8.
 * */
public class readFileByScanner {
    public static void main(String[] args) throws Exception {
        // reading a text file line by line using Scanner
        System.out.println("Reading a text file line by line: ");
        Scanner sc = new Scanner(new File("res/glazing_info.txt"));
        while (sc.hasNext()) {
            String str = sc.nextLine();
            System.out.println(str);
            String [] words = str.split("\\s");
            for (String s : words) System.out.println(s);
        }
        sc.close();
        // reading all words from file using Scanner
        System.out.println("Reading a text file word by word: ");
        Scanner sc2 = new Scanner(new File("res/file.txt"));
        while (sc2.hasNext()) {
            String word = sc2.next();
            System.out.println(word);
        }
        sc2.close();
    }

    public int numOfLines_method1(String fileName) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        int lines = 0;
        while (reader.readLine() != null) lines++;
        reader.close();
        return lines;
    }

    public int numOfLines_method2(String fileName) throws IOException{
        return 1;

    }
}
//Output
//Reading a text file line by line:
// Java, JavaScript, Jython
// Reading a text file word by word:
// Java,
// JavaScript,
// Jython