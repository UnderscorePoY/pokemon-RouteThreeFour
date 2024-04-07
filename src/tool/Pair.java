package tool;

public class Pair<T, R> {

	  private T left;
	  private R right;

	  public Pair(T left, R right) {
	    assert left != null;
	    assert right != null;

	    this.left = left;
	    this.right = right;
	  }

	  public T getLeft() { return left; }
	  public R getRight() { return right; }
	  
	  public void setLeft(T left) { this.left = left; }
	  public void setRight(R right) { this.right = right; }

	  @Override
	  public int hashCode() {
		  long l = left.hashCode() * 2654435761L;
		  return (int)l + (int)(l >>> 32) + right.hashCode();
	  }

	  @Override
	  public boolean equals(Object o) {
	    if (!(o instanceof Pair)) return false;
	    @SuppressWarnings("unchecked")
		Pair<T, R> pairo = (Pair<T, R>) o;
	    return this.left.equals(pairo.getLeft()) &&
	           this.right.equals(pairo.getRight());
	  }
	  
	  @Override
	  public String toString() {
		  return String.format("(%s-%s)", left, right);
	  }
	}
