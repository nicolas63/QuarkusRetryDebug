package org.acme;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.security.SecureRandom;
import java.time.Duration;

@Path("/retry")
public class RetryResource {

    @Inject
    public Logger log;

    private static SecureRandom rdm = new SecureRandom();

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() throws InterruptedException {
        log.info("begin");
        try {
            return returnAFailure()
                    .onItem().invoke(() -> {
                                log.info("this works");
                                // throw new RuntimeException("Failure");
                            }
                    )
                    .onFailure().invoke(() -> {
                        log.info("this fails");
                    })
                    .onFailure().retry().atMost(3).await().atMost(Duration.ofMinutes(1));
        }catch (RuntimeException e){
            log.error(e);
            return "Fail";
        }
    }

    public Uni<String> returnAFailure() throws InterruptedException {
        Integer rd = rdm.nextInt(5);
        if(rd < 3){

            log.info("Im failing on " + Thread.currentThread().getName());
            Thread.sleep(1000);
            return Uni.createFrom().failure(new RuntimeException("Failure"));
        }

        return Uni.createFrom().item("Result");
    }
}