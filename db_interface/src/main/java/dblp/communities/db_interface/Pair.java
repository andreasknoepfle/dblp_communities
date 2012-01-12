package dblp.communities.db_interface;


public class Pair {
	private long from;
	private long to;
	
	public Pair(Long from ,Long to) {
		this.from=from;
		this.to=to;
	}
	public long getFrom() {
		return from;
	}
	public long getTo() {
		return to;
	}
	
}
