package mux;

import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.lang.UnsupportedOperationException;

import listeners.QueueCloseListener;
import listeners.QueueExceptionListener;
import listeners.StreamCloseListener;
import listeners.StreamExceptionListener;


/** MuxObjectInputStream itself is not thread-safe */
public class MuxObjectInputStream implements ObjectInput {
	/** name is used for String representation and to name the Mux threads */

	protected final String name;
	protected final String streamClosedMessage = "Stream Closed";
	protected final CopyOnWriteArrayList<ObjectInputStreamRunnable> streamRunnables;
	protected final CopyOnWriteArrayList<BlockingQueueRunnable> queueRunnables;
	protected final BlockingQueue<Object> input;
	protected final AtomicBoolean isClosing;
	protected boolean closed;
	protected final CopyOnWriteArrayList<QueueCloseListener> queueCloseListeners;
	protected final CopyOnWriteArrayList<QueueExceptionListener> queueExceptionListeners;
	protected final CopyOnWriteArrayList<StreamCloseListener> streamCloseListeners;
	protected final CopyOnWriteArrayList<StreamExceptionListener> streamExceptionListeners;
	protected final CopyOnWriteArrayList<Thread> readingThreads;
	/** if removeOnClose is set, any stream / queue is removed when it is closed */
	protected final boolean removeOnClose;
	
	
	@SuppressWarnings("rawtypes")
	public MuxObjectInputStream(
			String name, int capacity, boolean removeOnClose,
			ObjectInputStream[] objectInputStreams, Class[] streamFilters,
			BlockingQueue[] blockingQueues, Class[] queueFilters
	) {
		if (objectInputStreams.length != streamFilters.length) {
			throw new IllegalArgumentException(
				"You must pass as many stream filters as you pass streams!"
			);
		}
		if(blockingQueues.length != queueFilters.length) {
			throw new IllegalArgumentException(
				"You must pass as many queue filters as you pass queues!"
			);
		}
		this.name = name;
		this.removeOnClose = removeOnClose;
		this.isClosing = new AtomicBoolean(false);
		this.closed = false;
		this.queueCloseListeners = new CopyOnWriteArrayList<QueueCloseListener>();
		this.queueExceptionListeners = new CopyOnWriteArrayList<QueueExceptionListener>();
		this.streamCloseListeners = new CopyOnWriteArrayList<StreamCloseListener>();
		this.streamExceptionListeners = new CopyOnWriteArrayList<StreamExceptionListener>();
		this.readingThreads = new CopyOnWriteArrayList<Thread>();
		this.input = new LinkedBlockingQueue<Object>(capacity);
		ArrayList<Thread> streamThreads = new ArrayList<Thread>();
		this.streamRunnables = new CopyOnWriteArrayList<ObjectInputStreamRunnable>();
		ArrayList<Thread> queueThreads = new ArrayList<Thread>();
		this.queueRunnables = new CopyOnWriteArrayList<BlockingQueueRunnable>();
		for (int i = 0; i < objectInputStreams.length; i++) {
			Thread thread = addStreamWithoutStartingIt(objectInputStreams[i], streamFilters[i]);
			streamThreads.add(thread);
		}
		for (int i = 0; i < blockingQueues.length; i++) {
			Thread thread = addQueueWithoutStartingIt(blockingQueues[i], queueFilters[i]);
			queueThreads.add(thread);
		}
		for (Thread thread : streamThreads) {
			thread.start();
		}
		for (Thread thread : queueThreads) {
			thread.start();
		}
	}
	@SuppressWarnings("rawtypes")
	public MuxObjectInputStream(
			String name, int capacity, boolean removeOnClose,
			ObjectInputStream[] objectInputStreams, Class[] streamFilters,
			BlockingQueue[] blockingQueues, Class[] queueFilters,
			QueueCloseListener[] queueCloseListeners,
			QueueExceptionListener[] queueExceptionListeners,
			StreamCloseListener[] streamCloseListeners,
			StreamExceptionListener[] streamExceptionListeners
	) {
		this(
			name, capacity, removeOnClose, objectInputStreams,
			streamFilters, blockingQueues, queueFilters
		);
		for (StreamCloseListener streamCloseListener : streamCloseListeners) {
			this.streamCloseListeners.add(streamCloseListener);
		}
		for (StreamExceptionListener streamExceptionListener : streamExceptionListeners) {
			this.streamExceptionListeners.add(streamExceptionListener);
		}
		for (QueueCloseListener queueCloseListener : queueCloseListeners) {
			this.queueCloseListeners.add(queueCloseListener);
		}
		for (QueueExceptionListener queueExceptionListener : queueExceptionListeners) {
			this.queueExceptionListeners.add(queueExceptionListener);
		}
	}

	
	public int getNumberOfStreams() {
		return this.streamRunnables.size();
	}
	
	
	public int getNumberOfQueues() {
		return this.queueRunnables.size();
	}
	
	
	@SuppressWarnings("rawtypes")
	public void addStream(ObjectInputStream stream, Class filter) {
		addStreamWithoutStartingIt(stream, filter).start();
	}
	
	
	@SuppressWarnings("rawtypes")
	private Thread addStreamWithoutStartingIt(ObjectInputStream stream, Class filter) {
		ObjectInputStreamRunnable streamRunnable = new ObjectInputStreamRunnable(stream, filter);
		this.streamRunnables.add(streamRunnable);
		Thread thread = new Thread(streamRunnable, "Mux[name="+name+"]: Thread for stream "+stream);
		streamRunnable.setThread(thread);
		return thread;
	}
	
	
	@SuppressWarnings("rawtypes")
	public void addQueue(BlockingQueue queue, Class filter) {
		addQueueWithoutStartingIt(queue, filter).start();
	}
	@SuppressWarnings("rawtypes")
	private Thread addQueueWithoutStartingIt(BlockingQueue queue, Class filter) {
		BlockingQueueRunnable queueRunnable = new BlockingQueueRunnable(queue, filter);
		this.queueRunnables.add(queueRunnable);
		Thread thread = new Thread(queueRunnable, "Mux[name="+name+"]: Thread for queue "+queue);
		queueRunnable.setThread(thread);
		return thread;
	}
	
	
	public void removeClosedStream(ObjectInputStream stream) {
		ObjectInputStreamRunnable streamRunnable = getRunnableOfStream(stream);
		if (streamRunnable == null) {
			throw new IllegalArgumentException("Could not find given stream!");
		}
		
		if (!streamRunnable.isClosed()) {
			throw new RuntimeException("You must close the stream first before you remove it!");
		}
		Thread thread = streamRunnable.getThread();
		if (Thread.currentThread() != thread) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		streamRunnables.remove(streamRunnable);
	}

	
	@SuppressWarnings("rawtypes")
	public void removeClosedQueue(BlockingQueue queue) {
		BlockingQueueRunnable queueRunnable = getRunnableOfQueue(queue);
		if (queueRunnable == null) {
			throw new IllegalArgumentException("Could not find given queue!");
		}
		
		if (!queueRunnable.isClosed()) {
			throw new RuntimeException("You must close the queue first before you remove it!");
		}
		Thread thread = queueRunnable.getThread();
		if (Thread.currentThread() != thread) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				//TODO: find all catch(InterruptedException) and
				//check whether thread should close before printing
				//stack trace
				e.printStackTrace();
			}
		}
		queueRunnables.remove(queueRunnable);
	}
	
	
	public ObjectInputStreamRunnable getRunnableOfStream(ObjectInputStream stream) {
		for (ObjectInputStreamRunnable streamRunnable : streamRunnables) {
			if (streamRunnable.getStream() == stream) {
				return streamRunnable;
			}
		}
		return null;
	}
	
	
	@SuppressWarnings("rawtypes")
	public BlockingQueueRunnable getRunnableOfQueue(BlockingQueue queue) {
		for (BlockingQueueRunnable queueRunnable : queueRunnables) {
			if (queueRunnable.getQueue() == queue) {
				return queueRunnable;
			}
		}
		return null;
	}
	
	
	public void addStreamCloseListener(StreamCloseListener listener) {
		streamCloseListeners.add(listener);
	}
	public void addStreamExceptionListener(StreamExceptionListener listener) {
		streamExceptionListeners.add(listener);
	}
	public void addQueueCloseListener(QueueCloseListener listener) {
		queueCloseListeners.add(listener);
	}
	public void addQueueExceptionListener(QueueExceptionListener listener) {
		queueExceptionListeners.add(listener);
	}
	
	
	public boolean isClosed() {
		return closed;
	}

	
	@Override
	public Object readObject() throws IOException {
		Thread currentThread = Thread.currentThread();
		readingThreads.add(currentThread);
		try {
			if (closed) {
				throw new IOException(streamClosedMessage);
			}
			try {
				Object result = input.take();
				return result;
			} catch (InterruptedException e) {
				if (isClosing.get()) {
					throw new IOException(streamClosedMessage);
				} else {
					throw new InterruptedIOException(e.getMessage());
				}
			}
		} finally {
			readingThreads.remove(currentThread);
		}
	}
	
	
	
	@Override
	public void close() {
		// Note: close() must work no matter if it is called by
		// one of this.threads, and closing may not trigger
		// an endless recursion -> assert that only one thread
		// can close
		boolean success = isClosing.compareAndSet(false, true);
		if (success) {
			closed = true;
			for (Thread thread : readingThreads) {
				thread.interrupt();
			}
			for (ObjectInputStreamRunnable streamRunnable : streamRunnables) {
				streamRunnable.close(removeOnClose);
			}
			for (BlockingQueueRunnable queueRunnable : queueRunnables) {
				queueRunnable.close(removeOnClose);
			}
		}
	}
	
	public void closeStream(ObjectInputStream stream) {
		getRunnableOfStream(stream).close(removeOnClose);
	}
	public void closeStream(ObjectInputStream stream, boolean removeToo) {
		getRunnableOfStream(stream).close(removeToo);
	}
	@SuppressWarnings("rawtypes")
	public void closeQueue(BlockingQueue queue) {
		getRunnableOfQueue(queue).close(removeOnClose);
	}
	@SuppressWarnings("rawtypes")
	public void closeQueue(BlockingQueue queue, boolean removeToo) {
		getRunnableOfQueue(queue).close(removeToo);
	}
	
	public ObjectInputStreamRunnable getStreamRunnable(int index) {
		return streamRunnables.get(index);
	}
	
	public BlockingQueueRunnable getQueueRunnable(int index) {
		return queueRunnables.get(index);
	}
	
	
	protected boolean messageMeansStreamClosed(String errorMessage) {
		if (
			errorMessage.toLowerCase().startsWith("stream closed")
			|| errorMessage.toLowerCase().startsWith("socket closed")
		) {
			return true;
		} else {
			return false;
		}
	}
	
	
	@Override
	public String toString() {
		return "Mux[name="+name+"]";
	}
	
	
	public class FilterException extends Exception {
		private static final long serialVersionUID = 1L;
		private Object read;
		
		public FilterException(Object read) {
			this.read = read;
		}
		
		public Object hasRead() {
			return read;
		}
	}
	
	
	@SuppressWarnings("rawtypes")
	public class BlockingQueueRunnable implements Runnable {
		private Thread thread;
		private BlockingQueue queue;
		private Class _class;
		private AtomicBoolean queueIsClosing;
		private boolean queueClosed;
		
		public BlockingQueueRunnable(
				BlockingQueue inputQueue, Class _class
		) {
			this.queue = inputQueue;
			this._class = _class;
			this.queueIsClosing = new AtomicBoolean(false);
			this.queueClosed = false;
		}

		/** Call this after construction and before starting the thread */
		protected void setThread(Thread thread) {
			this.thread = thread;
		}
		
		protected Thread getThread() {
			return thread;
		}
		
		@Override
		public void run() {
			while(!queueIsClosing.get()) {
				Object hasRead = null;
				try {
					hasRead = queue.take();
				} catch(InterruptedException e) {
					if (queueIsClosing.get()) {
						break;
					} else {
						e.printStackTrace();
					}
				}
				if (hasRead != null) {
					if (_class.isInstance(hasRead)) {
						try {
							MuxObjectInputStream.this.input.put(hasRead);
						} catch (InterruptedException e) {
							if (queueIsClosing.get()) {
								break;
							} else {
								e.printStackTrace();
							}
						}
					} else {
						for (QueueExceptionListener listener : queueExceptionListeners) {
							listener.onEvent(
								MuxObjectInputStream.this, queue, new FilterException(hasRead)
							);
						}
					}
				}
			}
			for (QueueCloseListener listener : queueCloseListeners) {
				listener.onEvent(MuxObjectInputStream.this, queue);
			}
		}
		
		public BlockingQueue getQueue() {
			return queue;
		}
		
		public void close() {
			close(removeOnClose);
		}
		public void close(boolean removeToo) {
			boolean success = queueIsClosing.compareAndSet(false, true);
			if (success) {
				if (Thread.currentThread() != thread) {
					thread.interrupt();
					try {
						thread.join();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				queueClosed = true;
				if (removeToo) {
					removeClosedQueue(queue);
				}
			}
		}
		
		public boolean isClosed() {
			return queueClosed;
		}
	}
	
	
	@SuppressWarnings("rawtypes")
	public class ObjectInputStreamRunnable implements Runnable {
		private Thread thread;
		private ObjectInputStream stream;
		private Class _class;
		private AtomicBoolean streamIsClosing;
		private boolean streamClosed;
		
		public ObjectInputStreamRunnable(
				ObjectInputStream inputStream, Class _class
		) {
			this.stream = inputStream;
			this._class = _class;
			this.streamIsClosing = new AtomicBoolean(false);
			this.streamClosed = false;
		}
		
		/** Call this after construction and before starting the thread */
		protected void setThread(Thread thread) {
			this.thread = thread;
		}
		
		protected Thread getThread() {
			return thread;
		}
		
		@Override
		public void run() {
			while(!streamIsClosing.get()) {
				Object hasRead = null;
				try {
					hasRead = stream.readObject();
				} catch (ClassNotFoundException e) {
					for (StreamExceptionListener listener : streamExceptionListeners) {
						listener.onEvent(MuxObjectInputStream.this, stream, e);
					}
				} catch (IOException e) {
					if (
						(
							e.getMessage() != null 
							&& messageMeansStreamClosed(e.getMessage())
							&& streamIsClosing.get()
						) || (
							streamIsClosing.get()
							&& e instanceof EOFException
						)
					) {
						break;
					} else {
						for (StreamExceptionListener listener : streamExceptionListeners) {
							listener.onEvent(MuxObjectInputStream.this, stream, e);
						}
					}
				}
				if (hasRead != null) {
					if (_class.isInstance(hasRead)) {
						try {
							MuxObjectInputStream.this.input.put(hasRead);
						} catch (InterruptedException e) {
							if (streamIsClosing.get()) {
								break;
							} else {
								e.printStackTrace();
							}
						}
					} else {
						for (StreamExceptionListener listener : streamExceptionListeners) {
							listener.onEvent(
								MuxObjectInputStream.this, stream, new FilterException(hasRead)
							);
						}
					}
				}
			}
			for (StreamCloseListener listener : streamCloseListeners) {
				listener.onEvent(MuxObjectInputStream.this, stream);
			}
		}
		
		public ObjectInputStream getStream() {
			return stream;
		}
		
		public void close() {
			close(removeOnClose);
		}
		public void close(boolean removeToo) {
			boolean success = streamIsClosing.compareAndSet(false, true);
			if (success) {
				try {
					stream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (Thread.currentThread() != thread) {
					thread.interrupt();
					try {
						thread.join();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				// We have to assume that the stream is now closed
				streamClosed = true;
				if (removeToo) {
					removeClosedStream(stream);
				}
			}
		}
		
		public boolean isClosed() {
			return streamClosed;
		}
	}
	

	
	// The only thing that remains are unimplemented methods

	@Override
	public boolean readBoolean() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte readByte() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public char readChar() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public double readDouble() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public float readFloat() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void readFully(byte[] arg0) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void readFully(byte[] arg0, int arg1, int arg2) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int readInt() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String readLine() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public long readLong() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public short readShort() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String readUTF() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int readUnsignedByte() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int readUnsignedShort() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int skipBytes(int arg0) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int available() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int read() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int read(byte[] b) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public long skip(long n) throws IOException {
		throw new UnsupportedOperationException();
	}

}
