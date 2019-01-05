package org.homo.orderdemo.repository;

import org.homo.authority.model.User;
import org.homo.dbconnect.session.InventoryFactory;
import org.homo.dbconnect.session.InventoryManager;
import org.homo.orderdemo.model.Order;
import org.homo.core.repository.AbstractRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * @author wujianchuan 2018/12/26
 */
@Repository
public class OrderRepositoryImpl extends AbstractRepository<Order> implements OrderRepository {

    private final
    InventoryFactory inventoryFactory;

    InventoryManager inventoryManager;

    @Autowired
    public OrderRepositoryImpl(InventoryFactory inventoryFactory) {
        this.inventoryFactory = inventoryFactory;
        this.inventoryManager = this.inventoryFactory.getManager("order");
    }

    @Override
    public Order save(Order entity, User operator) throws SQLException, IllegalAccessException {
        return (Order) inventoryManager.save(entity);
    }

    @Override
    public Order update(Order entity, User operator) {
        return entity;
    }

    @Override
    public int delete(Order entity, User operator) {
        return 1;
    }


    @Override
    public Order findOne(long uuid) throws IllegalAccessException, SQLException, InstantiationException {
        return (Order) inventoryManager.findOne(Order.class, uuid);
    }
}
