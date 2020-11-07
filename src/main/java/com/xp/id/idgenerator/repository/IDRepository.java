package com.xp.id.idgenerator.repository;

import com.xp.id.idgenerator.entity.IDEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IDRepository extends JpaRepository<IDEntity, Integer> {
}
