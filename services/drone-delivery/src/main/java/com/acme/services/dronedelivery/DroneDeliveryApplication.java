package com.acme.services.dronedelivery;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(
    info =
        @Info(
            title = "Drone Delivery Management Service",
            description =
                "This application manages drone delivery operations including job assignments, "
                    + "order tracking, and drone status management. Supports authentication via JWT "
                    + "for admin, enduser, and drone roles.",
            license = @License(name = "Apache 2.0", url = "https://acmes.io"),
            contact =
                @Contact(
                    name = "Acme Solutions",
                    email = "contact@acmes.io",
                    url = "https://acme.io")))
@SpringBootApplication
public class DroneDeliveryApplication {

  public static void main(String[] args) {
    SpringApplication.run(DroneDeliveryApplication.class, args);
  }
}
