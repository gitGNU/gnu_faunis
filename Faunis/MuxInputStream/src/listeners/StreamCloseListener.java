package listeners;

import java.io.ObjectInputStream;
import mux.MuxObjectInputStream;


public interface StreamCloseListener extends MuxListener {
	void onEvent(MuxObjectInputStream mux, ObjectInputStream stream);
}
