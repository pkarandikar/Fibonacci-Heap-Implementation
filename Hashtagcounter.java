
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.ParseConversionEvent;

public class Hashtagcounter {

	//HashMap to store all the strings and corresponding node pointers.
	HashMap<String, TreeNode> map = new HashMap<>();
	
	//Maximum key used to point to maximum key String in the tree.
	TreeNode maximum = null;

	int nodeCount = 0;

	/*
* InsertString inserts the String into the tree.
	 * If there exists a node with corresponding string, it modifies the key value of the node and readjusts the tree. 
	 */
	void insertString(String str, int val) {

		if (map.containsKey(str)) {
			// Creating new Node and adding default values
			TreeNode n1 = map.get(str);
			increaseKey(n1, val);

		} else {			
			
			TreeNode newNode = new TreeNode();
			newNode.key = val;
			newNode.word = str;
			newNode.degree = 0;
			insertNode(newNode);	
			nodeCount++;
			map.put(str, newNode);
		}

	}
	
	/*
	 * InsertNode inserts TreeNode into top level of Fibonacci heap.
	 * Resets the maximum.
	 */
	void insertNode(TreeNode n1) {
		if (maximum == null) {
			maximum = n1;
			n1.left = n1;
			n1.right = n1;
		} else {
			n1.left = maximum;
			n1.right = maximum.right;
			maximum.right = n1;
			n1.right.left = n1;

			if (n1.key > maximum.key) {
				maximum = n1;
			}
		}
		n1.childCut = false;
		n1.parent = null;
	}

	/*
	 * IncreaseKey modifies the value of the node.
	 * Performs cascade cut and readjusts the tree based on childCut values and sets maximum.
	 */
	void increaseKey(TreeNode n1, int val) {
		n1.key = n1.key + val;
		if (n1.parent == null) {
			//change1
			if (n1.key > maximum.key) {
				maximum = n1;
			}
			return;
			//change2
		} else if (n1.parent != null && n1.key < n1.parent.key) {
			return;
		}
		else {
			// If key increases. remove node from parent and put it in the root.
			TreeNode temp = n1;
			while (true) {
				//change3
				TreeNode par = temp.parent;
				boolean mark = par.childCut;
				par.degree--;

				if (par.parent != null)
					par.childCut = true;

				if (par.child == temp) {
					if (temp.right == temp) {
						par.child = null;
					} else {
						par.child = temp.right;
					}
				}

				if (temp.right != temp) {
					temp.right.left = temp.left;
					temp.left.right = temp.right;
				}

				insertNode(temp);
				if (!mark) {
					break;
				}
				temp = par;
			}
		}
	}
	
	/*
	 * removeMax removes the maximum node from the tree and readjusts the maximum to one of the siblings temporarily.
	 */

	TreeNode removeMax() {
		TreeNode max = maximum;
		if (max == null) {
			return null;
		} else if (max.left == max) {
			maximum = null;
		} else {
			max.right.left = max.left;
			max.left.right = max.right;			
			maximum = max.right;
		}

		TreeNode child = max.child;

		adjustChildren(child);
		nodeCount--;
		return max;
	}

	/*
	 * If the removed maximum node has children, the AdjustChildren will consolidate child nodes together 
	 */
	void adjustChildren(TreeNode maxNodeChild) {
		//if (maxNodeChild == null)
			//return;
		
		//TreeNode l1, m1; //Temporary nodes

		TreeNode l1 = maxNodeChild;
		TreeNode m1 = maxNodeChild;

		if (maxNodeChild != null) {
			while (l1.right != maxNodeChild) {
				l1.parent = null;
				l1.childCut = false;
				if (l1.key > m1.key) {
					m1 = l1;
				}
				l1 = l1.right;
			}
		}

		if (maximum == null) {
			maximum = m1;
			combineTrees();
			return;
		} else if (maxNodeChild != null) {
				maxNodeChild.left = maximum;
				l1.right = maximum.right;
				maximum.right = maxNodeChild;
				l1.right.left = l1;
			}
		//}

		TreeNode temp1 = maximum, temp2 = maximum;
		while (maximum != temp1.right) {
			if (temp2.key < temp1.key) {
				temp2 = temp1;
			}
			temp1 = temp1.right;
		}

		if (temp1.key > temp2.key) {
			temp2 = temp1;
		}

		maximum = temp2;
		combineTrees();
	}

	/*
	 * CombineTree() merges all the trees at root level so that no two roots have same degree.
	 * This operation is performed in a amortized constant time.
	 */
	void combineTrees() {

		if (maximum == null) {
			return;
		}
		boolean flag = true;

		while (flag) {
			flag = false;

			//A tracker is used to maintain the degrees of merged root nodes
			Map<Integer, TreeNode> degreeTracker = new HashMap<>();
			degreeTracker.put(maximum.degree, maximum);
			
			TreeNode temp = maximum.right;
			
			// Iterating through the root level nodes.
			while (temp != maximum) {
				TreeNode next = temp.right;
				TreeNode newNode = null;
				int nodeDegree = temp.degree;
				if (degreeTracker.containsKey(temp.degree)) {
					TreeNode n1 = degreeTracker.get(temp.degree);
					flag = true;
					if (n1.key >= temp.key) {
						newNode = joinNodes(n1, temp);
					} else {
						newNode = joinNodes(temp, n1);
					}

					degreeTracker.remove(nodeDegree);
					if (!degreeTracker.containsKey(newNode.degree)) {
						degreeTracker.put(newNode.degree, newNode);
					}

				}

				else {
					degreeTracker.put(temp.degree, temp);
				}
				temp = next;
			}
		}
	}

	/*
	 * joinNodes merges two nodes of same degree.
	 * n2 node becomes the child of n1 node.
	 */
	TreeNode joinNodes(TreeNode n1, TreeNode n2) {
		if (n1 == n2) {
			System.out.println("Exception");
		}

		n2.right.left = n2.left;
		n2.left.right = n2.right;
		n1.degree++;
		n2.parent = n1;

		if (n1.child == null) {
			n1.child = n2;
			n2.left = n2;
			n2.right = n2;
		}

		else {
			n2.left = n1.child;
			n2.right = n1.child.right;
			n1.child.right = n2;
			n2.right.left = n2;
		}
		return n1;
	}

	/*
	 * getMaxNodes is a helper function to remove and reinsert the required number of maximum nodes in the tree.
	 * It returns a list of Strings in the order of maximum key values.
	 */
	List<String> getMaxNodes(int count) {
		List<String> out = new ArrayList<>();
		List<TreeNode> tempList = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			TreeNode temp = removeMax();
			String word = temp.word;
			map.remove(word);
			out.add(word);
			tempList.add(temp);
		}
		for (int i = 0; i < tempList.size(); i++) {
			insertString(tempList.get(i).word, tempList.get(i).key);
		}
		return out;
	}

	/*
	 * 
	 */
	public static void main(String args[]) throws IOException {
		//provide inputfile as argument 
		// Eg: sampleInput_Million.txt
		
		String line = null;
		
		//Creating a Fibonacci heap object
		Hashtagcounter heapObject = new Hashtagcounter();
		
		InputStream inp = new FileInputStream(args[0]);
		InputStreamReader isr = new InputStreamReader(inp, Charset.forName("UTF-8"));
		BufferedReader br = new BufferedReader(isr);
		
		String filename = "output_file.txt";
		
		FileWriter filewr = new FileWriter(filename, false);
		
		BufferedWriter buffwr = new BufferedWriter(filewr);
		
		while ((line = br.readLine()) != null) {
			String str[] = line.split(" ");
			if (str.length > 1) {
				
				//Line contains a String and its key value.
				
				str[0] = str[0].substring(str[0].indexOf("#") + 1);
				//System.out.println(str[0] + "  "+ Integer.parseInt(str[1]) );
				heapObject.insertString(str[0], Integer.parseInt(str[1]));
				
			} else if (str[0].equals("STOP")) {
				
				//End of the file. Breaking the while loop. 
				
				break;
				
			} else {
				
				//read integer from the line and output Strings with maximum values.
				
				int removemaxcount = Integer.parseInt(str[0]);

				List<String> words = heapObject.getMaxNodes(removemaxcount);
				StringBuilder outputList = new StringBuilder();
				for (int i = 0; i < words.size(); i++) {
					outputList.append(words.get(i)).append(",");
				}
				outputList.deleteCharAt(outputList.length() - 1);
				
				buffwr.write(outputList.toString());
				buffwr.newLine();
				//System.out.println(outputList.toString());
				
			}
		}

		br.close();
		buffwr.close();
	}
	
	/*
	 * TreeNode contains the basic structure of our fibonacci heap node.
	 * The constructor sets all the links to null.
	 */
	class TreeNode {
		TreeNode child;
		TreeNode parent;
		TreeNode left;
		TreeNode right;

		boolean childCut;
		int key;
		String word;
		int degree;

		TreeNode() {
			this.left = null;
			this.right = null;
			this.parent = null;
			this.child = null;
		}
	}
}
