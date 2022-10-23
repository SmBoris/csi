package price.updater.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import price.updater.model.Product;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ProductValidatorImplTest {

    @InjectMocks
    private ProductValidatorImpl productValidator;

    @Mock
    private PriceValidator priceValidator;

    private List<Product> productList;
    private Product product1;
    private Product product2;
    private Product product3;
    private Product product4;
    private Product product5;
    private Product product6;

    @BeforeEach
    void init(){
        product1 = new Product();
        product2 = new Product();
        product3 = new Product();
        product4 = new Product();
        product5 = new Product();
        product6 = new Product();
    }

    @Test
    void validate() {
        product1.setCode("one"); //product to update
        product2.setCode("two"); //product to delete
        product3.setCode("three"); //product to delete
        product4.setCode("four"); // product to update
        product5.setCode("one"); //importProduct
        product6.setCode("four"); //importProduct

        Map<String, Product> productMap = new HashMap<>();
        Map<String, Product> importingProductMap = new HashMap<>();

        Mockito.doNothing().when(priceValidator).validate(importingProductMap);

        productMap.put(product1.getCode(), product1);
        productMap.put(product2.getCode(), product2);
        productMap.put(product3.getCode(), product3);
        productMap.put(product4.getCode(), product4);

        importingProductMap.put(product5.getCode(), product5);
        importingProductMap.put(product6.getCode(), product6);

        productValidator.validate(productMap, importingProductMap);
    }

    @Test
    void convertToMapWithoutDuplicates() {
        product1.setCode("one"); //duplicate
        product2.setCode("two"); //duplicate
        product3.setCode("one"); //duplicate
        product4.setCode("one"); //duplicate
        product5.setCode("three"); //expected
        product6.setCode("two"); //duplicate

        productList = new ArrayList<>(Arrays.asList(product1, product2, product3, product4,
                product5, product6));

        Map<String, Product> expected = new HashMap<>();
        expected.put(product5.getCode(), product5);

        Map<String, Product> actual = productValidator.convertToMapWithoutDuplicates(productList);
        Map<Product, ValidatorError> actualInvalidatedProducts = productValidator.getInvalidatedProducts();

        assertEquals(expected, actual);
        assertThat(actualInvalidatedProducts).containsKey(product1);
        assertThat(actualInvalidatedProducts).containsKey(product2);
        assertThat(actualInvalidatedProducts).containsKey(product3);
        assertThat(actualInvalidatedProducts).containsKey(product4);
        assertThat(actualInvalidatedProducts).containsKey(product6);
    }
}