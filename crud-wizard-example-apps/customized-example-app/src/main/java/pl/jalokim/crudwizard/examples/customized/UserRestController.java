package pl.jalokim.crudwizard.examples.customized;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.jalokim.crudwizard.examples.customized.repo.UserEntity;
import pl.jalokim.crudwizard.examples.customized.repo.UserRepository;

@RestController
@RequestMapping("/custom/users")
@Transactional
@RequiredArgsConstructor
@Api(tags = "users")
public class UserRestController {

    private final UserRepository userRepository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("add user")
    public Long addUser(@RequestBody UserEntity user) {
        return userRepository.save(user).getId();
    }

    @GetMapping
    @ApiOperation("get all users")
    public List<UserEntity> getAll() {
        return userRepository.findAllByOrderById();
    }
}
