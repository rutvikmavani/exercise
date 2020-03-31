package Grep;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Grep {
    public static void main(String args[]) throws IOException {

        File file = new File(args[1]);
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

        String pattern = args[0];
        int prefix[] = preProcess(pattern);
        String str = null;

        List<Integer> matchedLineNumbers = new ArrayList<>();
        int lineNumber = 1;
        while ((str = bufferedReader.readLine()) != null) {
            boolean matchExist = patternMatching(str,pattern,prefix);
            if (matchExist) {
                matchedLineNumbers.add(lineNumber);
            }
            lineNumber++;
        }

        if (matchedLineNumbers.size() == 0)
            System.out.println("No match found");
        else {
            for (Integer matchedLineNumber : matchedLineNumbers)
            System.out.println("Match found at line number : " + matchedLineNumber);
        }
    }
    public static boolean patternMatching(String text,String pattern,int prefix[]) {

        int textLen = text.length();
        int patternLen = pattern.length();

        int q = 0;
        for(int i=0;i<textLen;i++) {
            while (q > 0 && pattern.charAt(q) != text.charAt(i))
                q = prefix[q-1];

            if (pattern.charAt(q) == text.charAt(i))
                q++;
            if (q == patternLen)
                return true;
        }
        return false;
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