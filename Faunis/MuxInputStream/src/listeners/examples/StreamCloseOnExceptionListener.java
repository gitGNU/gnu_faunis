package listeners.examples;

import java.io.ObjectInputStream;
import listeners.StreamExceptionListener;
import mux.MuxObjectInputStream;


/**
 * Listener which closes a given queue if it throws an exception of given class
 * while reading from it.
 * Pass null to the constructor if this should happen for any
 * queue / exception class.
 */
public class StreamCloseOnExceptionListener implements StreamExceptionListener {
	private ObjectInputStream onStream;
	@SuppressWarnings("rawtypes")
	private Class onExceptionClass;
	private boolean removeToo;

	
	@SuppressWarnings("rawtypes")
	public StreamCloseOnExceptionListener(
			ObjectInputStream onStream, Class onExceptionClass, boolean removeToo
	) {
		this.onStream = onStream;
		this.onExceptionClass = onExceptionClass;
		this.removeToo = removeToo;
	}
	
	
	@Override
	public void onEvent(
			MuxObjectInputStream mux, ObjectInputStream stream,
			Exception exception
	) {
		if (
			(onStream == null || onStream == stream) &&
			(onExceptionClass == null || onExceptionClass.isInstance(exception))
		) {
			mux.closeStream(stream, removeToo);
		}
	}

}
