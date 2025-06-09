package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.entity.BankAccount;
import org.example.entity.TransactionType;
import org.example.entity.User;
import org.example.entity.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

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
        for (Transaction transaction : bankAccount.getTransactions()) {
            if (transaction.getType().equals(TransactionType.PAYMENT) &&
                    transaction.getCreatedDate().isAfter(monthAgo) &&
                    transaction.getCategory().equals(category)) {
                result = result.add(transaction.getValue());
            }
        }
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
        for (BankAccount bankAccount : user.getBankAccounts()) {
            for (Transaction transaction : bankAccount.getTransactions()) {
                if (TransactionType.PAYMENT.equals(transaction.getType()) &&
                        validCategories.contains(transaction.getCategory()) &&
                        transaction.getCreatedDate().isAfter(monthAgo)) {
                    resultMap.merge(transaction.getCategory(), transaction.getValue(), BigDecimal::add);
                }
            }
        }
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
        List<Transaction> transactions = new ArrayList<>();
        for (BankAccount bankAccount : user.getBankAccounts()) {
            for (Transaction transaction : bankAccount.getTransactions()) {
                if (TransactionType.PAYMENT.equals(transaction.getType())) {
                    transactions.add(transaction);
                }
            }
        }
        transactions.sort(Comparator.comparing(Transaction::getValue));
        for (Transaction transaction : transactions) {
            resultLinkedMap.computeIfAbsent(transaction.getCategory(), k -> new ArrayList<>()).add(transaction);
        }
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
        List<Transaction> transactions = new ArrayList<>();
        for (BankAccount bankAccount : user.getBankAccounts()) {
            transactions.addAll(bankAccount.getTransactions());
        }
        transactions.sort((t1, t2) -> t2.getCreatedDate().compareTo(t1.getCreatedDate()));
        for (int i = 0; i < Math.min(n, transactions.size()); i++){
            listLastTransactions.add(transactions.get(i));
        }
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
        for (BankAccount bankAccount : user.getBankAccounts()) {
            for (Transaction transaction : bankAccount.getTransactions()) {
                if (TransactionType.PAYMENT.equals(transaction.getType())) {
                    if (transactionPriorityQueue.size() < n)
                        transactionPriorityQueue.offer(transaction);
                    else if (transactionPriorityQueue.peek() != null
                            && transactionPriorityQueue.peek().getValue().compareTo(transaction.getValue()) < 0) {
                        transactionPriorityQueue.poll();
                        transactionPriorityQueue.offer(transaction);
                    }
                }
            }
        }
        return transactionPriorityQueue;
    }
}