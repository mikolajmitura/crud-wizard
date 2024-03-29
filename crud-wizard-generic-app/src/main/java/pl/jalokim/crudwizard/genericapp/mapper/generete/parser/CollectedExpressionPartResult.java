package pl.jalokim.crudwizard.genericapp.mapper.generete.parser;

import lombok.Value;

@Value
public class CollectedExpressionPartResult {

    String collectedText;
    char cutWithText;

    public boolean isCutByEof() {
        return cutWithText == '\n';
    }
}
