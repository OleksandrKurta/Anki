package service.learn.comparator;


import org.springframework.stereotype.Component;
import service.model.CardLearningEntity;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;


public class PresetComparator implements Comparator<CardLearningEntity> {
    @Override
    public int compare(CardLearningEntity o1, CardLearningEntity o2) {

        Instant now = Instant.now();
        Instant time1 = o1.getLastLearn();
        Instant time2 = o2.getLastLearn();

        if (time1 == null && time2 == null) return 0;
        if (time1 == null) return -1;
        if (time2 == null) return 1;

        long delta1 = Duration.between(time1, now).toMillis();
        long delta2 = Duration.between(time2, now).toMillis();

        return Long.compare(delta2, delta1);
    }
}
