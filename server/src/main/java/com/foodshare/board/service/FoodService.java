package com.foodshare.board.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foodshare.board.dto.FoodDTO;
import com.foodshare.board.exception.NotFoundException;
import com.foodshare.board.repository.CategoryRepository;
import com.foodshare.board.repository.FavoriteRepository;
import com.foodshare.board.repository.FoodImageRepository;
import com.foodshare.board.repository.FoodRepository;
import com.foodshare.domain.Category;
import com.foodshare.domain.FavoriteFood;
import com.foodshare.domain.Food;
import com.foodshare.domain.FoodImage;
import com.foodshare.board.mapper.EntityMapper;
import com.foodshare.domain.User;
import com.foodshare.security.dto.CustomUserDetails;
import com.foodshare.security.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class FoodService {
	private final FoodRepository foodRepository;
	private final FoodImageRepository foodImageRepository;
	private final CategoryRepository categoryRepository;
	private final UserRepository userRepository;
	private final FavoriteRepository favoriteRepository;
	private final EntityMapper entityMapper;

	private Long getUserIdFromAuthentication() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		log.info("인증자는 도대체 값이 어떻게 되는거야?!" + authentication);
		log.info("인증자는 도대체 값이 어떻게 되는거야?!" + authentication.getPrincipal());
		log.info("인증자는 도대체 값이 어떻게 되는거야?!" + ((CustomUserDetails)authentication.getPrincipal()));
		log.info("인증자는 도대체 값이 어떻게 되는거야?!" + ((CustomUserDetails)authentication.getPrincipal()).getUserId());
		if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
			return ((CustomUserDetails) authentication.getPrincipal()).getUserId();
		} else {
			throw new UsernameNotFoundException("Authentication failed - no valid user found.");
		}
	}
	public Page<FoodDTO> getAllFoods(Pageable pageable) {
		log.info("Service Getting all foods with pageable: {}", pageable);
		return foodRepository.findAll(pageable).map(food -> {
			List<FoodImage> foodImages = foodImageRepository.findByFoodFoodId(food.getFoodId());
			Category category = categoryRepository.findById(food.getCategory().getCategoryId()).orElseThrow(() -> new NotFoundException("Category not found"));
			Optional<FavoriteFood> favoriteFood = favoriteRepository.findByFoodFoodIdAndUserUserId(food.getFoodId(), getUserIdFromAuthentication());
			long favoriteCount = favoriteRepository.countByFoodFoodId(food.getFoodId());
			FoodDTO foodDTO = entityMapper.convertToFoodDTO(food, foodImages, category, favoriteFood.orElse(null));
			foodDTO.setLikes((int) favoriteCount);
			boolean isFavorite = favoriteRepository.existsByFoodFoodIdAndUserUserId(food.getFoodId(), getUserIdFromAuthentication());
			foodDTO.setIsFavorite(isFavorite);
			return foodDTO;
		});
	}

	public FoodDTO read(Long id) {
		log.info("Service Reading food with id: {}", id);
		Food food = foodRepository.findById(id).orElseThrow(() -> new NotFoundException("Food not found with id: " + id));
		List<FoodImage> foodImages = foodImageRepository.findByFoodFoodId(food.getFoodId());
		Category category;
		category = categoryRepository.findById(food.getCategory().getCategoryId()).orElseThrow(() -> new NotFoundException("Category not found"));
		Optional<FavoriteFood> favoriteFood;
		favoriteFood = favoriteRepository.findByFoodFoodIdAndUserUserId(food.getFoodId(), getUserIdFromAuthentication());
		long favoriteCount = favoriteRepository.countByFoodFoodId(food.getFoodId());
		FoodDTO foodDTO = entityMapper.convertToFoodDTO(food, foodImages, category, favoriteFood.orElse(null));
		foodDTO.setLikes((int) favoriteCount);
		boolean isFavorite = favoriteRepository.existsByFoodFoodIdAndUserUserId(food.getFoodId(), getUserIdFromAuthentication());
		foodDTO.setIsFavorite(isFavorite);
		return foodDTO;
	}

	public Food create(FoodDTO foodDTO) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		User user = userRepository.findById(((CustomUserDetails) authentication.getPrincipal()).getUserId()).orElseThrow(() -> new UsernameNotFoundException("User not found"));
		foodDTO.setGiver(user);
		log.info("User value retrieved from token: {}", user);

		Category category = entityMapper.convertToCategory(foodDTO.getCategory());
		category = categoryRepository.save(category);

		Food food = entityMapper.convertToFood(foodDTO);
		food.setCategory(category);
		food = foodRepository.save(food);

		FoodImage foodImage = entityMapper.convertToFoodImage(foodDTO, food);
		if (foodImage != null) {
			foodImageRepository.save(foodImage);
		}


		return food;
	}

	public Food update(Long id, FoodDTO foodDTO) throws NotFoundException {

		Food existingFood = foodRepository.findById(id)
			.orElseThrow(() -> new NotFoundException("Food not found with id: " + id));

		// 기존 Food 엔티티 업데이트
		existingFood.setTitle(foodDTO.getTitle());
		existingFood.setDescription(foodDTO.getDescription());
		// 다른 필드들도 마찬가지로 업데이트

		// Category 업데이트
		Category category = categoryRepository.findById(existingFood.getCategory().getCategoryId())
			.orElseThrow(() -> new NotFoundException("Category not found"));
		category.setName(foodDTO.getCategory());
		categoryRepository.save(category);
		existingFood.setCategory(category);

		List<FoodImage> existingImages = foodImageRepository.findByFoodFoodId(id);
		foodImageRepository.deleteAll(existingImages); // 이전 이미지 제거

		List<FoodImage> newImages = foodDTO.getImageUrls().stream().map(url -> {
			FoodImage image = new FoodImage();
			List<String> paths = new ArrayList<>();
			paths.add(url);
			image.setImagePaths(paths);
			image.setFood(existingFood);
			return image;
		}).collect(Collectors.toList());

		foodImageRepository.saveAll(newImages); // 새 이미지 저장
		foodRepository.save(existingFood);
		return existingFood;
	}

	@Transactional
	public void delete(Long id) throws NotFoundException {
		// 음식 정보를 찾기
		Food existingFood = foodRepository.findById(id)
			.orElseThrow(() -> new NotFoundException("Food not found with id: " + id));

		// 관련된 이미지 삭제
		List<FoodImage> images = foodImageRepository.findByFoodFoodId(existingFood.getFoodId());
		if (!images.isEmpty()) {
			foodImageRepository.deleteAll(images);
		}

		// 음식 삭제
		foodRepository.delete(existingFood);

		// 이 카테고리를 참조하는 다른 음식이 없으면 카테고리 삭제
		boolean isCategoryUsed = foodRepository.existsByCategoryCategoryId(existingFood.getCategory().getCategoryId());
		if (!isCategoryUsed) {
			categoryRepository.deleteById(existingFood.getCategory().getCategoryId());
		}
	}

	@Transactional(readOnly = true)
	public List<FoodDTO> searchFoodsByCategoryName(String categoryName) {
		List<Food> foods = foodRepository.findByCategoryNameContainingIgnoreCase(categoryName);
		return foods.stream()
			.map(food -> {
				List<FoodImage> foodImages = foodImageRepository.findByFoodFoodId(food.getFoodId());
				Category category = categoryRepository.findById(food.getCategory().getCategoryId()).orElse(null);
				User user = userRepository.findById(getUserIdFromAuthentication().longValue()).orElseThrow(() -> new UsernameNotFoundException("User not found"));
				Optional<FavoriteFood> favoriteFood = favoriteRepository.findByFoodFoodIdAndUserUserId(food.getFoodId(), user.getUserId());
				return entityMapper.convertToFoodDTO(food, foodImages, category, favoriteFood.orElse(null));
			}).collect(Collectors.toList());
	}

}