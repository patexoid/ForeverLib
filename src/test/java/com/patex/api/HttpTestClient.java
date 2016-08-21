package com.patex.api;


import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Alexey on 8/15/2016.
 */
public class HttpTestClient {

    private final String url;


    public HttpTestClient(String url) {
        this.url = url;

    }


    public <M, E extends List<M>> ResponseEntity<E> uploadFiles(String path, String filesPropName, Map<String, InputStream> files, ParameterizedTypeReference<E> responseType) {
        RestTemplate template = new RestTemplate();

        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.put(filesPropName,
                files.entrySet().stream().
//                        map(entry -> new InputStreamResource(entry.getValue(), entry.getKey())).
        map(entry -> new ClassPathResource(entry.getKey())).
        collect(Collectors.toList()));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<LinkedMultiValueMap<String, Object>>(
                map, headers);
        return template.exchange(url + "/" + path,
                HttpMethod.POST, requestEntity, responseType);
    }

//    public HttpResponse makeGet(String uri) throws IOException {
//        HttpGet httpGet = new HttpGet(url+"/"+uri);
//        return httpClient.execute(httpGet);
//    }


//    public <E> E getReguest(){
//        RestTemplate restTemplate = new RestTemplate();
//
//    }

}
