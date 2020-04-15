package Grep;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Grep {

    private static class MatchingCriteriaDetails {
        String keywordToSearch;
        Set<Character> flags;
        int[] LPS;
        ExecutorService executorService;

        MatchingCriteriaDetails(String keywordToSearch,String flagStr) {
            this.keywordToSearch = keywordToSearch;

            flags = new HashSet<>();
            insertFlagsFromStr(flags,flagStr);

            LPS = preProcess(keywordToSearch);
            executorService = Executors.newFixedThreadPool(16);
        }

        public String getKeywordToSearch() {
            return keywordToSearch;
        }

        private void insertFlagsFromStr(Set<Character> flags, String flagStr) {
            if (flagStr == null)
                return;

            for (int i = 1; i < flagStr.length(); i++) {
                flags.add(flagStr.charAt(i));
            }
        }

        public boolean containsFlag(char f) {
            return flags.contains(f);
        }
    }

    public static void main(String args[]) throws InterruptedException {

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
                matchingFromInitialFilePath(matchingCriteriaDetails, filePath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        matchingCriteriaDetails.executorService.shutdown();
        matchingCriteriaDetails.executorService.awaitTermination(3600, TimeUnit.SECONDS);
        long programEndTime = System.currentTimeMillis();

        long ExecutionTimeInMilli = (programEndTime - programStartTime);
        System.out.println("program execution time in milli seconds : " + ExecutionTimeInMilli);


    }

    private static void matchingFromInitialFilePath(MatchingCriteriaDetails matchingCriteriaDetails, String filePath) throws IOException {

        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println(filePath + " : No such file or directory");
            return;
        }

        if (file.isDirectory()) {
            matchingFromDirectory(matchingCriteriaDetails,file);
        } else {
            matchingFromFile(matchingCriteriaDetails,file);
        }
    }

    private static void matchingFromDirectory(MatchingCriteriaDetails matchingCriteriaDetails, File folder) throws IOException {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                if (matchingCriteriaDetails.containsFlag('r'))
                    matchingFromDirectory(matchingCriteriaDetails,file);
            } else {
                matchingCriteriaDetails.executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            matchingFromFile(matchingCriteriaDetails,file);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
//                long fileMatchStartTime = System.currentTimeMillis();
//                matchingFromFile(matchingCriteriaDetails,file);
//                long fileMatchEndTime = System.currentTimeMillis();
//                matchingCriteriaDetails.fileMatchingTime += (fileMatchEndTime - fileMatchStartTime);

            }
        }
    }

    private static void matchingFromFile(MatchingCriteriaDetails matchingCriteriaDetails, File file) throws IOException {

        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

        String keywordToSearch = matchingCriteriaDetails.getKeywordToSearch();
        int[] LPS = matchingCriteriaDetails.LPS;

        int keywordLen = keywordToSearch.length();
        int lineNumber = 1;
        List<Integer> matchedLineNumbers = new ArrayList<>();

        String str = null;
        long lineReadingTime = 0;
        while (true) {

            long lineReadingStartTime = System.currentTimeMillis();
            str = bufferedReader.readLine();
            long lineReadingEndTime = System.currentTimeMillis();
            if (str == null)
                break;

            int q = 0;
            for(int i=0;i<str.length();i++) {

                while (q > 0 && keywordToSearch.charAt(q) != str.charAt(i))
                    q = LPS[q-1];

                if (keywordToSearch.charAt(q) == str.charAt(i))
                    q++;

                if (q == keywordLen) {
                    /* match found */
                    q = LPS[q-1];
                    matchedLineNumbers.add(lineNumber);
                }
            }
            lineNumber++;
        }

        bufferedReader.close();



//        if (matchedLineNumbers.size() == 0)
//            System.out.println("No match found in file : " + file.getPath());
//        else {
//            for (Integer matchedLineNumber : matchedLineNumbers) {
//                System.out.println(file.getPath() + " : " + matchedLineNumber);
//            }
//        }
        System.out.println(file.getPath() + " : " + matchedLineNumbers.size());

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