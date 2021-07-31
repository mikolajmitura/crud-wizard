package pl.jalokim.crudwizard.genericapp.rest

import static pl.jalokim.utils.test.DataFakerHelper.randomText

import com.fasterxml.jackson.databind.node.TextNode
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import pl.jalokim.crudwizard.genericapp.service.GenericServiceArgument
import pl.jalokim.crudwizard.genericapp.service.GenericServiceDelegator
import pl.jalokim.utils.test.DataFakerHelper
import spock.lang.Specification

class GenericRestControllerTest extends Specification {

    private GenericServiceDelegator genericServiceDelegator = Mock()
    private HttpServletRequest request = Mock()
    private HttpServletResponse response = Mock()
    private GenericRestController genericRestController = new GenericRestController(genericServiceDelegator)

    def "should invoke GenericServiceDelegator with expected GenericServiceArgument object"() {
        def jsonNode = new TextNode(randomText())
        def headers = [
            cookie: randomText()
        ]
        def httpQueryParams = null

        def responseEntity = ResponseEntity.created()
            .body(DataFakerHelper.randomLong())

        when:
        def result = genericRestController.invokeHttpMethod(
            jsonNode,
            httpQueryParams,
            headers,
            request,
            response
        )

        then:
        result == responseEntity
        1 * genericServiceDelegator.findAndInvokeHttpMethod(
            GenericServiceArgument.builder()
                .requestBody(jsonNode)
                .httpQueryParams(null)
                .headers(headers)
                .request(request)
                .response(response)
                .build()) >> responseEntity
    }
}
