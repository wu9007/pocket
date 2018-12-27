package homo.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * @author wujianchuan 2018/12/26
 */
public abstract class AbstractRepository implements HomoRepository {
    @Autowired
    private ApplicationContext context;

    private RepositoryProxy proxy;

    public RepositoryProxy getProxy() {
        if (this.proxy != null) {
            return this.proxy;
        } else {
            this.proxy = new RepositoryProxy();
            proxy.setRepository(context, this);
        }
        return this.proxy;
    }
}
