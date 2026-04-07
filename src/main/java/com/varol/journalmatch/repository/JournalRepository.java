package com.varol.journalmatch.repository;

import com.varol.journalmatch.entity.Journal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JournalRepository extends JpaRepository<Journal, Long> {
}