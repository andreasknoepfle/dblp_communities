package dblp.communities.roles;

import java.util.HashSet;
import java.util.Iterator;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ReturnableEvaluator;
import org.neo4j.graphdb.StopEvaluator;
import org.neo4j.graphdb.Traverser;
import org.neo4j.graphdb.Traverser.Order;

import dblp.communities.db_interface.AuthorGraphRelationshipType;
import dblp.communities.db_interface.DBConnector;

public class Launcher {

	
	private static DBConnector dbconnector;

	public static void main(String[] args) {

		
		if (args.length == 1) {

			dbconnector = DBConnector.getInstance(args[0]);
			
			Node start=dbconnector.getGraphDb().getReferenceNode();
			System.out.println("Starting with node "+start.getId());
			try {
				threadStarter(start,dbconnector);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("done");
			System.out.println("Analyze Roles");
			AnalyzeRoles analyze=new AnalyzeRoles();
			Traverser traverser=dbconnector.getGraphDb().getReferenceNode().traverse(Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL_BUT_START_NODE, AuthorGraphRelationshipType.BELONGS_TO, Direction.INCOMING);
			for (Node n:traverser) {
				analyze.rolesDistribution(n, dbconnector);
			}
			
			System.out.println("done");
			

		} else {
			System.out.println("Usage: Roles <db_path>");
		}

		
	}

	public static void threadStarter(Node root, DBConnector dbconnector2) throws InterruptedException {

		Iterable<Relationship> rel = root.getRelationships(
				AuthorGraphRelationshipType.BELONGS_TO, Direction.INCOMING);

		
		HashSet<Thread> threads=new HashSet<Thread>();
		Iterator<Relationship> iterator=rel.iterator();
		while (iterator.hasNext()) {
			Node source=iterator.next().getStartNode();
			//if(source.getId()==1421038) {
				CalculateRoleValues roles_thread=new CalculateRoleValues(source,dbconnector);
				threads.add(roles_thread);
				roles_thread.start();
			//}
			
		}
		//join Threads 
		for (Thread thread : threads) {
			thread.join();
		}	

	}

}
