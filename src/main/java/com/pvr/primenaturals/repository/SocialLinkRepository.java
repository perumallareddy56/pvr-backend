package com.pvr.primenaturals.repository;

import com.pvr.primenaturals.entity.SocialLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SocialLinkRepository extends JpaRepository<SocialLink, Long> {
    List<SocialLink> findByActiveTrue();
    Optional<SocialLink> findByPlatformIgnoreCase(String platform);
}
