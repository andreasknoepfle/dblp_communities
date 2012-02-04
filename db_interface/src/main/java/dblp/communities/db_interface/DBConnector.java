package dblp.communities.db_interface;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ReturnableEvaluator;
import org.neo4j.graphdb.StopEvaluator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.Traverser;
import org.neo4j.graphdb.Traverser.Order;
import org.neo4j.kernel.EmbeddedGraphDatabase;

public class DBConnector implements IDBConnector {

	public static final String WEIGHT_PROPERTY = "weight";
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
			
			rel.setProperty(WEIGHT_PROPERTY, weight);
			tx.success();
			
		} finally {
			tx.finish();
		}
		return rel.getId();
	}
	
	public Long belongsTo(Node node , Node superNode){
		
		Transaction tx = graphDb.beginTx();
		Relationship rel;
		try {
			
			rel=node.createRelationshipTo( superNode, AuthorGraphRelationshipType.BELONGS_TO );
			
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
	
	/*
	 * (non-Javadoc)
	 * @see dblp.communities.db_interface.IDBConnector#setAuthorProperty(java.lang.Long, java.lang.String, java.lang.String)
	 * To be complatible
	 */
	@Override
	public void setAuthorProperty(Long node, String id, String value) {
		setAuthorProperty(node, id, value);
		
	}
	
	public void setAuthorProperty(Node node, String id, Object value) {
		Transaction tx = graphDb.beginTx();
		try {
			node.setProperty(id, value);
			tx.success();
			
		} finally {
			tx.finish();
		}
		
	}
	
	public void setAuthorProperty( Long node,String id, Object value){
		
		setAuthorProperty(graphDb.getNodeById(node), id, value);
		
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

	public Node createYear(Integer from, Integer to) {
		Node node = null;
		Transaction tx = graphDb.beginTx();
		try {
			
			node = graphDb.createNode();
		
			tx.success();
			
		} finally {
			tx.finish();
		}
		tx = graphDb.beginTx();
		try {
			node.setProperty("fromYear", from);
			node.setProperty("toYear", to);
			tx.success();
			
		} finally {
			tx.finish();
		}
		
		return node;
		
	}

	public void removeCommunity(Node fake) {
		Transaction tx = graphDb.beginTx();
		try {
			
			fake.delete();
		
			tx.success();
			
		} finally {
			tx.finish();
		}
		
	}

	public void setProperties(HashMap<Node, Object> properties ,String property) {
		Transaction tx = this.getGraphDb().beginTx();
		
		try {
			for (Node writenode : properties.keySet()) {
				writenode.setProperty(property, properties.get(writenode));
			}
			tx.success();
	
		} finally {
			tx.finish();
		}
	}

	public void propertyImport(Map<Long,Object> nodeProperties,String property) {
		int num = 0;
		int totalnum = nodeProperties.size();
		HashMap<Node, Object> properties = new HashMap<Node, Object>();
		for (Long node : nodeProperties.keySet()) {
			if (num % 1000 == 0 && num != 0) {
				// Input 1000 in one Transaction
				if (num % 10000 == 0)
					System.out.println(num + "/" + totalnum);
					setProperties(properties, property);
	
				properties.clear();
	
			}
			num++;
			// Adding to List
			properties.put(this.getGraphDb().getNodeById(node), nodeProperties.get(node));
		}
		// Get the last ones
		setProperties(properties, property);
	}

	



	public static LinkedNodeList collect(Node node) {
	
		Iterable<Relationship> relations = node.getRelationships(
				AuthorGraphRelationshipType.BELONGS_TO, Direction.INCOMING);
		if(!relations.iterator().hasNext()) {
			//author node
			return new LinkedNodeList(node);
		} 
		//community node
	
		Iterator<Relationship> iterator=relations.iterator();
		Relationship relation=iterator.next();
		LinkedNodeList nodelist=collect(relation.getStartNode());
	
	
		//call on all children and get their communities
		while(iterator.hasNext()) {
			relation=iterator.next();
			LinkedNodeList list=collect(relation.getStartNode());
	
			nodelist.add(list); //add to our community
	
		}
		return nodelist;
	}
	
	public static long isInCommunity(Node node,Set<Long> communities) {
		Iterator<Relationship> parent=node.getRelationships(Direction.OUTGOING,AuthorGraphRelationshipType.BELONGS_TO).iterator();
		
		if(!parent.hasNext()) {
			return -1;
		}
		Node parentnode=parent.next().getEndNode();
		if(communities.contains(parentnode.getId())) {
			return parentnode.getId();
		} else {
			return isInCommunity(parentnode, communities);
		}
	}
	
	public HashMap<Long,HashSet<Long>> getTopLevelNodes(){
		HashMap<Long,HashSet<Long>> topLevelNodes = new HashMap<Long,HashSet<Long>>();
		Node ref_Node = graphDb.getReferenceNode();
		
		Traverser traverse_years = ref_Node.traverse(Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL_BUT_START_NODE, 
				AuthorGraphRelationshipType.BELONGS_TO, Direction.INCOMING);
		
		for (Node year_node : traverse_years) {
			Iterable<Relationship> topLevel = year_node.getRelationships(Direction.INCOMING, AuthorGraphRelationshipType.BELONGS_TO);
			
			HashSet<Long> topLevelYear = new HashSet<Long>();
			
			for (Relationship relationship_topLevel : topLevel) {
				
				topLevelYear.add(relationship_topLevel.getStartNode().getId());
				
			}
			
			topLevelNodes.put(year_node.getId(), topLevelYear);
			
		}
		return topLevelNodes;
		
	}


}