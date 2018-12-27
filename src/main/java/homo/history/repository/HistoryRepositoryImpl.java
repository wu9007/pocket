package homo.history.repository;

import homo.common.model.Entity;
import homo.repository.AbstractRepository;
import org.springframework.stereotype.Repository;

/**
 * @author wujianchuan 2018/12/27
 */
@Repository
public class HistoryRepositoryImpl extends AbstractRepository {

    @Override
    public int save(Entity entity) {
        System.out.println("保存历史数据：" + entity.getDescribe());
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
