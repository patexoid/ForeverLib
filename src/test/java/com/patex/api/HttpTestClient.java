package com.patex.api;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;

/**
 * Created by Alexey on 8/15/2016.
 */
public class HttpTestClient {

    String url;
    private HttpClient httpClient;

    public HttpTestClient(String url) {
        this.url = url;
        httpClient= HttpClients.createDefault();
    }

    public HttpResponse makePost(String uri, HttpEntity entity) throws IOException {
        HttpPost httpPost = new HttpPost(url+"/"+uri);
        httpPost.setEntity(entity);
        return httpClient.execute(httpPost);
    }

    public HttpResponse makeGet(String uri) throws IOException {
        HttpGet httpGet = new HttpGet(url+"/"+uri);
        return httpClient.execute(httpGet);
    }


}
