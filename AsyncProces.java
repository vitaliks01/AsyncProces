package pr_2_11;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.*;

public class AsyncProces {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Введіть розмір масиву (40-60): ");
        int arraySize = readIntInRange(scanner, 40, 60);

        System.out.print("Введіть множник (2): ");
        int multiplier = readInt(scanner);

        System.out.print("Введіть нижню межу діапазону (-100): ");
        int lowerBound = readInt(scanner);

        System.out.print("Введіть верхню межу діапазону (100): ");
        int upperBound = readInt(scanner);

        // ініціаліз випадк числ у мас
        Random random = new Random();
        List<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < arraySize; i++) {
            numbers.add(random.nextInt(upperBound - lowerBound + 1) + lowerBound);
        }

        ExecutorService executor = Executors.newFixedThreadPool(4);
        List<Future<List<Integer>>> futures = new ArrayList<>();

        // розбив мас на частин і оброб в окрем поток
        for (int i = 0; i < arraySize; i += 10) {
            int end = Math.min(i + 10, arraySize);
            List<Integer> sublist = new ArrayList<>(numbers.subList(i, end));
            Callable<List<Integer>> task = new MultiplicationTask(sublist, multiplier);
            futures.add(executor.submit(task));
        }

        List<Integer> result = new CopyOnWriteArrayList<>();
        long startTime = System.currentTimeMillis();

        // збир рез
        for (Future<List<Integer>> future : futures) {
            try {
                result.addAll(future.get());
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Помилка під час виконання завдання: " + e.getMessage());
            }
        }

        long endTime = System.currentTimeMillis();
        executor.shutdown();

        System.out.println("Вихідний масив: " + numbers);
        System.out.println("Оброблений масив: " + result);
        System.out.println("Час виконання: " + (endTime - startTime) + " мс");
    }

    // мет для зчит ціл числ з введен з перевір
    public static int readInt(Scanner scanner) {
        while (!scanner.hasNextInt()) {
            System.err.print("Помилка! Введіть ціле число: ");
            scanner.next(); // пропуск некоректн введ
        }
        return scanner.nextInt();
    }

    // мет для зчит ціл числ в меж задан діап
    public static int readIntInRange(Scanner scanner, int min, int max) {
        int number = readInt(scanner);
        while (number < min || number > max) {
            System.err.print("Неправильне значення. Введіть значення у діапазоні (" + min + "-" + max + "): ");
            number = readInt(scanner);
        }
        return number;
    }
}

class MultiplicationTask implements Callable<List<Integer>> {
    private final List<Integer> numbers;
    private final int multiplier;

    public MultiplicationTask(List<Integer> numbers, int multiplier) {
        this.numbers = numbers;
        this.multiplier = multiplier;
    }

    @Override
    public List<Integer> call() {
        List<Integer> result = new ArrayList<>();
        for (Integer number : numbers) {
            result.add(number * multiplier);
        }
        return result;
    }
}