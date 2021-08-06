package pl.jalokim.crudwizard.genericapp.metamodel.context;

import static javax.persistence.EnumType.STRING;

import java.time.OffsetDateTime;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import pl.jalokim.crudwizard.genericapp.metamodel.BaseEntity;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "meta_model_context_refresh_entity")
public class MetaModelContextRefreshEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    String refreshReason;
    OffsetDateTime refreshDateTime;

    @Enumerated(STRING)
    ContextRefreshStatus contextRefreshStatus;

    String errorReason;

    public static MetaModelContextRefreshEntity refreshError(String refreshReason, OffsetDateTime refreshDateTime, Throwable errorReason) {
        return MetaModelContextRefreshEntity.builder()
            .refreshReason(refreshReason)
            .refreshDateTime(refreshDateTime)
            .errorReason(errorReason.getMessage())
            .contextRefreshStatus(ContextRefreshStatus.INVALID)
            .build();
    }

    public static MetaModelContextRefreshEntity refreshCorrect(String refreshReason, OffsetDateTime refreshDateTime) {
        return MetaModelContextRefreshEntity.builder()
            .refreshReason(refreshReason)
            .refreshDateTime(refreshDateTime)
            .contextRefreshStatus(ContextRefreshStatus.CORRECT)
            .build();
    }
}
