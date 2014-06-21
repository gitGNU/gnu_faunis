package specialObjectInputStreams.randomObjectInputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.Random;


class MyInputStream extends InputStream {
	private final String messageStreamClosed;
	private Random random;
	private Object[] objects;
	private byte[] bytes;
	private int counter;
	private ByteArrayOutputStream bos;
	private ObjectOutputStream oos;
	private boolean closed;
	
	public MyInputStream(Object[] objects) throws IOException {
		this.messageStreamClosed = "Stream Closed";
		this.bytes = new byte[0];
		this.counter = 0;
		this.random = new Random();
		this.objects = objects;
		this.bos = new ByteArrayOutputStream();
		this.oos = new ObjectOutputStream(bos);
		this.closed = false;
	}
	
	public void fillNextObject() throws IOException {
		Object object = objects[random.nextInt(objects.length)];
		oos.writeObject(object);
		oos.flush();
		bytes = bos.toByteArray();
		oos.reset();
		bos.reset();
		counter = 0;
	}
	
	@Override
	public int available() throws IOException {
		if (closed) {
			throw new IOException(messageStreamClosed);
		} else {
			return bytes.length - counter;
		}
	}
	
	@Override
	public int read() throws IOException {
		if (closed) {
			throw new IOException(messageStreamClosed);
		}
		if (counter >= bytes.length) {
			fillNextObject();
		}
		assert(bytes.length > 0);
		int result = bytes[counter];
		counter++;
		return result;
	}
	
	@Override
	public void close() {
		closed = true;
	}
}

