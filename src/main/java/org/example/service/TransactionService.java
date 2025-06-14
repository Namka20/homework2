package org.example.service;


import org.example.entity.Transaction;
import org.example.entity.User;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * Сервис отвечает за управление платежами и переводами
 */
public class TransactionService {

    public static final Set<String> transactionCategories = Set.of(
            "Health", "Beauty", "Education");

    public Boolean categoryIsExist(String category) {
        return category != null && transactionCategories.contains(category);
    }

    public Set<String> validCategories(Set<String> categories) {
        Set<String> validCategories = new HashSet<>();
        for (String category : categories) {
            if (categoryIsExist(category)) {
                validCategories.add(category);
            }
        }
        return validCategories;
    }

    /**
     * Фильтрация транзакций пользователя
     *
     * @param user      - пользователь
     * @param predicate - условие фильтрации
     * @return список транзакций, удовлетворяющих условию
     */
    public List<Transaction> filterTransactions(User user, Predicate<Transaction> predicate) {
        if (user == null || predicate == null) {
            return Collections.emptyList();
        }
        return user.getBankAccounts().stream()
                .flatMap(bankAccount -> bankAccount.getTransactions().stream())
                .filter(predicate)
                .collect(Collectors.toList());
    }

    /**
     * Преобразование транзакций пользователя
     *
     * @param user     - пользователь
     * @param function - функция преобразования
     * @return список строковых представлений транзакций
     */
    public List<String> transformTransactions(User user, Function<Transaction, String> function) {
        if (user == null || function == null) {
            return Collections.emptyList();
        }
        return user.getBankAccounts().stream()
                .flatMap(bankAccount -> bankAccount.getTransactions().stream())
                .map(function)
                .collect(Collectors.toList());
    }

    /**
     * Обрабатывание транзакций пользователя
     *
     * @param user     - пользователь
     * @param consumer - функция обработки
     */
    public void processTransactions(User user, Consumer<Transaction> consumer) {
        if (user == null || consumer == null) {
            return;
        }
        user.getBankAccounts().stream()
                .flatMap(bankAccount -> bankAccount.getTransactions().stream())
                .forEach(consumer);
    }

    /**
     * Создание списока транзакций
     *
     * @param supplier - поставщик
     * @return созданный список транзакций
     */
    public List<Transaction> createTransactionList(Supplier<List<Transaction>> supplier) {
        if (supplier == null) {
            return Collections.emptyList();
        }
        return supplier.get();

    }

    /**
     * Объединение списков транзакций в один список
     *
     * @param list1  - 1-й список транзакций
     * @param list2  - 2-й список транзакций
     * @param merger - функция объединения транзакций
     * @return возвращает объединённый список транзакций
     */
    public List<Transaction> mergeTransactionList(List<Transaction> list1, List<Transaction> list2,
                                                  BiFunction<List<Transaction>, List<Transaction>,
                                                          List<Transaction>> merger) {
        if (merger == null) {
            return Collections.emptyList();
        }
        return merger.apply(
                list1 != null ? list1 : Collections.emptyList(),
                list2 != null ? list2 : Collections.emptyList());
    }
}
