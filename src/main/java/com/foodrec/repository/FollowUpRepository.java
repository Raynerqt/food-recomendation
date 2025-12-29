package com.foodrec.repository;

import com.foodrec.entity.FollowUpEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FollowUpRepository extends JpaRepository<FollowUpEntity, Long> {
    List<FollowUpEntity> findByRecommendationIdOrderByDateDesc(Long recommendationId);
}