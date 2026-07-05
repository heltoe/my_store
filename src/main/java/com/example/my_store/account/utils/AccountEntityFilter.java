package com.example.my_store.account.utils;

import com.example.my_store.account.repository.entity.AccountEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public record AccountEntityFilter(String phoneNumberContains, String firstNameContains, String lastNameContains,
                                  String secondNameContains) {
    public Specification<AccountEntity> toSpecification() {
        return phoneNumberContainsSpec()
                .and(firstNameContainsSpec())
                .and(lastNameContainsSpec())
                .and(secondNameContainsSpec());
    }

    private Specification<AccountEntity> phoneNumberContainsSpec() {
        return ((root, query, cb) -> StringUtils.hasText(phoneNumberContains)
                ? cb.like(root.get("phoneNumber"), "%" + phoneNumberContains + "%")
                : null);
    }

    private Specification<AccountEntity> firstNameContainsSpec() {
        return ((root, query, cb) -> StringUtils.hasText(firstNameContains)
                ? cb.like(cb.lower(root.get("firstName")), "%" + firstNameContains.toLowerCase() + "%")
                : null);
    }

    private Specification<AccountEntity> lastNameContainsSpec() {
        return ((root, query, cb) -> StringUtils.hasText(lastNameContains)
                ? cb.like(cb.lower(root.get("lastName")), "%" + lastNameContains.toLowerCase() + "%")
                : null);
    }

    private Specification<AccountEntity> secondNameContainsSpec() {
        return ((root, query, cb) -> StringUtils.hasText(secondNameContains)
                ? cb.like(cb.lower(root.get("secondName")), "%" + secondNameContains.toLowerCase() + "%")
                : null);
    }
}