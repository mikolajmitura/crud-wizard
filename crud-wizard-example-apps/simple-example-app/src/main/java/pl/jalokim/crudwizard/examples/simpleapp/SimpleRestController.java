package pl.jalokim.crudwizard.examples.simpleapp;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/simple/users")
@Transactional
@RequiredArgsConstructor
public class SimpleRestController {

    @GetMapping
    public List<String> getAll() {
        return List.of("user1", "user2");
    }
}
