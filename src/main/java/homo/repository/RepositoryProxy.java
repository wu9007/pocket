package homo.repository;


import homo.model.Entity;
import homo.observe.evens.ModelSaveEven;
import org.springframework.context.ApplicationContext;

/**
 * @author wujianchuan 2018/12/26
 */
public class RepositoryProxy implements AbcRepository {

    private ApplicationContext context;
    private AbstractRepository repository;

    void setRepository(ApplicationContext context, AbstractRepository repository) {
        this.repository = repository;
        this.context = context;
    }

    @Override
    public int save(Entity entity) {
        int affected = this.repository.save(entity);
        context.publishEvent(new ModelSaveEven(entity));
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
