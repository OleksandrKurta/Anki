package service.model;

public class LearnItem<T> {
    private T item;
    public LearnItem(T item) {this.item = item;};
    public T getItem() {return this.item;}
    public void setItem(T item) {this.item = item;}
}
