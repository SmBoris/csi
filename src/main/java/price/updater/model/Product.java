package price.updater.model;

import lombok.Data;

import java.util.List;

@Data
public class Product {
    //id
    private String code;

    //One to many
    private List<Price> prices;
}
