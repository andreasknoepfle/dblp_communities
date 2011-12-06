import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.EmbeddedGraphDatabase;


public class DBConnector {

	public GraphDatabaseService graphDb;
	
	private String DB_PATH = "F:\\FU\\AnalyseAufBibDaten\\Server\\data\\graph.db";
	
	private static DBConnector instance = null;

	public void setDB_PATH(String dBPATH) {
		DB_PATH = dBPATH;
	}

	private DBConnector(){
		try {
			
			graphDb = new EmbeddedGraphDatabase( DB_PATH );
			registerShutdownHook( graphDb );
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void createRelationship(Node node1,Node node2,RelationshipType type){
		Transaction tx = DBConnector.getInstance().graphDb.beginTx();
		try {
			
			Relationship relationship = node1.createRelationshipTo( node2, type );
			tx.success();
			
		} finally {
			tx.finish();
		}
	}
	
	public void removeAllNodesAndRelations(){
		Transaction tx = DBConnector.getInstance().graphDb.beginTx();
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
	
	public Node createNode(){
		Node node = null;
		Transaction tx = DBConnector.getInstance().graphDb.beginTx();
		try {
			
			node = DBConnector.getInstance().graphDb.createNode();
			tx.success();
			
		} finally {
			tx.finish();
		}
		return node;
	}
	
	public void setProperty(String id, String value, Node node){
		
		Transaction tx = DBConnector.getInstance().graphDb.beginTx();
		try {
	
			node.setProperty(id, value);
			tx.success();
			
		} finally {
			tx.finish();
		}
	}
	
	public static DBConnector getInstance() {
        if (instance == null) {
            instance = new DBConnector();
        }
        return instance;
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
