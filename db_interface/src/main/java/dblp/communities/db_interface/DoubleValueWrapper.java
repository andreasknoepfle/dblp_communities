package dblp.communities.db_interface;

public class DoubleValueWrapper implements Comparable<DoubleValueWrapper>{
	private double value;
	private long nodeid;
	private String name;
	
	public DoubleValueWrapper(double value, long nodeid,String name) {
		super();
		this.value = value;
		this.nodeid = nodeid;
		this.name=name;
	}
	public String getName() {
		return name;
	}
	public double getValue() {
		return value;
	}
	public long getId() {
		return nodeid;
	}
	@Override
	public int compareTo(DoubleValueWrapper o) {
		
		return (-1)*Double.compare(value, o.getValue());
	}
}
