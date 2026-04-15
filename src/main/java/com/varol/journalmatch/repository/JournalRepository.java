package com.varol.journalmatch.repository;

import com.varol.journalmatch.entity.Journal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JournalRepository extends JpaRepository<Journal, Long> {
    List<Journal> findAllByName(String name);
}