package pl.jalokim.crudwizard.genericapp.metamodel.context;

import static pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextRefreshEntity.refreshCorrect;
import static pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextRefreshEntity.refreshError;

import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MetaModelContextRefreshListener {

    private final MetaModelContextService metaModelContextService;
    private final MetaModelContextRefreshRepository metaModelContextRefreshRepository;

    @EventListener
    public void refresh(MetaModelContextRefreshEvent metaModelContextRefreshEvent) {
        String refreshReason = metaModelContextRefreshEvent.getReason();
        OffsetDateTime refreshDateTime = metaModelContextRefreshEvent.getRefreshDateTime();
        try {
            metaModelContextService.reloadAll();
            metaModelContextRefreshRepository.save(refreshCorrect(refreshReason, refreshDateTime));
            log.info("Finished refresh  meta model context with reason: {}", metaModelContextRefreshEvent.getReason());
        } catch (Throwable ex) {
            metaModelContextRefreshRepository.save(refreshError(refreshReason, refreshDateTime, ex));
            log.error("Refresh meta model context with reason: " + metaModelContextRefreshEvent.getReason() + " finished with error", ex);
            throw ex;
        }
    }
}
