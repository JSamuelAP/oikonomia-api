package dev.jsamuelap.oikonomiaapi;

import org.springframework.boot.SpringApplication;

public class TestOikonomiaApiApplication {

  public static void main(String[] args) {
    SpringApplication.from(OikonomiaApiApplication::main).with(TestcontainersConfiguration.class).run(args);
  }

}
