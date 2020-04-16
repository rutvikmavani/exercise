package Grep;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.concurrent.ExecutorService;

public class Grep {

    private static class MatchingCriteriaDetails {
        private String keywordToSearch;
        private Set<Character> flags;
        private int[] LPS;
        private int numberOfThreads;
        private ExecutorService executorService;

        MatchingCriteriaDetails(String keywordToSearch,String flagStr,int numberOfThreads) {
            this.keywordToSearch = keywordToSearch;

            this.flags = new HashSet<>();
            insertFlagsFromStr(flags,flagStr);

            this.LPS = preProcess(keywordToSearch);
            this.numberOfThreads = numberOfThreads;
            this.executorService = new MyThreadPoolExecutor(numberOfThreads);
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
        while (!matchingCriteriaDetails.executorService.isTerminated()) {
            Thread.sleep(1000);
        }

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

                if (matchingCriteriaDetails.numberOfThreads > 1) {
                    matchingCriteriaDetails.executorService.execute(() -> {
                        try {
                            matchingFromFileRandomAccess(matchingCriteriaDetails, file);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
                else {
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
    }

    private static void matchingFromFile(MatchingCriteriaDetails matchingCriteriaDetails, File file) throws IOException {

        String keywordToSearch = matchingCriteriaDetails.keywordToSearch;
        int keywordLen = keywordToSearch.length();
        int[] LPS = matchingCriteriaDetails.LPS;

        int lineNumber = 1;
        List<Integer> matchedLineNumbers = new ArrayList<>();

        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

        String str;
        while ((str = bufferedReader.readLine()) != null) {

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

        printResults(file.getPath(),matchedLineNumbers,matchingCriteriaDetails);
    }


    private static void matchingFromFileRandomAccess(MatchingCriteriaDetails matchingCriteriaDetails, File file) throws IOException {

        String keywordToSearch = matchingCriteriaDetails.keywordToSearch;
        int keywordLen = keywordToSearch.length();
        int[] LPS = matchingCriteriaDetails.LPS;

        int lineNumber = 1;
        List<Integer> matchedLineNumbers = new ArrayList<>();

        RandomAccessFile randomAccessFile = new RandomAccessFile(file.getPath(), "r");
        FileChannel inChannel = randomAccessFile.getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        int q = 0;
        int charRead = 0;
        while((charRead = inChannel.read(buffer)) > 0)
        {
            buffer.flip();
            for (int i = 0; i < charRead; i++)
            {
                char c = (char) buffer.get();
                while (q > 0 && keywordToSearch.charAt(q) != c)
                    q = LPS[q-1];

                if (keywordToSearch.charAt(q) == c)
                    q++;

                if (q == keywordLen) {
                    /* match found */
                    q = LPS[q-1];
                    matchedLineNumbers.add(lineNumber);
                }
                if (c == '\n')
                    lineNumber++;
            }
            buffer.clear();
        }
        inChannel.close();
        randomAccessFile.close();

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