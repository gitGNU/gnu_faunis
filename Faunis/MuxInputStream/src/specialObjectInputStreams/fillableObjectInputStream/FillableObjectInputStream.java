package specialObjectInputStreams.fillableObjectInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;


public class FillableObjectInputStream extends ObjectInputStream {
	public FillableObjectInputStream(Object[] objects) throws IOException {
		super(new MyInputStream(objects));
	}
}
