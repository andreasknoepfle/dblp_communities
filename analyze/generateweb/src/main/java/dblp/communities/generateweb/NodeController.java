package dblp.communities.generateweb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import dblp.communities.db_interface.AuthorGraphRelationshipType;

public class NodeController {
	
	public static final String NAME = "name";
	Node node;
	public NodeController(Node n) {
		this.node=n;
	}
	
	public Map<String,Object> getProperties() {
		Map<String,Object> props=new HashMap<String, Object>();
		for (String key : node.getPropertyKeys()) {
			props.put(key, node.getProperty(key));
		}
		return props;
	}
	
	public long getId() {
		return node.getId();
	}
	
	public String getName() {
		String name=(String) node.getProperty(NAME,null);
		if(name!=null) {
			return name;
		} else {
			return String.valueOf(node.getId());
		}
	}
	
	public List<NodeController> getCommunities() {
		List<NodeController> list=new ArrayList<NodeController>();
		list.add(this);
		Iterable<Relationship> rels=node.getRelationships(AuthorGraphRelationshipType.BELONGS_TO, Direction.OUTGOING);
		while(rels.iterator().hasNext()) {
			Node community=rels.iterator().next().getEndNode();
			rels=community.getRelationships(AuthorGraphRelationshipType.BELONGS_TO, Direction.OUTGOING);
			if(rels.iterator().hasNext()) {
					list.add(new NodeController(community));
			}
			
		}
		Collections.reverse(list);
		// Remove Year node
		if(!list.isEmpty()) {
			list.remove(0);
		}
		return list;
	}
	
	
	
}
