package ru.javawebinar.topjava.util;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import ru.javawebinar.topjava.model.UserMeal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 2, jvmArgs = {"-Xms2G", "-Xmx2G"})
@Warmup(iterations = 3)
@Measurement(iterations = 7)
public class TimeUtilPerformanceTests {

    @Param({"10000"})
    private int N;

    private List<UserMeal> meals;
    private LocalTime startTime;
    private LocalTime endTime;
    private int caloriesPerDay;

    private Predicate<UserMeal> timeFrameFilter;


    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(TimeUtilPerformanceTests.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    @Setup
    public void setup() {
        meals = new ArrayList<>();
        LocalDateTime startDateForPopulator = LocalDateTime.of(2020, Month.JANUARY, 30, 10, 0);
        for (int i = 0; i < N; i+=2) {
            LocalDateTime currentMealDateTime = startDateForPopulator.plusDays(i);
            meals.add(new UserMeal(currentMealDateTime, "Завтрак", 500));
            meals.add(new UserMeal(currentMealDateTime.plusHours(3), "Обед", 1000));
            meals.add(new UserMeal(currentMealDateTime.plusHours(10), "Ужин", 500));
            meals.add(new UserMeal(currentMealDateTime.plusDays(1).minusHours(20), "Еда на граничное значение", 100));
            meals.add(new UserMeal(currentMealDateTime.plusHours(10), "Завтрак", 1000));
            meals.add(new UserMeal(currentMealDateTime.plusHours(3), "Обед", 500));
            meals.add(new UserMeal(currentMealDateTime.plusHours(7), "Ужин", 410));
        }
        startTime = LocalTime.of(7, 0);
        endTime = LocalTime.of(12, 0);
        caloriesPerDay = 2000;
        timeFrameFilter = timeFrameFilter(startTime, endTime);
    }

    @Benchmark
    public void consumeFilteredByCycles(Blackhole blackhole) {
        blackhole.consume(UserMealsUtil.filteredByCycles(meals, startTime, endTime, caloriesPerDay));
    }

    @Benchmark
    public void consumeFilteredByEnhancedForCycles(Blackhole blackhole) {
        blackhole.consume(UserMealsUtil.filteredByEnhancedForCycles(meals, startTime, endTime, caloriesPerDay));
    }

    @Benchmark
    public void consumeFilteredByStreamsWithCrossCycledProblem(Blackhole blackhole) {
        blackhole.consume(UserMealsUtil.filteredByStreams(meals, startTime, endTime, caloriesPerDay));
    }

    @Benchmark
    public void consumeFilteredByStreams(Blackhole blackhole) {
        blackhole.consume(UserMealsUtil.filteredByStreams(meals, timeFrameFilter, caloriesPerDay));
    }

    @Benchmark
    public void consumeFilteredByRecursiveHeadCutting(Blackhole blackhole) {
        blackhole.consume(UserMealsUtil.filteredByRecursiveHeadcutting(meals, startTime, endTime, caloriesPerDay));
    }

    @Benchmark
    public void consumeFilteredByRecursionThreadSafe(Blackhole blackhole) {
        blackhole.consume(UserMealsUtil.filteredByRecursionThreadSafe(meals, startTime, endTime, caloriesPerDay));
    }

    @Benchmark
    public void consumeFilteredWithAtomicBoolean(Blackhole blackhole) {
        blackhole.consume(UserMealsUtil.filteredWithAtomicBoolean(meals, startTime, endTime, caloriesPerDay));
    }

    @Benchmark
    public void consumeFilteredWithBooleanThroughReflection(Blackhole blackhole) {
        try {
            blackhole.consume(UserMealsUtil.filteredWithBooleanThroughReflection(meals, startTime, endTime, caloriesPerDay));
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    //
    private static Predicate<UserMeal> timeFrameFilter(LocalTime startTime, LocalTime endTime) {
        return userMeal -> TimeUtil.isBetweenHalfOpen(userMeal.getDateTime().toLocalTime(), startTime, endTime);
    }
}
