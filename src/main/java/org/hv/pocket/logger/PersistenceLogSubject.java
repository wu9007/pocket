package org.hv.pocket.logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    public static PersistenceLogSubject getInstance() {
        return INSTANCE;
    }

    public void registerObserver(PersistenceLogObserver persistenceLogObserver) {
        this.observers.add(persistenceLogObserver);
    }

    public void pushLog(String sql, List<?> beforeMirror, List<?> afterMirror) {
        this.notifyObservers(new PersistenceLogView(sql, beforeMirror, afterMirror));
    }

    private void notifyObservers(PersistenceLogView log) {
        for (PersistenceLogObserver observer : this.observers) {
            try {
                observer.dealWithPersistenceLog(this.objectMapper.writeValueAsString(log));
            } catch (JsonProcessingException e) {
                this.logger.error(e.getMessage());
            }
        }
    }

    private static class PersistenceLogView {
        private String sql;
        private List<?> beforeMirror;
        private List<?> afterMirror;

        public PersistenceLogView(String sql, List<?> beforeMirror, List<?> afterMirror) {
            this.sql = sql;
            this.beforeMirror = beforeMirror;
            this.afterMirror = afterMirror;
        }

        public PersistenceLogView() {
        }

        public void setSql(String sql) {
            this.sql = sql;
        }

        public void setBeforeMirror(List<?> beforeMirror) {
            this.beforeMirror = beforeMirror;
        }

        public void setAfterMirror(List<?> afterMirror) {
            this.afterMirror = afterMirror;
        }

        public String getSql() {
            return sql;
        }

        public List<?> getBeforeMirror() {
            return beforeMirror;
        }

        public List<?> getAfterMirror() {
            return afterMirror;
        }
    }
}
