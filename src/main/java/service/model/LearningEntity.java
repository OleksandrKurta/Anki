package service.model;

public abstract class LearningEntity {

    abstract <T> LearnItem<T> getHint();
    abstract <T> LearnItem<T> getAnswer();
    abstract String getRateId();

}


