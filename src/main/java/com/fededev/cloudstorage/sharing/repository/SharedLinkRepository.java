package com.fededev.cloudstorage.sharing.repository;

import com.fededev.cloudstorage.sharing.model.SharedLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface SharedLinkRepository extends JpaRepository<SharedLink, UUID> {

    @Query("""
        SELECT l FROM SharedLink l
        JOIN FETCH l.file
        WHERE l.token = :token
            AND l.revoked = false
            AND l.expiresAt > :now
    """)
    Optional<SharedLink> findValidByToken(String token, Instant now);

}
