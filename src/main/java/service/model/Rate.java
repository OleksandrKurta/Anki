package service.model;

public interface Rate<T> {
    T getRate();
    void setRate(T rate);
}
