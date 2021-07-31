package pl.jalokim.crudwizard.examples.simpleapp;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.Map;
import lombok.Data;
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

    @PostMapping("/by-list")
    @ApiOperation("create user by list")
    public void createByList(@RequestBody List<UserDto> userDto) {
        log.info("createByList: {}", userDto);
    }

    @PostMapping("/by-map-list")
    @ApiOperation("create user by map list")
    public void createByMapList(@RequestBody Map<String, List<UserDto>> userDto) {
        log.info("createByMapList: {}", userDto);
    }

    @PostMapping("/by-map-list-with-some-genericval")
    @ApiOperation("create user by map list with some generic value")
    public void createByMapListWithGenValue(@RequestBody Map<String, List<SomeGenericValue<UserDto, String>>> userDto) {
        log.info("createByMapList: {}", userDto);
    }

    @PostMapping("/raw-string")
    @ApiOperation("create user by raw string")
    public void createByRawJson(@RequestBody String raw) {
        log.info("createByMapList: {}", raw);
    }

    @PostMapping("/consume-json-node")
    @ApiOperation("create user by json node")
    public void consumeJsonNode(@RequestBody JsonNode jsonNode) {
        log.info("json node: {}", jsonNode);
    }

    @Data
    public static class SomeGenericValue<T, B> {
        T tType;
        B bType;
    }
}
