package price.updater.service;

import org.springframework.stereotype.Service;
import price.updater.model.Product;

import java.util.List;

@Service
public interface ProductManagerService {
    List<Product> getUpdatedSellingPrice(List<Product> currentPrices, List<Product> newPrices);

}
