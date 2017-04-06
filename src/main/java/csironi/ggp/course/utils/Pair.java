package csironi.ggp.course.utils;

public class Pair<T, V> {

	private T t;

	private V v;

	public Pair(T t, V v) {
		this.t = t;
		this.v = v;
	}

	public T getFirst(){
		return this.t;
	}

	public void setFirst(T t){
		this.t=t;
	}

	public void setSecond(V v){
		this.v=v;
	}

	public V getSecond(){
		return this.v;
	}

	@Override
	public String toString(){
		return "<"+t+","+v+">";
	}

	@Override
	public boolean equals(Object otherPair){
		if (otherPair == null) return false;
	    if (otherPair == this) return true;
	    if (!(otherPair instanceof Pair)) return false;
	    Pair<?,?> pair = (Pair<?,?>) otherPair;
	    return this.t.equals(pair.getFirst()) && this.v.equals(pair.getSecond());

	}

}
