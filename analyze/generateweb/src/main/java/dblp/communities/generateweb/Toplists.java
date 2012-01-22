package dblp.communities.generateweb;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import dblp.communities.db_interface.AuthorGraphRelationshipType;
import dblp.communities.db_interface.DoubleValueWrapper;

public class Toplists {
	
	public static List<DoubleValueWrapper> maxAuthorValue(Node node,String value,int topcount) {
		PriorityQueue<DoubleValueWrapper> prio=maxAuthorValueRecursive(node, value, topcount);
		ArrayList<DoubleValueWrapper> array=new ArrayList<DoubleValueWrapper>();
		for(int i=0;i<topcount && (!prio.isEmpty());i++) {
			array.add(prio.remove());
		}
		return array;
	}
	
	private static PriorityQueue<DoubleValueWrapper> maxAuthorValueRecursive(Node node,String value,int topcount) {
		
		Iterable<Relationship> rel = node.getRelationships(
				AuthorGraphRelationshipType.BELONGS_TO, Direction.INCOMING);
		Iterator<Relationship> iterator=rel.iterator();
		PriorityQueue<DoubleValueWrapper> queue=new PriorityQueue<DoubleValueWrapper>();
		while (iterator.hasNext()) {
	
				Relationship relation = iterator.next();
				Node child=relation.getStartNode();
				if(!child.getRelationships(AuthorGraphRelationshipType.BELONGS_TO, Direction.INCOMING).iterator().hasNext()) {
					 double val=(Double)child.getProperty(value);
					if(val!=0L)
						queue.add(new DoubleValueWrapper(val ,child.getId(),(String) child.getProperty("name")));
				} else {
					PriorityQueue<DoubleValueWrapper> subqueue=maxAuthorValueRecursive(child,value,topcount);
					for(int i=0;i<topcount && (!subqueue.isEmpty());i++) {
						queue.add(subqueue.remove());
					}
				}
				
		}
		return queue;
	}
}
