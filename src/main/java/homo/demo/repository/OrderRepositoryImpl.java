package homo.demo.repository;

import homo.demo.model.Order;
import homo.repository.AbstractRepository;
import org.springframework.stereotype.Repository;

/**
 * @author wujianchuan 2018/12/26
 */
@Repository
public class OrderRepositoryImpl extends AbstractRepository<Order> {

    @Override
    public int save(Order entity) {
        System.out.println("保存订单。");
        return 0;
    }

    @Override
    public int update(Order entity) {
        return 0;
    }

    @Override
    public int delete(Order entity) {
        return 0;
    }
}
