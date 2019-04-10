package me.hwproj;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class LazyFactory {
    public static <T> Lazy<T> createSimpleSingleThreadLazy(@NotNull Supplier<T> supplier) {
        return new Lazy<T>() {
            T value;
            boolean isSetted;

            @Override
            public T get() {
                if (!isSetted) {
                    value = supplier.get();
                    isSetted = true;
                }
                return value;
            }
        };
    }


    public static <T> Lazy<T> createSimpleMultiThreadLazy(@NotNull Supplier<T> supplier) {
        return new Lazy<T>() {
            volatile T value;
            Boolean isSetted = Boolean.FALSE;

            @Override
            public T get() {
                if (isSetted) {
                    return value;
                }
                synchronized (isSetted) {
                    if (!isSetted) {
                        value = supplier.get();
                        isSetted = Boolean.TRUE;
                    }
                }
                return value;
            }
        };
    }

    public static <T> Lazy<T> createComplexMultiThreadLazy(@NotNull Supplier<T> supplier) {
        return new Lazy<T>() {
            volatile T value;
            AtomicInteger status = new AtomicInteger(0);

            @Override
            public T get() {
                if (status.get() != 1) {
                    T tempValue = supplier.get();
                    if (status.compareAndSet(0, 1)) {
                        value = tempValue;
                    }
                }
                return value;
            }
        };
    }
}
