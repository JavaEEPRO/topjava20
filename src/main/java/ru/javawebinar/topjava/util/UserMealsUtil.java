package ru.javawebinar.topjava.util;

import ru.javawebinar.topjava.model.UserMeal;
import ru.javawebinar.topjava.model.UserMealWithExcess;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class UserMealsUtil {
    public static void main(String[] args) {
        List<UserMeal> meals = Arrays.asList(
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 10, 0), "Завтрак", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 13, 0), "Обед", 1000),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 20, 0), "Ужин", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 0, 0), "Еда на граничное значение", 100),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 10, 0), "Завтрак", 1000),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 13, 0), "Обед", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 20, 0), "Ужин", 410)
        );

        List<UserMealWithExcess> mealsTo = filteredByCycles(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000);
        mealsTo.forEach(System.out::println);

       System.out.println(filteredByStreams(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000));
        System.out.println(filteredByStreams(meals, 2000, timeFrameFilter(LocalTime.of(7, 0), LocalTime.of(12, 0))));
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
//        meals.forEach(meal -> caloriesMappedByDay.merge(meal.getDateTime().toLocalDate(), meal.getCalories(), Integer::sum));
//        meals.forEach(meal -> {
//            if (TimeUtil.isBetweenHalfOpen(meal.getDateTime().toLocalTime(), startTime, endTime)) {
//                res.add(new UserMealWithExcess(meal.getDateTime(), meal.getDescription(), meal.getCalories(), caloriesMappedByDay.get(meal.getDateTime().toLocalDate()) > caloriesPerDay));
//            }
//        });
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
                    boolean currentExceeded = false;
                    if(caloriesPerDate(localDate) > caloriesPerDay) {
                        currentExceeded = true;
                    }
                    res.add(createWithExceed(userMeal, currentExceeded));
                });
        return res;
    }

    private static List<UserMealWithExcess> filteredByStreams(Collection<UserMeal> meals, int caloriesPerDay, Predicate<UserMeal> filter) {
        Map<LocalDate, Integer> caloriesSumByDate =
                meals.stream()
                 .collect(
                        Collectors.groupingBy(UserMeal::getLocalDate, Collectors.summingInt(UserMeal::getCalories)));


        return meals.stream()
                .filter(filter)
                .map(meal -> createWithExceed(meal, caloriesSumByDate.get(meal.getDateTime().toLocalDate()) > caloriesPerDay))
                .collect(toList());
    }

    private static Predicate<UserMeal> timeFrameFilter(LocalTime startTime, LocalTime endTime) {
        return userMeal -> TimeUtil.isBetweenHalfOpen(userMeal.getDateTime().toLocalTime(), startTime, endTime);
    }

    private static UserMealWithExcess createWithExceed(UserMeal meal, boolean exceeded) {
        return new UserMealWithExcess(meal.getDateTime(), meal.getDescription(), meal.getCalories(), exceeded);
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