package dblp.communities.db_interface;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.kernel.impl.batchinsert.BatchInserter;
import org.neo4j.kernel.impl.batchinsert.BatchInserterImpl;

public class DBBatchInserter implements IDBConnector {
	
	static BatchInserter inserter;
	
	
	public DBBatchInserter(String dbPath) {
		
		inserter=new BatchInserterImpl(dbPath);
		
		registerShutdownHook();
	}

	@Override
	public Long createAuthor(String name) {
		Map<String, Object> properties=null;
		
		 properties = MapUtil.map( "name", name );
		
		return inserter.createNode(properties);
	}

	@Override
	public Long createPublications(Long authorFrom, Long authorTo,
			int weight) {
		Map<String, Object> properties=null;
		
		properties = MapUtil.map( "weight", weight );
		
		Long relId=inserter.createRelationship(authorFrom, authorTo, AuthorGraphRelationshipType.PUBLICATED_TOGETHER, properties);
		return relId;
	}

	public Long createCommunity() {
		Map<String, Object> properties=null;
		
		 //properties = MapUtil.map( "name", name );
		
		return inserter.createNode(properties);
		
	}
	
	public Long createYear(int from,int to) {
		Map<String, Object> properties=new HashMap<String,Object>();
		
		 properties.put("fromYear", from);
		 properties.put("toYear", to);
		
		Long yearid=inserter.createNode(properties);
		inserter.createRelationship(yearid, inserter.getReferenceNode(), AuthorGraphRelationshipType.BELONGS_TO, null);
		return yearid;
		
	}
	
	
	public Long belongsTo(Long node,Long supernode) {
		return inserter.createRelationship(node, supernode, AuthorGraphRelationshipType.BELONGS_TO, null);
	}
	
	@Override
	public void setAuthorProperty(Long node, String id, String value) {
		inserter.setNodeProperty(node, id, value);
		
	}
	
	public void shutdown() {
		inserter.shutdown();
	}
	
	private static void registerShutdownHook() {
		// Registers a shutdown hook for the Neo4j instance so that it
		// shuts down nicely when the VM exits (even if you "Ctrl-C" the
		// running example before it's completed)
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				inserter.shutdown();
			}
		});
	}

	public long createNode(Node node) {
		Map<String, Object> properties=new HashMap<String, Object>();
		for(String p:node.getPropertyKeys())
			properties.put(p, node.getProperty(p));
		
		return inserter.createNode(properties);
		
	}
	
	public long createRelationship(Relationship rel) {
		Map<String, Object> properties=new HashMap<String, Object>();
		for(String p:rel.getPropertyKeys())
			properties.put(p, rel.getProperty(p));
		
		return inserter.createRelationship(rel.getStartNode().getId(), rel.getEndNode().getId(), rel.getType(), properties);
		
	}
}
