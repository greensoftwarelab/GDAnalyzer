package AnaDroidAnalyzer.GreenSourceBridge;



import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GSUtils {




    public static Pair<Integer,String> sendJSONtoDB(String url, String JSONMessage) {
        List<Header> headerList = new ArrayList<>();
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        Integer httpRes = 0;
        String responseContent="";
        try {
            HttpPost request = new HttpPost(url);
            StringEntity params = new StringEntity(JSONMessage);
            request.addHeader("content-type", "application/json");
            request.setEntity(params);
            HttpResponse httpResponse = httpClient.execute(request);
            Arrays.stream(httpResponse.getAllHeaders()).filter( x -> x.getName().equals("Set-Cookie") ).forEach( z -> headerList.add(z));
            HttpEntity responseEntity = httpResponse.getEntity();
            httpRes = httpResponse.getStatusLine().getStatusCode();
            if (responseEntity != null) {
                responseContent = EntityUtils.toString(responseEntity);
            }

        } catch (Exception ex) {
            // handle exception here
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new Pair<Integer, String>(httpRes,responseContent);
    }
}
