package homo.repository;


import homo.common.model.Entity;
import homo.observe.evens.ModelSaveEven;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wujianchuan 2018/12/26
 */
public class RepositoryProxy implements HomoRepository {

    private ApplicationContext context;
    private AbstractRepository repository;

    void setRepository(ApplicationContext context, AbstractRepository repository) {
        this.repository = repository;
        this.context = context;
    }

    @Override
    public int save(Entity entity) {
        int affected = this.repository.save(entity);

        Class clazz = entity.getClass();
        Map<String, Object> source = new HashMap<>(2);
        source.put("clazz", clazz);
        source.put("entity", entity);
        context.publishEvent(new ModelSaveEven(source));

        return affected;
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
