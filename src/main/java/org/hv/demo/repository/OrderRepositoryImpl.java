package org.hv.demo.repository;

import org.hv.demo.model.Order;
import org.hv.pocket.criteria.Criteria;
import org.hv.pocket.criteria.Restrictions;
import org.hv.pocket.session.Session;
import org.hv.pocket.session.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author wujianchuan
 */
@Repository
public class OrderRepositoryImpl implements OrderRepository {
    @Override
    public List<Order> loadByCode(String code) {
        Session session = SessionFactory.getSession("order");
        session.open();
        Criteria criteria = session.createCriteria(Order.class)
                .add(Restrictions.equ("code", "C-001"));
        List<Order> orders = criteria.listNotCleanRestrictions();
        session.close();
        return orders;
    }
}
