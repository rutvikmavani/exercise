package Grep;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Grep {


    private final static int BUFFER_SIZE = 1024 * 8;
    private static class MatchingCriteriaDetails {
        String keywordToSearch;
        String flags;               // to do
        int[] LPS;

        MatchingCriteriaDetails(String keywordToSearch,String flags) {
            this.keywordToSearch = keywordToSearch;
            this.flags = flags;
            LPS = preProcess(keywordToSearch);
        }

        public String getKeywordToSearch() {
            return keywordToSearch;
        }

        public void setKeywordToSearch(String keywordToSearch) {
            this.keywordToSearch = keywordToSearch;
        }

        public String getFlags() {
            return flags;
        }

        public void setFlags(String flags) {
            this.flags = flags;
        }

        public int[] getLPS() {
            return LPS;
        }

        public void setLPS(int[] LPS) {
            this.LPS = LPS;
        }
    }

    public static void main(String args[]) {

        long programStartTime = System.currentTimeMillis();

        int argumentsLength = args.length;
        if (argumentsLength <= 1) {
            System.out.println("usage : java Grep [-flags] [keywordToSearch] [file/directory path ...]");
            return;
        }

        int argCount = 0;
        String flags = null;
        if (args[0].charAt(0) == '-') {
            flags = args[0];
            argCount++;
        }
        String keywordToSearch = args[argCount++];


        MatchingCriteriaDetails matchingCriteriaDetails = new MatchingCriteriaDetails(keywordToSearch,flags);
        while(argCount < argumentsLength) {
            String filePath = args[argCount++];
            try {
                matchFinder(matchingCriteriaDetails, filePath);
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }

        long programEndTime = System.currentTimeMillis();

        long ExecutionTimeInMilli = (programEndTime - programStartTime);
        System.out.println("program execution time in milli seconds : " + ExecutionTimeInMilli);
        System.out.println(programEndTime - programStartTime);
    }

    private static void matchFinder(MatchingCriteriaDetails matchingCriteriaDetails,String filePath) throws IOException {

        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println(filePath + " : No such file or directory");
            return;
        }

        if (file.isDirectory()) {
            directoryExplorer(matchingCriteriaDetails,file);
        } else {
            fileMatcher(matchingCriteriaDetails,file);
        }
    }

    private static void directoryExplorer(MatchingCriteriaDetails matchingCriteriaDetails, File folder) throws IOException {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                directoryExplorer(matchingCriteriaDetails,file);
            } else {
                fileMatcher(matchingCriteriaDetails,file);
            }
        }
    }

    private static void fileMatcher(MatchingCriteriaDetails matchingCriteriaDetails,File file) throws IOException {

        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

        char[] buf = new char[BUFFER_SIZE];

        String keywordToSearch = matchingCriteriaDetails.getKeywordToSearch();
        int[] LPS = matchingCriteriaDetails.getLPS();

        int q = 0;
        int keywordLen = keywordToSearch.length();
        int lineNumber = 1;
        List<Integer> matchedLineNumbers = new ArrayList<>();

        int charactersRead = 0;
        while((charactersRead = bufferedReader.read(buf,0,BUFFER_SIZE)) != -1) {

            for(int i=0;i<charactersRead;i++) {
                if (buf[i] == '\n')
                    lineNumber++;

                while (q > 0 && keywordToSearch.charAt(q) != buf[i])
                    q = LPS[q-1];
                if (keywordToSearch.charAt(q) == buf[i])
                    q++;
                if (q == keywordLen) {
                    /* match found */
                    q = LPS[q-1];
                    matchedLineNumbers.add(lineNumber);
                }
            }

        }

        if (matchedLineNumbers.size() == 0)
            System.out.println("No match found in file : " + file.getPath());
        else {
            for (Integer matchedLineNumber : matchedLineNumbers) {
                System.out.println(file.getPath() + " : " + matchedLineNumber);
            }
        }
    }

    private static int[] preProcess(String pattern) {
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