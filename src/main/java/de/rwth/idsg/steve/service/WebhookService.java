package de.rwth.idsg.steve.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collections;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * @author Anirudh Ramesh <anirudh@irasus.com>
 * @since 11.03.2021
 */
@Slf4j
@Service
public class WebhookService {

    private RestTemplate restTemplate;

    private static final int API_TIMEOUT_IN_MILLIS = 4_000;

    @PostConstruct
    private void init() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setReadTimeout(API_TIMEOUT_IN_MILLIS);
        factory.setConnectTimeout(API_TIMEOUT_IN_MILLIS);

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategy.SnakeCaseStrategy());

        restTemplate = new RestTemplate(Collections.singletonList(new MappingJackson2HttpMessageConverter(mapper)));
        restTemplate.setRequestFactory(factory);
    }

    public void sendAsync(String endpoint, String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "6ovuIrVd1vVGdVnE5TtwTxPhlzZf+Dmkf6mIQw6IBMk=");
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity entity = new HttpEntity(body, headers);
        // System.out.println(body);
        try {
            String response = restTemplate.postForObject("https://dev-api.chargedock.in/ocpp/" + endpoint, entity, String.class);
            // System.out.println(response);
        } catch (RestClientException e) {
            // System.out.println("WebhookService Error");
            // System.out.println("");
        }
    }

}