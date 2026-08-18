package com.vishnu.urlshortener.link.persistence;

import com.vishnu.urlshortener.link.domain.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface LinkRepository extends JpaRepository<Link, Long> {

    Optional<Link> findByShortCode(String shortCode);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            update links
            set click_count = click_count + 1,
                last_accessed_at = :accessedAt
            where short_code = :shortCode
            """, nativeQuery = true)
    int incrementAccessStats(@Param("shortCode") String shortCode, @Param("accessedAt") Instant accessedAt);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            delete from links
            where short_code = :shortCode
            """, nativeQuery = true)
    int deleteByShortCode(@Param("shortCode") String shortCode);
}
