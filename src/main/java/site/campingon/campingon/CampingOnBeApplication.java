package site.campingon.campingon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
@EnableAspectJAutoProxy
public class CampingOnBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampingOnBeApplication.class, args);
    }

}
