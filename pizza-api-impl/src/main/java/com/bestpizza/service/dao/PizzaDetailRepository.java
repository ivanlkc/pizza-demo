package com.bestpizza.service.dao;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bestpizza.service.entity.PizzaDetail;

@Repository
public interface PizzaDetailRepository extends JpaRepository<PizzaDetail, UUID> {

}
