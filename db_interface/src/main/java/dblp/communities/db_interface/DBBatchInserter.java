package dblp.communities.db_interface;

import java.util.Map;

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
}
