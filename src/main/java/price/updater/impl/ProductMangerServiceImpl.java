package price.updater.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import price.updater.model.Department;
import price.updater.model.Price;
import price.updater.model.Product;
import price.updater.service.ProductManagerService;
import price.updater.validator.ProductValidator;
import price.updater.validator.ValidatorError;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@AllArgsConstructor
public class ProductMangerServiceImpl implements ProductManagerService {

    private ProductValidator productValidator;

    @Override
    public List<Product> getUpdatedSellingPrice(List<Product> products, List<Product> importingProducts) {

        if (importingProducts == null || importingProducts.isEmpty()) {
            // some exception
        }
        if (products == null || products.isEmpty()){
            // some exception
        }

        //The better solution would be to use maps at once without converting
        Map<String, Product> productMap =  productValidator.convertToMapWithoutDuplicates(products);
        Map<String, Product> importingProductMap = productValidator.convertToMapWithoutDuplicates(importingProducts);

        productValidator.validate(productMap, importingProductMap);

        return mergeCollections(productMap, importingProductMap);
    }

    public Map<Product, ValidatorError> getInvalidatedProducts(){
        return productValidator.getInvalidatedProducts();
    }

    public List<Product> mergeCollections(Map<String, Product> productMap, Map<String, Product> importingProductMap){
        List<Product> res = new ArrayList<>();
        importingProductMap.forEach((code, product) -> res.add(mergeProduct(product, productMap.get(code))));

        return res;
    }

    public Product mergeProduct(Product product, Product importingProduct){
         if (product.getPrices().isEmpty() || product.getPrices() == null){
             product.setPrices(importingProduct.getPrices());

             return product;
         }
         product.setPrices(mergePrices(product.getPrices(), importingProduct.getPrices()));

         return product;
    }


    public List<Price> mergePrices(List<Price> prices, List<Price> importPrice){
        List<Price> sortedPrices = getPricesByDepartmentsSort(prices, importPrice);

        Stack<Price> pricesStack = new Stack<>();
        Stack<Price> priceForMerge = new Stack<>();
        int number = 0;
        int departNumber = 0;

        for (Price price : sortedPrices){
            if (departNumber == price.getDepartment().getNumber() && number == price.getNumber()){
                priceForMerge.push(price);
                continue;
            }
            if (priceForMerge.size() > 1){
                for (Price pricesList : mergePriceByDepartmentAndNumber(new ArrayList<>(priceForMerge),
                        priceForMerge.peek().getDepartment())){

                    pricesStack.push(pricesList);
                }
                priceForMerge.clear();
            }

            if (pricesStack.isEmpty() ||
                    pricesStack.peek().getDepartment().getNumber() != price.getDepartment().getNumber() ||
                    pricesStack.peek().getNumber() != price.getNumber() ||
                    haveNextPeriod(pricesStack.peek().getEnd(), price.getBegin())) {

                pricesStack.push(price);
                departNumber = 0;
                number = 0;
                continue;
            }
            if (!priceForMerge.isEmpty() &&
                    priceForMerge.peek().getDepartment().getNumber() != price.getDepartment().getNumber()){

                Price price1 = pricesStack.pop();

                for (Price pricesList : mergePriceByDepartmentAndNumber(new ArrayList<>(priceForMerge),
                        priceForMerge.peek().getDepartment())){

                    pricesStack.push(pricesList);
                }

                pricesStack.push(price1);
                priceForMerge.clear();
            }
            priceForMerge.push(pricesStack.pop());
            priceForMerge.push(price);
            departNumber = price.getDepartment().getNumber();
            number = price.getNumber();
        }

        if (!priceForMerge.isEmpty()){
            for (Price pricesList : mergePriceByDepartmentAndNumber(new ArrayList<>(priceForMerge),
                    priceForMerge.peek().getDepartment())){

                pricesStack.push(pricesList);
            }
        }

        return new ArrayList<>(pricesStack);
    }

    private boolean haveNextPeriod(LocalDateTime start, LocalDateTime finish){
        return start.isBefore(finish);
    }

    private List<Price> getPricesByDepartmentsSort(List<Price> prices, List<Price> importPrice){

        Comparator<Price> comparator = Comparator.comparing(Price::getNumber);
        comparator = comparator.thenComparing(p -> p.getDepartment().getNumber());

        return  Stream
                .concat(prices.stream(), importPrice.stream())
                .sorted(comparator)
                .collect(Collectors.toList());

    }

    private List<Price> mergePriceByDepartmentAndNumber(List<Price> input, Department department) {
        NavigableMap<LocalDateTime, Map<Long, Integer>> map = new TreeMap<>();
        map.put(LocalDateTime.MIN, new HashMap<>());

        for (Price price : input) {
            if (!map.containsKey(price.getBegin())) {
                map.put(price.getBegin(), new HashMap<>(map.lowerEntry(price.getBegin()).getValue()));
            }
            if (!map.containsKey(price.getEnd())) {
                map.put(price.getEnd(), new HashMap<>(map.lowerEntry(price.getEnd()).getValue()));
            }
            for (Map<Long, Integer> mapInt : map.subMap(price.getBegin(), price.getEnd()).values()) {
                mapInt.put(price.getValue(), price.getNumber());
            }
        }

        return mergeByPeriod(map, department);
    }

    private List<Price> mergeByPeriod(NavigableMap<LocalDateTime, Map<Long, Integer>> map, Department department){
        List<Price> result = new ArrayList<>();
        List<Long> valueCache = new ArrayList<>();
        List<Price> totalResult = new ArrayList<>();
        final long[] prevPrice = {0};

        map.forEach((date, mapValues)-> {

            if (mapValues.isEmpty()){
                if (result.size() == 1){
                    result.get(0).setEnd(date);
                    totalResult.add(result.get(0));
                }
                return;
            }

            if (!valueCache.isEmpty()){
                for (int i = valueCache.size() - 1; i >= 0; i --) {

                    if (mapValues.containsKey(valueCache.get(i))) {
                        result.get(i).setEnd(date);
                        mapValues.remove(valueCache.get(i));
                    }
                    else {
                        result.get(i).setEnd(date);
                        totalResult.add(result.get(i));
                        result.remove(result.get(i));
                        valueCache.remove(i);
                    }
                }
            }
            if (!mapValues.isEmpty()) {
                mapValues.forEach((value, code) -> {
                    Price price = new Price();
                    price.setBegin(date);
                    price.setEnd(date);
                    price.setValue(value);
                    price.setNumber(code);
                    price.setDepartment(department);
                    prevPrice[0] = value;
                    if (!result.isEmpty()) {
                        if (result.get(0).getNumber() == price.getNumber()) {
                            totalResult.add(result.get(0));
                            result.remove(0);
                            valueCache.remove(0);
                        }
                    }
                    result.add(price);
                    valueCache.add(prevPrice[0]);
                });
            }
        });

        return totalResult;
    }
}
