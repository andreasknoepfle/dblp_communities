package dblp.communities.generateweb;

import org.neo4j.graphdb.Relationship;

public class RelationsController {
	private static final String WEIGHT = "weight";
	Relationship relation;
	NodeController node;
	public RelationsController(Relationship rel, NodeController node) {
		this.relation=rel;
		this.node=node;
	}
	
	public NodeController getNode() {
		return node;	
	}
	
	public int getCount() {
		
		return (Integer) relation.getProperty(WEIGHT);
	}
}
