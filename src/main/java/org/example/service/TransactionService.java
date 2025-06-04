package org.example.service;


import java.util.HashSet;
import java.util.Set;

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

}
