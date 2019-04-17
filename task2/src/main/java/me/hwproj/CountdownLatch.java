package me.hwproj;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CountdownLatch {
    private volatile AtomicInteger counter; // volatile is unnecessary because of locks
    private final Lock lock = new ReentrantLock();
    private final Condition positiveCondition;
    private final Condition zeroCondition;

    public CountdownLatch(int counter) {
        this.counter = new AtomicInteger(counter);
        positiveCondition = lock.newCondition();
        zeroCondition = lock.newCondition();
    }

    public void await() throws InterruptedException { // Maybe not all threads will be unlocked
        lock.lock();
        try {
            while (counter.get() != 0) {
                zeroCondition.await();
            }
        } finally {
            lock.unlock();
        }
    }

    public void countDown() throws InterruptedException {
        lock.lock();
        try {
            while (counter.get() == 0) {
                positiveCondition.await();
            }
            counter.addAndGet(-1);
            if (counter.get() == 0) {
                zeroCondition.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }

    public void countUp() {
        lock.lock();
        try {
            counter.addAndGet(1);
            if (counter.get() != 0) {
                positiveCondition.signalAll(); // only 1 needed
            }
        } finally {
            lock.unlock();
        }
    }
}
