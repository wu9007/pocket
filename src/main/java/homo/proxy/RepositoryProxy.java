package homo.proxy;


import homo.demo.repository.AbcRepository;
import homo.demo.repository.AbstractRepository;
import homo.observe.evens.ModelSaveEven;
import org.springframework.context.ApplicationContext;

/**
 * @author wujianchuan 2018/12/26
 */
public class RepositoryProxy implements AbcRepository {

    private ApplicationContext context;
    private AbstractRepository repository;

    public void setRepository(ApplicationContext context, AbstractRepository repository) {
        this.repository = repository;
        this.context = context;
    }

    @Override
    public int save() {
        int affected = this.repository.save();
        context.publishEvent(new ModelSaveEven(""));
        return affected;
    }

    @Override
    public int update() {
        return 0;
    }

    @Override
    public int delete() {
        return 0;
    }
}
