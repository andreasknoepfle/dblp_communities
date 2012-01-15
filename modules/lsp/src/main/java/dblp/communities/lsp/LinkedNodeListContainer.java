package dblp.communities.lsp;

import org.neo4j.graphdb.Node;

public class LinkedNodeListContainer {
	private Node node;
	private LinkedNodeListContainer nextNode;
	
	public LinkedNodeListContainer(Node node) {
		super();
		this.node = node;
	}
	public void setNode(Node node) {
		this.node = node;
	}
	public void setNextContainer(LinkedNodeListContainer nextNode) {
		this.nextNode = nextNode;
	}
	public Node getNode() {
		return node;
	}
	public LinkedNodeListContainer getNextNode() {
		return nextNode;
	}
}
