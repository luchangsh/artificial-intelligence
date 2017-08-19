import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

public class homework {
	
	static class Problem {
		public int numQueries;
		public List<String> queries;
		public int numSentences;
		public List<List<String>> sentences;
	}
	
	static class Pair {
		public int row;
		public int col;
		public Pair(int row, int col) {
			this.row = row;
			this.col = col;
		}
	}
	
	/*----------------------------------------------main()----------------------------------------------*/
	public static void main(String[] args) {
		try {
			Problem prob = readFile();
			boolean[] results = solveProblem(prob);
			outputResult(results);
		} catch (FileNotFoundException e) {
			System.out.println("ERROR: Input file not found or Can't open output file.");
		}
	}
	
	/*----------------------------------------------outputResult()----------------------------------------------*/
	private static void outputResult(boolean[] result) throws FileNotFoundException {
		PrintWriter out = new PrintWriter("testCasesHW3/test-2/output.txt");
		try {
			for (int i = 0; i < result.length; i++) {
				boolean b = result[i];
				if (b) {
					out.println("TRUE");
				} else {
					out.println("FALSE");
				}
			}
		} finally {
			out.close();
		}
	}
	
	/*----------------------------------------------readFile()----------------------------------------------*/
	private static Problem readFile() throws FileNotFoundException {
		File inputFile = new File("testCasesHW3/test-2/input.txt");
		Scanner in = new Scanner(inputFile);
		Problem prob = new Problem();
		try {
			prob.numQueries = in.nextInt();
			in.nextLine();			
			prob.queries = new ArrayList<>();
			for (int i = 0; i < prob.numQueries; i++) {
				prob.queries.add(in.nextLine());
			}			
			int numSentences = in.nextInt();
			in.nextLine();			
			String[] kb = new String[numSentences];
			for (int i = 0; i < kb.length; i++) {
				kb[i] = in.nextLine();
			}
			List<String> kb_cnf = transformToCNFClauses(kb);			
			prob.numSentences = kb_cnf.size();			
			prob.sentences = new ArrayList<>();
			for (int i = 0; i < prob.numSentences; i++) {
				String[] arr = kb_cnf.get(i).split("\\s\\|\\s");
				List<String> arr2 = new ArrayList<>();
				for (int j = 0; j < arr.length; j++) {
					arr2.add(arr[j]);
				}
				prob.sentences.add(arr2);
			}			
			return prob;
		} finally {
			in.close();
		}
	}
	
	/*----------------------------------------------transformToCNFClauses()----------------------------------------------*/
	private static List<String> transformToCNFClauses(String[] kb) {
		for (int i = 0; i < kb.length; i++) {
			String s = kb[i];
			s = removeUnnecessarySpace(s);
			s = eliminateInference(s);
			s = moveNegateInward(s);
			s = distributeOrOperatorForward(s);
			s = distributeOrOperatorBackward(s);
			s = removeParentheses(s);
			kb[i] = s;
		}		
		List<String> result = new ArrayList<>();
		for (int i = 0; i < kb.length; i++) {
			String s = kb[i];
			String[] a = s.split("\\s\\&\\s");
			for (int j = 0; j < a.length; j++) {
				result.add(a[j]);
			}			
		}		
		return standardizeVariable(result);
	}
	
	/*-----------------------------------------removeUnnecessarySpace()-----------------------------------------*/
	private static String removeUnnecessarySpace(String str) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < str.length(); i++) {
			char ch = str.charAt(i);
			if (!Character.isWhitespace(ch)) {
				if (ch == '=') {
					builder.append(" =");
				} else if (ch == '>') {
					builder.append("> ");
				} else if (ch == '&') {
					builder.append(" & ");
				} else if (ch == '|') {
					builder.append(" | ");
				} else {
					builder.append(ch);
				}
			}
		}		
		return builder.toString();
	}
	
	/*-----------------------------------------eliminateInference()-----------------------------------------*/
	private static String eliminateInference(String str) {
		int startOfInferSymbol = str.indexOf(" => ");
		while (startOfInferSymbol != -1) {
			str = eliminateInferenceHelper(str, startOfInferSymbol);
			startOfInferSymbol = str.indexOf(" => ");
		}		
		return str;
	}
	
	private static String eliminateInferenceHelper(String str, int startOfInferSymbol) {
		//find left operand
		int startOfLeftOperand = findStartOfLeftOperand(str, startOfInferSymbol);
		String leftOperand = str.substring(startOfLeftOperand, startOfInferSymbol);

		//find right operand
		int endOfRightOperand = findEndOfRightOperand(str, startOfInferSymbol + 3);
		String rightOperand = str.substring(startOfInferSymbol + 4, endOfRightOperand + 1);
		
		//build the result string
		StringBuilder builder = new StringBuilder();
		if (startOfLeftOperand > 1) {
			builder.append(str.substring(0, startOfLeftOperand - 1));
		}
		builder.append('('); //for or
		builder.append("(~"); //for negation
		builder.append(leftOperand);
		builder.append(')'); //for negation
		builder.append(" | ");
		builder.append(rightOperand);
		builder.append(')'); //for or
		if (endOfRightOperand < str.length() - 2) {
			builder.append(str.substring(endOfRightOperand + 2, str.length()));
		}		
		return builder.toString();
	}
	
	/*-----------------------------------------moveNegateInward()-----------------------------------------*/
	private static String moveNegateInward(String str) {
		int startOfNegateSymbol = str.indexOf("(~("); //find negation of a compound sentence
		while (startOfNegateSymbol != -1) {
			str = moveNegateInwardHelper(str, startOfNegateSymbol);
			startOfNegateSymbol = str.indexOf("(~(");
		}
		return str;
	}
	
	private static String moveNegateInwardHelper(String str, int startOfNegateSymbol) {
		//find the negation operand (the right operand)
		int endOfRightOperand = findEndOfRightOperand(str, startOfNegateSymbol + 1);
		String rightOperand = str.substring(startOfNegateSymbol + 2, endOfRightOperand + 1);
		
		//find the operator of the right operand
		int indexOfOperator = findIndexOfOperator(rightOperand);
		char operator = rightOperand.charAt(indexOfOperator);
		
		//build the result
		StringBuilder builder = new StringBuilder();
		if (startOfNegateSymbol > 0) {
			builder.append(str.substring(0, startOfNegateSymbol));
		}
		if (operator == '~') {
			builder.append(rightOperand.substring(2, rightOperand.length() - 1));
		} else if (operator == '&') {
			builder.append('('); //for or
			builder.append("(~"); // for negation
			builder.append(rightOperand.substring(1, indexOfOperator - 1));
			builder.append(')'); // for negation
			builder.append(" | ");
			builder.append("(~"); // for negation
			builder.append(rightOperand.substring(indexOfOperator + 2, rightOperand.length() - 1));
			builder.append(')'); // for negation
			builder.append(')'); //for or			
		} else if (operator == '|') {
			builder.append('('); //for and
			builder.append("(~"); // for negation
			builder.append(rightOperand.substring(1, indexOfOperator - 1));
			builder.append(')'); // for negation
			builder.append(" & ");
			builder.append("(~"); // for negation
			builder.append(rightOperand.substring(indexOfOperator + 2, rightOperand.length() - 1));
			builder.append(')'); // for negation
			builder.append(')'); //for and		
		}
		if (endOfRightOperand < str.length() - 2) {
			builder.append(str.substring(endOfRightOperand + 2, str.length()));
		}
		return builder.toString();
	}
	
	/*-----------------------------------------distributeOrOperatorForward()-----------------------------------------*/
	private static String distributeOrOperatorForward(String str) {
		while (!isDoneForward(str)) {
			for (int i = 0; i < str.length(); i++) {
				if (str.charAt(i) == '|' && str.charAt(i + 2) == '(') {
					int indexOfOpeningParen = i + 2;
					int indexOfClosingParen = findClosingParenFromOpeningParen(str, indexOfOpeningParen);
					String rightOperand = str.substring(indexOfOpeningParen, indexOfClosingParen + 1); //includes ()
					int indexOfOperator = findIndexOfOperator(rightOperand);		
					if (rightOperand.charAt(indexOfOperator) == '&') {
						str = distributeOrOperatorForwardHelper(str, i);
						break;
					}
				}				
			}
		}
		return str;
	}
	
	private static boolean isDoneForward(String str) {
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == '|' && str.charAt(i + 2) == '(') {
				int indexOfOpeningParen = i + 2;
				int indexOfClosingParen = findClosingParenFromOpeningParen(str, indexOfOpeningParen);
				String rightOperand = str.substring(indexOfOpeningParen, indexOfClosingParen + 1); //includes ()
				int indexOfOperator = findIndexOfOperator(rightOperand);		
				if (rightOperand.charAt(indexOfOperator) == '&') return false;
			}			
		}		
		return true;
	}
	
	private static String distributeOrOperatorForwardHelper(String str, int indexOfOrSymbol) {
		//get left operand of or operator
		int startOfLeftOperand = findStartOfLeftOperand(str, indexOfOrSymbol - 1);
		String leftOperand = str.substring(startOfLeftOperand, indexOfOrSymbol - 1); // not includes ()
		
		//get left and right operand of and operator
		int indexOfOpeningParenRight = indexOfOrSymbol + 2;
		int indexOfClosingParenRight = findClosingParenFromOpeningParen(str, indexOfOpeningParenRight);
		String rightOperand = str.substring(indexOfOpeningParenRight, indexOfClosingParenRight + 1); //includes()
		int indexOfAndOperator = findIndexOfOperator(rightOperand);
		String leftOfRightOperand = rightOperand.substring(1, indexOfAndOperator - 1); //not includes ()
		String rightOfRightOperand = rightOperand.substring(indexOfAndOperator + 2, rightOperand.length() - 1); //not includes ()
		
		//build the result string
		StringBuilder builder = new StringBuilder();
		if (startOfLeftOperand > 1) {
			builder.append(str.substring(0, startOfLeftOperand - 1));
		}
		builder.append('('); // for and
		builder.append('('); // for or
		builder.append(leftOperand);
		builder.append(" | ");
		builder.append(leftOfRightOperand);
		builder.append(')'); // for or
		builder.append(" & ");
		builder.append('('); // for or
		builder.append(leftOperand);
		builder.append(" | ");
		builder.append(rightOfRightOperand);
		builder.append(')'); // for or
		builder.append(')'); // for and
		if (indexOfClosingParenRight < str.length() - 2) {
			builder.append(str.substring(indexOfClosingParenRight + 2, str.length()));
		}
		return builder.toString();
	}

	/*-----------------------------------------distributeOrOperatorBackward()-----------------------------------------*/
	private static String distributeOrOperatorBackward(String str) {
		while (!isDoneBackward(str)) {
			for (int i = 0; i < str.length(); i++) {
				if (str.charAt(i) == '|' && str.charAt(i - 2) == ')' && !Character.isLowerCase(str.charAt(i - 3))) {
					int indexOfClosingParen = i - 2;
					int indexOfOpeningParen = findOpeningParenFromClosingParen(str, indexOfClosingParen);
					String leftOperand = str.substring(indexOfOpeningParen, indexOfClosingParen + 1); //includes ()
					int indexOfOperator = findIndexOfOperator(leftOperand);
					if (leftOperand.charAt(indexOfOperator) == '&') {
						str = distributeOrOperatorBackwardHelper(str, i);
						break;
					}
				}				
			}
		}
		return str;
	}
	
	private static boolean isDoneBackward(String str) {
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == '|' && str.charAt(i - 2) == ')' && !Character.isLowerCase(str.charAt(i - 3))) {
				int indexOfClosingParen = i - 2;
				int indexOfOpeningParen = findOpeningParenFromClosingParen(str, indexOfClosingParen);
				String leftOperand = str.substring(indexOfOpeningParen, indexOfClosingParen + 1); //includes ()
				int indexOfOperator = findIndexOfOperator(leftOperand);
				if (leftOperand.charAt(indexOfOperator) == '&') return false;
			}			
		}		
		return true;
	}
	
	private static String distributeOrOperatorBackwardHelper(String str, int indexOfOrSymbol) {
		//get right operand of or operator
		int endOfRightOperand = findEndOfRightOperand(str, indexOfOrSymbol + 1);
		String rightOperand = str.substring(indexOfOrSymbol + 2, endOfRightOperand + 1); // not includes ()
		
		//get left and right operand of and operator
		int indexOfClosingParenLeft = indexOfOrSymbol - 2;
		int indexOfOpeningParenLeft = findOpeningParenFromClosingParen(str, indexOfClosingParenLeft);
		String leftOperand = str.substring(indexOfOpeningParenLeft, indexOfClosingParenLeft + 1); //includes()
		int indexOfAndOperator = findIndexOfOperator(leftOperand);
		String leftOfLeftOperand = leftOperand.substring(1, indexOfAndOperator - 1); //not includes ()
		String rightOfLeftOperand = leftOperand.substring(indexOfAndOperator + 2, leftOperand.length() - 1); //not includes ()
		
		//build the result string
		StringBuilder builder = new StringBuilder();
		if (indexOfOpeningParenLeft > 1) {
			builder.append(str.substring(0, indexOfOpeningParenLeft - 1));
		}
		builder.append('('); // for and
		builder.append('('); // for or
		builder.append(leftOfLeftOperand);
		builder.append(" | ");
		builder.append(rightOperand);
		builder.append(')'); // for or
		builder.append(" & ");
		builder.append('('); // for or
		builder.append(rightOfLeftOperand);
		builder.append(" | ");
		builder.append(rightOperand);
		builder.append(')'); // for or
		builder.append(')'); // for and
		if (endOfRightOperand < str.length() - 2) {
			builder.append(str.substring(endOfRightOperand + 2, str.length()));
		}
		return builder.toString();
	}

	/*-----------------------------------------removeParentheses()-----------------------------------------*/
	private static String removeParentheses(String str) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < str.length(); i++) {
			char ch = str.charAt(i);
			if ((ch == '(' && i == 0) ||					
				(ch == '(' && !(Character.isLetter(str.charAt(i + 1)) && Character.isLetter(str.charAt(i - 1)))) || 
				(ch == ')' && !Character.isLetter(str.charAt(i - 1)))) {
				continue;
			}
			builder.append(ch);			
		}		
		return builder.toString();
	}
	
	/*-----------------------------------------Preprocessor Utilities-----------------------------------------*/
	private static int findStartOfLeftOperand(String str, int oneAfterLeftOperand) {
		int curr = oneAfterLeftOperand;
		int num = 0;
		while (num != 1) {
			curr--;
			if (str.charAt(curr) == '(') num++;
			else if (str.charAt(curr) == ')') num--;
		}		
		return curr + 1;
	}

	private static int findEndOfRightOperand(String str, int oneBeforeRightOperand) {
		int curr = oneBeforeRightOperand;
		int num = 0;
		while (num != -1) {
			curr++;
			if (str.charAt(curr) == '(') num++;
			else if (str.charAt(curr) == ')') num--;
		}		
		return curr - 1;
	}
	
	private static int findClosingParenFromOpeningParen(String str, int indexOfOpeningParen) {
		int curr = indexOfOpeningParen;
		int num = 0;
		while (num != -1) {
			curr++;
			if (str.charAt(curr) == '(') num++;
			else if (str.charAt(curr) == ')') num--;
		}
		return curr;
	}
	
	private static int findOpeningParenFromClosingParen(String str, int indexOfClosingParen) {
		int curr = indexOfClosingParen;
		int num = 0;
		while (num != 1) {
			curr--;
			if (str.charAt(curr) == '(') num++;
			else if (str.charAt(curr) == ')') num--;
		}
		return curr;
	}	
	
	private static int findIndexOfOperator(String str) { //str: the operand includes () if any
		int num = 0;
		int i;
		for (i = 0; i < str.length(); i++) {
			char ch = str.charAt(i);
			if (ch == '(') num++;
			else if (ch == ')') num--;
			else if ((ch == '~' || ch == '&' || ch == '|') && num == 1) break;
		}
		return i;		
	}
	
	/*----------------------------------------------standardizeVariable()----------------------------------------------*/
	private static List<String> standardizeVariable(List<String> arr) {
		List<String> result = new ArrayList<String>();		
		for (int i = 0; i < arr.size(); i++) {
			String s = arr.get(i);
			result.add(standardizeIthSentence(s, i));			
		}		
		return result;
	}
	
	private static String standardizeIthSentence(String str, int num) {
		String[] arr = str.split("\\s\\|\\s");		
		for (int i = 0; i < arr.length; i++) {
			arr[i] = standardizeIthItem(arr[i], num);
		}		
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < arr.length; i++) {
			builder.append(arr[i]);
			if (i != arr.length - 1) builder.append(" | ");
		}		
		return builder.toString();
	}
	
	private static String standardizeIthItem(String str, int num) {
		String args = str.substring(str.indexOf('(') + 1, str.indexOf(')')); // not includes ()		
		String[] arr = args.split(",");		
		for (int i = 0; i < arr.length; i++) {
			char ch = arr[i].charAt(0);
			if (Character.isLetter(ch) && Character.isLowerCase(ch)) { // is a variable
				arr[i] += num; 
			}
		}		
		StringBuilder builder = new StringBuilder();
		builder.append(str.substring(0, str.indexOf('(') + 1));		
		for (int i = 0; i < arr.length; i++) {
			builder.append(arr[i]);
			if (i != arr.length - 1) builder.append(',');
		}
		builder.append(')');		
		return builder.toString();
	}
	
	/*----------------------------------------------solveProblem()----------------------------------------------*/	
	private static boolean[] solveProblem(Problem prob) {
		boolean[] results = new boolean[prob.numQueries];
		for (int i = 0; i < prob.queries.size(); i++) {
			results[i] = resolution(prob.sentences, prob.queries.get(i));
		}
		return results;
	}
	
	private static boolean resolution(List<List<String>> kb, String query) {
		List<List<String>> clauses = initClauses(kb, query);
		List<List<String>> newClauses = new ArrayList<List<String>>();
		
		int count = 1;
		while (count <= 5) {
//		while (true) {			
			Map<String, List<Pair>> table = indexClauses(clauses);
			for (int i = 0; i < clauses.size(); i++) {
				for (int j = 0; j < clauses.get(i).size(); j++) {
					String str = clauses.get(i).get(j);
					List<List<String>> resolvents = resolve(str, table, clauses, i);
					if (containEmptyClause(resolvents)) return true;
					newClauses = union(newClauses, resolvents);
				}			
			}
			if (include(clauses, newClauses)) return false;
			clauses = union(clauses, newClauses);
			clauses = removeUnnecessarySentences(clauses);			
			print2DArray(clauses);
			System.out.println("Count: " + count);
			System.out.println("=================================================================================");
			count++;
		}
		return false;
	}
	
	private static List<List<String>> removeUnnecessarySentences(List<List<String>> clauses) {
		Map<String, List<Pair>> table = createIndexMap(clauses);
		List<List<String>> result = new ArrayList<>();
		for (int i = 0; i < clauses.size(); i++) {
			List<String> l1 = clauses.get(i);
			List<Pair> pairs = table.get(l1.get(0));
			boolean found = false;
			for (Pair p : pairs) {
				if (p.row != i) {
					List<String> l2 = clauses.get(p.row);
					if (includeOtherSentence(l1, l2)) {
						found = true;
						break;
					}					
				}
			}
			if (!found) result.add(l1);
		}
		return result;
	}
	
	private static boolean includeOtherSentence(List<String> l1, List<String> l2) {
		if (l1.size() < l2.size()) return false;
		for (String s2 : l2) {
			boolean found = false;
			for (String s1 : l1) {
				if (s1.equals(s2)) {
					found = true;
					break;
				}
			}
			if (!found) return false;
		}		
		return true;
	}	

	private static List<List<String>> initClauses(List<List<String>> kb, String query) {
		List<List<String>> clauses = new ArrayList<>();
		for (int i = 0; i < kb.size(); i++) {
			List<String> c = new ArrayList<String>();
			for (int j = 0; j < kb.get(i).size(); j++) {
				c.add(kb.get(i).get(j));
			}
			clauses.add(c);			
		}
		List<String> nq = new ArrayList<String>();
		nq.add(negate(query));
		clauses.add(nq);
		return clauses;
	}
	
	private static String negate(String str) {
		return str.charAt(0) == '~' ? str.substring(1, str.length()) : ("~" + str);
	}

	private static Map<String, List<Pair>> indexClauses(List<List<String>> clauses) {
		Map<String, List<Pair>> table = new HashMap<>();
		for (int i = 0; i < clauses.size(); i++) {
			for (int j = 0; j < clauses.get(i).size(); j++) {
				String op = getOp(clauses.get(i).get(j));
				List<Pair> pairs = table.containsKey(op) ? table.get(op) : new ArrayList<Pair>();
				pairs.add(new Pair(i, j));
				table.put(op, pairs);
			}
		}
		return table;
	}

	private static List<List<String>> resolve(String str, Map<String, List<Pair>> table, List<List<String>> clauses, int i) {
		List<List<String>> resolvents = new ArrayList<>();
		List<Pair> pairs = table.get(negate(getOp(str)));
		for (Pair p : pairs) {
			String str2 = clauses.get(p.row).get(p.col);
			if (p.row > i) {
				Map<String, String> subst = unify(str, negate(str2), new HashMap<>());
				if (subst != null) {
					List<String> resolvent = createResolvent(str, str2, clauses.get(i), clauses.get(p.row), subst);
					resolvent = factoring(resolvent);
					if (!containComplementary(resolvent) && !containTautology(resolvent)) {
						resolvents.add(resolvent);
					}
				}
			}
		}
		return resolvents;
	}
	
	private static List<String> factoring(List<String> resolvent) {
		for (int i = 0; i < resolvent.size() - 1; i++) {
			String s1 = resolvent.get(i);
			for (int j = i + 1; j < resolvent.size(); j++) {
				String s2 = resolvent.get(j);
				if (getOp(s1).equals(getOp(s2))) {
					Map<String, String> subst = unify(s1, s2, new HashMap<>());
					if (subst != null) {
						resolvent = createNewResolvent(resolvent, subst);						
					}
				}				
			}			
		}
		Set<String> set = new HashSet<>();
		for (String s : resolvent) {
			set.add(s);
		}
		resolvent = new ArrayList<String>(set);
		return resolvent;
	}
	
	private static List<String> createNewResolvent(List<String> resolvent, Map<String, String> subst) {
		if (subst.size() != 0) {
			for (int i = 0; i < resolvent.size(); i++) {
				String curr = resolvent.get(i);
				String[] args = getArgs(curr).split(",");
				for (int j = 0; j < args.length; j++) {
					String arg = args[j];
					if (isVariable(arg) && subst.containsKey(arg)) {
						args[j] = subst.get(arg);
					}
				}
				String newStr = getOp(curr) + "(" + createArgStr(args) + ")";
				resolvent.set(i, newStr);
			}			
		}
		return resolvent;
	}	
	
	private static boolean containTautology(List<String> arr) {
		for (String s : arr) {
			if (s.charAt(0) == '~') {
				String[] args = getArgs(s).split(",");
				if (args.length > 1 && isAllEqual(args)) return true;
			}
		}	
		return false;
	}
	
	private static boolean isAllEqual(String[] arr) {
		for (int i = 1; i < arr.length; i++) {
			if (!arr[i].equals(arr[0])) return false;
		}		
		return true;
	}	
	
	private static boolean containComplementary(List<String> arr) {
		for (int i = 0; i < arr.size() - 1; i++) {
			String s1 = arr.get(i);
			for (int j = i + 1; j < arr.size(); j++) {
				String s2 = arr.get(j);
				if (s1.equals(negate(s2))) return true;				
			}			
		}		
		return false;
	}	
	
	private static List<String> createResolvent(String str1, String str2, List<String> arr1, List<String> arr2, Map<String, String> subst) {
		List<String> resolvent = new ArrayList<>();
		for (String s : arr1) {
			if (!s.equals(str1)) {
				resolvent.add(s);
			}
		}
		for (String s : arr2) {
			if (!s.equals(str2)) {
				resolvent.add(s);
			}
		}
		if (subst.size() != 0) {
			for (int i = 0; i < resolvent.size(); i++) {
				String curr = resolvent.get(i);
				String[] args = getArgs(curr).split(",");
				for (int j = 0; j < args.length; j++) {
					String arg = args[j];
					if (isVariable(arg) && subst.containsKey(arg)) {
						args[j] = subst.get(arg);
					}
				}
				String newStr = getOp(curr) + "(" + createArgStr(args) + ")";
				resolvent.set(i, newStr);
			}			
		}
		Set<String> set = new HashSet<>();
		for (String s : resolvent) {
			set.add(s);
		}
		resolvent = new ArrayList<String>(set);		
		return resolvent;
	}

	private static String createArgStr(String[] arr) {
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < arr.length - 1; i++) {
			str.append(arr[i] + ",");
		}
		str.append(arr[arr.length - 1]);
		return str.toString();
	}

	private static boolean containEmptyClause(List<List<String>> resolvents) {
		for (int i = 0; i < resolvents.size(); i++) {
			if (resolvents.get(i).size() == 0) {
				return true;
			}
		}
		return false;
	}	
	
	private static List<List<String>> union(List<List<String>> arr1, List<List<String>> arr2) {
		if (arr1.size() == 0) { return arr2; }
		if (arr2.size() == 0) { return arr1; }
		for (List<String> a2 : arr2) {
			Map<String, List<Pair>> table = createIndexMap(arr1);
			List<Pair> pairs = table.get(a2.get(0));
			boolean found = false;
			if (pairs != null) {
				for (Pair p : pairs) {
					List<String> a1 = arr1.get(p.row);
					if (equals(a1, a2)) {
						found = true;
						break;
					}				
				}				
			}
			if (!found) { 
				arr1.add(a2);
			}
		}
		return arr1;
	}

	private static boolean equals(List<String> a1, List<String> a2) {
		if (a1.size() != a2.size()) { return false;	}
		Set<String> set = new HashSet<>();
		for (String s : a1) {
			set.add(s);
		}
		for (String s : a2) {
			if (!set.contains(s)) {
				return false;
			}
		}		
		return true;
	}	

	private static Map<String, List<Pair>> createIndexMap(List<List<String>> clauses) {
		Map<String, List<Pair>> map = new HashMap<>();
		for (int i = 0; i < clauses.size(); i++) {
			for (int j = 0; j < clauses.get(i).size(); j++) {
				String str = clauses.get(i).get(j);
				List<Pair> pairs = map.containsKey(str) ? map.get(str) : new ArrayList<Pair>();
				pairs.add(new Pair(i, j));
				map.put(str, pairs);
			}
		}
		return map;		
	}
		
	private static boolean include(List<List<String>> set, List<List<String>> subset) {
		if (set.size() == 0) { return false; }
		if (subset.size() == 0) { return true; }
		Map<String, List<Pair>> table = createIndexMap(set);
		for (List<String> a2 : subset) {			
			List<Pair> pairs = table.get(a2.get(0));
			boolean found = false;
			if (pairs != null) {
				for (Pair p : pairs) {
					List<String> a1 = set.get(p.row);
					if (equals(a1, a2)) {
						found = true;
						break;
					}				
				}				
			}
			if (!found) {
				return false;
			}
		}
		return true;
	}	
	
	/*----------------------------------------------unify()----------------------------------------------*/
	private static Map<String, String> unify(String x, String y, Map<String, String> subst) {
		if (subst == null) {
			return null;
		} else if (x.equals(y)) {
			return subst;
		} else if (isVariable(x)) {
			return unifyVar(x, y, subst);
		} else if (isVariable(y)) {
			return unifyVar(y, x, subst);
		} else if (isPredicate(x) && isPredicate(y)) {
			return unify(getArgs(x), getArgs(y), unify(getOp(x), getOp(y), subst));
		} else if (isList(x) && isList(y)) {
			return unify(getRest(x), getRest(y), unify(getFirst(x), getFirst(y), subst));
		}
 		return null;
	}
	
	private static Map<String, String> unifyVar(String var, String x, Map<String, String> subst) {		
		if (subst.containsKey(var)) {
			return unify(subst.get(var), x, subst);
		} else if (subst.containsKey(x)) {
			return unify(var, subst.get(x), subst);
		}
		subst.put(var, x);
		return subst;
	}
	
	private static boolean isVariable(String str) {
		char ch = str.charAt(0);
		return Character.isLetter(ch) && Character.isLowerCase(ch) && str.indexOf(',') == -1;
	}
	
	private static boolean isPredicate(String str) {
		String str1 = str.charAt(0) == '~' ? str.substring(1) : str;
		char ch = str1.charAt(0);
		return Character.isLetter(ch) && Character.isUpperCase(ch) && str1.indexOf('(') != -1 && str1.indexOf(')') != -1;
	}
	
	private static boolean isList(String str) {
		return str.indexOf(',') != -1;
	}
	
	private static String getOp(String str) {
		return str.substring(0, str.indexOf('('));
	}
	
	private static String getArgs(String str) {
		return str.substring(str.indexOf('(') + 1, str.indexOf(')'));
	}

	private static String getFirst(String str) {
		return str.substring(0, str.indexOf(','));
	}

	private static String getRest(String str) {
		return str.substring(str.indexOf(',') + 1, str.length());
	}
	
	
	
	
	
	
	
	
	/*----------------------------------------------Utilities----------------------------------------------*/
	private static void printProblem(Problem prob) {
		System.out.println(prob.numQueries);
		for (int i = 0; i < prob.queries.size(); i++) {
			System.out.println(prob.queries.get(i));
		}
		System.out.println(prob.numSentences);
		for (int i = 0; i < prob.sentences.size(); i++) {
			printArrayList(prob.sentences.get(i));
		}
		System.out.println("=================================================================================");
	}
	
	private static void printArray(String[] arr) {
		for (int i = 0; i < arr.length; i++) {
			System.out.print(arr[i]);
			if (i != arr.length - 1) System.out.print(" | ");
		}
		System.out.println();
	}

	private static void print2DArray(List<List<String>> arr) {
		for (List<String> a : arr) {
			printArrayList(a);
		}
		System.out.println("=================================================================================");
	}

	private static void printArrayList(List<String> arr) {
		for (int i = 0; i < arr.size(); i++) {
			System.out.print(arr.get(i));
			if (i != arr.size() - 1) System.out.print(" | ");
		}
		System.out.println();
	}

	private static void printResults(boolean[] results) {
		for (int i = 0; i < results.length; i++) {
			System.out.println(results[i]);
		}
		System.out.println("=================================================================================");
	}
	
	private static void printIndexTable(Map<String, List<Pair>> table) {
		for (Map.Entry<String, List<Pair>> e : table.entrySet()) {
			System.out.print(e.getKey() + " --> ");
			printPairList(e.getValue());
		}
		System.out.println("=================================================================================");
	}

	private static void printPairList(List<Pair> pairs) {
		for (Pair p : pairs) {
			System.out.print("(" + p.row + "," + p.col + ") ");
		}
		System.out.println();
	}

	private static void printSubst(Map<String, String> subst) {
		if (subst == null) {
			System.out.println("Failed");
		} else {
			System.out.print("{");
			for (Map.Entry<String, String> e : subst.entrySet()) {
				System.out.print(e.getKey() + "/" + e.getValue() + " ");
			}
			System.out.println("}");
		}
	}
	
}