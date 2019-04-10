package me.hwproj;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LazyFactoryTest {

    Supplier<Integer> supInt;

    @BeforeEach
    void initialize() {
        supInt = new Supplier<>() {

            volatile private int calls = 0;

            @Override
            public Integer get() {
                calls++;
                return calls;
            }
        };
    }

    @Test
    void singleThreadLazy_FirstGetCall_Compute() {
        var lazy = LazyFactory.createSimpleSingleThreadLazy(supInt);

        assertEquals(Integer.valueOf(1), lazy.get());
    }

    @Test
    void singleThreadLazy_SecondGetCall_NoComputation() {
        var lazy = LazyFactory.createSimpleSingleThreadLazy(supInt);

        lazy.get();
        assertEquals(Integer.valueOf(1), lazy.get());
    }

    @Test
    void simpleMultiThreadLazy_SeveralGetCalls_NoComputation() {
        var lazy = LazyFactory.createSimpleMultiThreadLazy(supInt);

        ArrayList<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            threads.add(new Thread(new Runnable() {
                @Override
                public void run() {
                    lazy.get();
                }
            }));
        }

        for (int i = 0; i < 100; i++) {
            threads.get(i).start();
        }

        assertEquals(Integer.valueOf(1), lazy.get());
    }

    @Test
    void complexMultiThreadLazy_SeveralGetCalls_NoComputation() {
        var lazy = LazyFactory.createComplexMultiThreadLazy(supInt);

        ArrayList<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            threads.add(new Thread(new Runnable() {
                @Override
                public void run() {
                    lazy.get();
                }
            }));
        }

        for (int i = 0; i < 100; i++) {
            threads.get(i).start();
        }

        assertEquals(Integer.valueOf(1), lazy.get());
    }

}