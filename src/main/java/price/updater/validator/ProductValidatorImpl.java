package price.updater.validator;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import price.updater.model.Product;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class ProductValidatorImpl implements ProductValidator {
    private final PriceValidator priceValidator;
    private final Map<Product, ValidatorError> INVALIDATED_PRODUCTS = new HashMap<>();
    @Override
    public void validate(Map<String, Product> products, Map<String, Product> importingProducts){
        Map<String, Product> filteredProducts = new HashMap<>();

        importingProducts.forEach((code, product) -> {
            if(!products.containsKey(code)){
                INVALIDATED_PRODUCTS.put(product, ValidatorError.CODE_PRODUCT_NOT_FOUND_ERROR);
                return;
            }
            filteredProducts.put(code, products.get(code));
        });

        INVALIDATED_PRODUCTS.forEach((key, value) -> {
            importingProducts.remove(key.getCode());
        });

        products.clear();
        products.putAll(filteredProducts);

        priceValidator.validate(importingProducts);

        if (!haveToImportProducts(importingProducts)){
            // some exception or message
        }
    }

    @Override
    public Map<String, Product> convertToMapWithoutDuplicates(List<Product> product){
        // Remove all duplicates
        Map<String, Product> productMap = product
                .stream()
                .collect(Collectors.toMap(Product::getCode, Product -> Product, (p1, p2) ->{
                    INVALIDATED_PRODUCTS.put(p1, ValidatorError.PRODUCT_DUPLICATE_CODE);
                    INVALIDATED_PRODUCTS.put(p2, ValidatorError.PRODUCT_DUPLICATE_CODE);
                    return null;
                }
                ));

        // Remove the remaining duplicates
        Map<Product, ValidatorError> invalidatedProductCache = new HashMap<>();
        getInvalidatedProducts().forEach((code, errorCode) -> {
            if (productMap.containsKey(code.getCode())){
                invalidatedProductCache.put(productMap.get(code.getCode()), ValidatorError.PRODUCT_DUPLICATE_CODE);
                productMap.remove(code.getCode());
            }
        });

        INVALIDATED_PRODUCTS.putAll(invalidatedProductCache);

        return productMap;
    }

    @Override
    public Map<Product, ValidatorError> getInvalidatedProducts(){
        INVALIDATED_PRODUCTS.putAll(priceValidator.getInvalidatedProducts());
        return INVALIDATED_PRODUCTS;

    }
    private boolean haveToImportProducts(Map<String, Product> productMap){
        return productMap.size() > 0;
    }
}
