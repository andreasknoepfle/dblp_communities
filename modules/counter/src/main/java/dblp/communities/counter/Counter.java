package dblp.communities.counter;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Relationship;

import dblp.communities.db_interface.AuthorGraphRelationshipType;
import dblp.communities.db_interface.DBConnector;

public class Counter {


	
	final static String count_str = "count";
	private static DBConnector dbconnector;
	
	public static void main(String[] args) {
		
		try{
    		if(args.length == 2){

    	        dbconnector = DBConnector.getInstance(args[0]);       
    	
    	        count(dbconnector, Long.valueOf(args[1]));
    	          
    	        System.out.println("done");
        	
        	}
        	else{
        		System.out.println("Usage: Counter <db_path> <root_id>");
        	}

    	}catch (Exception e) {
    		System.out.println("Usage: Counter <db_path> <root_id>");
		}
    	
	}
	
	    
	private static int count(DBConnector connector, long community){
		
		Iterable<Relationship> rel = connector.getGraphDb().getNodeById(community).getRelationships(AuthorGraphRelationshipType.BELONGS_TO,Direction.INCOMING);

		if(rel.iterator().hasNext()){
			
			int counter_community=0;
			
			while (rel.iterator().hasNext() ) {
    		
				Relationship element = rel.iterator().next ();

				int count = count( connector, element.getStartNode().getId());
    		
				counter_community += count;
				
    		}
			
			connector.setAuthorProperty(community, count_str, String.valueOf(counter_community));
	    	
			return counter_community;
	    	
		}
		// the author node
    	else{
    		return 1;
    	}
    		
	}

}
