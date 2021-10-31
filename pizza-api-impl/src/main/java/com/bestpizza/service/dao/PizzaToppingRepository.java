package com.bestpizza.service.dao;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.bestpizza.service.entity.PizzaTopping;

@Repository
public interface PizzaToppingRepository extends JpaRepository<PizzaTopping, PizzaTopping.PizzaToppingPK> {
	@Transactional(readOnly = true)
	List<PizzaTopping> findByPizzaId(UUID pizzaId);
}
