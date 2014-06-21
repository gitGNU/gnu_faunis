package listeners;

import java.util.concurrent.BlockingQueue;
import mux.MuxObjectInputStream;


public interface QueueCloseListener extends MuxListener {
	void onEvent(MuxObjectInputStream mux, BlockingQueue<?> queue);
}
