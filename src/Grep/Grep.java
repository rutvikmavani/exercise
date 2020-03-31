package Grep;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Grep {
    public static void main(String args[]) throws IOException {

        String pattern = args[0];
        String filePath = args[1];

        final int BUFFER_SIZE = 1024 * 8;
        File file = new File(filePath);
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));


        char[] buf = new char[BUFFER_SIZE];
        int charactersRead = 0;

        int[] LPS = preProcess(pattern);
        int q = 0;
        int patternLen = pattern.length();
        int lineNumber = 1;
        List<Integer> matchedLineNumbers = new ArrayList<>();
        while((charactersRead = bufferedReader.read(buf,0,BUFFER_SIZE)) != -1) {

            for(int i=0;i<charactersRead;i++) {
                if (buf[i] == '\n')
                    lineNumber++;

                while (q > 0 && pattern.charAt(q) != buf[i])
                    q = LPS[q-1];
                if (pattern.charAt(q) == buf[i])
                    q++;
                if (q == patternLen) {
                    /* match found */
                    q = LPS[q-1];
                    matchedLineNumbers.add(lineNumber);
                }
            }

        }

        if (matchedLineNumbers.size() == 0)
            System.out.println("No match found");
        else {
            for (Integer matchedLineNumber : matchedLineNumbers) {
                System.out.println("Matching found at line number : " + matchedLineNumber);
            }
        }
    }

    public static int[] preProcess(String pattern) {
        int patternLen = pattern.length();
        int prefix[] = new int[patternLen];
        prefix[0] = 0;
        int k = 0;
        for(int i=1;i<patternLen;i++) {
            while (k > 0 && pattern.charAt(k) != pattern.charAt(i))
                k = prefix[k-1];
            if (pattern.charAt(k) == pattern.charAt(i))
                k++;
            prefix[i] = k;
        }

        return prefix;
    }
}