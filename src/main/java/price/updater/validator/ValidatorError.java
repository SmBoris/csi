package price.updater.validator;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ValidatorError {
    DEPARTMENT_NUMBER_ERROR("Number of Department is not correctly"),
    CODE_PRODUCT_NOT_FOUND_ERROR("Not found product code"),
    PRICE_EMPTY_NULL_ERROR("Price null or empty"),
    PRICE_NUMBER_ERROR("Number of price is not correctly"),
    PRICE_PERIOD_ERROR("Period for price is wrong or expired"),
    PRICE_VALUE_ERROR("Price value is not correctly"),
    PRODUCT_DUPLICATE_CODE("Product have a duplicate code");

    private final String name;
}
