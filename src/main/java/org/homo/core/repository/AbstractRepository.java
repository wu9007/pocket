package org.homo.core.repository;

import org.homo.core.model.BaseEntity;
import org.homo.dbconnect.session.InventoryFactory;
import org.homo.dbconnect.session.InventoryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * @author wujianchuan 2018/12/26
 */
public abstract class AbstractRepository<T extends BaseEntity> implements HomoRepository<T> {
    @Autowired
    private ApplicationContext context;

    private RepositoryProxy<T> proxy;

    protected InventoryManager inventoryManager;

    @Autowired
    public AbstractRepository(InventoryFactory inventoryFactory) {
        this.inventoryManager = inventoryFactory.getManager();
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
