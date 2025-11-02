package com.tamv.systema.repositories;

import com.tamv.systema.entities.Status;
import com.tamv.systema.entities.StatusType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StatusRepository extends JpaRepository<Status, Long> {
    Optional<Status> findByName(String name);
    Optional<Status> findByNameAndType(String name, StatusType type);
}
