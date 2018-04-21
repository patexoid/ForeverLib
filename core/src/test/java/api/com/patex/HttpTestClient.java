package api.com.patex;


import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.http.HttpHeaders.AUTHORIZATION;

/**
 * Created by Alexey on 8/15/2016.
 */
@SuppressWarnings("Duplicates")
public class HttpTestClient {

    private final String url;

    private String username;
    private String password;

    public HttpTestClient(String url) {
        this.url = url;

    }

    public void setCreds(String username,String pasword){
        this.username=username;
        this.password=pasword;
    }


    private void updateHeaders(HttpHeaders headers){
        if(username!=null) {
            String basic = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
            headers.add(AUTHORIZATION, "Basic " + basic);
        }
    }


    public <M, E extends List<M>> ResponseEntity<E> uploadFiles(String path,
                                                                String filesPropName,
                                                                Map<String, InputStream> files,
                                                                ParameterizedTypeReference<E> responseType) {
        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.put(filesPropName,
                files.entrySet().stream().
                        map(entry -> new MultipartFileResource(entry.getValue(), entry.getKey())).
                        collect(Collectors.toList()));
//                        map(entry -> new InputStreamResource(entry.getValue(), entry.getKey())).
        return post(path, map, MediaType.MULTIPART_FORM_DATA, responseType);
    }

    public <E, M> ResponseEntity<E> post(String path,
                                         M body,
                                         MediaType mediaType,
                                         ParameterizedTypeReference<E> responseType) {
        return exchange(path, body, mediaType, responseType, HttpMethod.POST);
    }

    private <E, M> ResponseEntity<E> exchange(String path, M body, MediaType mediaType, ParameterizedTypeReference<E> responseType, HttpMethod method) {
        RestTemplate template = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        updateHeaders(headers);
        HttpEntity<M> requestEntity = new HttpEntity<>(body, headers);
        return template.exchange(url + "/" + path,
                method, requestEntity, responseType);
    }

    public <E, M> ResponseEntity<E> post(String path,
                                         M body,
                                         MediaType mediaType,
                                         Class<E> responseType) {
        return exchange(path, body, mediaType, responseType, HttpMethod.POST);
    }

    private <E, M> ResponseEntity<E> exchange(String path, M body, MediaType mediaType,
                                              Class<E> responseType, HttpMethod method) {
        RestTemplate template = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        if(mediaType!=null) {
            headers.setContentType(mediaType);
        }
        updateHeaders(headers);
        HttpEntity<M> requestEntity = new HttpEntity<>(body, headers);
        return template.exchange(url + "/" + path, method, requestEntity, responseType);
    }


    public <T> T get(String uri, Class<T> clazz) throws IOException {
        return exchange(uri, null, MediaType.TEXT_HTML, clazz, HttpMethod.GET).getBody();
    }

    public <T> T get(String uri, ParameterizedTypeReference<T> responseType) throws IOException {
        return exchange(uri, null, MediaType.TEXT_HTML, responseType, HttpMethod.GET).getBody();
    }

    private class MultipartFileResource extends InputStreamResource {

        final String filename;

        public MultipartFileResource(InputStream inputStream, String filename) {
            super(inputStream);
            this.filename = filename;
        }

        @Override
        public String getFilename() {
            return this.filename;
        }

        @Override
        public long contentLength() throws IOException {
            return -1;
        }
    }
}
