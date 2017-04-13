import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 *
 * Course #: 612
 * Lab #: 1
 * Author: Carlos Rocha
 *
 */
public class InvertedIndex {
    private Map<String, ArrayList<Integer>> invertedIndex;//
    private ArrayList<String> documents;
    private Parser parser;

    /**
     * Instantiates this class, loads the stop-words list form a txt file located
     * in docPath, parse the stop-words using the Parser class and call indexDocuments()
     * method to create posting lists matrix
     *
     * @param docPath location of the folder containing the stopwords.txt file and documents folder
     *
     */
    public InvertedIndex(String docPath){

        invertedIndex = new HashMap<>();
        documents = new ArrayList<>();

        String stopWords = docPath + "/stopwords.txt";

        //create parser instance passing a stopwords text documents as parameter
        try {
            parser = new Parser(stopWords);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        indexDocuments(docPath);
    }

    /**
     * Read all files in folderName and parse each file content using the Parser class,
     * then constructs postings lists matrix with the stemms returned by the Parser object
     *
     * @param folderName location of the folder containing the documents folder with
     *                   all txt files to be processed
     * @return void
     */
    private void indexDocuments(String folderName){
        String documentsFolder = folderName + "/documents";

        File folder = new File(documentsFolder);
        File[] listOfFiles = folder.listFiles();
        List<String> stemms;// = new ArrayList<>();

        for (int i=0; i < listOfFiles.length; i++) {
            documents.add(i, listOfFiles[i].getName());

            try {

                Scanner scan = new Scanner(listOfFiles[i]);
                String allLines = "";

                while(scan.hasNextLine()){
                    allLines += scan.nextLine().toLowerCase();
                }

                stemms = parser.parse(allLines);

                for (String steam: stemms) {

                    //add new term:postingList if not present already and
                    // add document id to postings list only if doesn't exist already
                    if (invertedIndex.containsKey(steam)){

                        if(!invertedIndex.get(steam).contains(i)){
                            invertedIndex.get(steam).add(i);
                        }
                    } else {
                        ArrayList<Integer> docList = new ArrayList<>();
                        docList.add(i);
                        invertedIndex.put(steam, docList);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Searches for a word of phrase (multiple words) in the postings lists
     * and merges the matching postings lists using the merge method
     *
     * @param query the word of set of words to be used in the search
     * @param operator indicates whether the search should be treated using
     *                 AND|OR binary operators
     * @return the list of documentsIds that match the search query
     */

    public ArrayList<Integer> search(String query, int operator){
        ArrayList<Integer> postings = new ArrayList<>();
        ArrayList<String> stemmedTerms = parser.parse(query);
        ArrayList<String> terms = new ArrayList<>();

//        stemmedTerms.forEach(System.out::println);
        for (String term: stemmedTerms) {
            if (invertedIndex.containsKey(term)) {
                terms.add(term);
            }
        }
        if (operator == 2 || terms.size() == stemmedTerms.size()) {
            if(terms.size() == 1){
                postings = invertedIndex.get(terms.get(0));
                System.out.println("1.. " + terms.get(0));
            } else if (terms.size() >= 1) {

                Collections.sort(terms, new Comparator<String>() {
                    @Override
                    public int compare(String s1, String s2) {
                        int res = 0;
                        int diff = invertedIndex.get(s1).size() - invertedIndex.get(s2).size();

                        if (diff > 0) {
                            res = 1;
                        } else if (diff < 0) {
                            res = -1;
                        }

                        return res;
                    }
                });

                postings = invertedIndex.get(terms.get(0));

                System.out.println("1. " + terms.get(0));
                if (terms.size() > 1) {
                    for (int i=1; i < terms.size(); i++){
                        System.out.println((i+1)+". " + terms.get(i));
                        postings = merge(postings, invertedIndex.get(terms.get(i)), operator);
                    }
                }
            }
        }

        Collections.sort(postings);
        return postings;
    }

    /**
     * Merges two ArrayLists<Integer> in sorted order without duplicates.
     * The operator argument specifies whether all the the items in both lists should
     * be included or if the merging process should stop after all the elements
     * in the first or shorted list have been included
     *
     * @param posting1 the first and shorter postingList to merge
     * @param posting2 the second portingList to merge
     * @param operator denotes whether postings should be merged using AND|OR binary operators
     * @return         the resulting combination of posting
     */
    private ArrayList<Integer> merge(ArrayList<Integer> posting1, ArrayList<Integer> posting2, int operator) {
        ArrayList<Integer> mergedPostings = new ArrayList<>();

        int p1 = 0;
        int p2 = 0;

        while (p1 < posting1.size() && p2 < posting2.size()){

            int docId1 = posting1.get(p1);
            int docId2 = posting2.get(p2);

            if (docId1 == docId2) {
                mergedPostings.add(docId1);
                p1++;
                p2++;
            } else if (docId1 < docId2){
                p1++;
                if(operator == 2)  mergedPostings.add(docId1);
            } else {
                p2++;
                if(operator == 2)  mergedPostings.add(docId2);
            }
        }

        if (operator == 2){
            for (int i=p2; i<posting2.size(); i++){
                mergedPostings.add(posting2.get(i));
            }
        }

        return mergedPostings;
    }


    /**
     * Creates an string representation of the InvertedIndex object
     *
     * @return the generated string representing the InvertedIndex object
     */
    public String toString(){
        String output = "";
        for(Map.Entry<String, ArrayList<Integer>> term : invertedIndex.entrySet()) {

            output += term.getKey()+": ";
            for (Integer docId: term.getValue()) {
                output += docId+", ";
            }
            output += "\n";
        }
        return output;
    }

    /**
     * Creates an console menu for using this program in an iterative manner
     *
     * @return the option selected by the user from the presented menu
     */
    private int getOption(){
        System.out.println("Select one of the options below:");
        System.out.println("1. Search for one word or multiple using AND");
        System.out.println("2. Search for multiple words using OR");
        System.out.println("3. Exit");

        System.out.print("Option:");
        Scanner scanner = new Scanner(System.in);
        return scanner.nextInt();

    }

    /**
     * Starts the execution of the program.
     *
     * expects to receive the data folder with the following structure
     * -data
     *     -stopwords.txt
     *     -documents
     *          -document-1.txt
     *          -document-2.txt
     *          .
     *          .
     *          -document-n.txt
     *
     */
    public static void main(String[] args){

        String folder = args.length > 0 ? args[0] : "./data";

        InvertedIndex invertedIndex = new InvertedIndex(folder);

        while (true){
            System.out.flush();
            int option = invertedIndex.getOption();
            if (option == 3) {
                break;
            }
            ArrayList<Integer> documentIds = new ArrayList<>();
            System.out.print("Please insert your query:");
            Scanner scanner = new Scanner(System.in);
            String query = scanner.nextLine();

            switch (option){
                case 1:
                    documentIds = invertedIndex.search(query, 1);
                    break;

                case 2:
                    documentIds = invertedIndex.search(query, 2);
                    break;

                default:
                    System.out.println("Please select a valid option from the menu!");
                    break;
            }

            System.out.println("\nResults:");
            for (int docId: documentIds) {
                System.out.println(docId+". "+invertedIndex.documents.get(docId));
            }

            System.out.println("*Hit enter to show menu");
            try {
                System.in.read();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }
}
