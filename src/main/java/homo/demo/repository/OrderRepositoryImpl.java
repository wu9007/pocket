package homo.demo.repository;

import homo.model.Entity;
import homo.repository.AbstractRepository;
import org.springframework.stereotype.Repository;

/**
 * @author wujianchuan 2018/12/26
 */
@Repository
public class OrderRepositoryImpl extends AbstractRepository {

    @Override
    public int save(Entity entity) {
        System.out.println("保存订单。");
        return 0;
    }

    @Override
    public int update(Entity entity) {
        return 0;
    }

    @Override
    public int delete(Entity entity) {
        return 0;
    }
}
