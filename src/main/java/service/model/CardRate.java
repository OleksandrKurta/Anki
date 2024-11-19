package service.model;

public class CardRate<T> implements Rate<T> {
    private T rate;

    public CardRate(T rate) {
        this.rate = rate;
    }

    @Override
    public T getRate() {
        return this.rate;
    }

    @Override
    public void setRate(T rate) {
        this.rate = rate;
    }
}
