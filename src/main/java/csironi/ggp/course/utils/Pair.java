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
		if (this == otherPair)
			return true;
		if (otherPair == null)
			return false;
		if (getClass() != otherPair.getClass())
			return false;
		Pair<?, ?> other = (Pair<?, ?>) otherPair;
		if (this.t == null) {
			if (other.t != null)
				return false;
		} else if (!this.t.equals(other.t))
			return false;
		if (this.v == null) {
			if (other.v != null)
				return false;
		} else if (!this.v.equals(other.v))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.t == null) ? 0 : this.t.hashCode());
		result = prime * result + ((this.v == null) ? 0 : this.v.hashCode());
		return result;
	}

}
