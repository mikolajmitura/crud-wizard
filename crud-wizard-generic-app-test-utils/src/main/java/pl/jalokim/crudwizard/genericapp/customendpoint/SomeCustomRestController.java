package pl.jalokim.crudwizard.genericapp.customendpoint;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(tags = "simple-rest")
@RequestMapping("some-endpoint/{firstId}")
public class SomeCustomRestController {

    @PostMapping("/second-part/{secondId}")
    @ApiOperation("somePost")
    public void somePost(@PathVariable Long firstId, @PathVariable String secondId) {

    }

    @PutMapping("/second-part/{secondId}")
    @ApiOperation("somePost")
    public void somePut(@PathVariable Long firstId, @PathVariable String secondId) {

    }

    @GetMapping
    @ApiOperation("somePost")
    public void someGet(@PathVariable Long firstId) {

    }
}
