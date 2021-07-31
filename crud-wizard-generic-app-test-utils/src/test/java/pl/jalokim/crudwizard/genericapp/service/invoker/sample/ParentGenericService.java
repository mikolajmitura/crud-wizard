package pl.jalokim.crudwizard.genericapp.service.invoker.sample;

import javax.websocket.server.PathParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

public class ParentGenericService<I, R> {

    public R processObject(@Validated @RequestBody I input, @PathParam("id") Long id) {
        return null;
    }
}
