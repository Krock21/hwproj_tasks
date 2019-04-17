package me.hwproj;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class CountdownLatchTest {
    final int COUNTER = 50;
    AtomicInteger awaitsDone;
    AtomicInteger countDownsDone;
    AtomicInteger countUpsDone;
    CountdownLatch latch;
    List<Thread> awaitThreads;
    List<Thread> countDownThreads;
    List<Thread> countUpThreads;

    void InitThreads() {
        Lock lock = new ReentrantLock();
        awaitThreads = new ArrayList<>();
        countDownThreads = new ArrayList<>(COUNTER);
        countUpThreads = new ArrayList<>(COUNTER);
        for (int i = 0; i < COUNTER; i++) {
            awaitThreads.add(new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        latch.await();
                        lock.lock();
                        awaitsDone.addAndGet(1);
                        lock.unlock();
                    } catch (InterruptedException e) {
                        assertEquals(1, 0, "InterruptedException in Thread await");
                    }
                }
            }));
        }

        for (int i = 0; i < COUNTER; i++) {
            countDownThreads.add(new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        latch.countDown();
                        lock.lock();
                        countDownsDone.addAndGet(1);
                        lock.unlock();
                    } catch (InterruptedException e) {
                        assertEquals(1, 0, "InterruptedException in Thread countDown");
                    }
                }
            }));
        }
        for (int i = 0; i < COUNTER; i++) {
            countUpThreads.add(new Thread(new Runnable() {
                @Override
                public void run() {
                    latch.countUp();
                    lock.lock();
                    countUpsDone.addAndGet(1);
                    lock.unlock();

                }
            }));
        }
    }

    @BeforeEach
    void setUp() {
        awaitsDone = new AtomicInteger(0);
        countDownsDone = new AtomicInteger(0);
        countUpsDone = new AtomicInteger(0);
        latch = new CountdownLatch(COUNTER);
        InitThreads();
    }

    @Test
    void everyAwaitShouldPass() throws InterruptedException {
        awaitThreads.stream().limit(COUNTER).forEach(Thread::start);
        countDownThreads.stream().limit(COUNTER).forEach(Thread::start);
        awaitThreads.stream().limit(COUNTER).forEach(new Consumer<Thread>() {
            @Override
            public void accept(Thread thread) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    assertEquals(1, 0, "InterruptedException in everyAwaitShouldPass");
                }
            }
        });
        assertEquals(COUNTER, awaitsDone.get());
    }

    @Test
    void countDownAndUpTest() {
        countDownThreads.stream().limit(COUNTER).forEach(Thread::start);
        countDownThreads.stream().limit(COUNTER).forEach(new Consumer<Thread>() {
            @Override
            public void accept(Thread thread) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    assertEquals(1, 0, "InterruptedException in countDownTest");
                }
            }
        });
        assertEquals(COUNTER, countDownsDone.get());
        InitThreads();
        countDownThreads.stream().limit(COUNTER).forEach(Thread::start);
        countUpThreads.stream().limit(COUNTER).forEach(Thread::start);

        countDownThreads.stream().limit(COUNTER).forEach(new Consumer<Thread>() {
            @Override
            public void accept(Thread thread) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    assertEquals(1, 0, "InterruptedException in countDownTest");
                }
            }
        });
        assertEquals(2 * COUNTER, countDownsDone.get());
    }

    /*
    @Test
    void countUpTest() {
    }
    */
}