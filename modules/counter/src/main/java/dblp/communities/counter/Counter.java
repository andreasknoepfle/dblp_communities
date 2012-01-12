package dblp.communities.counter;

import java.util.HashMap;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import dblp.communities.db_interface.AuthorGraphRelationshipType;
import dblp.communities.db_interface.DBConnector;

public class Counter {

	final static String count_str = "count";
	private static DBConnector dbconnector;
	private static HashMap<Long, Object> counts;

	public static void main(String[] args) {

		try {
			if (args.length == 2) {

				dbconnector = DBConnector.getInstance(args[0]);
				counts = new HashMap<Long, Object>();
				count(dbconnector, dbconnector.getGraphDb().getNodeById(
						Long.valueOf(args[1])));

				dbconnector.propertyImport(counts, "count");
				System.out.println("done");

			} else {
				
				System.out.println("Usage: Counter <db_path> <root_id>");
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Usage: Counter <db_path> <root_id>");
		}

	}

	private static long count(DBConnector connector, Node community) {

		Iterable<Relationship> rel = community.getRelationships(
				AuthorGraphRelationshipType.BELONGS_TO, Direction.INCOMING);

		if (rel.iterator().hasNext()) {

			long counter_community = 0;

			while (rel.iterator().hasNext()) {

				Relationship element = rel.iterator().next();

				long count = count(connector, element.getStartNode());

				counter_community += count;

			}

			counts.put(community.getId(), counter_community);

			return counter_community;

		}
		// the author node
		else {
			return 1;
		}

	}

}
