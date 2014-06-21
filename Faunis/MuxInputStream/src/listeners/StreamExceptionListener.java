package listeners;

import java.io.ObjectInputStream;
import mux.MuxObjectInputStream;


public interface StreamExceptionListener extends MuxListener {
	void onEvent(
		MuxObjectInputStream mux, ObjectInputStream stream, Exception exception
	);
}
