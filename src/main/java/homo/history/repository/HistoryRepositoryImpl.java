package homo.history.repository;

import homo.history.model.History;
import org.springframework.stereotype.Repository;

/**
 * @author wujianchuan 2018/12/27
 */
@Repository
public class HistoryRepositoryImpl implements HistoryRepository {

    @Override
    public int save(History entity) {
        System.out.println(entity.getDescribe());
        return 1;
    }
}
