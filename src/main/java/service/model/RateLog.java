package service.model;

public class RateLog {
        private String id;
        private Rate<RateEnum> learningRate;
        private String learningItemId;
        private String userId;
        private String deckId;

        public RateLog(
                String id,
                Rate<RateEnum> learningRate,
                String deckId,
                String learningItemId,
                String userId
        ) {
            this.id = id;
            this.learningRate = learningRate;
            this.deckId = deckId;
            this.learningItemId = learningItemId;
            this.userId = userId;
        }

        public RateLog(
                Rate<RateEnum> learningRate,
                String deckId,
                String learningItemId,
                String userId
        ) {
            this(null, learningRate, deckId, learningItemId, userId);
        }

    public String getId() {return id;}

    public String getLearningItemId() {return learningItemId;}
    public void setLearningItemId(String learningItemId) {this.learningItemId = learningItemId;}

    public Rate<RateEnum> getLearningRate() {return learningRate;}
    public void setLearningRate(Rate<RateEnum> learningRate) {this.learningRate = learningRate;}

    public String getUserId() {return userId;}
    public void setUserId(String userId) {this.userId = userId;}


    public String getDeckId() {
        return deckId;
    }
}
