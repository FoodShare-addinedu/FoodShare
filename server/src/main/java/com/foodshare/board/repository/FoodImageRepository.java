package com.foodshare.board.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.foodshare.domain.FoodImage;
@Repository
public interface FoodImageRepository extends JpaRepository<FoodImage, Long> {
	List<FoodImage> findByFoodFoodId(Long foodId);
}
