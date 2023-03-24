package com.andedit.viewmc.util;

import java.util.NoSuchElementException;

/** A resizable, ordered array of longs with efficient add and remove at the beginning and end. Values in the backing array may
 * wrap back to the beginning, making add and remove at the beginning and end O(1) (unless the backing array needs to resize when
 * adding). Deque functionality is provided via {@link #removeLast()} and {@link #addFirst(long)}. */
public class IntQueue {
	/** Contains the values in the queue. Head and tail indices go in a circle around this array, wrapping at the end. */
	protected int[] values;

	/** Index of first element. Logically smaller than tail. Unless empty, it points to a valid element inside queue. */
	protected int head = 0;

	/** Index of last element. Logically bigger than head. Usually points to an empty position, but points to the head when full
	 * (size == values.length). */
	protected int tail = 0;

	/** Number of elements in the queue. */
	public int size = 0;

	/** Creates a new LongQueue which can hold 16 values without needing to resize backing array. */
	public IntQueue () {
		this(16);
	}

	/** Creates a new LongQueue which can hold the specified number of values without needing to resize backing array. */
	public IntQueue (int initialSize) {
		// noinspection unchecked
		this.values = new int[initialSize];
	}

	/** Append given value to the tail. (enqueue to tail) Unless backing array needs resizing, operates in O(1) time. */
	public void addLast (int value) {
		int[] values = this.values;

		if (size == values.length) {
			resize(values.length << 1);// * 2
			values = this.values;
		}

		values[tail++] = value;
		if (tail == values.length) {
			tail = 0;
		}
		size++;
	}

	/** Prepend given value to the head. (enqueue to head) Unless backing array needs resizing, operates in O(1) time.
	 * @see #addLast(long) */
	public void addFirst (int value) {
		int[] values = this.values;

		if (size == values.length) {
			resize(values.length << 1);// * 2
			values = this.values;
		}

		int head = this.head;
		head--;
		if (head == -1) {
			head = values.length - 1;
		}
		values[head] = value;

		this.head = head;
		this.size++;
	}

	/** Increases the size of the backing array to accommodate the specified number of additional items. Useful before adding many
	 * items to avoid multiple backing array resizes. */
	public void ensureCapacity (int additional) {
		final int needed = size + additional;
		if (values.length < needed) {
			resize(needed);
		}
	}

	/** Resize backing array. newSize must be bigger than current size. */
	protected void resize (int newSize) {
		final int[] values = this.values;
		final int head = this.head;
		final int tail = this.tail;

		final int[] newArray = new int[newSize];
		if (head < tail) {
			// Continuous
			System.arraycopy(values, head, newArray, 0, tail - head);
		} else if (size > 0) {
			// Wrapped
			final int rest = values.length - head;
			System.arraycopy(values, head, newArray, 0, rest);
			System.arraycopy(values, 0, newArray, rest, tail);
		}
		this.values = newArray;
		this.head = 0;
		this.tail = size;
	}

	/** Remove the first item from the queue. (dequeue from head) Always O(1).
	 * @return removed value
	 * @throws NoSuchElementException when queue is empty */
	public int removeFirst () {
		if (size == 0) {
			// Underflow
			throw new NoSuchElementException("Queue is empty.");
		}

		final int[] values = this.values;

		final int result = values[head];
		head++;
		if (head == values.length) {
			head = 0;
		}
		size--;

		return result;
	}

	/** Remove the last item from the queue. (dequeue from tail) Always O(1).
	 * @see #removeFirst()
	 * @return removed
	 * @throws NoSuchElementException when queue is empty */
	public int removeLast () {
		if (size == 0) {
			throw new NoSuchElementException("Queue is empty.");
		}

		final int[] values = this.values;
		int tail = this.tail;
		tail--;
		if (tail == -1) {
			tail = values.length - 1;
		}
		final int result = values[tail];
		this.tail = tail;
		size--;

		return result;
	}

	/** Returns true if the queue has one or more items. */
	public boolean notEmpty () {
		return size > 0;
	}

	/** Returns true if the queue is empty. */
	public boolean isEmpty () {
		return size == 0;
	}

	/** Returns the first (head) item in the queue (without removing it).
	 * @see #addFirst(long)
	 * @see #removeFirst()
	 * @throws NoSuchElementException when queue is empty */
	public int first () {
		if (size == 0) {
			// Underflow
			throw new NoSuchElementException("Queue is empty.");
		}
		return values[head];
	}

	/** Returns the last (tail) item in the queue (without removing it).
	 * @see #addLast(long)
	 * @see #removeLast()
	 * @throws NoSuchElementException when queue is empty */
	public int last () {
		if (size == 0) {
			// Underflow
			throw new NoSuchElementException("Queue is empty.");
		}
		final int[] values = this.values;
		int tail = this.tail;
		tail--;
		if (tail == -1) {
			tail = values.length - 1;
		}
		return values[tail];
	}

	/** Retrieves the value in queue without removing it. Indexing is from the front to back, zero based. Therefore get(0) is the
	 * same as {@link #first()}.
	 * @throws IndexOutOfBoundsException when the index is negative or >= size */
	public int get (int index) {
		if (index < 0) throw new IndexOutOfBoundsException("index can't be < 0: " + index);
		if (index >= size) throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + size);
		final int[] values = this.values;

		int i = head + index;
		if (i >= values.length) {
			i -= values.length;
		}
		return values[i];
	}

	/** Removes all values from this queue. */
	public void clear () {
		if (size == 0) return;

		this.head = 0;
		this.tail = 0;
		this.size = 0;
	}
}
