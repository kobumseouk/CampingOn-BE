package site.campingon.campingon.common.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

@EntityListeners(AuditingEntityListener.class)
@Getter
@MappedSuperclass
public class BaseEntity {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "DATETIME(0)")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "modified_at", nullable = false, columnDefinition = "DATETIME(0)")
    private LocalDateTime modifiedAt;
}
