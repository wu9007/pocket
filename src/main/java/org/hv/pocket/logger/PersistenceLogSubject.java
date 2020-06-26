package org.hv.pocket.logger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hv.pocket.logger.view.PersistenceMirrorView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wujianchuan
 */
public class PersistenceLogSubject {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final PersistenceLogSubject INSTANCE = new PersistenceLogSubject();
    private final List<PersistenceLogObserver> observers = new ArrayList<>();

    private PersistenceLogSubject() {
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public static PersistenceLogSubject getInstance() {
        return INSTANCE;
    }

    public void registerObserver(PersistenceLogObserver persistenceLogObserver) {
        this.observers.add(persistenceLogObserver);
    }

    public void pushLog(String sql, List<?> beforeMirror, List<?> afterMirror, long milliseconds) {
        this.notifyObservers(new PersistenceMirrorView(sql, beforeMirror, afterMirror, milliseconds));
    }

    private void notifyObservers(PersistenceMirrorView log) {
        for (PersistenceLogObserver observer : this.observers) {
            try {
                observer.dealWithPersistenceLog(this.objectMapper.writeValueAsString(log));
            } catch (JsonProcessingException e) {
                this.logger.error(e.getMessage());
            }
        }
    }
}
