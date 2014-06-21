package mux;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.concurrent.BlockingQueue;

import listeners.QueueCloseListener;
import listeners.QueueExceptionListener;
import listeners.StreamCloseListener;
import listeners.StreamExceptionListener;

import org.junit.Test;

import specialBlockingQueues.RandomBlockingQueue;
import specialObjectInputStreams.randomObjectInputStream.RandomObjectInputStream;
import static org.junit.Assert.*;


public class WorkTest {
	
	public ObjectInputStream streamCloseNoticed;
	public ObjectInputStream streamExceptionNoticed;
	public BlockingQueue<?> queueCloseNoticed;
	public BlockingQueue<?> queueExceptionNoticed;
	
	@Test
	public void testCloseStreams() {
		ObjectInputStream[] streams = null;
		Object[] objects1 = new Object[] {"a"};
		Object[] objects2 = new Object[] {"e"};
		Object[] objects3 = new Object[] {"i"};
		Object[] objects4 = new Object[] {"o"};
		Object[] objects5 = new Object[] {"u"};
		try {
			streams = new ObjectInputStream[] {
				new RandomObjectInputStream(objects1, "stream a"),
				new RandomObjectInputStream(objects2, "stream e"),
				new RandomObjectInputStream(objects3, "stream i"),
				new RandomObjectInputStream(objects4, "stream o"),
				new RandomObjectInputStream(objects5, "stream u"),
			};
		} catch (SecurityException e) {
			e.printStackTrace();
			fail();			
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
		
		MuxObjectInputStream mux = new MuxObjectInputStream(
			"mux", 32, false, streams, new Class[] {
				String.class, String.class, String.class,
				String.class, String.class
			}, new BlockingQueue<?>[0], new Class<?>[0]
		);
		mux.addStreamCloseListener(new StreamCloseListener() {
			@Override
			public void onEvent(
					MuxObjectInputStream mux, ObjectInputStream stream
			) {
				streamCloseNoticed = stream;
			}
		});
		mux.addStreamExceptionListener(new StreamExceptionListener() {
			@Override
			public void onEvent(
					MuxObjectInputStream mux, ObjectInputStream stream,
					Exception exception
			) {
				streamExceptionNoticed = stream;
			}
		});
		
		long time = System.nanoTime();
		while(System.nanoTime() - time < 500000000l) {
			try {
				String read = (String) mux.readObject();
				System.out.println(read);
			} catch (IOException e) {
				fail();
			}
		}
		System.out.println("Closing stream 0 and 1");
		mux.closeStream(streams[0]);
		try {
			streams[1].close();
		} catch (IOException e1) {
			e1.printStackTrace();
			fail();
		}
		time = System.nanoTime();
		while(System.nanoTime() - time < 500000000l) {
			try {
				String read = (String) mux.readObject();
				System.out.println(read);
			} catch (IOException e) {
				fail();
			}
		}
		assertEquals(streamCloseNoticed, streams[0]);
		assertEquals(streamExceptionNoticed, streams[1]);
		
		mux.close();
		boolean thrown = false;
		try {
			mux.readObject();
		} catch(IOException e) {
			assertEquals(e.getMessage(), "Stream Closed");
			thrown = true;
		}
		assertTrue(thrown);
	}
	
	
	@Test
	public void testCloseQueues() {
		RandomBlockingQueue[] queues;
		Object[] objects1 = new Object[] {"a"};
		Object[] objects2 = new Object[] {"e"};
		Object[] objects3 = new Object[] {"i"};
		Object[] objects4 = new Object[] {"o"};
		Object[] objects5 = new Object[] {"u"};
		queues = new RandomBlockingQueue[] {
			new RandomBlockingQueue(objects1, "queue a"),
			new RandomBlockingQueue(objects2, "queue e"),
			new RandomBlockingQueue(objects3, "queue i"),
			new RandomBlockingQueue(objects4, "queue o"),
			new RandomBlockingQueue(objects5, "queue u"),
		};
		
		MuxObjectInputStream mux = new MuxObjectInputStream(
			"mux", 32, false, new ObjectInputStream[0], new Class[0],
			queues, new Class[] {
				String.class, String.class, String.class,
				String.class, String.class
			}
		);
		mux.addQueueCloseListener(new QueueCloseListener() {
			@Override
			public void onEvent(
					MuxObjectInputStream mux, BlockingQueue<?> queue
			) {
				queueCloseNoticed = queue;
			}
		});
		mux.addQueueExceptionListener(new QueueExceptionListener() {
			@Override
			public void onEvent(
					MuxObjectInputStream mux, BlockingQueue<?> queue,
					Exception exception
			) {
				queueExceptionNoticed = queue;
			}
		});
		
		long time = System.nanoTime();
		while(System.nanoTime() - time < 500000000l) {
			try {
				String read = (String) mux.readObject();
				System.out.println(read);
			} catch (IOException e) {
				fail();
			}
		}
		System.out.println(
			"Closing queue-thread 0, causing filter exception on queue 1"
		);
		mux.closeQueue(queues[0]);
		queues[1].setObjects(new Integer[] {new Integer(1)});
		time = System.nanoTime();
		while(System.nanoTime() - time < 500000000l) {
			try {
				String read = (String) mux.readObject();
				System.out.println(read);
			} catch (IOException e) {
				fail();
			}
		}
		assertEquals(queueCloseNoticed, queues[0]);
		assertEquals(queueExceptionNoticed, queues[1]);
		
		mux.close();
		boolean thrown = false;
		try {
			mux.readObject();
		} catch(IOException e) {
			assertEquals(e.getMessage(), "Stream Closed");
			thrown = true;
		}
		assertTrue(thrown);
	}

}
