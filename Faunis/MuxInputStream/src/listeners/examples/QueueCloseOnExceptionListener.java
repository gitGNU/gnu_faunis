package listeners.examples;

import java.util.concurrent.BlockingQueue;

import listeners.QueueExceptionListener;
import mux.MuxObjectInputStream;


/**
 * Listener which closes a given queue if it throws an exception of given class
 * while reading from it.
 * Pass null to the constructor if this should happen for any
 * queue / exception class.
 */
public class QueueCloseOnExceptionListener implements QueueExceptionListener {
	@SuppressWarnings("rawtypes")
	private BlockingQueue onQueue;
	@SuppressWarnings("rawtypes")
	private Class onExceptionClass;
	private boolean removeToo;

	
	@SuppressWarnings("rawtypes")
	public QueueCloseOnExceptionListener(
			BlockingQueue onQueue, Class onExceptionClass, boolean removeToo
	) {
		this.onQueue = onQueue;
		this.onExceptionClass = onExceptionClass;
		this.removeToo = removeToo;
	}
	
	
	@Override
	public void onEvent(
			MuxObjectInputStream mux, BlockingQueue<?> queue,
			Exception exception
	) {
		if (
			(onQueue == null || onQueue == queue) &&
			(onExceptionClass == null || onExceptionClass.isInstance(exception))
		) {
			mux.closeQueue(queue, removeToo);
		}
	}

}
