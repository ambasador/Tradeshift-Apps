package com.tradeshift.demo;

import java.util.List;
import java.util.UUID;

import junit.framework.Assert;

import org.codehaus.jettison.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class HelloTradeshiftTest {
    private TradeshiftAPI api;
    
    @Before
    public void setup() {
        api = new TradeshiftAPI ("xAlAzHyWbYXfwyWVvi0i", "PnrblwzbPUkTFkVrUW3L31m2emyRN8H5EBwWsHLA");
        api.setLogging(true);
    }
    
    @Test
    public void confirm_we_have_access_to_an_account() {
        List<UUID> companyAccounts = api.getCompanyAccounts();
        Assert.assertTrue(companyAccounts.size() > 0);
        System.out.println("We have access to account with company account Id: " + companyAccounts.get(0));
    }
    
    @Test
    public void print_some_account_info() {
        UUID companyAccountId = api.getCompanyAccounts().get(0);
        JSONObject result = api.getCompanyAccountInfo(companyAccountId);
        String companyName = result.optString("CompanyName");
        Assert.assertNotNull(companyName);
        System.out.println("We have access to account with company name: " + companyName);
    }
}
