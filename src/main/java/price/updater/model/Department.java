package price.updater.model;

import lombok.Data;

import java.util.List;

@Data
public class Department {

    //id
    private Long id;

    private int number;

    //One to many
    private List<Price> price;
}
