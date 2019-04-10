package me.hwproj;

import org.jetbrains.annotations.NotNull;

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
            T value;
            boolean isSetted;

            @Override
            public T get() {
                T answer = null;
                synchronized (value) {
                    if (!isSetted) {
                        value = supplier.get();
                        isSetted = true;
                    }
                    answer = value;
                }
                return answer;
            }
        };
    }

    public static <T> Lazy<T> createComplexMultiThreadLazy(@NotNull Supplier<T> supplier) {
        return new Lazy<T>() {
            T value;
            boolean isSetted;

            @Override
            public T get() {
                T answer = null;
                synchronized (value) {
                    if (!isSetted) {
                        value = supplier.get();
                        isSetted = true;
                    }
                    answer = value;
                }
                return answer;
            }
        };
    }
}
