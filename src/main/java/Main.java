import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import model.Book;
import model.SmsMessage;
import model.Visitor;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        String filePath = "books.json"; // Убедись, что файл лежит в корне проекта
        List<Visitor> visitors;

        // КРИТЕРИЙ 1: Чтение JSON через Gson
        try (FileReader reader = new FileReader(filePath)) {
            Gson gson = new Gson();
            Type visitorListType = new TypeToken<List<Visitor>>() {}.getType();
            visitors = gson.fromJson(reader, visitorListType);
        } catch (IOException e) {
            System.err.println("Ошибка чтения файла: " + e.getMessage());
            return;
        }

        if (visitors == null || visitors.isEmpty()) {
            System.out.println("Список посетителей пуст.");
            return;
        }

        // =====================================================================
        // № 1. Посетители библиотеки
        // =====================================================================
        System.out.println("--- № 1. Посетители библиотеки ---");
        visitors.stream()
                .map(v -> v.getName() + " " + v.getSurname())
                .forEach(System.out.println);

        long totalVisitors = visitors.stream().count();
        System.out.println("Всего посетителей: " + totalVisitors);
        System.out.println();

        // =====================================================================
        // № 2. Книги без повторений
        // =====================================================================
        System.out.println("--- № 2. Книги без повторений ---");
        List<Book> uniqueBooks = visitors.stream()
                .flatMap(v -> v.getFavoriteBooks().stream())
                .distinct()
                .collect(Collectors.toList());

        uniqueBooks.forEach(b -> System.out.println(b.getName() + " — " + b.getAuthor()));
        System.out.println("Количество уникальных книг: " + uniqueBooks.size());
        System.out.println();

        // =====================================================================
        // № 3. Сортировка книг
        // =====================================================================
        System.out.println("--- № 3. Сортировка книг ---");
        visitors.stream()
                .flatMap(v -> v.getFavoriteBooks().stream())
                .distinct()
                .sorted((b1, b2) -> Integer.compare(b1.getPublishingYear(), b2.getPublishingYear()))
                .forEach(b -> System.out.println(b.getPublishingYear() + " г. | " + b.getName()));
        System.out.println();

        // =====================================================================
        // № 4. Поиск автора
        // =====================================================================
        System.out.println("--- № 4. Поиск автора ---");
        boolean hasJaneAusten = visitors.stream()
                .flatMap(v -> v.getFavoriteBooks().stream())
                .anyMatch(b -> "Jane Austen".equalsIgnoreCase(b.getAuthor()));

        System.out.println("Есть ли книги Jane Austen в избранном? " + (hasJaneAusten ? "Да" : "Нет"));
        System.out.println();

        // =====================================================================
        // № 5. Максимальное число книг
        // =====================================================================
        System.out.println("--- № 5. Максимальное число книг ---");
        int maxBooksCount = visitors.stream()
                .mapToInt(v -> v.getFavoriteBooks().size())
                .max()
                .orElse(0);

        System.out.println("Максимальное количество книг у одного читателя: " + maxBooksCount);
        System.out.println();

        // =====================================================================
        // № 6. Сообщения подписчикам
        // =====================================================================
        System.out.println("--- № 6. Сообщения подписчикам ---");

        // Шаг 6.1: Считаем среднее количество книг по ВСЕМ посетителям
        double averageBooksCount = visitors.stream()
                .mapToInt(v -> v.getFavoriteBooks().size())
                .average()
                .orElse(0.0);
        System.out.printf("Среднее количество книг на посетителя: %.2f\n\n", averageBooksCount);

        // Шаг 6.2: Фильтруем только подписчиков и формируем SMS-сообщения
        List<SmsMessage> smsMessages = visitors.stream()
                .filter(Visitor::isSubscribed)
                .map(v -> {
                    int booksCount = v.getFavoriteBooks().size();
                    String msgText;
                    if (booksCount > averageBooksCount) {
                        msgText = "you are a bookworm";
                    } else if (booksCount < averageBooksCount) {
                        msgText = "read more";
                    } else {
                        msgText = "fine";
                    }
                    return new SmsMessage(v.getPhone(), msgText);
                })
                .collect(Collectors.toList());

        // Вывод сгенерированных сообщений
        smsMessages.forEach(sms ->
                System.out.println("Кому: " + sms.getPhoneNumber() + " | Текст: \"" + sms.getMessage() + "\"")
        );
    }
}
