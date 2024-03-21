package com.nycdata.fetcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Ny511FetcherApplication {

    public static void main(String[] args) {
        SpringApplication.run(Ny511FetcherApplication.class, args);
    }

}
