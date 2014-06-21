package listeners;

import java.util.concurrent.BlockingQueue;
import mux.MuxObjectInputStream;


public interface QueueExceptionListener extends MuxListener {
	void onEvent(
		MuxObjectInputStream mux, BlockingQueue<?> queue, Exception exception
	);
}
