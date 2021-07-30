package pl.jalokim.crudwizard.examples.simpleapp;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/simple/users")
@Transactional
@RequiredArgsConstructor
@Api(tags = "simple-rest")
@Slf4j
public class SimpleRestController {

    @GetMapping
    @ApiOperation("get all")
    public List<String> getAll() {
        return List.of("user1", "user2");
    }

    @PostMapping
    @ApiOperation("create user")
    public void create(@RequestBody UserDto userDto) {
        log.info("create user: {}", userDto);
    }
}
