package com.bestpizza.service.api.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.bestpizza.api.model.Order;
import com.bestpizza.api.model.Pizza;
import com.bestpizza.service.dao.OrderContentRepository;
import com.bestpizza.service.dao.OrderDetailRepository;
import com.bestpizza.service.dao.PizzaDetailRepository;
import com.bestpizza.service.dao.PizzaToppingRepository;
import com.bestpizza.service.entity.OrderContent;
import com.bestpizza.service.entity.OrderDetail;
import com.bestpizza.service.entity.PizzaDetail;
import com.bestpizza.service.entity.PizzaTopping;

import io.micrometer.core.instrument.util.StringUtils;

/**
 * The REST controller for handling orders related requests. Each method is
 * secured by JWT Scope using PreAuthorize annotation. The JWT subject is
 * extracted, logged and used as the customer name when making new Pizza orders.
 */
@RestController
@RequestMapping(path = "/v1/orders")
@lombok.extern.slf4j.Slf4j
public class OrdersController {
	@Autowired
	private OrderDetailRepository orderDetailRepository;
	@Autowired
	private OrderContentRepository orderContentRepository;
	@Autowired
	private PizzaDetailRepository pizzaDetailRepository;
	@Autowired
	private PizzaToppingRepository pizzaToppingRepository;

	@PreAuthorize("hasAuthority('SCOPE_orders.create')")
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Transactional(rollbackFor = Exception.class)
	public Order createOrder(@AuthenticationPrincipal(expression = "subject") String customer,
			@RequestBody Order order) {
		log.info("Order being created for customer {}. Request was {}", customer, order);
		// During Create, the ID should be null; it is assigned by the backend server.
		if (order == null || order.getId() != null || order.getPrice() == null
				|| CollectionUtils.isEmpty(order.getPizzas())) {
			log.error("Order message incorrect!");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order message incorrect!");
		}
		for (Pizza p : order.getPizzas()) {
			if (p.getName() == null || p.getQuantity() == null || p.getSize() == null) {
				log.error("Pizza message incorrect!");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pizza message incorrect!");
			}
		}

		final UUID assignedOrderID = UUID.randomUUID();

		final OrderDetail od = new OrderDetail();
		od.setOrderId(assignedOrderID);
		od.setCustomer(customer);
		od.setPrice(order.getPrice());
		od.setCreatedTime(new Date());
		orderDetailRepository.save(od);

		for (Pizza p : order.getPizzas()) {
			final UUID assignedPizzaID = UUID.randomUUID();

			final OrderContent oc = new OrderContent();
			oc.setOrderId(assignedOrderID);
			oc.setPizzaId(assignedPizzaID);
			oc.setQuantity(p.getQuantity());
			orderContentRepository.save(oc);

			final PizzaDetail pd = new PizzaDetail();
			pd.setPizzaId(assignedPizzaID);
			pd.setName(p.getName());
			pd.setSize(p.getSize());
			pizzaDetailRepository.save(pd);

			if (!CollectionUtils.isEmpty(p.getToppings())) {
				for (String topping : p.getToppings()) {
					final PizzaTopping pt = new PizzaTopping();
					pt.setPizzaId(assignedPizzaID);
					pt.setTopping(topping);
					pizzaToppingRepository.save(pt);
				}
			}
		}

		// In the response, populate the assigned order ID.
		order.setId(assignedOrderID.toString());
		return order;
	}

	@PreAuthorize("hasAuthority('SCOPE_orders.read')")
	@GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Transactional(readOnly = true, rollbackFor = Exception.class)
	public Order getSingleOrder(@AuthenticationPrincipal(expression = "subject") String user,
			@PathVariable("id") String id) {
		log.info("Order ID {} being read by user {}", id, user);
		if (StringUtils.isBlank(id)) {
			log.error("Input ID is empty!");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Input ID is empty!");
		}

		final UUID requestedOrderId;
		try {
			requestedOrderId = UUID.fromString(id);
		} catch (IllegalArgumentException e) {
			log.error("Input ID incorrect! {}", id, e);
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Input ID incorrect!");
		}

		final OrderDetail od;

		try {
			od = orderDetailRepository.getById(requestedOrderId);
			// fetch a field to force JPA to throw EntityNotFoundException if not exist
			od.getPrice();
		} catch (EntityNotFoundException e) {
			log.error("Input ID not found! {}", id, e);
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Input ID not found!");
		}

		return this.fillOrderPojoFromDatabase(od);
	}

	@PreAuthorize("hasAuthority('SCOPE_orders.read')")
	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@Transactional(readOnly = true, rollbackFor = Exception.class)
	public List<Order> getAllOrders(@AuthenticationPrincipal(expression = "subject") String user) throws SQLException {
		log.info("All Orders being read by user {}", user);
		List<Order> result = new ArrayList<>();
		for (OrderDetail od : orderDetailRepository.findAll()) {
			result.add(this.fillOrderPojoFromDatabase(od));
		}
		return result;
	}

	/**
	 * Converts Database Entities into the OpenAPI POJO format. Used for sending
	 * HTTP Responses in GET requests.
	 */
	private Order fillOrderPojoFromDatabase(OrderDetail od) {
		final Order result = new Order();
		try {
			result.setId(od.getOrderId().toString());
			result.setPrice(od.getPrice());

			final List<OrderContent> ocs = orderContentRepository.findByOrderId(od.getOrderId());
			for (OrderContent oc : ocs) {
				final Pizza p = new Pizza();
				p.setQuantity(oc.getQuantity());

				final PizzaDetail pd = pizzaDetailRepository.getById(oc.getPizzaId());
				p.setName(pd.getName());
				p.setSize(pd.getSize());

				final List<PizzaTopping> pts = pizzaToppingRepository.findByPizzaId(oc.getPizzaId());
				final List<String> toppings = new ArrayList<>();
				for (PizzaTopping pt : pts) {
					toppings.add(pt.getTopping());
				}
				p.setToppings(toppings);
				result.getPizzas().add(p);
			}
			return result;
		} catch (Exception e) {
			log.error("Unexpected exception occur!", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}
}
