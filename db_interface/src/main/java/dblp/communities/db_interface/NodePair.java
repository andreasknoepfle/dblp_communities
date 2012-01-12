package dblp.communities.db_interface;

import org.neo4j.graphdb.Node;

public class NodePair {
	private Node n_from;
	private Node n_to;
	
	
	public NodePair(Node nFrom, Node nTo) {
		super();
		n_from = nFrom;
		n_to = nTo;
	}
	public Node getN_from() {
		return n_from;
	}
	public void setN_from(Node nFrom) {
		n_from = nFrom;
	}
	public Node getN_to() {
		return n_to;
	}
	public void setN_to(Node nTo) {
		n_to = nTo;
	}
}
