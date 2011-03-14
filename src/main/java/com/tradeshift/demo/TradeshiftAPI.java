package com.tradeshift.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.oauth.client.OAuthClientFilter;
import com.sun.jersey.oauth.signature.OAuthParameters;
import com.sun.jersey.oauth.signature.OAuthSecrets;

/**
 * A simple wrapper of the Tradeshift REST API
 */
public class TradeshiftAPI {
    private Client restClient;
    private final String consumerKey;
    private final String consumerSecret;
    private final String tradeshiftRestUrl;
    private boolean logging = false;
    
    /**
     * Creates a new TradeshiftAPI instance, which will call into the Tradeshift API Sandbox environment.
     */
    public TradeshiftAPI(String consumerKey, String consumerSecret) {
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        this.tradeshiftRestUrl = "https://api-sandbox.tradeshift.com/tradeshift/rest/";
        restClient = Client.create();
        restClient.setFollowRedirects(false);
    }
    
    /**
     * Returns a list of company accounts that our consumer key has access to.
     */
    public List<UUID> getCompanyAccounts() {
        JSONObject json = getResource()
            .path("external/consumer/accounts")
            .accept(MediaType.APPLICATION_JSON)
            .get(JSONObject.class);
        List<UUID> result = new ArrayList<UUID>();
        JSONArray a = json.optJSONArray("CompanyAccounts");
        if (a != null) for (int i = 0; i < a.length(); i++) {
            String id = a.optString(i);
            if (id != null) result.add(UUID.fromString(id));
        }
        return result;
    }
    
    /**
     * Returns company information about a specific account that our consumer key has access to.
     */
    public JSONObject getCompanyAccountInfo (UUID companyAccountId) {
        return getResource()
            .path("/external/account/info")
            .header("X-Tradeshift-TenantId", companyAccountId.toString())
            .accept(MediaType.APPLICATION_JSON)
            .get(JSONObject.class);
    }
    
    /**
     * Specifies whether all client requests should be logged using com.sun.jersey.api.client.filter.LoggingFilter .
     */
    public void setLogging(boolean logging) {
        this.logging = logging;
    }
    
    /**
     * Prepares a new Jersey WebResource with an appropriate OAuth filter attached, that will add 
     * an OAuth authorization signature, according to our consumer key and secret. 
     */
    private WebResource getResource() {    
        OAuthParameters params = new OAuthParameters().signatureMethod("HMAC-SHA1").consumerKey(consumerKey).version();
        OAuthSecrets secrets = new OAuthSecrets().consumerSecret(consumerSecret);        
        OAuthClientFilter filter = new OAuthClientFilter(restClient.getProviders(), params, secrets);
        
        WebResource resource = restClient.resource(tradeshiftRestUrl);
        if (logging) resource.addFilter(new LoggingFilter());
        resource.addFilter(filter);
        return resource;
    }
    
}
