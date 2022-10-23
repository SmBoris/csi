package price.updater.validator;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import price.updater.model.Department;
import price.updater.model.Price;
import price.updater.model.Product;

import java.time.LocalDateTime;
import java.util.*;

@Component
@AllArgsConstructor
public class PriceValidator {
    private final Map<Product, ValidatorError> INVALIDATED_PRODUCTS = new HashMap<>();
    private static final int PRICE_NUMBER_MAX_VALUE = 10;
    private static final int DEPARTMENT_MAX_VALUE = 10;

    public void validate(Map<String, Product> products){

        products.forEach((code, product) -> {
            if (product.getPrices() == null || product.getPrices().isEmpty() || product.getPrices().contains(null)){
                INVALIDATED_PRODUCTS.put(product, ValidatorError.PRICE_EMPTY_NULL_ERROR);
                return;
            }
            ValidatorError message = priceFieldValidate(product.getPrices());
            if (message != null){
                INVALIDATED_PRODUCTS.put(product, message);
            }
        });

        INVALIDATED_PRODUCTS.forEach((key, value) -> {
            products.remove(key.getCode());
        });
    }

    private ValidatorError priceFieldValidate(List<Price> prices){

        for (Price price : prices) {
            if (!isValidDepartment(price.getDepartment())) {
                return ValidatorError.DEPARTMENT_NUMBER_ERROR;
            }
            ValidatorError message = doCheckPriceValueError(price);
            if (message != null) {
                return message;
            }
        }

        return null;
    }

    private ValidatorError doCheckPriceValueError(Price price){

            if (price.getNumber() < 0 || price.getNumber() > PRICE_NUMBER_MAX_VALUE){
                return ValidatorError.PRICE_NUMBER_ERROR;
            }
            if (price.getValue() < 0){
                return ValidatorError.PRICE_VALUE_ERROR;
            }
            if (!isValidPeriod(price.getBegin(), price.getEnd())){
                return ValidatorError.PRICE_PERIOD_ERROR;
            }
            return null;
    }

    private boolean isValidPeriod(LocalDateTime begin, LocalDateTime end){
        return end.isAfter(begin);
    }

    private boolean isValidDepartment(Department department){
        return department.getNumber() >= 1 && department.getNumber() < DEPARTMENT_MAX_VALUE;
    }

    public Map<Product, ValidatorError> getInvalidatedProducts(){
        return INVALIDATED_PRODUCTS;
    }
}
