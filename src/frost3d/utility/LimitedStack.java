package frost3d.utility;

import java.util.Stack;

public class LimitedStack<T> {
	
	Stack<T> data = new Stack<T>();
	
	public LimitedStack<T> push(T item) {
		data.push(item);
		return this;
	}
	
	public T pop() {
		return data.pop();
	}
	
	public T peek() {
		return data.peek();
	}

}
