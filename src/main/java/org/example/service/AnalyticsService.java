package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.entity.BankAccount;
import org.example.entity.TransactionType;
import org.example.entity.User;
import org.example.entity.Transaction;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Сервис предоставляет аналитику по операциям пользователей
 */
@RequiredArgsConstructor
public class AnalyticsService {
    private final TransactionService transactionService;

    /**
     * Вывод суммы потраченных средств на категорию за последний месяц
     *
     * @param bankAccount - счет
     * @param category    - категория
     */
    public BigDecimal getMonthlySpendingByCategory(BankAccount bankAccount, String category) {
        LocalDateTime monthAgo = LocalDateTime.now().minusMonths(1L);
        BigDecimal result = BigDecimal.ZERO;
        if (bankAccount == null || !transactionService.categoryIsExist(category)) {
            return result;
        }
        result = bankAccount.getTransactions().stream()
                .filter(transaction -> TransactionType.PAYMENT.equals(transaction.getType()))
                .filter(transaction -> StringUtils.equals(transaction.getCategory(), category))
                .filter(transaction -> transaction.getCreatedDate().isAfter(monthAgo))
                .map(Transaction::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return result;
    }

    /**
     * Вывод суммы потраченных средств на n категорий за последний месяц
     * со всех счетов пользователя
     *
     * @param user       - пользователь
     * @param categories - категории
     * @return мапа категория - сумма потраченных средств
     */
    public Map<String, BigDecimal> getMonthlySpendingByCategories(User user, Set<String> categories) {
        Map<String, BigDecimal> resultMap = new HashMap<>();
        Set<String> validCategories = transactionService.validCategories(categories);
        LocalDateTime monthAgo = LocalDateTime.now().minusMonths(1L);
        if (user == null || validCategories.isEmpty()) {
            return resultMap;
        }
        resultMap = user.getBankAccounts().stream()
                .flatMap(bankAccount -> bankAccount.getTransactions().stream())
                .filter(transaction -> TransactionType.PAYMENT.equals(transaction.getType())
                        && validCategories.contains(transaction.getCategory())
                        && transaction.getCreatedDate().isAfter(monthAgo))
                .collect(Collectors.toMap(Transaction::getCategory, Transaction::getValue, BigDecimal::add));
        return resultMap;
    }

    /**
     * Вывод платежных операций по всем счетам и по всем категориям от наибольшей к наименьшей
     *
     * @param user - пользователь
     * @return мапа категория - все операции совершенные по ней
     */
    public LinkedHashMap<String, List<Transaction>> getTransactionHistorySortedByAmount(User user) {
        LinkedHashMap<String, List<Transaction>> resultLinkedMap = new LinkedHashMap<>();
        if (user == null) {
            return resultLinkedMap;
        }
        resultLinkedMap = user.getBankAccounts().stream()
                .flatMap(bankAccount -> bankAccount.getTransactions().stream())
                .filter(transaction -> TransactionType.PAYMENT.equals(transaction.getType()))
                .sorted(Comparator.comparing(Transaction::getValue))
                .collect(Collectors.groupingBy(Transaction::getCategory, LinkedHashMap::new, Collectors.toList()));
        return resultLinkedMap;
    }

    /**
     * Вывод последних N транзакций пользователя.
     *
     * @param user - пользователь
     * @param n    - количество последних транзакций
     * @return LinkedHashMap, где ключом является идентификатор транзакции, а значением — объект Transaction
     */
    public List<Transaction> getLastNTransactions(User user, int n) {
        List<Transaction> listLastTransactions = new ArrayList<>();
        if (user == null) {
            return listLastTransactions;
        }
        listLastTransactions = user.getBankAccounts().stream()
                    .flatMap(bankAccount -> bankAccount.getTransactions().stream())
                    .sorted(Comparator.comparing(Transaction::getCreatedDate).reversed())
                    .limit(n)
                    .collect(Collectors.toList());
        return listLastTransactions;
    }

    /**
     * Вывод топ-N самых больших платежных транзакций пользователя.
     *
     * @param user - пользователь
     * @param n    - количество топовых транзакций
     * @return PriorityQueue, где транзакции хранятся в порядке убывания их значения
     */
    public PriorityQueue<Transaction> getTopNLargestTransactions(User user, int n) {
        PriorityQueue<Transaction> transactionPriorityQueue =
                new PriorityQueue<>(Comparator.comparing(Transaction::getValue));
        if (user == null) {
            return transactionPriorityQueue;
        }
        transactionPriorityQueue = user.getBankAccounts().stream()
                .flatMap(bankAccount -> bankAccount.getTransactions().stream())
                .filter(transaction -> TransactionType.PAYMENT.equals(transaction.getType()))
                .sorted(Comparator.comparing(Transaction::getValue).reversed()).limit(n)
                .collect(Collectors.toCollection(() ->new PriorityQueue<>(Comparator.comparing(Transaction::getValue).reversed())));
        return transactionPriorityQueue;
    }

}