package frost3d.utility;

import java.util.ArrayList;

public class NavigableLimitedStack<T> {
	
	int idx = -1;
	
	ArrayList<T> data = new ArrayList<T>();
	
	public NavigableLimitedStack<T> push(T item) {
		idx++;
		while (size() > idx) {
			data.removeLast();
		}
		data.add(item);
		return this;
	}
	
	public T pop() {
		idx--;
		return data.remove(idx);
	}
	
	public T peek() {
		return data.get(idx);
	}

	public int size() {
		return data.size();
	}
	
	public T prev() {
		idx--;
		return peek();
	}
	
	public T next() {
		idx++;
		return peek();
	}

	public int index() {
		return idx;
	}

	public T get(int i) {
		return data.get(i);
	}

}
