package neder.net.firebase;

import com.google.gson.Gson;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

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
        trustEveryone();
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

    private void trustEveryone() {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier(){
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }});
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[]{new X509TrustManager(){
                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {}
                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {}
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }}}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(
                    context.getSocketFactory());
        } catch (Exception e) { // should never happen
            e.printStackTrace();
        }
    }
}
