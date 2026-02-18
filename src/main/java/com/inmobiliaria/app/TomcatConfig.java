package com.inmobiliaria.app;

import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatConfig {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
        return factory -> factory.addConnectorCustomizers(connector -> {
            connector.setMaxPostSize(1024 * 1024 * 1024); // 1 GB
            if (connector.getProtocolHandler() instanceof Http11NioProtocol protocol) {
                protocol.setMaxSwallowSize(1024 * 1024 * 1024); // 1 GB
            }
        });
    }
}
