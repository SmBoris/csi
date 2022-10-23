package price.updater.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import price.updater.model.Department;
import price.updater.model.Price;
import price.updater.model.Product;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class PriceValidatorTest {

    @InjectMocks
    private PriceValidator priceValidator;
    private final Map<String, Product> productMap = new HashMap<>();

    private final Map<Product, ValidatorError> expectedErrors = new HashMap<>();

    private final static LocalDate LOCAL_DATE = LocalDate.of(2022, 11, 13);

    @BeforeEach
    void init(){
        Product product1 = new Product();
        product1.setCode("one");
        Product product2 = new Product();
        product2.setCode("two");
        Product product3 = new Product();
        product3.setCode("three");
        Product product4 = new Product();
        product4.setCode("four");
        Product product5 = new Product();
        product5.setCode("five");
        Product product6 = new Product();
        product6.setCode("six");
        Product product7 = new Product();
        product7.setCode("seven");

        Department department1 = new Department();
        department1.setNumber(1);

        Department department2 = new Department();
        department2.setNumber(-10); // wrong number

        Department department3 = new Department();
        department2.setNumber(15); //wrong number - more than max value


        //Price without errors
        Price price1 = new Price();
        price1.setNumber(1);
        price1.setDepartment(department1);
        price1.setValue(10000);
        price1.setBegin(LocalDateTime.now());
        price1.setEnd(LocalDateTime.now().plusDays(10)); // 0 -> 10

        Price price2 = new Price();
        price2.setNumber(1);
        price2.setDepartment(department1);
        price2.setValue(20000);
        price2.setBegin(LocalDateTime.now().plusDays(10).plusSeconds(1)); // 10 + 1 sec
        price2.setEnd(LocalDateTime.now().plusDays(20)); // 10(1) -> 20

        Price price3 = new Price();
        price3.setNumber(2);
        price3.setDepartment(department1);
        price3.setValue(5000);
        price3.setBegin(LocalDateTime.now().plusDays(9));
        price3.setEnd(LocalDateTime.now().plusDays(11)); //insert between 9 -> 10

        //Price with errors
        Price price4 = new Price();
        price4.setNumber(1);
        price4.setDepartment(department2); // wrong number
        price4.setValue(50000);
        price4.setBegin(LocalDateTime.now().plusDays(30));
        price4.setEnd(LocalDateTime.now().plusDays(31));

        Price price5 = new Price();
        price5.setNumber(5000); // wrong number more than max value
        price5.setDepartment(department1);
        price5.setValue(5000);
        price5.setBegin(LocalDateTime.now().plusDays(32));
        price5.setEnd(LocalDateTime.now().plusDays(33));

        Price price6 = new Price();
        price6.setNumber(2);
        price6.setDepartment(department1);
        price6.setValue(-1000); //wrong price value
        price6.setBegin(LocalDateTime.now().plusDays(9));
        price6.setEnd(LocalDateTime.now().plusDays(11));

        Price price7 = new Price();
        price7.setNumber(2);
        price7.setDepartment(department1);
        price7.setValue(5000);
        price7.setBegin(LocalDateTime.now().plusDays(9));
        price7.setEnd(LocalDateTime.now().minusDays(11)); // wrong period time

        List<Price> priceList1 = new ArrayList<>(Arrays.asList(price1, price2, price3));
        product1.setPrices(priceList1);

        List<Price> priceList2 = new ArrayList<>(Arrays.asList(price4, price5, price6, price7));
        product2.setPrices(priceList2);

        List<Price> priceList3 = new ArrayList<>(Collections.singletonList(price4));
        product3.setPrices(priceList3);

        List<Price> priceList4 = new ArrayList<>(Collections.singletonList(price5));
        product4.setPrices(priceList4);

        List<Price> priceList5 = new ArrayList<>(Collections.singletonList(price6));
        product5.setPrices(priceList5);

        List<Price> priceList6 = new ArrayList<>(Collections.singletonList(price7));
        product6.setPrices(priceList6);

        productMap.put(product1.getCode(), product1);
        productMap.put(product2.getCode(), product2);
        productMap.put(product3.getCode(), product3);
        productMap.put(product4.getCode(), product4);
        productMap.put(product5.getCode(), product5);
        productMap.put(product6.getCode(), product6);
        productMap.put(product7.getCode(), product7);


        expectedErrors.put(product3, ValidatorError.DEPARTMENT_NUMBER_ERROR);
        expectedErrors.put(product4, ValidatorError.PRICE_NUMBER_ERROR);
        expectedErrors.put(product5, ValidatorError.PRICE_VALUE_ERROR);
        expectedErrors.put(product6, ValidatorError.PRICE_PERIOD_ERROR);

    }

    @Test
    void validateWhenPriceIsNull() {
        priceValidator.validate(productMap);
        Map<Product, ValidatorError> actualErrors = priceValidator.getInvalidatedProducts();

        //Size after validate
        assertEquals(1, productMap.size());

        expectedErrors.forEach((key, value) -> {
            assertEquals(value, actualErrors.get(key));
        });
    }
}