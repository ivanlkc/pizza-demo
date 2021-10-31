package com.bestpizza.service.entity;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.Type;

@Entity
@IdClass(PizzaTopping.PizzaToppingPK.class)
@lombok.Data
@Table(schema = "DBO", name = "PIZZA_TOPPING")
public class PizzaTopping implements Serializable {
	private static final long serialVersionUID = -1620865535525654628L;

	@Id
	@Type(type = "uuid-char")
	@Column(name = "PIZZA_ID")
	private UUID pizzaId;

	@Id
	@Column(name = "TOPPING", columnDefinition = "nvarchar(500)")
	@Nationalized
	private String topping;

	@Embeddable
	@lombok.Getter
	@lombok.Setter
	@lombok.EqualsAndHashCode(callSuper = false, doNotUseGetters = true)
	public static class PizzaToppingPK implements Serializable {
		private static final long serialVersionUID = -265948336856267726L;

		@Type(type = "uuid-char")
		@Column(name = "PIZZA_ID")
		private UUID pizzaId;

		@Column(name = "TOPPING", columnDefinition = "nvarchar(500)")
		@Nationalized
		private String topping;
	}
}
