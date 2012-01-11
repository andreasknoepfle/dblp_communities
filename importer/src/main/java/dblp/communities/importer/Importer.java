package dblp.communities.importer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import dblp.communities.db_interface.AuthorGraphRelationshipType;
import dblp.communities.db_interface.DBBatchInserter;
import dblp.communities.db_interface.DBConnector;

public class Importer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 4) {
			System.err.println("Usage: Inputdir Uniquedir Neo4jPath OldNeo4jPath");
			return;

		}
	
		
		
		File dir = new File(args[0]);
		File unique = new File(args[1]);
		//HashMap<Long, Long> author_to_communities_insertions = new HashMap<Long, Long>();
		String dbPath = args[2];
		DBBatchInserter dbbatch = new DBBatchInserter(dbPath);
		DBConnector olddb=DBConnector.getInstance(args[3]);
		for(Node node:olddb.getGraphDb().getAllNodes()) {
			dbbatch.createNode(node);
			
		}
		for(Node node:olddb.getGraphDb().getAllNodes()) {
			Iterable<Relationship> iterable =node.getRelationships(Direction.OUTGOING);
			for (Relationship relationship : iterable) {
				dbbatch.createRelationship(relationship);
			}
		}
		System.out.println("Old DB Imported!");
		HashMap<Long, Long> realnodes = new HashMap<Long, Long>();
		for (String file : unique.list()) {
			try {
				HashMap<Long, Long> communityLevel = new HashMap<Long, Long>();
				BufferedReader buf = new BufferedReader(new FileReader(
						new File(unique, file)));
				
				String line;
				long num = 0l;
				while ((line = buf.readLine()) != null) {
					realnodes.put(num, Long.valueOf(line));
					num++;
				}
				File name = new File(file);
				buf = new BufferedReader(new FileReader(new File(dir, name
						.getName().substring(0,
								name.getName().length() - ".unique".length())
						+ ".tree")));
				HashMap<Long, Long> insertions = new HashMap<Long, Long>();

				boolean authors = true;
				num = 0l;
				while ((line = buf.readLine()) != null) {
					String[] columns = line.split(" ");
					long from = Long.valueOf(columns[0]);
					long to = Long.valueOf(columns[1]);
					if (num != from) {
						// Write everything on level so last level wont be
						// written!
						num = 0l;

						Long real_to;
						for (Long insert_from : insertions.keySet()) {
							Long insert_to = insertions.get(insert_from);
							if ((real_to = communityLevel.get(insert_to)) == null) {
								real_to = dbbatch.createCommunity();
								communityLevel.put(insert_to, real_to);
							}
							if (authors) {
								Node node_from = olddb.getGraphDb().getNodeById(from);
								dbbatch.belongsTo(node_from.getId(), real_to);
							} else {
								// communities can be inserted now
								dbbatch.belongsTo(realnodes.get(insert_from),
										real_to);
							}

						}
						insertions.clear();
						realnodes = communityLevel;
						communityLevel = new HashMap<Long, Long>();
					}

					insertions.put(from, to);
					num++;
				}
				// Hang in top level communities
				String[] years = name.getName().substring(0,
						name.getName().length() - ".unique".length())
						.split("-");
				Long year = dbbatch.createYear(Integer.valueOf(years[0]),
						Integer.valueOf(years[1]));
				for (Long realnode : realnodes.values()) {
					dbbatch.belongsTo(realnode, year);
				}
				

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			}

		}
		
		/*DBConnector connector = DBConnector.getInstance(dbPath);
		GraphDatabaseService graphdb = connector.getGraphDb();
		Transaction tx = graphdb.beginTx();
		System.out.println("Batch Import finished.");
		System.out.println("Cleaning up and Linking Database.");
		try {
			for (Long from : author_to_communities_insertions.keySet()) {
				Long author_to = author_to_communities_insertions.get(from);
				Node node_from = graphdb.getNodeById(from);
				Node node_to = graphdb.getNodeById(author_to);

				node_from.createRelationshipTo(node_to,
						AuthorGraphRelationshipType.BELONGS_TO);
			}
			tx.success();

		} finally {
			tx.finish();
		}
		connector.getGraphDb().shutdown();*/
	}

}
