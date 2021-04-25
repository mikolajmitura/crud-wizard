package pl.jalokim.crudwizard.core.config.jackson;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = SimplePageImpl.class)
public interface PageMixIn {

}
