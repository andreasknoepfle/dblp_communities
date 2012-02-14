package dblp.communities.conductance;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ReturnableEvaluator;
import org.neo4j.graphdb.StopEvaluator;
import org.neo4j.graphdb.Traverser;
import org.neo4j.graphdb.Traverser.Order;

import dblp.communities.db_interface.AuthorGraphRelationshipType;
import dblp.communities.db_interface.DBConnector;

public class Conductance {


	private static DBConnector dbconnector;
	private static HashMap<Long , Object> top_level_node_property;
	private static HashMap<Long , Object> cutsize_property;
	private static HashMap<Long , Object> edgesize_property;
	private static HashMap<Long , Object> conductance_property;
	
	public static void main(String[] args) {
		
		dbconnector = DBConnector.getInstance(args[0]);
		Node root=dbconnector.getGraphDb().getReferenceNode();
		HashMap<Long, HashSet<Long>> topLevelNodes=dbconnector.getTopLevelNodes();
		
		top_level_node_property=new HashMap<Long, Object>();
		cutsize_property=new HashMap<Long, Object>();
		edgesize_property=new HashMap<Long, Object>();
		conductance_property=new HashMap<Long, Object>();
		Traverser years=root.traverse(Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL_BUT_START_NODE, AuthorGraphRelationshipType.BELONGS_TO, Direction.INCOMING);
		System.out.println("Starting Calulation of cutsize and edgesize.");
		for (Node year : years) {
			Traverser communities=year.traverse(Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL_BUT_START_NODE, AuthorGraphRelationshipType.BELONGS_TO, Direction.INCOMING);
			int num=0;
			System.out.println("Year " + year.getId());
			long total_edges=0;
			for (Node node : communities) {
				if(num%10000==0) {
					System.out.println(num);
				}
				HashSet<Long> cutSet=new HashSet<Long>();
				HashSet<Long> edgeSet=new HashSet<Long>();
				edgeAndCutSize(node,dbconnector,topLevelNodes.get(year.getId()),cutSet,edgeSet);
				cutsize_property.put(node.getId(),cutSet.size());
				edgesize_property.put(node.getId(),edgeSet.size());
				total_edges+=edgeSet.size();
				num++;
				
			}
			
			communities=year.traverse(Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL_BUT_START_NODE, AuthorGraphRelationshipType.BELONGS_TO, Direction.INCOMING);
			for (Node node : communities) {
				int cutsize=(Integer)cutsize_property.get(node.getId());
				int edgesize=(Integer)edgesize_property.get(node.getId());
				double conductance= ((double)cutsize)/(double)(edgesize<total_edges?edgesize:total_edges);
				conductance_property.put(node.getId(), conductance);
			}
			
		}
		
		System.out.println("Importing Properties.");
		dbconnector.propertyImport(top_level_node_property, "top_level_community");
		dbconnector.propertyImport(edgesize_property, "edge_count");
		dbconnector.propertyImport(cutsize_property, "cutsize");
		dbconnector.propertyImport(conductance_property, "conductance");
		
		
	}

	private static void edgeAndCutSize(Node node, DBConnector dbconnector,Set<Long> communities, HashSet<Long> cutSet, HashSet<Long> edgeSet) {
		
		Traverser children_traverser=node.traverse(Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL_BUT_START_NODE, AuthorGraphRelationshipType.BELONGS_TO, Direction.INCOMING);
		Collection<Node> children=children_traverser.getAllNodes();
		if(children.isEmpty()) {
			//Author node
			Iterable<Relationship> rels=node.getRelationships(AuthorGraphRelationshipType.PUBLICATED_TOGETHER);
			Iterator<Relationship> rels_iterator=rels.iterator();
			Long top_level_id_node = DBConnector.isInCommunity(node, communities);
			top_level_node_property.put(node.getId(), top_level_id_node);
			
			while (rels_iterator.hasNext()) {
				Relationship rel= rels_iterator.next();
				Long top_level_id_other=DBConnector.isInCommunity(rel.getOtherNode(node), communities);
				if(top_level_id_node.equals(top_level_id_other)) {
					edgeSet.add(rel.getId());
				} else {
					cutSet.add(rel.getId());
				}
				
				
			}
		} else {
			for (Node child : children) {
				edgeAndCutSize(child, dbconnector, communities, cutSet, edgeSet);
			}
		}
		
		
		
	}


}
