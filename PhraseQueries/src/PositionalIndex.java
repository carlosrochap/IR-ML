import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

/**
 *
 * Course #: 612
 * Lab #: 2
 * Author: Carlos Rocha
 *
 */
public class PositionalIndex {
	String[] myDocs;
	ArrayList<String> termList;
	ArrayList<ArrayList<Doc>> docLists;

	/**
	 * Constructs a positional index from a list of documents
	 *
	 * @param docs List of input strings or file names
	 * 
	 */
	public PositionalIndex(String[] docs)
	{
        myDocs = docs;
		termList = new ArrayList<>();
		docLists = new ArrayList<>();

        ArrayList<Doc> docList;

		for (int i=0; i<docs.length; i++) {

			String[] tokens = docs[i].split(" ");

			for (int j=0; j < tokens.length; j++){

                Doc doc;
                String term = tokens[j];

				//adds term to term list
				if (!termList.contains(term)){

                    doc = new Doc(i, j);
                    docList = new ArrayList<>();

                    termList.add(term);
					docList.add(doc);
					docLists.add(docList);

				} else {

                    int postingsIndex = termList.indexOf(term);
                    docList = docLists.get(postingsIndex);

                    Comparator<Doc> c = new Comparator<Doc>() {
                        public int compare(Doc d1, Doc d2) {
                        	return new Integer(d1.docId).compareTo(d2.docId);
                        }
                    };

                    int index = Collections.binarySearch(docList, new Doc(i, j), c);

                    if (index < 0){
                        docList.add(new Doc(i,j));
                    } else {
                        doc = docList.get(index);
                        doc.insertPosition(j);
                        docList.set(index, doc);
                    }

                    docLists.set(postingsIndex, docList);
				}

			}

		}
	}

	/**
	 * Return the string representation of a positional index
	 */
	public String toString()
	{
		String matrixString = "";
		ArrayList<Doc> docList;
		for(int i=0;i<termList.size();i++){
				matrixString += String.format("%-15s", termList.get(i));
				docList = docLists.get(i);
				for(int j=0;j<docList.size();j++)
				{
					matrixString += docList.get(j)+ "\t";
				}
				matrixString += "\n";
			}
		return matrixString;
	}
	
	/**
	 * Merges two posting lists with matching documentIds and with
	 * positional difference less than k=1
	 * 
	 * @param post1 first postings
	 * @param post2 second postings
	 * @return merged result of two postings
	 */
	public ArrayList<Doc> intersect(ArrayList<Doc> post1, ArrayList<Doc> post2)
	{
		int p1 = 0;
		int p2 = 0;
		int k = 1;
		ArrayList<Doc> mergedPostings = new ArrayList<>();

		while (p1 < post1.size() && p2 < post2.size()){

			Doc doc1 = post1.get(p1);
			Doc doc2 = post2.get(p2);
			Doc answer = new Doc(doc1.docId);

			if (doc1.docId == doc2.docId) {
                for (int pos1: doc1.positionList) {
                    for (int pos2 : doc2.positionList){
                        int diff = pos2 - pos1;
                        if (diff > 0 && diff <= k) {
                            answer.positionList.add(pos2);
                        } else if (pos2 > pos1){
                            break;
                        }
                    }
                }

                if (answer.positionList.size() > 0) {
                    mergedPostings.add(answer);
                }

				p1++;
				p2++;
			} else if (doc1.docId < doc2.docId){
				p1++;
			} else {
				p2++;
			}
		}

		return mergedPostings;
	}
	
	/**
	 * Gets all documents that match all query terms and their positions
     *
	 * @param query a phrase query that consists of any number of terms in the sequential order
	 * @return ids of documents that contain the phrase
	 */
	public ArrayList<Doc> phraseQuery(String[] query)
	{
//        String[] queryTerms = query;
        int index = termList.indexOf(query[0]);
		ArrayList<Doc> matchingDocs = docLists.get(index);

		for (int i=1; i < query.length; i++){
            index = termList.indexOf(query[i]);
            matchingDocs = intersect(matchingDocs, docLists.get(index));
		}

		return matchingDocs;
	}

    /**
     * Starts the execution of the program.
     * Searches for a given phrase query using the positional index and prints the results
     *
     */
	public static void main(String[] args)
	{
      String[] docs = {"data warehousing over big data",
                       "dimensional data warehouse over big data",
                       "nlp before text mining",
                       "nlp before text classification"};
                       
		PositionalIndex pi = new PositionalIndex(docs);
//		System.out.print(pi);

//        String query = "data warehouse over";
        String query = "before text classification";
		String[] queryTerms = query.split(" ");
		ArrayList<Doc> resutl = pi.phraseQuery(queryTerms);

        System.out.println("Results for query="+query+"");
        for(int j=0;j<resutl.size();j++)
        {
            Doc doc = resutl.get(j);
			System.out.println("\t Document: "+docs[doc.docId]);
            System.out.println("\t Positions: "+doc+ "\t");
            System.out.println();
        }

	}
}
