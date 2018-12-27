package homo.demo.repository;

import homo.User;
import homo.demo.model.Order;
import homo.common.repository.AbstractRepository;
import org.springframework.stereotype.Repository;

/**
 * @author wujianchuan 2018/12/26
 */
@Repository
public class OrderRepositoryImpl extends AbstractRepository<Order> {

    @Override
    public int save(Order entity, User operator) {
        System.out.println("保存订单。");
        return 1;
    }

    @Override
    public int update(Order entity, User operator) {
        System.out.println("更新订单。");
        return 1;
    }

    @Override
    public int delete(Order entity, User operator) {
        System.out.println("删除订单。");
        return 1;
    }
}
