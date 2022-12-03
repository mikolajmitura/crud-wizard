package pl.jalokim.crudwizard.genericapp.metamodel.context;

import java.util.Objects;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TemporaryModelContextHolder {

    private static final ThreadLocal<TemporaryMetaModelContext> TEMPORARY_META_MODEL_CONTEXT_THREAD_LOCAL = new ThreadLocal<>();

    public static TemporaryMetaModelContext getTemporaryMetaModelContext() {
        return Objects.requireNonNull(TEMPORARY_META_MODEL_CONTEXT_THREAD_LOCAL.get(),
            "TEMPORARY_META_MODEL_CONTEXT_THREAD_LOCAL should be created already");
    }

    public static void clearTemporaryMetaModelContext() {
        TEMPORARY_META_MODEL_CONTEXT_THREAD_LOCAL.set(null);
    }

    public static boolean isTemporaryContextExists() {
        return TEMPORARY_META_MODEL_CONTEXT_THREAD_LOCAL.get() != null;
    }

    public static void setTemporaryContext(TemporaryMetaModelContext temporaryContext) {
        TEMPORARY_META_MODEL_CONTEXT_THREAD_LOCAL.set(temporaryContext);
    }

    public static Long getSessionTimeStamp() {
        return getTemporaryMetaModelContext().getSessionTimestamp();
    }
}
