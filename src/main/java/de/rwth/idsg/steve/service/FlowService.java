package de.rwth.idsg.steve.service;

import com.google.common.base.Strings;
import de.rwth.idsg.steve.SteveException;
import de.rwth.idsg.steve.repository.SteveSettingsRepository;
import de.rwth.idsg.steve.repository.dto.FlowSettings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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

import org.json.JSONObject;

@Slf4j
@Service
public class FlowService {
    @Autowired private SteveSettingsRepository settingsRepository;
    @Autowired private ScheduledExecutorService executorService;

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();

    private FlowSettings settings;

    @PostConstruct
    public void loadSettingsFromDB() {
        writeLock.lock();
        try {
            settings = settingsRepository.getFlowSettings();
        } finally {
            writeLock.unlock();
        }
        //session = createSession(getSettings());
    }

    public FlowSettings getSettings() {
        readLock.lock();
        try {
            return this.settings;
        } finally {
            readLock.unlock();
        }
    }

    private RestTemplate restTemplate;

    private static final int API_TIMEOUT_IN_MILLIS = 4_000;
    //@PostConstruct
    // private void init() {
    //     HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
    //     factory.setReadTimeout(API_TIMEOUT_IN_MILLIS);
    //     factory.setConnectTimeout(API_TIMEOUT_IN_MILLIS);

    //     ObjectMapper mapper = new ObjectMapper();
    //     mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    //     mapper.setPropertyNamingStrategy(new PropertyNamingStrategy.SnakeCaseStrategy());

    //     restTemplate = new RestTemplate(Collections.singletonList(new MappingJackson2HttpMessageConverter(mapper)));
    //     restTemplate.setRequestFactory(factory);
    // }

    public void sendAsync(String endpoint, JSONObject body) {

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setReadTimeout(API_TIMEOUT_IN_MILLIS);
        factory.setConnectTimeout(API_TIMEOUT_IN_MILLIS);

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategy.SnakeCaseStrategy());

        restTemplate = new RestTemplate(Collections.singletonList(new MappingJackson2HttpMessageConverter(mapper)));
        restTemplate.setRequestFactory(factory);




        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "6ovuIrVd1vVGdVnE5TtwTxPhlzZf+Dmkf6mIQw6IBMk=");
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity entity = new HttpEntity(body.toMap(), headers);
        System.out.println(body);
        FlowSettings settings = getSettings();
        String flow_link = settings.getFlow();

        try {
            String response = restTemplate.postForObject(flow_link, entity, String.class);
             System.out.println(response);
        } catch (RestClientException e) {
            //System.out.println("WebhookService Error");
            //System.out.println(e);
        }
    }

}

// org.springframework.web.client.UnknownContentTypeException: Could not extract response: no suitable HttpMessageConverter found for 
// response type [class java.lang.String] and content type [text/html;charset=utf-8]

       

