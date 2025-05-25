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
        return transactionCategories.contains(category);
    }

    public Set<String> oneCategory(Set<String> categories) {
        Set<String> oneCategory = new HashSet<>();
        for (String category : categories) {
            if (categoryIsExist(category)) {
                oneCategory.add(category);
            }
        }
        return oneCategory;
    }

}
