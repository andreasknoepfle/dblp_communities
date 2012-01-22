package dblp.communities.db_interface;

public class NamePair implements Comparable<NamePair> {
	private String name;
	private long id;
	public NamePair(String name, long id) {
		this.name = name;
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public long getId() {
		return id;
	}
	@Override
	public int compareTo(NamePair o) {
		return this.getName().compareTo(o.getName());
	}
}
