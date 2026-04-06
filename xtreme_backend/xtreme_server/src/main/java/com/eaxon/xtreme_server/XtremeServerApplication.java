package com.eaxon.xtreme_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(
        scanBasePackages = {
                "com.eaxon.xtreme_common",
                "com.eaxon.xtreme_server"
        }
)
public class XtremeServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(XtremeServerApplication.class, args);
    }

}
