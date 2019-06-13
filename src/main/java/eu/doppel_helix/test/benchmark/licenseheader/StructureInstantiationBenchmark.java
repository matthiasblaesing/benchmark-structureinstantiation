package eu.doppel_helix.test.benchmark.licenseheader;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import org.openjdk.jmh.annotations.Benchmark;

public class StructureInstantiationBenchmark {

    @Benchmark
    public DummyWithPointerConstructor testExceptionPointerConstructor() throws IOException, URISyntaxException {
        return newInstanceException(DummyWithPointerConstructor.class, DummyWithPointerConstructor.dummyPointer);
    }

    @Benchmark
    public DummyWithPointerConstructor testIterationPointerConstructor() throws IOException, URISyntaxException {
        return newInstanceIterate(DummyWithPointerConstructor.class, DummyWithPointerConstructor.dummyPointer);
    }

    @Benchmark
    public DummyWithoutPointerConstructor testExceptionWithoutPointerConstructor() throws IOException, URISyntaxException {
        return newInstanceException(DummyWithoutPointerConstructor.class, DummyWithoutPointerConstructor.dummyPointer);
    }

    @Benchmark
    public DummyWithoutPointerConstructor testIterationWithoutPointerConstructor() throws IOException, URISyntaxException {
        return newInstanceException(DummyWithoutPointerConstructor.class, DummyWithoutPointerConstructor.dummyPointer);
    }

    public static <T extends PublicUseMemoryStructure> T newInstanceException(Class<T> type, Pointer init) throws IllegalArgumentException {
        try {
            Constructor<T> ctor = type.getConstructor(Pointer.class);
            return ctor.newInstance(init);
        }
        catch(NoSuchMethodException e) {
            // Not defined, fall back to the default
        } catch (SecurityException e) {
            // Might as well try the fallback
        } catch (InstantiationException e) {
            String msg = "Can't instantiate " + type;
            throw new IllegalArgumentException(msg, e);
        } catch (IllegalAccessException e) {
            String msg = "Instantiation of " + type + " (Pointer) not allowed, is it public?";
            throw new IllegalArgumentException(msg, e);
        } catch (InvocationTargetException e) {
            String msg = "Exception thrown while instantiating an instance of " + type;
            throw new IllegalArgumentException(msg, e);
        }
        try {
            T s = type.getConstructor().newInstance();
            s.useMemory(init);
            return s;
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | SecurityException | NoSuchMethodException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    public static <T extends PublicUseMemoryStructure> T newInstanceIterate(Class<T> type, Pointer init) throws IllegalArgumentException {
        try {
            for (Constructor<T> constructor : (Constructor<T>[])type.getConstructors()) {
                Class[] parameterTypes = constructor.getParameterTypes();
                if (parameterTypes.length == 1 && parameterTypes[0].equals(Pointer.class)) {
                    return constructor.newInstance(init);
                }
            }
        }
        catch(SecurityException e) {
            // Might as well try the fallback
        }
        catch(InstantiationException e) {
            String msg = "Can't instantiate " + type;
            throw new IllegalArgumentException(msg, e);
        }
        catch(IllegalAccessException e) {
            String msg = "Instantiation of " + type + " (Pointer) not allowed, is it public?";
            throw new IllegalArgumentException(msg, e);
        }
        catch(InvocationTargetException e) {
            String msg = "Exception thrown while instantiating an instance of " + type;
            throw new IllegalArgumentException(msg, e);
        }
        try {
            T s = type.getConstructor().newInstance();
            s.useMemory(init);
            return s;
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | SecurityException | NoSuchMethodException ex) {
           throw new IllegalArgumentException(ex);
        }
    }

    @Structure.FieldOrder("dummyValue")
    public static class DummyWithoutPointerConstructor extends PublicUseMemoryStructure {
        public static final int SIZE = new DummyWithPointerConstructor().size();
        public static final Memory dummyPointer = new Memory(SIZE);

        public int dummyValue;
    }

    @Structure.FieldOrder("dummyValue")
    public static class DummyWithPointerConstructor extends PublicUseMemoryStructure {
        public static final int SIZE = new DummyWithPointerConstructor().size();
        public static final Memory dummyPointer = new Memory(SIZE);

        public DummyWithPointerConstructor() {
        }

        public DummyWithPointerConstructor(Pointer p) {
            super(p);
        }

        public int dummyValue;
    }

    public static class PublicUseMemoryStructure extends Structure {

        public PublicUseMemoryStructure() {
        }

        public PublicUseMemoryStructure(Pointer p) {
            super(p);
        }

        public void useMemory(Pointer init) {
            super.useMemory(init);
        }
    }
}
