package com.tradeshift.explorer

import groovy.beans.Bindable;

class Request {

	@Bindable String url = 'https://api-sandbox.tradeshift.com/tradeshift/rest/external/'
	@Bindable String method = 'GET'
	@Bindable String companyAccountId
	@Bindable String consumerKey = ''
	@Bindable String consumerSecret = ''
	@Bindable String token = ''
	@Bindable String tokenSecret = ''
	@Bindable String body = ''
	@Bindable String accept = 'application/json'
	@Bindable String contentType = 'application/json'
	
	@Bindable boolean threelegged = false
	
	@Bindable int selectedTab = 0

	def getThreelegged() {
		return threelegged
	}
		
}
