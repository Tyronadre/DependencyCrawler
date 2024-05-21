package data;

public interface Bom16Component<T> {
    T toBom16();

    default String getBomRef() {
        throw new UnsupportedOperationException("Not implemented");
    }
}
