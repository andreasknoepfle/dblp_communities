package dblp.communities.generateweb.template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import dblp.communities.db_interface.AuthorGraphRelationshipType;

public class CommunityNodeController {
	private Node node;
	boolean sorted;
	public CommunityNodeController(Node n,boolean sorted) {
		this.node=n;
		this.sorted=sorted;
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
		if(sorted) {
			Comparator<NodeController> c=new Comparator<NodeController>() {
				@Override
				public int compare(NodeController o1, NodeController o2) {
					Long o1_num=(Long) o1.node.getProperty("count");
					Long o2_num=(Long) o2.node.getProperty("count");
					return (-1)*o1_num.compareTo(o2_num);
				}
			};
			Collections.sort(list, c);
		}
		return list;
	}
}
