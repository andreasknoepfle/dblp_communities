package dblp.communities.max;

import java.util.Iterator;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import dblp.communities.db_interface.AuthorGraphRelationshipType;
import dblp.communities.db_interface.DBConnector;

public class Maximum {

	final static String count_str = "count";
	private static DBConnector dbconnector;

	public static void main(String[] args) {

		
			if (args.length == 2) {
				dbconnector = DBConnector.getInstance(args[0]);
				
				Iterable<Relationship> rel = dbconnector.getGraphDb().getNodeById(
						Long.valueOf(args[1])).getRelationships(
						AuthorGraphRelationshipType.BELONGS_TO, Direction.INCOMING);
				Iterator<Relationship> iterator=rel.iterator();
				long max=0;
				long nodeid=-1;
				while (iterator.hasNext()) {

						Relationship element = iterator.next();
						Iterable<Relationship> communities = element.getStartNode().getRelationships(
								AuthorGraphRelationshipType.BELONGS_TO, Direction.INCOMING);
						Iterator<Relationship> iterator2=communities.iterator();
						while(iterator2.hasNext()) {
							Node n=iterator2.next().getStartNode();
							Long count=(Long) n.getProperty(count_str);
							if(count>max) {
								max=count;
								nodeid=n.getId();
							}
						}
				}
				System.out.println("Max: "+max + " NodeId: "+nodeid );
				
			} else {
				System.out.println("Usage: Max <db_path> <root_id>");
			}

	}

	

}
