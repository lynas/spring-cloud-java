package com.lynas.reservationclient;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.sun.org.apache.regexp.internal.RE;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.hateoas.Resources;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

@EnableFeignClients
@EnableZuulProxy
@EnableDiscoveryClient
@SpringBootApplication
public class ReservationClientApplication {



	public static void main(String[] args) {
		SpringApplication.run(ReservationClientApplication.class, args);
	}
}

@FeignClient("reservation-service")
interface ReservationReader {
	@RequestMapping(method = RequestMethod.GET, value = "/reservations")
	Resources<Reservation> read();
}

class Reservation {
	private String reservationName;

	public Reservation() {
	}

	public Reservation(String reservationName) {
		this.reservationName = reservationName;
	}

	public String getReservationName() {
		return reservationName;
	}

	public void setReservationName(String reservationName) {
		this.reservationName = reservationName;
	}
}
@RestController
@RequestMapping("/reservations")
class ReservationApiAdapterRestController {

	private final ReservationReader reservationReader;

	ReservationApiAdapterRestController(ReservationReader reservationReader) {
		this.reservationReader = reservationReader;
	}

	public Collection<String> fallback() {
		return new ArrayList<String>();
	}

	@HystrixCommand(fallbackMethod = "fallback")
	@GetMapping("/names")
	public Collection<String> names() {
		return reservationReader.read().getContent().stream().map(Reservation::getReservationName).collect(Collectors.toList());
	}
}