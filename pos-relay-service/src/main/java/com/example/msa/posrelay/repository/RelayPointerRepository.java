package com.example.msa.posrelay.repository;

import com.example.msa.posrelay.domain.RelayPointer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RelayPointerRepository extends JpaRepository<RelayPointer, Long> {
}
