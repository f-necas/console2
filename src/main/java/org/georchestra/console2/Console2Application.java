package org.georchestra.console2;

import org.georchestra.console2.bs.ExpiredTokenCleanTask;
import org.georchestra.console2.bs.ExpiredTokenManagement;
import org.georchestra.console2.ds.UserTokenDao;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@ImportResource("classpath:/beans.xml")
public class Console2Application {

    public static void main(String[] args) {
        SpringApplication.run(Console2Application.class, args);
    }



/*    @Bean
    ExpiredTokenManagement expiredTokenManagement() {
        var expiredTokenCleanTask = new ExpiredTokenCleanTask(new UserTokenDao());
        expiredTokenCleanTask.setDelayInMilliseconds(1000);
        var expiredTokenManagement = new ExpiredTokenManagement(expiredTokenCleanTask);
        expiredTokenManagement.setDelayInDays(1);
        return expiredTokenManagement;
    }*/

}
