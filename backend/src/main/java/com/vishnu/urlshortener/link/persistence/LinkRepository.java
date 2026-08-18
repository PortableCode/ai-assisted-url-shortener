package com.vishnu.urlshortener.link.persistence;

import com.vishnu.urlshortener.link.domain.Link;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LinkRepository extends JpaRepository<Link, Long> {

    Optional<Link> findByShortCode(String shortCode);
}
