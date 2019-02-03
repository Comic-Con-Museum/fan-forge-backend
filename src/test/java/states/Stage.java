package states;

public abstract class Stage<T> {
    @SuppressWarnings("unchecked")
    public T and() { return (T) this; }
    
    /**
     * Resets the stage for another test
     */
    public abstract void reset();
}
