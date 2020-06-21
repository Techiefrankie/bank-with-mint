package com.bankwithmint.cardschemeproducer.repository;

import com.bankwithmint.cardschemeproducer.dao.CardScheme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardSchemeRepository extends JpaRepository<CardScheme, Long> {
    Iterable<CardScheme> findByBin(Long bin);
}
