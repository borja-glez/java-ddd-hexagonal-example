package com.borjaglez.dddlab.ordering.infrastructure.out.payment;

import com.legacypay.sdk.LegacyPayClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** El SDK de terceros se registra aquí, dentro de la ACL: es el único paquete que lo conoce. */
@Configuration(proxyBeanMethods = false)
class LegacyPayConfiguration {

  @Bean
  LegacyPayClient legacyPayClient() {
    return new LegacyPayClient();
  }
}
