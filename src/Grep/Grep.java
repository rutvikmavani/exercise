package Grep;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class Grep {

    private static class MatchingCriteriaDetails {
        private String keywordToSearch;
        private byte[] keywordToSearchByteArray;
        private Set<Character> flags;
        private int[] LPS;
        private int numberOfThreads;
        private ExecutorService executorService;

        MatchingCriteriaDetails(String keywordToSearch,String flagStr,int numberOfThreads) {
            this.keywordToSearch = keywordToSearch;
            this.keywordToSearchByteArray = keywordToSearch.getBytes();

            this.flags = new HashSet<>();
            insertFlagsFromStr(flags,flagStr);

            this.LPS = preProcess(this.keywordToSearchByteArray);
            this.numberOfThreads = numberOfThreads;
            this.executorService = new MyThreadPoolExecutorVersion1(numberOfThreads);
        }

        private void insertFlagsFromStr(Set<Character> flags, String flagStr) {
            if (flagStr == null)
                return;

            // flagsStr.charAt(0) is '-'
            for (int i = 1; i < flagStr.length(); i++) {
                flags.add(flagStr.charAt(i));
            }
        }

        public boolean containsFlag(char f) {
            return flags.contains(f);
        }

    }

    public static void main(String args[]) throws InterruptedException, FileNotFoundException {

        long programStartTime = System.currentTimeMillis();

        int argumentsLength = args.length;
        if (argumentsLength <= 1) {
            System.out.println("usage : java Grep [-flags] [keywordToSearch] [-numberOfThreads] [file/directory path ...]");
            return;
        }

        // flags if provided by user
        int argCount = 0;
        String flags = null;
        if (args[0].charAt(0) == '-') {
            flags = args[0];
            argCount++;
        }

        // keyword to search
        String keywordToSearch = args[argCount++];

        // extract number of threads if provided by user
        int numberOfThreads = 1;    // default
        if (argCount < argumentsLength) {
            int result = extractNumberOfThreads(args[argCount]);
            if (result != -1) {
                numberOfThreads = result;
                argCount++;
            }
        }

        MatchingCriteriaDetails matchingCriteriaDetails = new MatchingCriteriaDetails(keywordToSearch,flags,numberOfThreads);

        // file/directory names
        while(argCount < argumentsLength) {
            String filePath = args[argCount++];
            try {
                matchingFromInitialFilePath(matchingCriteriaDetails, filePath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        matchingCriteriaDetails.executorService.shutdown();
        matchingCriteriaDetails.executorService.awaitTermination(10, TimeUnit.MINUTES);

        long programEndTime = System.currentTimeMillis();

        System.out.println("program execution time in milli seconds : " + (programEndTime - programStartTime));

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
        for (File file : Objects.requireNonNull(folder.listFiles())) {
            if (file.isDirectory()) {
                if (matchingCriteriaDetails.containsFlag('r'))
                    matchingFromDirectory(matchingCriteriaDetails,file);
            } else {
                matchingCriteriaDetails.executorService.execute(() -> {
                    try {
                        matchingFromFile(matchingCriteriaDetails, file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }

    private static void matchingFromFile(MatchingCriteriaDetails matchingCriteriaDetails, File file) throws IOException {

        byte[] keywordToSearchArray = matchingCriteriaDetails.keywordToSearchByteArray;
        int keywordLen = keywordToSearchArray.length;
        int[] LPS = matchingCriteriaDetails.LPS;

        int lineNumber = 1;
        List<Integer> matchedLineNumbers = new ArrayList<>();

        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] text = new byte[1024];
        int byteRead = 0;
        int q = 0;
        while ((byteRead = fileInputStream.read(text)) != -1) {

            for(int i=0;i<byteRead;i++) {

                while (q > 0 && keywordToSearchArray[q] != text[i])
                    q = LPS[q-1];

                if (keywordToSearchArray[q] == text[i])
                    q++;

                if (q == keywordLen) {
                    /* match found */
                    q = LPS[q-1];
                    matchedLineNumbers.add(lineNumber);
                }
                if (text[i] == '\n')
                    lineNumber++;
            }
        }
        fileInputStream.close();
        printResults(file.getPath(),matchedLineNumbers,matchingCriteriaDetails);
    }

    private static void printResults(String filepath, List<Integer> matchedLineNumbers, MatchingCriteriaDetails matchingCriteriaDetails) {
        // print results according to flags
        if (matchingCriteriaDetails.containsFlag('c')) {
            System.out.println(filepath + " : " + matchedLineNumbers.size());
        }
        else {
            if (matchedLineNumbers.size() == 0)
                System.out.println("No match found in file : " + filepath);
            else {
                for (Integer matchedLineNumber : matchedLineNumbers) {
                    System.out.println(filepath + " : " + matchedLineNumber);
                }
            }
        }
    }

    private static int[] preProcess(byte[] pattern) {
        int patternLen = pattern.length;
        int prefix[] = new int[patternLen];
        prefix[0] = 0;
        int k = 0;
        for(int i=1;i<patternLen;i++) {
            while (k > 0 && pattern[k] != pattern[i])
                k = prefix[k-1];
            if (pattern[k] == pattern[i])
                k++;
            prefix[i] = k;
        }

        return prefix;
    }

    // if valid string is entered returns number of threads
    // else return -1
    private static int extractNumberOfThreads(String str) {
        if (str == null || str.length() < 1)
            return -1;

        for(int i=1;i<str.length();i++) {
            if (str.charAt(i) < '0' || str.charAt(i) > '9')
                return -1;
        }

        int num = 0;
        for(int i=1;i<str.length();i++) {
            int digit = (str.charAt(i) - '0');
            num = num * 10 + digit;
        }
        return num;
    }
}