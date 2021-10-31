package com.bestpizza.service.entity;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

@Entity
@IdClass(OrderContent.OrderContentPK.class)
@lombok.Data
@Table(schema = "DBO", name = "ORDER_CONTENT")
public class OrderContent implements Serializable {
	private static final long serialVersionUID = 854068810954410247L;

	@Id
	@Type(type = "uuid-char")
	@Column(name = "ORDER_ID")
	private UUID orderId;

	@Id
	@Type(type = "uuid-char")
	@Column(name = "PIZZA_ID")
	private UUID pizzaId;

	@Column(name = "QUANTITY")
	private int quantity;

	@Embeddable
	@lombok.Getter
	@lombok.Setter
	@lombok.EqualsAndHashCode(callSuper = false, doNotUseGetters = true)
	public static class OrderContentPK implements Serializable {
		private static final long serialVersionUID = 9015796989060248911L;

		@Type(type = "uuid-char")
		@Column(name = "ORDER_ID")
		private UUID orderId;

		@Type(type = "uuid-char")
		@Column(name = "PIZZA_ID")
		private UUID pizzaId;
	}
}
