package org.homo.core.launch;

import org.homo.core.repository.AbstractRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author wujianchuan 2019/1/12
 */
@Component
@Order(value = 1)
public class RepositoryLauncher implements CommandLineRunner {
    private final
    Map<String, AbstractRepository> repositoryMap;

    @Autowired
    public RepositoryLauncher(Map<String, AbstractRepository> repositoryMap) {
        this.repositoryMap = repositoryMap;
    }

    @Override
    public void run(String... args) throws Exception {
        repositoryMap.forEach((key, value) -> value.installSession());
    }
}
