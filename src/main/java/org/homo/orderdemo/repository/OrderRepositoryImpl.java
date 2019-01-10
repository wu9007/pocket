package org.homo.orderdemo.repository;

import org.homo.authority.model.User;
import org.homo.dbconnect.inventory.InventoryFactory;
import org.homo.orderdemo.model.Order;
import org.homo.core.repository.AbstractRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

/**
 * @author wujianchuan 2018/12/26
 */
@Repository
public class OrderRepositoryImpl extends AbstractRepository<Order> implements OrderRepository {

    public OrderRepositoryImpl(InventoryFactory inventoryFactory) {
        super(inventoryFactory);
    }

    @Override
    public Order save(Order entity, User operator) throws Exception {
        return (Order) this.inventoryManager.save(entity);
    }

    @Override
    public Order update(Order entity, User operator) throws Exception {
        return (Order) this.inventoryManager.update(entity);
    }

    @Override
    public int delete(Order entity, User operator) throws Exception {
        return this.inventoryManager.delete(entity);
    }

    @Override
    @Cacheable(value = "homo", key = "#root.method.getReturnType().getName()+#uuid")
    public Order findOne(long uuid) throws Exception {
        return (Order) inventoryManager.findOne(Order.class, uuid);
    }
}
