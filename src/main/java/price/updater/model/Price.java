package price.updater.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Price {

    // ID
    private Long id;

    private int number;

    private LocalDateTime begin;
    private LocalDateTime end;

    private long value;

    //Many to one
    private Product product;

    //One to one
    private Department department;
}
