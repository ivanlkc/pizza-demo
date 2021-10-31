package com.bestpizza.service.entity;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.Type;

@Entity
@lombok.Data
@Table(schema = "DBO", name = "PIZZA_DETAIL")
public class PizzaDetail implements Serializable {
	private static final long serialVersionUID = -4606419788478421116L;

	@Id
	@Type(type = "uuid-char")
	@Column(name = "PIZZA_ID")
	private UUID pizzaId;

	@Column(name = "NAME", columnDefinition = "nvarchar(500)")
	@Nationalized
	private String name;

	@Column(name = "SIZE")
	private int size;
}
