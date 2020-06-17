package ru.javawebinar.topjava.util;

import ru.javawebinar.topjava.model.UserMeal;
import ru.javawebinar.topjava.model.UserMealWithExcess;
import ru.javawebinar.topjava.model.UserMealWithExcessForReflectiveCall;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toList;

public class UserMealsUtil {
    public static void main(String[] args) throws NoSuchFieldException {
        List<UserMeal> meals = Arrays.asList(
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 10, 0), "Завтрак", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 13, 0), "Обед", 1000),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 20, 0), "Ужин", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 0, 0), "Еда на граничное значение", 100),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 10, 0), "Завтрак", 1000),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 13, 0), "Обед", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 20, 0), "Ужин", 410)
        );

        System.out.println("\nresult of old plain filteredByCycles, O(n):\n");
        List<UserMealWithExcess> mealsTo = filteredByCycles(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000);
        mealsTo
                .forEach(System.out::println);   // call of old plain filteredByCycles (expected, that this solution'll process data faster than all others)

        System.out.println("\nresult of filteredByEnhancedForCycles, O(n):\n");
        filteredByEnhancedForCycles(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000);
        mealsTo
                .forEach(System.out::println);   // call of filteredByEnhancedForCycles

        System.out.println("\nresult of filteredByStreams (NARROW PLACE) AS EXAMPLE of NEVER ARCHITECT AS THIS, has O(n^2) time complexity:\n");
        filteredByStreams(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000)
                .forEach(System.out::println); // call of filteredByStreams: old ugly filter edition - JUST AS EXAMPLE of NEVER DO THIS, to prevent similar mistakes of architecture with performance-non-friendly nested loops (has O(n^2) stigma over its face) going over list by stream API with extra looping at every step through secondary cache map, when called caloriesPerDate(LocalDateTime dateTime)

        System.out.println("\nresult of filteredByStreams with O(n) time complexity:\n");
        filteredByStreams(meals, timeFrameFilter(LocalTime.of(7, 0), LocalTime.of(12, 0)), 2000)
                .forEach(meal -> System.out.println(meal.toString()));  // call of filteredByStreams with O(n) time complexity, beautiful short solution

        System.out.println("\nresult of filteredByRecursiveHeadcutting with O(n) time complexity:\n");
        filteredByRecursiveHeadcutting(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000)
                .forEach(System.out::println);

        System.out.println("\nresult of filteredWithAtomicBoolean with O(n) time complexity:\n");
        filteredWithAtomicBoolean(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000)
                .forEach(System.out::println);

        System.out.println("\nresult of filteredWithBooleanThroughReflection with O(n) time complexity:\n");
        filteredWithBooleanThroughReflection(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000)
                .forEach(System.out::println);
    }

    public static List<UserMealWithExcess> filteredByCycles(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        final List<UserMealWithExcess> res = new ArrayList<>(meals.size()); // TODO return filtered list with excess. Implement by cycles
        final Map<LocalDate, Integer> caloriesMappedByDay = new HashMap<>();
        for( UserMeal userMeal : meals ) {
            caloriesMappedByDay.merge(userMeal.getDateTime().toLocalDate(), userMeal.getCalories(), Integer::sum);
        }
        for (UserMeal userMeal : meals) {
            if (TimeUtil.isBetweenHalfOpen(userMeal.getDateTime().toLocalTime(), startTime, endTime)) {
                res.add(new UserMealWithExcess(userMeal.getDateTime(), userMeal.getDescription(), userMeal.getCalories(), caloriesMappedByDay.get(userMeal.getDateTime().toLocalDate()) > caloriesPerDay));
            }
        }
        return res;
    }

    public static List<UserMealWithExcess> filteredByEnhancedForCycles(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        final List<UserMealWithExcess> res = new ArrayList<>(meals.size());
        final Map<LocalDate, Integer> caloriesMappedByDay = new HashMap<>();
        meals.forEach(meal -> caloriesMappedByDay.merge(meal.getDateTime().toLocalDate(), meal.getCalories(), Integer::sum));
        meals.forEach(meal -> {
            if (TimeUtil.isBetweenHalfOpen(meal.getDateTime().toLocalTime(), startTime, endTime)) {
                res.add(new UserMealWithExcess(meal.getDateTime(), meal.getDescription(), meal.getCalories(), caloriesMappedByDay.get(meal.getDateTime().toLocalDate()) > caloriesPerDay));
            }
        });
        return res;
    }

    static Map<LocalDateTime, Integer> caloriesPerMealsMap = new HashMap<>();

    public static List<UserMealWithExcess> filteredByStreams(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        List<UserMealWithExcess> res = new ArrayList<>();// TODO Implement by streams

        meals
                .stream()
                .forEach((meal) -> caloriesPerMealsMap.put(meal.getDateTime(), meal.getCalories()));

        Map<LocalDateTime, UserMeal> filteredMap =
        meals
                .stream()
                .filter(meal -> TimeUtil.isBetweenHalfOpen(meal.getDateTime().toLocalTime(), startTime, endTime))
                .collect(Collectors.toMap(UserMeal::getDateTime, (meal) -> meal));

        filteredMap
                .forEach((localDate, userMeal)->
                {
                    boolean currentExcess = false;
                    if(caloriesPerDate(localDate) > caloriesPerDay) {
                        currentExcess = true;
                    }
                    res.add(createWithExcess(userMeal, currentExcess));
                });
        return res;
    }

    private static List<UserMealWithExcess> filteredByStreams(Collection<UserMeal> meals, Predicate<UserMeal> filter, int caloriesPerDay) {
        Map<LocalDate, Integer> caloriesSumByDate =
                meals.stream()
                        .collect(
                                Collectors.groupingBy(UserMeal::getLocalDate, Collectors.summingInt(UserMeal::getCalories)));

        return meals.stream()
                .filter(filter)
                .map(meal -> createWithExcess(meal, caloriesSumByDate.get(meal.getDateTime().toLocalDate()) > caloriesPerDay))
                .collect(toList());
    }

    public static Map<LocalDate, Integer> caloriesMappedByDay = new HashMap<>();

    public static List<UserMealWithExcess> filteredByRecursiveHeadcutting(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        final List<UserMealWithExcess> res = new ArrayList<>(meals.size());
        caloriesMappedByDay.merge(meals.get(0).getDateTime().toLocalDate(), meals.get(0).getCalories(), (a, b) -> (a + b));

        if (meals.size() > 1) {
            res.addAll(filteredByRecursiveHeadcutting(meals.subList(1, meals.size()), startTime, endTime, caloriesPerDay));
        }

        if (TimeUtil.isBetweenHalfOpen(meals.get(0).getDateTime().toLocalTime(), startTime, endTime)) {
            res.add(new UserMealWithExcess(meals.get(0).getDateTime(),
                    meals.get(0).getDescription(),
                    meals.get(0).getCalories(),
                    !(caloriesMappedByDay.get(meals.get(0).getDateTime().toLocalDate()) <= caloriesPerDay)));
        }
        return res;
    }

    public static List<UserMealWithExcess> filteredWithAtomicBoolean(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        final Map<LocalDate, Integer> caloriesSumByDate = new HashMap<>();
        final Map<LocalDate, AtomicBoolean> exceededByDate = new HashMap<>();

        final List<UserMealWithExcess> mealsWithExcess = new ArrayList<>();

        meals.forEach(meal -> {
          AtomicBoolean wrapBoolean = exceededByDate.computeIfAbsent(meal.getDateTime().toLocalDate(), date -> new AtomicBoolean());
          Integer dailyCalories = caloriesSumByDate.merge(meal.getLocalDate(), meal.getCalories(), Integer::sum);
          if (dailyCalories > caloriesPerDay) {
              wrapBoolean.set(true);
          }
          if (TimeUtil.isBetweenHalfOpen(meal.getDateTime().toLocalTime(), startTime, endTime)) {
              mealsWithExcess.add(createWithExcess(meal, wrapBoolean));
          }
        });
        return mealsWithExcess;
    }

    public static List<UserMealWithExcessForReflectiveCall> filteredWithBooleanThroughReflection(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) throws NoSuchFieldException {
        final Map<LocalDate, Integer> caloriesSumByDate = new HashMap<>();
        final Map<LocalDate, Boolean> exceededByDate = new HashMap<>();
        Field field = Boolean.class.getDeclaredField("value");
        field.setAccessible(true);

        final List<UserMealWithExcessForReflectiveCall> mealsWithExcess = new ArrayList<>();

        meals.forEach(meal -> {
            Boolean mutableBoolean = exceededByDate.computeIfAbsent(meal.getDateTime().toLocalDate(), date -> new Boolean(false));
            Integer dailyCalories = caloriesSumByDate.merge(meal.getDateTime().toLocalDate(), meal.getCalories(), Integer::sum);
            if (dailyCalories > caloriesPerDay) {
                try {
                    field.setBoolean(mutableBoolean, true);
                    
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

            if (TimeUtil.isBetweenHalfOpen(meal.getDateTime().toLocalTime(), startTime, endTime)) {
                mealsWithExcess.add(new UserMealWithExcessForReflectiveCall(meal.getDateTime(),
                                                                            meal.getDescription(),
                                                                            meal.getCalories(),
                                                                            mutableBoolean));
            }
        });
        return mealsWithExcess;
    }

    //
    private static Predicate<UserMeal> timeFrameFilter(LocalTime startTime, LocalTime endTime) {
        return userMeal -> TimeUtil.isBetweenHalfOpen(userMeal.getDateTime().toLocalTime(), startTime, endTime);
    }

    private static UserMealWithExcess createWithExcess(UserMeal meal, boolean excess) {
        return new UserMealWithExcess(meal.getDateTime(), meal.getDescription(), meal.getCalories(), excess);
    }

    private static UserMealWithExcess createWithExcess(UserMeal meal, AtomicBoolean excess) {
        return new UserMealWithExcess(meal.getDateTime(), meal.getDescription(), meal.getCalories(), excess);
    }

    public static int caloriesPerDate(LocalDateTime dateTime) {
        LocalDate date = dateTime.toLocalDate();
        return caloriesPerMealsMap.entrySet().stream()
                .filter(currentMap -> (currentMap.getKey().toLocalDate().isEqual(date)))
                .mapToInt(Map.Entry::getValue).sum();
    }
}



/*
*     найгарнішій дівчинці в світі
*   личить тільки всміхатись і сяяти
*     і ніколи нічим не печалитись,
*    але завжди щасливій світитись,
*   і від щастя, мов пташці, співати!
*
*   хай Твій кожний крок буде вдалий,
*     наче пісні прекрасної такти
* (все-привсе щоб легенько вспівала Ти)
*     погляд Твоїх очей досконалий
*     сильний світ на краще міняти!
*
*      наймиліша дівчинка в світі,
*     усім серцем до Тебе я лину -
*      подаруй мені посмішку щиру,
*     щоб мені теж з Тобою радіти:
*    Я з Тобою мов на крилах орлиних :*
* */