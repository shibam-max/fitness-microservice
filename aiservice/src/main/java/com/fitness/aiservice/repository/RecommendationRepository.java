package com.fitness.aiservice.repository;

import com.fitness.aiservice.model.Recommendation;
import org.jspecify.annotations.Nullable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecommendationRepository extends MongoRepository<Recommendation,String> {

    @Nullable List<Recommendation> findByUserId(String userId);

    @Nullable List<Recommendation> findByAcitivityId(String activityId);
}
