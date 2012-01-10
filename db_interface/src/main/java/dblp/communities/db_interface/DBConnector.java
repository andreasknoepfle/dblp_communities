package dblp.communities.db_interface;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.EmbeddedGraphDatabase;



public class DBConnector implements IDBConnector {

	public GraphDatabaseService graphDb;
	
	public GraphDatabaseService getGraphDb() {
		return graphDb;
	}

	private static Map<String,DBConnector> instances = null;

	private DBConnector(String DB_PATH){
		try {
			
			graphDb = new EmbeddedGraphDatabase( DB_PATH );
			registerShutdownHook( graphDb );
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/* (non-Javadoc)
	 * @see dblp.communities.db_interface.IDBConnector#createPublication(java.lang.Long, java.lang.Long, int)
	 */
	public Long createPublications(Long author_from,Long author_to, int weight){
	
		Transaction tx = graphDb.beginTx();
		Relationship rel;
		try {
			Node author_from_node =graphDb.getNodeById(author_from);
			Node author_to_node = graphDb.getNodeById(author_to);
			rel=author_from_node.createRelationshipTo( author_to_node, AuthorGraphRelationshipType.PUBLICATED_TOGETHER );
			
			rel.setProperty("weight", weight);
			tx.success();
			
		} finally {
			tx.finish();
		}
		return rel.getId();
	}
	
	public Long authorToCommunity(Long author,Long community){
		
		Transaction tx = graphDb.beginTx();
		Relationship rel;
		try {
			Node author_node = graphDb.getNodeById(author);
			Node community_node = graphDb.getNodeById(community);
			
			rel=author_node.createRelationshipTo( community_node, AuthorGraphRelationshipType.BELONGS_TO );
			
			tx.success();
			
		} finally {
			tx.finish();
		}
		return rel.getId();
	}
	
	
	
	public void removeAllNodesAndRelations(){
		Transaction tx = graphDb.beginTx();
		try {

			for (Node node : graphDb.getAllNodes()) {
				for (Relationship rel : node.getRelationships()) {
					rel.delete();
				}
				node.delete();
			}
			
			tx.success();
			
		} finally {
			tx.finish();
		}
	}
	
	public void removeAuthor(Long id) {
		Transaction tx = graphDb.beginTx();
		try {

			graphDb.getNodeById(id).delete();
			
			tx.success();
			
		} finally {
			tx.finish();
		}
	}
	
	public void removeSingleAuthors() {
		Transaction tx = graphDb.beginTx();
		try {

			for (Node node : graphDb.getAllNodes()) {
				if(!node.hasRelationship()) {
					node.delete();
				}
			}
			
			tx.success();
			
		} finally {
			tx.finish();
		}
	}
	
	public Long createCommunity(){
		Node node = null;
		Transaction tx = graphDb.beginTx();
		try {
			
			node = graphDb.createNode();
		
			tx.success();
			
		} finally {
			tx.finish();
		}
		return node.getId();
	}
	
	
	public Long createAuthor(String name){
		Node node = null;
		Transaction tx = graphDb.beginTx();
		try {
			
			node = graphDb.createNode();
		
			tx.success();
			
		} finally {
			tx.finish();
		}
		setAuthorProperty(node.getId(), "name", name);
		return node.getId();
	}
	
	/* (non-Javadoc)
	 * @see dblp.communities.db_interface.IDBConnector#setProperty(org.neo4j.graphdb.Node, java.lang.String, java.lang.String)
	 */
	public void setAuthorProperty( Long node,String id, String value){
		
		Transaction tx = graphDb.beginTx();
		try {
			graphDb.getNodeById(node).setProperty(id, value);
			tx.success();
			
		} finally {
			tx.finish();
		}
	}
	
	public static DBConnector getInstance(String dbPath) {
        if (instances == null) {
            instances = new HashMap<String,DBConnector>();
        }
        DBConnector dbcon=instances.get(dbPath);
        if(dbcon==null) {
        	 dbcon=new DBConnector(dbPath);
             instances.put(dbPath, dbcon);
        }
        return dbcon;
    }
	
	
	protected void finalize () {
		
		graphDb.shutdown();
		
	}
	
	private static void registerShutdownHook(final GraphDatabaseService graphDb) {
		// Registers a shutdown hook for the Neo4j instance so that it
		// shuts down nicely when the VM exits (even if you "Ctrl-C" the
		// running example before it's completed)
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				graphDb.shutdown();
			}
		});
	}

	
}
