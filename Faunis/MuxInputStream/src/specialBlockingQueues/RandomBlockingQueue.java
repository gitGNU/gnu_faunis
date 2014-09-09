package specialBlockingQueues;

import java.util.Collection;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class RandomBlockingQueue implements BlockingQueue<Object> {
	Random random;
	Object[] objects;
	String representation;
	
	public RandomBlockingQueue(Object[] objects) {
		this.random = new Random();
		this.objects = objects;
	}
	
	public RandomBlockingQueue(Object[] objects, String representation) {
		this(objects);
		this.representation = representation;
	}
	
	public void setObjects(Object[] objects) {
		this.objects = objects;
	}
	
	@Override
	public String toString() {
		if (representation != null)
			return representation;
		else
			return super.toString();
	}

	@Override
	public Object remove() {
		throw new NotImplementedException();
	}

	@Override
	public Object poll() {
		throw new NotImplementedException();
	}

	@Override
	public Object element() {
		throw new NotImplementedException();
	}

	@Override
	public Object peek() {
		throw new NotImplementedException();
	}

	@Override
	public int size() {
		throw new NotImplementedException();
	}

	@Override
	public boolean isEmpty() {
		throw new NotImplementedException();
	}

	@Override
	public Iterator<Object> iterator() {
		throw new NotImplementedException();
	}

	@Override
	public Object[] toArray() {
		throw new NotImplementedException();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		throw new NotImplementedException();
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		throw new NotImplementedException();
	}

	@Override
	public boolean addAll(Collection<?> c) {
		throw new NotImplementedException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new NotImplementedException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new NotImplementedException();
	}

	@Override
	public void clear() {
		throw new NotImplementedException();
	}

	@Override
	public boolean add(Object e) {
		throw new NotImplementedException();
	}

	@Override
	public boolean offer(Object e) {
		throw new NotImplementedException();
	}

	@Override
	public void put(Object e) throws InterruptedException {
		throw new NotImplementedException();
	}

	@Override
	public boolean offer(Object e, long timeout, TimeUnit unit)
			throws InterruptedException {
		throw new NotImplementedException();
	}

	@Override
	public Object take() throws InterruptedException {
		return objects[random.nextInt(objects.length)];
	}

	@Override
	public Object poll(long timeout, TimeUnit unit) throws InterruptedException {
		throw new NotImplementedException();
	}

	@Override
	public int remainingCapacity() {
		throw new NotImplementedException();
	}

	@Override
	public boolean remove(Object o) {
		throw new NotImplementedException();
	}

	@Override
	public boolean contains(Object o) {
		throw new NotImplementedException();
	}

	@Override
	public int drainTo(Collection<? super Object> c) {
		throw new NotImplementedException();
	}

	@Override
	public int drainTo(Collection<? super Object> c, int maxElements) {
		throw new NotImplementedException();
	}
}
