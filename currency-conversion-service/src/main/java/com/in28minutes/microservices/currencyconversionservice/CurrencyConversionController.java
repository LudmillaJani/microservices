package com.in28minutes.microservices.currencyconversionservice;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class CurrencyConversionController {
	
	@Autowired
	private CurrencyExchangeServiceProxy proxy;
	
	@GetMapping("/currency-converter-feign/from/{from}/to/{to}/quantity/{quantity}")
	public CurrencyConversionBean convertCurrencyFeign(@PathVariable String from, @PathVariable String to, 
			@PathVariable BigDecimal quantity) {
		
		
		//1.FEIGN - invoking other microservices
		//FEIGN is a REST service client
		//it makes it easier to call RESTfull webservices
		//we are able to use that proxy to make the calls to the service
		//
		CurrencyConversionBean response = proxy.retrieveExchangeValue(from, to);
		
		//by using the content in reposnse, we are returning new bean
		return new CurrencyConversionBean(response.getId(), from, to, response.getConversionMultiple(), 
				quantity, quantity.multiply(response.getConversionMultiple()), response.getPort());
	}
	
	
	
	@GetMapping("/currency-converter/from/{from}/to/{to}/quantity/{quantity}")
	public CurrencyConversionBean convertCurrency(@PathVariable String from, @PathVariable String to, 
			@PathVariable BigDecimal quantity) {
		

		//pass the values for these variables
		//how these values should be replaced
		Map<String, String> uriVariables = new HashMap<>();
		uriVariables.put("from", from);
		uriVariables.put("to", to);
		
		//RestTemplate : to invoke CurrencyExchangeService from CurrencyCalculationService
		 //to invoke an external service, which is exposed using http
		//we will get the response that comes from currency-exchange-service
		//we want to map the response that is comming back to entity, so we sent a get request
		//response type that we are excpecting back : CurrencyConversionBean.class
		ResponseEntity<CurrencyConversionBean> responseEntity = new RestTemplate().getForEntity(
				"http://localhost:8000/currency-exchange/from/{from}/to/{to}",
				CurrencyConversionBean.class, uriVariables );
		
		CurrencyConversionBean response = responseEntity.getBody();
		//by using the content in reposnse, we are returning new bean
		return new CurrencyConversionBean(response.getId(), from, to, response.getConversionMultiple(), 
				quantity, quantity.multiply(response.getConversionMultiple()), response.getPort());
	}


}
