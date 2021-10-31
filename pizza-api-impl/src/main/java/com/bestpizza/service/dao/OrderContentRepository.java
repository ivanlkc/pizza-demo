package com.bestpizza.service.dao;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.bestpizza.service.entity.OrderContent;

@Repository
public interface OrderContentRepository extends JpaRepository<OrderContent, OrderContent> {
	@Transactional(readOnly = true)
	List<OrderContent> findByOrderId(UUID orderId);
}
