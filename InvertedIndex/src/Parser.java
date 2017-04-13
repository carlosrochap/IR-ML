import java.io.*;
import java.util.*;

/**
 *
 * Course #: 612
 * Lab #: 1
 * Author: Carlos Rocha
 *
 */
public class Parser {

    List<String> stopWords = new ArrayList<>();

    /**
     * Creates an instance of this class and create a list of sorted stop-words
     * from a local file
     *
     * @param stopWordsFile path of the file containing the stop-words to be used
     * @throws FileNotFoundException
     */
    public Parser(String stopWordsFile) throws FileNotFoundException {
        File file = new File(stopWordsFile);

        Scanner scan = new Scanner(file);

        while(scan.hasNextLine()){
            stopWords.add(scan.nextLine().toLowerCase());
        }

        Collections.sort(stopWords, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.compareTo(s2);
            }
        });
    }

    /**
     * Checks whether a particular word is in the stop-words list
     * @param key word to be checked
     * @return an numeric indicator of the search result
     */

    public int searchStopWords(String key) {
        int lo = 0;
        int hi = stopWords.size() - 1;

        while (lo <= hi) {
            int mid = lo + (hi-lo)/2;//(hi+lo) / 2;
            int result = key.compareTo(stopWords.get(mid));

            if (result< 0)
                hi = mid-1;
            else if (result > 0 )
                lo = mid + 1;
            else
                return mid;

        }

        return -1;
    }

    /**
     * Removes all stop-words in a given text and get stemmed form of
     * each word that is not considered an stop-word
     *
     * @param text the text to be processed
     * @return list of stemms of each non stop-word in the text
     */
    public ArrayList<String> parse(String text) {

        String[] tokens = text.split("[ '\":,.?!$%()\\-\\*\\+]+");
        ArrayList<String> stemms = new ArrayList<>();

        for (String token: tokens) {

            if (this.searchStopWords(token) < 0 && !token.equals("")) {
                Stemmer st = new Stemmer();
                st.add(token.toCharArray(), token.length());
                st.stem();
                String finalWord = st.toString();
                stemms.add(finalWord);
            }

        }

        return stemms;
    }

}
