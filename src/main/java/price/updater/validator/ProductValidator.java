package price.updater.validator;

import org.springframework.stereotype.Service;
import price.updater.model.Product;

import java.util.List;
import java.util.Map;

@Service
public interface ProductValidator {
    void validate(Map<String, Product> products, Map<String, Product> importingProducts);
    Map<String, Product> convertToMapWithoutDuplicates(List<Product> product);
    Map<Product, ValidatorError> getInvalidatedProducts();
}
