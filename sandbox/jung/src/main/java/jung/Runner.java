package jung;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;

public class Runner {
	Graph<String, Integer> graph = null;

	public Runner() {
		graph = new SparseMultigraph<String, Integer>();
	}

	public String readFile(String file) {
		StringBuffer sb = new StringBuffer();
		try {
			String line;
			BufferedReader br = new BufferedReader(new FileReader(file));
			while ((line = br.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	public void readCoOccurances(String coOccurranceFile, String delimiter) {
		HashSet<String> vertices = new HashSet<String>();
		String[] lines = readFile(coOccurranceFile).split("\n");
		int edgeCount = 0;
		for (String line : lines) {
			String[] split = line.split(delimiter);
			if (!vertices.contains(split[0].trim())) {
				graph.addVertex(split[0].trim());
				vertices.add(split[0].trim());
			}
			for (int i = 1; i < split.length; i++) {
				edgeCount++;
				graph.addEdge(edgeCount, split[0].trim(), split[i].trim());
			}
		}
	}

	public static void main(String[] args) {
		Runner r = new Runner();
		r
				.readCoOccurances(
						"/Users/tejaswi/Documents/workspace/reDygest/datasets/junggraphfiles/clinton_1_10.graph",
						",");
		System.out.println("The graph :  " + r.graph.toString());

		CommunityDetection cd = new CommunityDetection();
		Set<Set<String>> transformed = cd.run(r.graph, 10);
		System.out.println("Communities:");
		for (Set<String> community : transformed) {
			System.out.println(community);
		}
	}

}
