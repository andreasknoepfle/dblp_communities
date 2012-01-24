package dblp.communities.generateweb;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import dblp.communities.db_interface.AuthorGraphRelationshipType;
import dblp.communities.db_interface.DBConnector;
import dblp.communities.db_interface.DoubleValueWrapper;
/**
 * Has to be refactored to submodule
 * @author andi
 *
 */
public class Toplists {
	
	public static List<DoubleValueWrapper> maxAuthorValue(Node node,String value,int topcount,DBConnector db) {
		PriorityQueue<DoubleValueWrapper> prio=maxAuthorValueRecursive(node, value, topcount,0,db);
		ArrayList<DoubleValueWrapper> array=new ArrayList<DoubleValueWrapper>();
		for(int i=0;i<topcount && (!prio.isEmpty());i++) {
			array.add(prio.remove());
		}
		return array;
	}
	
	private static PriorityQueue<DoubleValueWrapper> maxAuthorValueRecursive(Node node,String value,int topcount,int level,DBConnector db) {
		
		Iterable<Relationship> rel = node.getRelationships(
				AuthorGraphRelationshipType.BELONGS_TO, Direction.INCOMING);
		Iterator<Relationship> iterator=rel.iterator();
		PriorityQueue<DoubleValueWrapper> queue=new PriorityQueue<DoubleValueWrapper>();
		PriorityQueue<DoubleValueWrapper> queue_top_level = null;
		
		if(level==2) {
			queue_top_level=new PriorityQueue<DoubleValueWrapper>();
		}
		while (iterator.hasNext()) {
	
				Relationship relation = iterator.next();
				Node child=relation.getStartNode();
				if(!child.getRelationships(AuthorGraphRelationshipType.BELONGS_TO, Direction.INCOMING).iterator().hasNext()) {
					 double val=(Double)child.getProperty(value);
					if(val!=0L)
						queue.add(new DoubleValueWrapper(val ,child.getId(),(String) child.getProperty("name")));
				} else {
					PriorityQueue<DoubleValueWrapper> subqueue=maxAuthorValueRecursive(child,value,topcount,level+1,db);
					for(int i=0;i<topcount && (!subqueue.isEmpty());i++) {
						if(level==2) {
								queue_top_level.add(subqueue.peek());
						}
						queue.add(subqueue.remove());
					}
				}
				
				
		}
		if(level==2) {
			ArrayList<String> list=new ArrayList<String>();
			for(int i=0;i<topcount && (!queue_top_level.isEmpty());i++) {
				// BAD! Refactoring needed
				DoubleValueWrapper wrapper=queue_top_level.remove();
				list.add("<a href="+wrapper.getId()+".html>"+wrapper.getName()+"</a>");
				
			}
			db.setAuthorProperty(node, "top_"+value, list.toArray(new String[0]));
			
		}
		return queue;
	}
}
