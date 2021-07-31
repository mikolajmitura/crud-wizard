package pl.jalokim.crudwizard.genericapp.metamodel.service

import static pl.jalokim.crudwizard.core.datastorage.RawEntityObjectSamples.createHttpQueryParams
import static pl.jalokim.crudwizard.core.datastorage.RawEntityObjectSamples.createRequestBody

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
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
        GenericServiceArgument.builder()
            .requestBody(createRequestBody())
            .httpQueryParams(createHttpQueryParams())
            .headers([
                cookie: DataFakerHelper.randomText(),
                responseType: DataFakerHelper.randomText(),
            ])
            .request(request)
            .response(response)
            .build()
    }
}
