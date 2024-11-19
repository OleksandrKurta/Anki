package service.model;

import java.time.Instant;
import java.util.Objects;

public class CardLearningEntity extends LearningEntity {

    private String id;
    private String deckId;
    private LearnItem hint;
    private LearnItem answer;
    private String lastRateId;
    private Instant lastLearn;


    public static CardLearningEntity buildFromRateLog(
            String id,
            String lastRateId,
            String deckId
    ) {
        return new CardLearningEntity(
                id,
                lastRateId,
                deckId,
                null,
                null,
                null);
    }

    public static CardLearningEntity buildFromDto(

            String deckId,
            Object key,
            Object value
    ) {
        return new CardLearningEntity(
                null,
                null,
                deckId,
                new LearnItem(key),
                new LearnItem(value),
                null);
    }

    public static CardLearningEntity buildFromPatchDto(
            String id,
            String deckId,
            Object key,
            Object value
    ) {
        return new CardLearningEntity(
                id,
                null,
                deckId,
                new LearnItem(key),
                new LearnItem(value),
                null);
    }



    public CardLearningEntity(
            String id,
            String lastRateId,
            String deckId,
            LearnItem hint,
            LearnItem answer,
            Instant lastLearn) {
        this.id = id;
        this.deckId = deckId;
        this.lastRateId = lastRateId;
        this.hint = hint;
        this.answer = answer;
        this.lastLearn = lastLearn;
    }

    @Override
    public LearnItem getHint() {
        return hint;
    }
    public void setHint(LearnItem hint) {
        this.hint = hint;
    }

    @Override
    public LearnItem getAnswer() {
        return answer;
    }
    public void setAnswer(LearnItem answer) {
        this.answer = answer;
    }

    @Override
    public String getRateId() {
        return lastRateId;
    }
    public void setLastRateId(String lastRateId) {
        this.lastRateId = lastRateId;
    }

    public String getId() {
        return id;
    }

    public String getDeckId() {
        return deckId;
    }

    public void setDeckId(String deckId) {
        this.deckId = deckId;
    }

    public Instant getLastLearn() {
        return lastLearn;
    }

    public void setLastLearn(Instant lastLearn) {
        this.lastLearn = lastLearn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CardLearningEntity that)) return false;
        return Objects.equals(id, that.id) && Objects.equals(deckId, that.deckId) && Objects.equals(hint, that.hint) && Objects.equals(answer, that.answer) && Objects.equals(lastRateId, that.lastRateId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, deckId, hint, answer, lastRateId);
    }
}
