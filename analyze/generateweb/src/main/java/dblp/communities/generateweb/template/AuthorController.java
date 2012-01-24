package dblp.communities.generateweb.template;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import dblp.communities.db_interface.AuthorGraphRelationshipType;

public class AuthorController {
	
	
	private Node node;
	
	public AuthorController(Node n) {
		this.node=n;
	}
	
	
	
	public List<RelationsController> getNeighbours() {
		List<RelationsController> neighbours=new ArrayList<RelationsController>();
		Iterable<Relationship> relationships=node.getRelationships(AuthorGraphRelationshipType.PUBLICATED_TOGETHER);
		Iterator<Relationship> iterator=relationships.iterator();
		while(iterator.hasNext()) {
			Relationship rel=iterator.next();
			Node other=rel.getOtherNode(node);
			neighbours.add(new RelationsController(rel,new NodeController(other)));
		}
		return neighbours;
	}
}
