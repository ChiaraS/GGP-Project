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

	public V getSecond(){
		return this.v;
	}

	@Override
	public String toString(){
		return "<"+t+","+v+">";
	}

}
