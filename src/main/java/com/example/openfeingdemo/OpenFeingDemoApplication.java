package com.example.openfeingdemo;

import feign.Feign;
import feign.micrometer.MicrometerCapability;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@EnableFeignClients
@SpringBootApplication
public class OpenFeingDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpenFeingDemoApplication.class, args);
    }


    @Component
    @FeignClient(name = "client", url = "https://postman-echo.com", configuration = MicrometerCapabilityConfig.class)
    public interface EchoClient {

        @GetMapping("/get?foo1=bar1&foo2=bar2")
        String echo();

    }

    @RestController
    public static class HomeController {

        private final SomeService someService;

        public HomeController(SomeService someService) {
            this.someService = someService;
        }

        @RequestMapping("/home")
        public String home() {
            return someService.echo();
        }

    }

    @Service
    public static class SomeService {

        private final EchoClient client;

        public SomeService(EchoClient client) {
            this.client = client;
        }


        @Timed("some-service.echo")
        public String echo() {
            return client.echo();
        }

    }

    @Configuration
    @ConditionalOnClass(MicrometerCapability.class)
    public static class MicrometerCapabilityConfig {

        @Bean
        public Feign.Builder feignBuilder(MeterRegistry meterRegistry) {
            return Feign.builder()
                    .addCapability(new MicrometerCapability(meterRegistry));
        }


    }

}
