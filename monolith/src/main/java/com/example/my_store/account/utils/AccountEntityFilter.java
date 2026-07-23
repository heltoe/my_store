package com.example.my_store.account.utils;

import com.example.my_store.account.repository.entity.AccountEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

@Schema(description = "Фильтр для поиска аккаунтов")
public record AccountEntityFilter(
        @Schema(description = "Подстрока в номере телефона", example = "+79")
        String phoneNumberContains,
        @Schema(description = "Подстрока в имени", example = "Иван")
        String firstNameContains,
        @Schema(description = "Подстрока в фамилии", example = "Иванов")
        String lastNameContains,
        @Schema(description = "Подстрока в отчестве", example = "Иванович")
        String secondNameContains
) {
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
