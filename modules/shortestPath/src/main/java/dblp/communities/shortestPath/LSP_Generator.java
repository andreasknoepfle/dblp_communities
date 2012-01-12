package dblp.communities.shortestPath;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import dblp.communities.db_interface.AuthorGraphRelationshipType;
import dblp.communities.db_interface.DBConnector;

public class LSP_Generator {

	final static String longestshortestpath = "lsp";
	private static DBConnector dbconnector;

	private static Map<Long, Object> lsps;
	public static ExecutorService pool = Executors.newFixedThreadPool(20);

	public static void main(String[] args) {

		try {
			if (args.length == 2) {

				dbconnector = DBConnector.getInstance(args[0]);
				lsps = Collections.synchronizedMap(new HashMap<Long, Object>());
				Node start=dbconnector.getGraphDb().getNodeById(Long.valueOf(args[1]));
				//new LSPThread(start,lsps).getLongestShortestPath(start);
				getLongestShortestPath(start);
				dbconnector.propertyImport(lsps, longestshortestpath);
				System.out.println("done");
				pool.shutdown();

			} else {
				System.out.println("Usage: LSP_Generator <db_path> <root_id>");
			}

		} catch (Exception e) {
			System.out.println("Usage: LSP_Generator <db_path> <root_id>");
		}
	}

	public static void getLongestShortestPath(Node community) {

		Iterable<Relationship> rel = community.getRelationships(
				AuthorGraphRelationshipType.BELONGS_TO, Direction.INCOMING);

		
		HashSet<Future<Long>> runable=new HashSet<Future<Long>>();
		
		while (rel.iterator().hasNext()) {
			LSPThread lsp_thread=new LSPThread(rel.iterator().next().getStartNode(),lsps);
			runable.add(pool.submit(lsp_thread));
			
		}
		//join Threads 
		
		long max=0;
		for (Future<Long> future : runable) {
			try {
				if(max<future.get()){
					max=future.get();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		lsps.put(community.getId(), max);

	}

	/*
	 * private static long createCommunityExample(DBConnector connector, int
	 * communities, int authors){
	 * 
	 * long id_root = connector.createCommunity();
	 * 
	 * for(int i = 0; i < communities;i ++){
	 * 
	 * ArrayList<Long> al_auths = new ArrayList<Long>();
	 * 
	 * long id_c = connector.createCommunity();
	 * connector.belongsTo(connector.getGraphDb().getNodeById(id_c),
	 * connector.getGraphDb().getNodeById(id_root));
	 * 
	 * for(int j = 0; j< authors*(i+1); j++){ long id_a =
	 * connector.createAuthor("author"+i+j); al_auths.add(id_a);
	 * connector.belongsTo(connector.getGraphDb().getNodeById(id_a),
	 * connector.getGraphDb().getNodeById(id_c)); }
	 * 
	 * for(int k = 0; k < authors*(i+1)-1;k++){
	 * 
	 * try{
	 * 
	 * Random randomGenerator = new Random(); int auth1 =
	 * k;//randomGenerator.nextInt(al_auths.size()); int auth2 =
	 * k+1;randomGenerator.nextInt(al_auths.size());
	 * 
	 * if(auth1 != auth2) connector.createPublications(al_auths.get(auth1),
	 * al_auths.get(auth2),1);
	 * 
	 * }catch (Exception e) { e.printStackTrace(); }
	 * 
	 * }
	 * 
	 * } return id_root; }
	 */

}
