package org.homo.core.repository;

import org.homo.core.annotation.Repository;
import org.homo.core.model.BaseEntity;
import org.homo.dbconnect.inventory.SessionFactory;
import org.homo.dbconnect.inventory.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * @author wujianchuan 2018/12/26
 */
public abstract class AbstractRepository<T extends BaseEntity> implements HomoRepository<T> {
    @Autowired
    private ApplicationContext context;

    private RepositoryProxy<T> proxy;

    protected Session inventoryManager;

    public AbstractRepository() {
        Repository repository = this.getClass().getAnnotation(Repository.class);
        this.inventoryManager = SessionFactory.getSession(repository.database(), repository.session());
    }

    public RepositoryProxy<T> getProxy() {
        if (this.proxy != null) {
            return this.proxy;
        } else {
            this.proxy = new RepositoryProxy<>();
            proxy.setRepository(context, this);
        }
        return this.proxy;
    }
}
