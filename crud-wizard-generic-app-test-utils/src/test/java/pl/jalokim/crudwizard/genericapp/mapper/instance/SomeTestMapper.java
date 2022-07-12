package pl.jalokim.crudwizard.genericapp.mapper.instance;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import pl.jalokim.crudwizard.genericapp.mapper.GenericMapperArgument;
import pl.jalokim.crudwizard.genericapp.mapper.conversion.SomeEnum1;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModel;
import pl.jalokim.crudwizard.genericapp.rest.samples.dto.SomeRawDto;
import pl.jalokim.crudwizard.genericapp.service.results.JoinedResultsRow;

@Component
public class SomeTestMapper {

    public List<SomeRawDto> mapperMethod1(@RequestBody Map<String, Long> arg1,
        @RequestHeader Object headers,
        @RequestHeader Map<String, Object> headers1,
        @RequestHeader Map<String, Long> headers2,
        @RequestHeader("x-value") Object headersXValue,
        @RequestParam("name") Object requestParamName,
        @RequestParam Object RequestParam,
        @PathVariable Object pathVars,
        EndpointMetaModel endpointMetaModel,
        HttpServletRequest httpServletRequest,
        Long invalidInputArgument,
        Object validPayload) {
        return null;
    }

    public List<SomeRawDto> mapperMethod2(LocalDateTime inputArgument) {
        return null;
    }

    public Map<String, Object> mapperMethod3(Map<String, Object> inputAsGenericMap,
        Object inputAsRawObject,
        @RequestHeader Map<String, String> headers1,
        @RequestBody JsonNode jsonNode,
        EndpointMetaModel endpointMetaModel,
        HttpServletRequest httpServletRequest) {
        return null;
    }

    public Map<String, Object> mapperFinalValid1(Map<String, Object> mapWithIds) {
        return null;
    }

    List<SomeRawDto> mapperMethodInvalid1(Long input) {
        return null;
    }

    Map<String, Object> mapperMethodValid1(List<Map<String, Object>> argument) {
        return null;
    }

    List<SomeRawDto> mapperMethodInvalid2(Map<String, Object> input) {
        return null;
    }

    List<SomeRawDto> mapperMethodInvalid3(Long input) {
        return null;
    }

    Map<String, Object> mapperMethodValid3(SomeRawDto input) {
        return null;
    }

    Map<String, Map<String, Object>> mapperFinalValid2(Map<String, Object> input) {
        return null;
    }

    Map<String, Object> mapperMethodValid4(List<SomeRawDto> input) {
        return null;
    }

    public Long mapperFinalValid3(Map<String, Object> mapWithIds) {
        return null;
    }

    List<SomeRawDto> mapperMethodInvalid4(Map<String, Object> input) {
        return null;
    }

    SomeRawDto mapperFinalInvalid1(Long input) {
        return null;
    }

    SomeRawDto mapperMethodValid5(Long input) {
        return null;
    }

    Long mapperFinalValid4(Map<String, Object> input) {
        return null;
    }

    Map<String, Object> mapperMethodInvalid6(Map<String, Long> input) {
        return null;
    }

    List<SomeRawDto> mapperFinalInvalid6(Long input) {
        return null;
    }

    SomeRawDto mapperMethodValid6(String input) {
        return null;
    }

    Long mapperFinalValid6(Map<String, Object> input) {
        return null;
    }

    SomeRawDto mapperMethodValid7(Map<String, Object> input) {
        return null;
    }

    public Map<String, Object> mapperMethodValid8(SomeEnum1 someEnum1) {
        return null;
    }

    SomeEnum1 mapperFinalValid8(Map<String, Object> input) {
        return null;
    }

    Long mapperMethodInvalid9(Map<String, String> input) {
        return null;
    }

    List<SomeRawDto> mapperFinalInvalid9(Long input) {
        return null;
    }

    Map<String, Object> mapperMethodValid9(Map<String, Long> input) {
        return null;
    }

    SomeEnum1 mapperFinalValid9(Map<String, Object> input) {
        return null;
    }

    public Map<String, Object> finalMapEntryValid1(SomeRawDto someRawDto) {
        return null;
    }

    public SomeRawDto finalMapEntryInvalid1(Long someLong) {
        return null;
    }

    public SomeRawDto finalMapEntryValid2(JoinedResultsRow joinedResultsRow) {
        return null;
    }

    public Long finalMapEntryInvalid2(Long someLong) {
        return null;
    }

    public SomeRawDto finalMapEntryValid3(Map<String, Object> dsResults) {
        return null;
    }

    public String mapIdValid1(Long id,
        GenericMapperArgument genericMapperArgument,
        @RequestHeader("x-uuid") String uuid) {
        return null;
    }

    public Long mapIdInvalid2(String id) {
        return null;
    }
}
