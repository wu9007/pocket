package homo.history.repository;

import homo.history.model.History;
import homo.repository.AbstractRepository;
import org.springframework.stereotype.Repository;

/**
 * @author wujianchuan 2018/12/27
 */
@Repository
public class HistoryRepositoryImpl extends AbstractRepository<History> {

    @Override
    public int save(History entity) {
        System.out.println("保存历史数据：" + entity.getDescribe());
        return 1;
    }

    @Override
    public int update(History entity) {
        return 0;
    }

    @Override
    public int delete(History entity) {
        return 0;
    }
}
