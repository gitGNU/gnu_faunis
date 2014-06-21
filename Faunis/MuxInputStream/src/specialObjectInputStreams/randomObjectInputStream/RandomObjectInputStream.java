package specialObjectInputStreams.randomObjectInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;


public class RandomObjectInputStream extends ObjectInputStream {
	String representation;

	public RandomObjectInputStream(Object[] objects) throws IOException {
		super(new MyInputStream(objects));
	}
	
	public RandomObjectInputStream(
		Object[] objects, String representation
	) throws IOException {
		this(objects);
		this.representation = representation;
	}

	@Override
	public String toString() {
		if (representation != null)
			return representation;
		else
			return super.toString();
	}
}
