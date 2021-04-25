package pl.jalokim.crudwizard.core.exception.handler;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import pl.jalokim.crudwizard.core.validation.javax.groups.CreateContext;
import pl.jalokim.crudwizard.core.validation.javax.groups.UpdateContext;

@Value
@Builder
@AllArgsConstructor
public class SomeBean {

    @NotNull(groups = UpdateContext.class)
    Long id;
    @NotNull
    String name;
    @NotNull
    String surName;
    String optional;
    @NotNull(groups = CreateContext.class)
    String pesel;
    @Valid SomeBean someBean;
    String first;
    String second;
}
