
import java.util.*;

/**
 * Course #: 612
 * Lab #: 3
 * @author Carlos Rocha
 *
 * a node in a binary search tree
 */
class BTNode{
	BTNode left, right;
	String term;
	ArrayList<Integer> docLists;
	
	/**
	 * Create a tree node using a term and a document list
	 * @param term the term in the node
	 * @param docList the ids of the documents that contain the term
	 */
	public BTNode(String term, ArrayList<Integer> docList)
	{
		this.term = term;
		this.docLists = docList;
	}
	
}

/**
 * 
 * Binary search tree structure to store the term dictionary
 */
public class BinaryTree {

	/**
	 * Insert a node to a subtree
	 * @param node root node of a subtree
	 * @param iNode the node to be inserted into the subtree
	 */
	public void add(BTNode node, BTNode iNode)
	{
		if (node.term.compareTo(iNode.term) > 0){
			if(node.left != null) add(node.left, iNode);
			else {
				node.left = iNode;
			}
		} else {
			if(node.right != null) add(node.right, iNode);
			else{
				node.right = iNode;
			}
		}
	}
	
	/**
	 * Search a term in a subtree
	 * @param n root node of a subtree
	 * @param key a query term
	 * @return tree nodes with term that match the query term or null if no match
	 */
	public BTNode search(BTNode n, String key)
	{
		if(n == null) return null;
		if(n.term.equals(key)) return n;

		if (n.term.compareTo(key) > 0) return search(n.left, key);
		else return search(n.right, key);
	}
	
	/**
	 * Do a wildcard search in a subtree
	 * @param n the root node of a subtree
	 * @param key a wild card term, e.g., ho (terms like home will be returned)
	 * @return tree nodes that match the wild card
	 */
	public ArrayList<BTNode> wildCardSearch(BTNode n, String key)
	{
		ArrayList<BTNode> results = new ArrayList<>();
        Queue q = new Queue();
        q.enqueue(n);

        while (!q.isEmpty()){
            BTNode node = (BTNode) q.dequeue();

            if (node.term.startsWith(key)){
                results.add(node);
                if(node.right != null) q.enqueue(node.right);

            } else{

                int comparison = node.term.length() > key.length()
                        ? node.term.substring(0, key.length()).compareTo(key)
                        : node.term.compareTo(key.substring(0, node.term.length()));

                if(comparison>0){
                    if(node.left != null) q.enqueue(node.left);
                } else {
                    if(node.right != null) q.enqueue(node.right);
                }
            }
        }

		return results;
	}
	
	/**
	 * Print the inverted index based on the increasing order of the terms in a subtree
	 * @param node the root node of the subtree
	 */
	public void printInOrder(BTNode node)
	{
		if(node != null) {
			printInOrder(node.left);
			System.out.print(node.term + "\t" + node.docLists + "\n");
			printInOrder(node.right);
		}
	}
}