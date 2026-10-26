package com.bazar.integracoes.webhooks;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ConfiguracaoDeWebhooks.class)
class ConfiguracaoDasEntregas {
}
