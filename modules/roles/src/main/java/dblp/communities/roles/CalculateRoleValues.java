package dblp.communities.roles;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import dblp.communities.db_interface.AuthorGraphRelationshipType;
import dblp.communities.db_interface.DBConnector;
import dblp.communities.db_interface.LinkedNodeList;
import dblp.communities.db_interface.LinkedNodeListContainer;


public class CalculateRoleValues extends Thread {
	private Node startnode;
	Map<Long, Object> participationCoeffitient;
	Map<Long, Object> withinModuleDegree;
	DBConnector dbConnector;
	

	public CalculateRoleValues(Node startnode, DBConnector dbConnector) {

		this.dbConnector=dbConnector;
		this.startnode=startnode;
		
	}

	@Override
	public void run() {
		
		System.out.println("Thread: "+Thread.currentThread().getId() + " started");
		Iterable<Relationship> relations = startnode.getRelationships(
				AuthorGraphRelationshipType.BELONGS_TO, Direction.INCOMING);
		Iterator<Relationship> iterator=relations.iterator();
		int num=1;
		long count=0;
		while(relations.iterator().hasNext()) {
			Relationship relation=iterator.next();
			count+=(Long) relation.getStartNode().getProperty("count");
			System.out.println("Thread: "+Thread.currentThread().getId() + " Num:"+num+ " Count:"+count);
			   LinkedNodeList nodelist=DBConnector.collect(relation.getStartNode());
			   calculateRoles(startnode, nodelist);
			num++;
		}
		
		System.out.println(Thread.currentThread().getId() + " finished");
	}
	
	


	private void calculateRoles(Node startnode, LinkedNodeList nodelist) {
		participationCoeffitient = new HashMap<Long, Object>();
		withinModuleDegree = new HashMap<Long, Object>();
		HashSet<Long> ids=new HashSet<Long>();
		LinkedNodeListContainer nodeiterator = nodelist.getStartnode();
		//
		while(nodeiterator!=null) {
			Node node=nodeiterator.getNode();
			ids.add(node.getId());
			nodeiterator=nodeiterator.getNextNode();
		}
		
		nodeiterator = nodelist.getStartnode();;
		//
		long module_degree_sum=0;
		long module_degree_count=0;
		StandardDeviation stddev=new StandardDeviation();
		
		HashSet<Long> neighbour_ids=new HashSet<Long>();
		//Get Parent Node
		Relationship parent=startnode.getRelationships(Direction.OUTGOING,AuthorGraphRelationshipType.BELONGS_TO).iterator().next();
		Node parentnode=parent.getEndNode();
		Iterator<Relationship> neighbours=parentnode.getRelationships(Direction.INCOMING,AuthorGraphRelationshipType.BELONGS_TO).iterator();
		
		while(neighbours.hasNext()) {
			neighbour_ids.add(neighbours.next().getStartNode().getId());
		}
		
		HashMap<Long,Integer> module_degree_map=new HashMap<Long, Integer>();
		HashMap<Long,Integer> total_count_map=new HashMap<Long, Integer>();
		while(nodeiterator!=null) {
			HashMap<Long,Integer> communityEdges=new HashMap<Long, Integer>();
			Node node=nodeiterator.getNode();
			Iterable<Relationship> nodelinks=node.getRelationships(AuthorGraphRelationshipType.PUBLICATED_TOGETHER);
			Iterator<Relationship> linkiterator=nodelinks.iterator();
			int module_degree=0;
			int total_count=0;
			while(linkiterator.hasNext()) {
				Node other=linkiterator.next().getOtherNode(node);
				if(ids.contains(other.getId())) {
					module_degree++;
				}
				long community_id=DBConnector.isInCommunity(other, neighbour_ids);
				if(community_id!=-1) {
					if(communityEdges.get(community_id)==null) {
						communityEdges.put(community_id, new Integer(1));
					} else {
						communityEdges.put(community_id, communityEdges.get(community_id)+1);
					}
				}
				total_count++;
			}
			module_degree_map.put(node.getId(), module_degree);
			total_count_map.put(node.getId(), total_count);
			module_degree_count++;
			module_degree_sum+=module_degree;
			stddev.increment(module_degree);
			nodeiterator=nodeiterator.getNextNode();
			double p_sum=0;
			for (Long community_id:communityEdges.keySet()) {
				p_sum+=Math.pow(((double)communityEdges.get(community_id)) / ((double)total_count),2);
			}
			double p=1-p_sum;
			participationCoeffitient.put(node.getId(), p);
		}
		double avg_module_degree=((double)module_degree_sum)/((double)module_degree_count);
		double std_dev_module_degree=stddev.getResult();
		
		nodeiterator = nodelist.getStartnode();
		while(nodeiterator!=null) {
			Node node=nodeiterator.getNode();
			double z=0;
			if(std_dev_module_degree!=0.0){
				z=(((double)module_degree_map.get(node.getId()))-avg_module_degree)/std_dev_module_degree;
			}
			withinModuleDegree.put(node.getId(), z);
			nodeiterator=nodeiterator.getNextNode();
		}
		
		//
		dbConnector.propertyImport(participationCoeffitient, "p");
		dbConnector.propertyImport(withinModuleDegree, "z");
		participationCoeffitient.clear();
		withinModuleDegree.clear();
	}
	
	
	

}
