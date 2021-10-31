package com.bestpizza.service.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.Type;

@Entity
@lombok.Data
@Table(schema = "DBO", name = "ORDER_DETAIL")
public class OrderDetail implements Serializable {
	private static final long serialVersionUID = -4494031139054855261L;

	@Id
	@Type(type = "uuid-char")
	@Column(name = "ORDER_ID")
	private UUID orderId;

	@Column(name = "PRICE")
	private int price;

	@Column(name = "CREATED_TIME")
	private Date createdTime;

	@Column(name = "CUSTOMER", columnDefinition = "nvarchar(500)")
	@Nationalized
	private String customer;
}
