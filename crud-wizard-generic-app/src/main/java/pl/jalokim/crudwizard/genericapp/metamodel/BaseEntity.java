package pl.jalokim.crudwizard.genericapp.metamodel;

import java.time.OffsetDateTime;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import pl.jalokim.crudwizard.core.exception.ResourceChangedException;

@Getter
@EqualsAndHashCode(callSuper = true)
@MappedSuperclass
public abstract class BaseEntity extends EntityWithId<Long> {

    @Version
    protected Long version;

    @CreatedDate
    private OffsetDateTime creationDateTime;

    @LastModifiedDate
    private OffsetDateTime modificationDateTime;

    @LastModifiedBy
    private String modifiedBy;

    @CreatedBy
    private String createdBy;

    public boolean isInvalidVersion(Long version) {
        return this.version == null || !this.version.equals(version);
    }

    public void validateVersion(Long version) {
        if (isInvalidVersion(version)) {
            throw new ResourceChangedException();
        }
    }
}
