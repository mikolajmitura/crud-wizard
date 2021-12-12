package pl.jalokim.crudwizard.core.datastorage.query;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@EqualsAndHashCode(callSuper = false)
public class NegatedExpression extends AbstractExpression {

    @Getter
    private final AbstractExpression realExpression;

    @Override
    LinkedExpression getLinkedExpression() {
        return new LinkedExpression(this);
    }
}
