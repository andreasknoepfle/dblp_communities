package dblp.communities.generateweb.template;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import dblp.communities.db_interface.AuthorGraphRelationshipType;

public class CommunityNodeController {
	private Node node;
	public CommunityNodeController(Node n) {
		this.node=n;
	}
	public List<NodeController> getChildren() {
		List<NodeController> list=new ArrayList<NodeController>();
		Iterable<Relationship> rels=node.getRelationships(AuthorGraphRelationshipType.BELONGS_TO, Direction.INCOMING);
		Iterator<Relationship> iterator=rels.iterator();
		while(iterator.hasNext()) {
			Relationship rel=iterator.next();
			Node other=rel.getOtherNode(node);
			list.add(new NodeController(other));
		}
		return list;
	}
}
