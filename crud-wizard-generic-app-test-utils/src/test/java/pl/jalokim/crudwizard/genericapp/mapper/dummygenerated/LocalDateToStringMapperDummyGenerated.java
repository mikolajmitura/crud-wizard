package pl.jalokim.crudwizard.genericapp.mapper.dummygenerated;

import java.time.LocalDateTime;
import pl.jalokim.crudwizard.genericapp.mapper.GenericMapperArgument;
import pl.jalokim.crudwizard.genericapp.mapper.generete.GeneratedMapper;

public class LocalDateToStringMapperDummyGenerated implements GeneratedMapper {

    @Override
    public Object mainMap(GenericMapperArgument genericMapperArgument) {
        LocalDateTime localDateTime = (LocalDateTime) genericMapperArgument.getSourceObject();
        int result =  localDateTime.getYear() + localDateTime.getMonthValue() + localDateTime.getDayOfMonth()
            + localDateTime.getHour() + localDateTime.getMinute();
        return Integer.toString(result);
    }
}
