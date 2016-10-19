package neder.net.firebase;

import com.google.gson.Gson;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;

import neder.net.firebase.exception.FirebasePushException;

/**
 * Created by Matheus on 18/10/2016.
 */

public class FirebaseClient {
    private String baseUrl;
    private static final Gson serializer = new Gson();

    private static class FirebasePushResponse {
        public String name;
    }

    public FirebaseClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String push(String path, Object data) throws FirebasePushException {
        HttpClient httpClient = new DefaultHttpClient();
        String url = baseUrl + path + ".json";
        HttpPost httPost = new HttpPost(url);

        try {
            String serializedData = serializer.toJson(data);
            HttpEntity requestEntity = new StringEntity(serializedData);
            httPost.setEntity(requestEntity);
            HttpResponse response = httpClient.execute(httPost);
            HttpEntity responseEntity = response.getEntity();
            InputStream responseStream = responseEntity.getContent();
            String responseText = IOUtils.toString(responseStream, "UTF-8");
            FirebasePushResponse firebasePushResponse = serializer.fromJson(responseText, FirebasePushResponse.class);
            return firebasePushResponse.name;
        } catch (IOException e) {
            throw new FirebasePushException(e);
        }
    }
}
