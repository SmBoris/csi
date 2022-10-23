package price.updater.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import price.updater.model.Department;
import price.updater.model.Price;
import price.updater.model.Product;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

@SpringBootTest
class ProductMangerServiceImplTest {

    @Autowired
    private ProductMangerServiceImpl productMangerService;
    private Department department1;
    private Department department2;
    private Department department3;
    private Department department4;
    private LocalDateTime start;
    private final List<Product> dbProductList = new ArrayList<>();
    private final List<Product> importProductList = new ArrayList<>();

    @BeforeEach
    void init(){
        department1 = new Department();
        department1.setNumber(1);

        department2 = new Department();
        department2.setNumber(2);

        department3 = new Department();
        department3.setNumber(3);

        department4 = new Department();
        department4.setNumber(500); //wrong number

        start = LocalDateTime.now()
                .withDayOfMonth(1)
                .withMonth(1)
                .withYear(2013)
                .withMinute(0)
                .withSecond(0)
                .withHour(0);
    }

    @DisplayName("Parameters like test task")
    @Test
    void getUpdateSellingPrice_likeTestTask() {

        //-----------------------Db prices--------------------------//
        //product - 122856
        Price price1 = buildPrice(1, start, start.plusDays(31), department1, 11000);
        Price price2 = buildPrice(2, start.plusDays(9), start.plusDays(19), department1, 99000);

        //product - 6654
        Price price3 = buildPrice(1, start, start.plusDays(30), department2, 5000);

        List<Price> dbPriceList1 = new ArrayList<>(Arrays.asList(price1, price2));
        List<Price> dbPriceList2 = new ArrayList<>(Collections.singletonList(price3));

        //-----------------------Import prices--------------------------//
        //product - 122856
        Price price4 = buildPrice(1, start.plusDays(20), start.plusDays(50), department1, 11000);
        Price price5 = buildPrice(2, start.plusDays(14), start.plusDays(24), department1, 92000);

        //product - 6654
        Price price6 = buildPrice(1, start.plusDays(11), start.plusDays(12), department2, 4000);

        List<Price> importPriceList1 = new ArrayList<>(Arrays.asList(price4, price5));
        List<Price> importPriceList2 = new ArrayList<>(Collections.singletonList(price6));

        Product dbProduct1 = buildProduct("122856", dbPriceList1);
        Product dbProduct2 = buildProduct("6654", dbPriceList2);

        Product importProduct1 = buildProduct("122856", importPriceList1);
        Product importProduct2 = buildProduct("6654", importPriceList2);

        //create list for test
        dbProductList.add(dbProduct1);
        dbProductList.add(dbProduct2);

        importProductList.add(importProduct1);
        importProductList.add(importProduct2);

        List<Product> result = productMangerService.getUpdatedSellingPrice(dbProductList, importProductList);
        printResult(result);
        printErrors();
    }
    
    @Test
    void getUpdateSellingPrice_withDifferentValuesAndErrors(){

        //-----------------------Db prices--------------------------//
        // product - 1111
        Price price1 = buildPrice(1, start , start.plusDays(20), department1, 11000);
        Price price2 = buildPrice(2, start, start.plusDays(30), department2, 10000);

        // product - 2222
        Price price3 = buildPrice(2, start.plusDays(5), start.plusDays(20), department2, 5000); //expect without changes
        Price price4 = buildPrice(3, start.plusDays(8), start.plusDays(16), department2, 8000);
        Price price5 = buildPrice(1, start, start.plusDays(10), department3, 50000); // expect without changes

        // product - 3333
        Price price6 = buildPrice(1, start.plusDays(12), start.plusDays(20), department3, 33000);
        Price price7 = buildPrice(2, start.plusDays(14), start.plusDays(25), department3, 8888);
        Price price8 = buildPrice(3,start.plusDays(25), start.plusDays(50), department3, 60000);

        List<Price> dbPriceList1 = new ArrayList<>(Arrays.asList(price1, price2));
        List<Price> dbPriceList2 = new ArrayList<>(Arrays.asList(price3, price4, price5));
        List<Price> dbPriceList3 = new ArrayList<>(Arrays.asList(price6, price7, price8));

        //-----------------------Import prices--------------------------//
        // product - 1111
        Price price9 = buildPrice(1, start.plusDays(8), start.plusDays(25), department1, 11000); //same value -> expect 20 to 25 days
        Price price10 = buildPrice(3, start.plusDays(8), start.plusDays(16), department1, 5555); //new number -> expect add new number price
        Price price11 = buildPrice(3, start.plusDays(17), start.plusDays(25), department2, 9000); //2 department -> expect add to 2 dep new 3 price

        // product - 2222
        Price price12 = buildPrice(3, start.plusDays(18), start.plusDays(25), department3, 60000); //no merge periods -> expect number 3 add price

        // product - 3333
        Price price13 = buildPrice(1, start.plusDays(15), start.plusDays(18), department3, 34000); //new price between period -> expect 3 periods
        Price price14 = buildPrice(2, start.plusDays(15), start.plusDays(30), department3, 999999);//new price between double periods! expect -> 6 periods

        // product - 4444
        Price price15 = buildPrice(1, start.plusDays(5), start.plusDays(15), department1, -10000); //Negative value, expected -> error

        // product - 5555
        Price price16 = buildPrice(5, start.plusDays(5), start.plusDays(1), department1, 5000); //Wrong period, expected - error

        // product - 6666
        Price price17 = buildPrice(500, start.plusDays(1), start.plusDays(5), department1, 8000); //Wrong Number, expected - error

        // product - 7777
        Price price18 = null; // expected - error

        // product - 8888
        Price price19 = buildPrice(1, start.plusDays(1), start.plusDays(5), department4, 5000);// Wrong Department with number 500

        List<Price> importPriceList1 = new ArrayList<>(Arrays.asList(price9, price10, price11));
        List<Price> importPriceList2 = new ArrayList<>(Collections.singletonList(price12));
        List<Price> importPriceList3 = new ArrayList<>(Arrays.asList(price13, price14));
        List<Price> importErrorList1 = new ArrayList<>(Collections.singletonList(price15));
        List<Price> importErrorList2 = new ArrayList<>(Collections.singletonList(price16));
        List<Price> importErrorList3 = new ArrayList<>(Collections.singletonList(price17));
        List<Price> importErrorList4 = new ArrayList<>(Collections.singletonList(price18));
        List<Price> importErrorList5 = new ArrayList<>(Collections.singletonList(price19));

        //-----------------------Products to merge--------------------------//
        Product dbProduct1 = buildProduct("1111", dbPriceList1);
        Product dbProduct2 = buildProduct("2222", dbPriceList2);
        Product dbProduct3 = buildProduct("3333", dbPriceList3);

        Product importProduct1 = buildProduct("1111", importPriceList1);
        Product importProduct2 = buildProduct("2222", importPriceList2);
        Product importProduct3 = buildProduct("3333", importPriceList3);

        //-----------------------Products for errors check--------------------------//
        Product dbProduct4 = buildProduct("4444", dbPriceList1);
        Product dbProduct5 = buildProduct("5555", dbPriceList1);
        Product dbProduct6 = buildProduct("6666", dbPriceList1);
        Product dbProduct7 = buildProduct("7777", dbPriceList1);
        Product dbProduct8 = buildProduct("8888", dbPriceList1);

        Product importProduct4 = buildProduct("4444", importErrorList1);
        Product importProduct5 = buildProduct("5555", importErrorList2);
        Product importProduct6 = buildProduct("6666", importErrorList3);
        Product importProduct7 = buildProduct("7777", importErrorList4);
        Product importProduct8 = buildProduct("8888", importErrorList5);

        //-----------------------Create list for test--------------------------//
        dbProductList.addAll(Arrays.asList(dbProduct1, dbProduct2, dbProduct3, dbProduct4, dbProduct5, dbProduct6,
                dbProduct7, dbProduct8));
        importProductList.addAll(Arrays.asList(importProduct1, importProduct2, importProduct3, importProduct4,
                importProduct5, importProduct6, importProduct7, importProduct8));

        List<Product> result = productMangerService.getUpdatedSellingPrice(dbProductList, importProductList);
        printResult(result);
        printErrors();
    }

    private Price buildPrice(int number, LocalDateTime begin, LocalDateTime end, Department department, int value){
        Price price = new Price();
        price.setNumber(number);
        price.setBegin(begin);
        price.setEnd(end);
        price.setDepartment(department);
        price.setValue(value);

        return price;
    }

    private Product buildProduct(String code, List<Price> prices){
        Product product = new Product();
        product.setCode(code);
        product.setPrices(prices);

        return product;
    }

    private void printErrors(){
        if (productMangerService.getInvalidatedProducts().isEmpty()){
            System.out.println("No errors detected");
        }
        productMangerService.getInvalidatedProducts().forEach((product, error) -> {
            System.out.println("Errors detected: \n" + product.getCode() + " have error: " + error.getName());
        });
    }

    private void printResult(List<Product> result){
        for (Product resultProduct : result){
            printTable(resultProduct);
        }
    }

    private void printTable(Product product){
        TableStringBuilder<Price> table = new TableStringBuilder<>();

        table.addColumn("Number", Price::getNumber);
        table.addColumn("Depart", p -> p.getDepartment().getNumber());
        table.addColumn("Begin", p -> p.getBegin().withNano(0));
        table.addColumn("End", p -> p.getEnd().withNano(0));
        table.addColumn("Value", Price::getValue);
        String res = table.createString(product.getPrices());
        System.out.println("Prices for product with code: " + product.getCode() + "\n" + res);
    }
}

class TableStringBuilder<T>
{
    private final List<String> columnNames;
    private final List<Function<? super T, String>> stringFunctions;

    TableStringBuilder()
    {
        columnNames = new ArrayList<>();
        stringFunctions = new ArrayList<>();
    }

    void addColumn(String columnName, Function<? super T, ?> fieldFunction)
    {
        columnNames.add(columnName);
        stringFunctions.add((p) -> (String.valueOf(fieldFunction.apply(p))));
    }

    private int computeMaxWidth(int column, Iterable<? extends T> elements)
    {
        int n = columnNames.get(column).length();
        Function<? super T, String> f = stringFunctions.get(column);
        for (T element : elements)
        {
            String s = f.apply(element);
            n = Math.max(n, s.length());
        }
        return n;
    }

    private static String padLeft(String s, char c, int length)
    {
        while (s.length() < length)
        {
            s = c + s;
        }
        return s;
    }

    private List<Integer> computeColumnWidths(Iterable<? extends T> elements)
    {
        List<Integer> columnWidths = new ArrayList<Integer>();
        for (int c=0; c<columnNames.size(); c++)
        {
            int maxWidth = computeMaxWidth(c, elements);
            columnWidths.add(maxWidth);
        }
        return columnWidths;
    }

    public String createString(Iterable<? extends T> elements)
    {
        List<Integer> columnWidths = computeColumnWidths(elements);

        StringBuilder sb = new StringBuilder();
        for (int c=0; c<columnNames.size(); c++)
        {
            if (c > 0)
            {
                sb.append("|");
            }
            String format = "%"+columnWidths.get(c)+"s";
            sb.append(String.format(format, columnNames.get(c)));
        }
        sb.append("\n");
        for (int c=0; c<columnNames.size(); c++)
        {
            if (c > 0)
            {
                sb.append("+");
            }
            sb.append(padLeft("", '-', columnWidths.get(c)));
        }
        sb.append("\n");

        for (T element : elements)
        {
            for (int c=0; c<columnNames.size(); c++)
            {
                if (c > 0)
                {
                    sb.append("|");
                }
                String format = "%"+columnWidths.get(c)+"s";
                Function<? super T, String> f = stringFunctions.get(c);
                String s = f.apply(element);
                sb.append(String.format(format, s));
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}