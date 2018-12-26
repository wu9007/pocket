package homo.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * @author wujianchuan 2018/12/26
 */
public abstract class AbstractRepository {
    @Autowired
    ApplicationContext context;

    public RepositoryProxy getProxy() {
        RepositoryProxy proxy = new RepositoryProxy();
        proxy.setRepository(context, this);
        return proxy;
    }

    public int save() {
        return 0;
    }

    public int update() {
        return 0;
    }

    public int delete() {
        return 0;
    }

}
