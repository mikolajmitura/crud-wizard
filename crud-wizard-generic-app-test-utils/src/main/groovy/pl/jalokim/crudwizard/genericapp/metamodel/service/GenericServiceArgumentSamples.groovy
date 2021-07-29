package pl.jalokim.crudwizard.genericapp.metamodel.service

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import pl.jalokim.crudwizard.core.datastorage.RawEntityObject
import pl.jalokim.crudwizard.genericapp.service.GenericServiceArgument
import pl.jalokim.utils.test.DataFakerHelper
import spock.mock.DetachedMockFactory

class GenericServiceArgumentSamples {

    static final MOCK_FACTORY = new DetachedMockFactory()

    /**
     * Input means that GenericServiceArgument will be without translated fields.
     */
    static GenericServiceArgument createInputGenericServiceArgument(HttpServletRequest request = MOCK_FACTORY.Mock(HttpServletRequest),
        HttpServletResponse response = MOCK_FACTORY.Mock(HttpServletResponse)) {

        def requestBody = [
            name   : DataFakerHelper.randomText(),
            surname: DataFakerHelper.randomText()
        ]
        def headers = [
            cookie: DataFakerHelper.randomText()
        ]
        def httpQueryParams = [
            queryParam: DataFakerHelper.randomText()
        ]

        GenericServiceArgument.builder()
            .requestBody(RawEntityObject.fromMap(requestBody))
            .httpQueryParams(RawEntityObject.fromMap(httpQueryParams))
            .headers(headers)
            .request(request)
            .response(response)
            .build()
    }
}
