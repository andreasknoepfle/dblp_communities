package dblp.communities.lsp;

import java.util.HashSet;
import java.util.Iterator;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import dblp.communities.db_interface.AuthorGraphRelationshipType;
import dblp.communities.db_interface.DBConnector;

public class Launcher {

	
	private static DBConnector dbconnector;

	public static void main(String[] args) {

		try {
			if (args.length == 2) {

				dbconnector = DBConnector.getInstance(args[0]);
				
				Node start=dbconnector.getGraphDb().getNodeById(Long.valueOf(args[1]));
				//new LSPThread(start,lsps).getLongestShortestPath(start);
				getLongestShortestPath(start,dbconnector);
				
				System.out.println("done");
				

			} else {
				System.out.println("Usage: LSP_Generator <db_path> <root_id>");
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Usage: LSP_Generator <db_path> <root_id>");
		}
	}

	public static void getLongestShortestPath(Node root, DBConnector dbconnector2) throws InterruptedException {

		Iterable<Relationship> rel = root.getRelationships(
				AuthorGraphRelationshipType.BELONGS_TO, Direction.INCOMING);

		
		HashSet<Thread> threads=new HashSet<Thread>();
		Iterator<Relationship> iterator=rel.iterator();
		while (iterator.hasNext()) {
			Node source=iterator.next().getStartNode();
			
			LongestShortestPathThread lsp_thread=new LongestShortestPathThread(source,dbconnector);
			threads.add(lsp_thread);
			lsp_thread.start();
			
		}
		//join Threads 
		for (Thread thread : threads) {
			thread.join();
		}	

	}

}
