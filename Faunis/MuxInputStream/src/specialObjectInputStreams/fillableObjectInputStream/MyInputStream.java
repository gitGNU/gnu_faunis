package specialObjectInputStreams.fillableObjectInputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;

class MyInputStream extends InputStream {
	private byte[] bytes;
	private int counter = 0;
	
	public MyInputStream(Object[] objects) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		for (Object object : objects) {
			oos.writeObject(object);
		}
		oos.flush();
		this.bytes = bos.toByteArray();
	}
	
	@Override
	public int read() throws IOException {
		if (counter < bytes.length) {
			int result = bytes[counter];
			counter++;
			return result;
		} else {
			System.out.println("Warning: The FillableObjectInputStream has come to an end!");
			Thread.currentThread().suspend();
			throw new IOException();
		}
	}
}

