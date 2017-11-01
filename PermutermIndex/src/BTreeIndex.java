import java.util.*;

/**
 * Course #: 612
 * Lab #: 3
 * @author Carlos Rocha
 */
public class BTreeIndex {
	String[] myDocs;
	BinaryTree termList;
	BTNode root;
	
	/**
	 * Construct binary search tree to store the term dictionary 
	 * @param docs List of input strings
	 * 
	 */
	public BTreeIndex(String[] docs)
	{
		myDocs = docs;
		termList = new BinaryTree();
        HashMap<String, ArrayList<Integer>> invertedIndex = new HashMap<>();

        for (int i = 0; i<myDocs.length; i++) {
            String doc = myDocs[i];
            String[] terms = doc.split(" ");

            for (String term : terms) {
                if (invertedIndex.containsKey(term)){

                    if(!invertedIndex.get(term).contains(i)){
                        invertedIndex.get(term).add(i);
                    }
                } else {
                    ArrayList<Integer> docList = new ArrayList<>();
                    docList.add(i);
                    invertedIndex.put(term, docList);
                }
            }
        }

        ArrayList<String> sortedTerms = new ArrayList<>(invertedIndex.keySet());
        int midIndex = sortedTerms.size()/2;
        String rootTerm = sortedTerms.get(midIndex);
        sortedTerms.add(0, rootTerm);

        for(String mapKey: sortedTerms){

            String term = mapKey+"$";
            for (int j = 0; j<term.length()-1; j++){
                term = j > 0 ? permutateTerm(term) : term;
                if (root != null) {
                    BTNode iNode = termList.search(root, term);
                    if (iNode == null) {
                        iNode = new BTNode(term, invertedIndex.get(mapKey));
                        termList.add(root, iNode);
                    }
                } else {
                    root = new BTNode(term, invertedIndex.get(mapKey));
                }
            }
        }

	}

    /**
     * Generates next permutation of a given term
     *
     * @param term the term to be permutated
     * @return next permutation of the given term or null if no more permutations are possible
     */
	private String permutateTerm(String term){
        if (term.charAt(1) == '$') return null;
        else {
            return term.substring(1, term.length()) + term.charAt(0);
        }
    }
	
	/**
	 * Single keyword search
	 * @param query the query string
	 * @return doclists that contain the term
	 */
	public ArrayList<Integer> search(String query)
	{
	    query += "$";

        BTNode node = termList.search(root, query);
        if(node==null)
            return null;
        return node.docLists;
	}
	
	/**
	 * conjunctive query search
	 * @param query the set of query terms
	 * @return doclists that contain all the query terms
	 */
	public ArrayList<Integer> search(String[] query)
	{

		ArrayList<Integer> result = search(query[0]);
		int termId = 1;
		while(termId<query.length)
		{
			ArrayList<Integer> result1 = search(query[termId]);
			result = merge(result,result1);
			termId++;
		}		
		return result;
	}
	
	/**
	 * Searches for nodes that math a given wildcard
     *
	 * @param wildcard the wildcard query, e.g., ho (so that home can be located)
	 * @return a list of ids of documents that contain terms matching the wild card
	 */
	public ArrayList<Integer> wildCardSearch(String wildcard)
	{

        ArrayList<Integer> docList = new ArrayList<>();
	    String[] queryTerms = wildcard.split(" ");

        for (int i=0; i<queryTerms.length; i++){
            String queryTerm = queryTerms[i]+"$";
            int starIndex = queryTerm.indexOf("*");
            ArrayList<Integer> termDocList = new ArrayList<>();
            ArrayList<BTNode> nodes = new ArrayList<>();
            if(starIndex >= 0){

                if(starIndex < queryTerm.length()-2) {
                    queryTerm = queryTerm.substring(starIndex + 1, queryTerm.length())
                            + queryTerm.substring(0, starIndex);

                } else {
                    queryTerm = queryTerm.substring(0, queryTerm.length()-2);
                }

                nodes = termList.wildCardSearch(root, queryTerm);
            } else {
                BTNode node = termList.search(root, queryTerm);
                if(node != null) {
                    nodes.add(node);
                }
            }

            for (BTNode n: nodes) {
                if(termDocList.size() > 0){
                    for (Integer docId: n.docLists) {
                        if (!termDocList.contains(docId)) termDocList.add(docId);
                    }

                } else {
                    termDocList = n.docLists;
                }
            }

            Collections.sort(termDocList);
            docList = docList.size()>0 || i > 0 ? merge(docList, termDocList) : termDocList;
        }

        return docList;
    }

    /**
     * Merges two document lists with matching documents
     *
     * @param l1 first document list
     * @param l2 second document list
     * @return a new list containing only matching document ids
     */
	private ArrayList<Integer> merge(ArrayList<Integer> l1, ArrayList<Integer> l2)
	{
		ArrayList<Integer> mergedList = new ArrayList<Integer>();
		int id1 = 0, id2=0;
		while(id1<l1.size()&&id2<l2.size()){
			if(l1.get(id1).intValue()==l2.get(id2).intValue()){
				mergedList.add(l1.get(id1));
				id1++;
				id2++;
			}
			else if(l1.get(id1)<l2.get(id2))
				id1++;
			else
				id2++;
		}
		return mergedList;
	}
	
	/**
	 * Test cases
	 * @param args commandline input
	 */
	public static void main(String[] args)
	{
		String[] docs = {
                "text warehousing over big data",
                "dimensional data warehouse over big data",
                "nlp before text mining",
                "nlp before text classification"
        };

		BTreeIndex bTreeIndex = new BTreeIndex(docs);

        //Normal Queries
        String query = "text";
        ArrayList<Integer> results = bTreeIndex.search(query);
        System.out.println("Query: "+query);
        System.out.println("Results: ");
        for (Integer docId: results) {
            System.out.println(docId+": "+docs[docId]);
        }

        //Phrase Queries
        String[] phraseQuery = {"nlp", "text",  "mining"};
        String separator = String.join("", Collections.nCopies(60, "-"));
        results = bTreeIndex.search(phraseQuery);
        System.out.println(separator+"\n\nPhrase Query: "+String.join(" ", phraseQuery));
        System.out.println("Results: ");
        for (Integer docId: results) {
            System.out.println(docId+": "+docs[docId]);
        }

        //Wildcard Queries
        query = "ware*ing da*";
		results = bTreeIndex.wildCardSearch(query);
        System.out.println(separator+"\n\nWildcard Query: "+query);
        System.out.println("Results: ");
        for (Integer docId: results) {
            System.out.println(docId+": "+docs[docId]);
        }
    }
}