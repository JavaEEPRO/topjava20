package ru.javawebinar.topjava.model;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

public class UserMealWithExcess {
    private final LocalDateTime dateTime;

    private final String description;

    private final int calories;

    private final Boolean excess;
    private final AtomicBoolean atomicExcess;

    public AtomicBoolean getExcess() {
        return this.atomicExcess;
    }

    public UserMealWithExcess(LocalDateTime dateTime, String description, int calories, AtomicBoolean excess) {
        this.dateTime = dateTime;
        this.description = description;
        this.calories = calories;
        this.atomicExcess = excess;
        this.excess = this.atomicExcess.get();
    }

    public UserMealWithExcess(LocalDateTime dateTime, String description, int calories, Boolean excess) {
        this.dateTime = dateTime;
        this.description = description;
        this.calories = calories;
        this.excess = excess;
        this.atomicExcess = new AtomicBoolean(excess);

    }

    @Override
    public String toString() {
        //if (excess) { atomicExcess.set(true); }
        return "UserMealWithExcess{" +
                "dateTime=" + dateTime +
                ", description='" + description + '\'' +
                ", calories=" + calories +
                ", excess=" + atomicExcess +
                '}';
    }
}
