package org.hv.pocket.lunch;

import org.hv.pocket.config.DatabaseConfig;
import org.hv.pocket.config.DatabaseNodeConfig;
import org.hv.pocket.connect.ConnectionManager;
import org.hv.pocket.constant.EncryptType;
import org.hv.pocket.exception.PocketMapperException;
import org.hv.pocket.identify.IdentifyGenerator;
import org.hv.pocket.identify.IdentifyGeneratorFactory;
import org.hv.pocket.model.MapperFactory;
import org.hv.pocket.session.SessionFactory;
import org.hv.pocket.utils.EncryptUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @author wujianchuan 2019/1/12
 */
@Component
public class PocketConfigDefault implements PocketConfig {
    private final DatabaseConfig databaseConfig;
    private final ApplicationContext context;
    private final List<IdentifyGenerator> identifyGeneratorList;

    @Autowired
    public PocketConfigDefault(DatabaseConfig databaseConfig, List<IdentifyGenerator> identifyGeneratorList, ApplicationContext context) {
        this.databaseConfig = databaseConfig;
        this.identifyGeneratorList = identifyGeneratorList;
        this.context = context;
    }

    @Override
    public void init() throws PocketMapperException {
        this.initConnectionManager();
        this.initSessionFactory();
        this.initIdentifyGenerator();
        MapperFactory.init(context);
    }

    @Override
    public PocketConfig setDesKey(String desKey) {
        EncryptUtil.setDesKey(desKey);
        return this;
    }

    @Override
    public PocketConfig setSm4Key(String sm4Key) {
        EncryptUtil.setSm4Key(sm4Key);
        for (DatabaseNodeConfig databaseNodeConfig : databaseConfig.getNode()) {
            if (StringUtils.isEmpty(databaseNodeConfig.getUser())) {
                databaseNodeConfig.setUser(EncryptUtil.decrypt(EncryptType.SM4_CEB, databaseNodeConfig.getEncryptedUser()));
            }
            if (StringUtils.isEmpty(databaseNodeConfig.getPassword())) {
                databaseNodeConfig.setPassword(EncryptUtil.decrypt(EncryptType.SM4_CBC, databaseNodeConfig.getEncryptedPassword()));
            }
        }
        return this;
    }

    /**
     * Initializes the connection pool for each node of the database
     */
    private void initConnectionManager() {
        ConnectionManager connectionManager = ConnectionManager.getInstance();
        connectionManager.register(databaseConfig);
    }

    /**
     * Register the database configuration corresponding to each Session name
     * and create a corresponding cache
     */
    private void initSessionFactory() {
        SessionFactory.register(databaseConfig);
    }

    /**
     * Initializes all ID generators
     */
    private void initIdentifyGenerator() {
        IdentifyGeneratorFactory identifyGeneratorFactory = IdentifyGeneratorFactory.getInstance();
        this.identifyGeneratorList.forEach(identifyGenerator -> {
            identifyGenerator.setGeneratorId();
            identifyGeneratorFactory.registerGenerator(identifyGenerator);
        });
    }
}
