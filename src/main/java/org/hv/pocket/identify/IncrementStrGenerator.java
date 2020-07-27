package org.hv.pocket.identify;

import org.hv.pocket.model.AbstractEntity;
import org.hv.pocket.session.Session;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * @author wujianchuan 2019/1/2
 */
@Component
public class IncrementStrGenerator extends IncrementLongGenerator {

    @Override
    public void setGeneratorId() {
        this.generationType = GenerationType.STR_INCREMENT;
    }

    @Override
    public Serializable getIdentify(Class<? extends AbstractEntity> clazz, Session session) {
        return String.valueOf(super.getIdentify(clazz, session));
    }
}
