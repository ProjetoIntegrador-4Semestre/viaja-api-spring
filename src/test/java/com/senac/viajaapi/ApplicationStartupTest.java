package com.senac.viajaapi;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApplicationStartupTest {

    @Autowired
    private LocalRepository localRepository;

    @Test
    void loadsSeedAndBrazilCatalogUsingCanonicalSources() {
        assertThat(localRepository.findAll())
                .hasSize(69)
                .allMatch(local -> local.getSource() == LocalSource.MOCK);
    }
}
