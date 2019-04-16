package org.hunter.demo.repository;

import org.hunter.demo.model.Order;
import org.hunter.pocket.annotation.Cache;
import org.hunter.pocket.criteria.Criteria;
import org.hunter.pocket.criteria.Restrictions;
import org.hunter.pocket.session.Session;
import org.hunter.pocket.session.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author wujianchuan
 */
@Repository
public class OrderRepositoryImpl implements OrderRepository {
    @Override
    @Cache(key = "'order code - ' + #code")
    public List<Order> loadByCode(String code) {
        Session session = SessionFactory.getSession("order");
        session.open();
        Criteria criteria = session.createCriteria(Order.class)
                .add(Restrictions.equ("code", "C-001"));
        List<Order> orders = criteria.list();
        session.close();
        return orders;
    }
}
