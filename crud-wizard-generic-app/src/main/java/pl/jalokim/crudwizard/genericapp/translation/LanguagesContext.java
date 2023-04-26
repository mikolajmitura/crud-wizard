package pl.jalokim.crudwizard.genericapp.translation;

import java.util.Map;
import lombok.Value;

@Value
public class LanguagesContext {

    Map<String, String> allLanguages;
}
