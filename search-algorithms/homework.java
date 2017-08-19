import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Stack;
import java.util.PriorityQueue;

public class homework {
	static class AdjacentLocationWithTravelTime {
		public String location;
		public Integer travelTime;
		
		public AdjacentLocationWithTravelTime(String location, Integer travelTime) {
			this.location = location;
			this.travelTime = travelTime;
		}
	}
	
	static class Problem {
		public String algorithm;
		public String startLocation;
		public String goalLocation;
		public Map<String, ArrayList<AdjacentLocationWithTravelTime>> liveTraffic;
		public Map<String, Integer> sundayTraffic;
	}
	
	static class SearchTreeNode implements Comparable<SearchTreeNode> {
		public String location;
		public Integer accumulatedTime; // g(n)
		public Integer estimatedTime;   // h(n)
		public SearchTreeNode parentNode;
		public Integer orderInLiveTraffic;
		public Integer orderOfGeneration;
		
		public SearchTreeNode(String location, Integer accumulatedTime, Integer estimatedTime, 
				SearchTreeNode parentNode, Integer orderInLiveTraffic, Integer orderOfGeneration) {
			this.location = location;
			this.accumulatedTime = accumulatedTime;
			this.estimatedTime = estimatedTime;
			this.parentNode = parentNode;
			this.orderInLiveTraffic = orderInLiveTraffic;
			this.orderOfGeneration = orderOfGeneration;
		}

		public SearchTreeNode(String location, Integer accumulatedTime, Integer estimatedTime, SearchTreeNode parentNode) {
			this.location = location;
			this.accumulatedTime = accumulatedTime;
			this.estimatedTime = estimatedTime;
			this.parentNode = parentNode;
			this.orderInLiveTraffic = 0;
			this.orderOfGeneration = 0;
		}

		public SearchTreeNode() { }

		@Override
		public int compareTo(SearchTreeNode other) {
			int result = Integer.compare(accumulatedTime + estimatedTime, other.accumulatedTime + other.estimatedTime);
			if (result == 0) {
				result = parentNode == other.parentNode ? Integer.compare(orderInLiveTraffic, other.orderInLiveTraffic) 
						: Integer.compare(orderOfGeneration, other.orderOfGeneration);
			}
			return result;
		}
	}
	
	/*----------------------------------formulateProblem()----------------------------------*/
	
	private static void getLiveTrafficData(Scanner inputFileScanner, Problem problem) {
		int numberOfLiveTrafficLines = inputFileScanner.nextInt();
		inputFileScanner.nextLine();
		problem.liveTraffic = new HashMap<String, ArrayList<AdjacentLocationWithTravelTime>>();
		for (int i = 0; i < numberOfLiveTrafficLines; i++) {
			Scanner lineScanner = new Scanner(inputFileScanner.nextLine());			
			String fromLocation = lineScanner.next();
			String toLocation = lineScanner.next().trim();
			int travelTime = lineScanner.nextInt();
			ArrayList<AdjacentLocationWithTravelTime> adjacentLocations = problem.liveTraffic.containsKey(fromLocation) ? 
					problem.liveTraffic.get(fromLocation) : new ArrayList<AdjacentLocationWithTravelTime>();
			adjacentLocations.add(new AdjacentLocationWithTravelTime(toLocation, travelTime));
			problem.liveTraffic.put(fromLocation, adjacentLocations);
			lineScanner.close();
		}
	}
	
	private static void getSundayTrafficData(Scanner inputFileScanner, Problem problem) {
		int numberOfSundayTrafficLines = inputFileScanner.nextInt();
		inputFileScanner.nextLine();
		problem.sundayTraffic = new HashMap<String, Integer>();
		for (int i = 0; i < numberOfSundayTrafficLines; i++) {
			Scanner lineScanner = new Scanner(inputFileScanner.nextLine());
			String fromLocation = lineScanner.next();
			int estimatedTime = lineScanner.nextInt();
			problem.sundayTraffic.put(fromLocation, estimatedTime);
			lineScanner.close();
		}		
	}	

	private static void formulateProblem(Problem problem) throws FileNotFoundException {
		File inputFile = new File("input.txt");
		Scanner inputFileScanner = new Scanner(inputFile);
		try {
			problem.algorithm = inputFileScanner.nextLine();
			problem.startLocation = inputFileScanner.nextLine();
			problem.goalLocation = inputFileScanner.nextLine();
			getLiveTrafficData(inputFileScanner, problem);
			getSundayTrafficData(inputFileScanner, problem);
		} finally {
			inputFileScanner.close();
		}
	}
	
	/*----------------------------------search()----------------------------------*/
	
	private static SearchTreeNode breadthFirstSearch(Problem problem) {
		SearchTreeNode startNode = new SearchTreeNode(problem.startLocation, 0, 0, null);
		if (startNode.location.equals(problem.goalLocation)) {
			return startNode;
		}
		Queue<SearchTreeNode> frontierNodes = new LinkedList<SearchTreeNode>();
		Set<String> frontierLocations = new HashSet<String>();
		frontierNodes.add(startNode);
		frontierLocations.add(startNode.location);
		Set<String> exploredLocations = new HashSet<String>();
		while (!frontierNodes.isEmpty()) {
			SearchTreeNode currentNode = frontierNodes.remove();
			frontierLocations.remove(currentNode.location);
			exploredLocations.add(currentNode.location);
			ArrayList<AdjacentLocationWithTravelTime> adjacentLocations = problem.liveTraffic.get(currentNode.location);
			if (adjacentLocations != null) {
				for (AdjacentLocationWithTravelTime adjLoc : adjacentLocations) {
					SearchTreeNode childNode = new SearchTreeNode(adjLoc.location, currentNode.accumulatedTime + 1, 0, currentNode);
					if (!exploredLocations.contains(childNode.location) && !frontierLocations.contains(childNode.location)) {
						if (childNode.location.equals(problem.goalLocation)) {
							return childNode;
						}
						frontierNodes.add(childNode);
						frontierLocations.add(childNode.location);
					}
				}
			}
		}
		return null;
	}
	
	private static SearchTreeNode depthFirstSearch(Problem problem) {
		SearchTreeNode startNode = new SearchTreeNode(problem.startLocation, 0, 0, null);
		Stack<SearchTreeNode> frontierNodes = new Stack<SearchTreeNode>();
		Set<String> frontierLocations = new HashSet<String>();		
		frontierNodes.push(startNode);
		frontierLocations.add(startNode.location);		
		Set<String> exploredLocations = new HashSet<String>();
		while (!frontierNodes.isEmpty()) {
			SearchTreeNode currentNode = frontierNodes.pop();
			frontierLocations.remove(currentNode.location);
			if (currentNode.location.equals(problem.goalLocation)) {
				return currentNode;
			}
			exploredLocations.add(currentNode.location);
			ArrayList<AdjacentLocationWithTravelTime> adjacentLocations = problem.liveTraffic.get(currentNode.location);
			if (adjacentLocations != null) {
				for (int i = adjacentLocations.size() - 1; i >= 0; i--) {
					AdjacentLocationWithTravelTime adjLoc = adjacentLocations.get(i);
					SearchTreeNode childNode = new SearchTreeNode(adjLoc.location, currentNode.accumulatedTime + 1, 0, currentNode);
					if (!exploredLocations.contains(childNode.location)	&& !frontierLocations.contains(childNode.location)) {
						frontierNodes.push(childNode);
						frontierLocations.add(childNode.location);
					}
				}
			}
		}
		return null;
	}
	
	private static SearchTreeNode uniformCostOrAStarSearch(Problem problem) {
		int orderOfGeneration = 0;
		SearchTreeNode startNode = new SearchTreeNode(problem.startLocation, 0, 0, null, 0, ++orderOfGeneration);
		PriorityQueue<SearchTreeNode> frontierNodes = new PriorityQueue<SearchTreeNode>();
		Map<String, SearchTreeNode> frontierLocationsToNodes = new HashMap<String, SearchTreeNode>();
		frontierNodes.add(startNode);
		frontierLocationsToNodes.put(startNode.location, startNode);
		Map<String, SearchTreeNode> exploredLocationsToNodes = new HashMap<String, SearchTreeNode>();
		while (!frontierNodes.isEmpty()) {
			SearchTreeNode currentNode = frontierNodes.remove();
			frontierLocationsToNodes.remove(currentNode.location);
			if (currentNode.location.equals(problem.goalLocation)) {
				return currentNode;
			}
			exploredLocationsToNodes.put(currentNode.location, currentNode);
			ArrayList<AdjacentLocationWithTravelTime> adjacentLocations = problem.liveTraffic.get(currentNode.location);
			int orderInLiveTraffic = 0;
			if (adjacentLocations != null) {
				for (AdjacentLocationWithTravelTime adjLoc : adjacentLocations) {
					Integer estimatedTime = problem.algorithm.equals("UCS") ? 0 : problem.sundayTraffic.get(adjLoc.location);
					SearchTreeNode childNode = new SearchTreeNode(adjLoc.location, currentNode.accumulatedTime + adjLoc.travelTime, 
							estimatedTime, currentNode, ++orderInLiveTraffic, ++orderOfGeneration);
					if (!exploredLocationsToNodes.containsKey(childNode.location) 
							&& !frontierLocationsToNodes.containsKey(childNode.location)) {
						frontierNodes.add(childNode);
						frontierLocationsToNodes.put(childNode.location, childNode);
					} else if (frontierLocationsToNodes.containsKey(childNode.location)) {
						SearchTreeNode frontierNode = frontierLocationsToNodes.get(childNode.location);
						if (childNode.accumulatedTime < frontierNode.accumulatedTime) {
							frontierNodes.remove(frontierNode);
							frontierLocationsToNodes.remove(frontierNode.location);
							frontierNodes.add(childNode);
							frontierLocationsToNodes.put(childNode.location, childNode);
						}
					} else if (exploredLocationsToNodes.containsKey(childNode.location)) {
						SearchTreeNode exploredNode = exploredLocationsToNodes.get(childNode.location);
						if (childNode.accumulatedTime < exploredNode.accumulatedTime) {
							exploredLocationsToNodes.remove(exploredNode.location);
							frontierNodes.add(childNode);
							frontierLocationsToNodes.put(childNode.location, childNode);
						}
					}
				}
			}
		}
		return null;
	}
	
	private static SearchTreeNode search(Problem problem) {
		if (problem.algorithm.equals("BFS")) {
			return breadthFirstSearch(problem);
		} else if (problem.algorithm.equals("DFS")) {
			return depthFirstSearch(problem);
		} else if (problem.algorithm.equals("UCS") || problem.algorithm.equals("A*")) {
			return uniformCostOrAStarSearch(problem);
		} else {
			System.out.println("ERROR: Invalid algorithm name.");
			return null;
		}
	}
	
	/*----------------------------------printSearchResult()----------------------------------*/
	
	private static void printSearchResult(SearchTreeNode goalNode) throws FileNotFoundException {
		PrintWriter outputFilePrinter = new PrintWriter("output.txt");
		try {
			Stack<SearchTreeNode> stack = new Stack<SearchTreeNode>();
			SearchTreeNode currentNode = goalNode;
			while (currentNode != null) {
				stack.push(currentNode);
				currentNode = currentNode.parentNode;
			}
			while (!stack.isEmpty()) {
				currentNode = stack.pop();
				outputFilePrinter.println(currentNode.location + " " + currentNode.accumulatedTime);
			}
		} finally {
			outputFilePrinter.close();
		}
	}	
	
	/*----------------------------------main()----------------------------------*/
	
	public static void main(String[] args) {
		try {
			Problem problem = new Problem();
			formulateProblem(problem);
			SearchTreeNode goalNode = search(problem);
			if (goalNode == null) {
				System.out.println("Search failed.");
			} else {
				printSearchResult(goalNode);
			}
		} catch (FileNotFoundException exception) {
			System.out.println("ERROR: Input file not found or Can't open output file.");
		}
	}
}