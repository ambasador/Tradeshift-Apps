package com.tradeshift.explorer

import java.awt.BorderLayout;

import groovy.swing.SwingBuilder

import javax.swing.WindowConstants

import org.codehaus.jackson.map.ObjectMapper

import com.sun.jersey.api.client.Client
import com.sun.jersey.api.client.ClientResponse
import com.sun.jersey.api.client.WebResource
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.filter.LoggingFilter
import com.sun.jersey.oauth.client.OAuthClientFilter
import com.sun.jersey.oauth.signature.OAuthParameters
import com.sun.jersey.oauth.signature.OAuthSecrets

class RESTExplorer {
	
	private Request model
	def restClient
	private Response response = new Response()
	
	private ObjectMapper mapper = new ObjectMapper()
	
	private OutputStream out
	
	def tabPanel
	
	def RESTExplorer() {
		restClient = Client.create();
		restClient.setFollowRedirects(false);

		def old = new File(new File(System.getProperty('user.home')), '.tradeshiftexplorer')
		if (old.exists()) {
			try {
				model = mapper.readValue(old, Request.class)
			} catch (Exception e) {
				println "Unable to load old config: ${e}"
				model = new Request()
			}
		} else {
			model = new Request()
		}
	}
	
	def start() {
		SwingBuilder.build() {
			frame(title: 'Tradeshift REST API Explorer', size: [500, 700], visible: true, defaultCloseOperation:WindowConstants.EXIT_ON_CLOSE) {
				borderLayout()
				
				panel(constraints: BorderLayout.NORTH) {
					gridLayout(rows: 10, cols: 2)
					
					label 'Company account ID:'
					textField(text: bind('companyAccountId', validator: { try {UUID.fromString(it) } catch (Exception e) {} }, source: model, mutual: true))
	
					label 'Consumer key:'
					textField(text: bind('consumerKey', source: model, mutual: true))
					
					label 'Consumer secret'
					textField(text: bind('consumerSecret', source: model, mutual: true))
					
					label '3-legged'
					checkBox(selected: bind('threelegged', source: model, mutual: true))
					
					label 'Token'
					textField(text: bind('token', source: model, mutual: true), enabled: bind { model.threelegged })
					
					label 'Token secret'
					textField(text: bind('tokenSecret', source: model, mutual: true), enabled: bind { model.threelegged })
	
					label 'Method'
					comboBox(items: ['GET', 'PUT', 'POST', 'DELETE', 'HEAD'], selectedItem: bind('method', source: model, mutual: true))
					
					label 'Return type'
					comboBox(items: ['application/json', 'text/xml', 'text/plain'], selectedItem: bind('accept', source: model, mutual: true))
					
					label 'Request URL'
					textField(text: bind('url', source: model, mutual: true))
					
					button(text: 'Execute', actionPerformed: { executeRequest() }, enabled: bind { model.url && model.companyAccountId && model.consumerKey && model.consumerSecret && (!model.threelegged || (model.token && model.tokenSecret)) } )
				}

				panel(constraints: BorderLayout.CENTER) {
					borderLayout()
					
					tabPanel = tabbedPane(constraints: BorderLayout.CENTER) {
						panel(title: 'Request') {
							borderLayout()
							
							panel(constraints: BorderLayout.NORTH) {
								gridLayout(rows: 1, cols: 2)
								
								label 'Content type'
								textField(text: bind('contentType', source: model, mutual: true))
							}
							
							scrollPane(constraints: BorderLayout.CENTER) {
								textArea(text: bind('body', source: model, mutual: true))
							}
						}
						panel(title: 'Response') {
							borderLayout()
							label(text: bind(source: response, sourceProperty: 'statusLine'), constraints: BorderLayout.NORTH, preferredSize: [25, 25])
							scrollPane(constraints: BorderLayout.CENTER) {
								textArea(text: bind(source: response, sourceProperty: 'responseBody'))
							}
							
						}
					}
					
					panel(preferredSize: [100, 100], constraints: BorderLayout.SOUTH) {
						borderLayout()
						
						label('Debug log', constraints: BorderLayout.NORTH)
						scrollPane(constraints: BorderLayout.CENTER) {
							textArea(text: bind(source: response, sourceProperty: 'log'))
						}
					}
				}
				
			}
		}
	}
	
	def executeRequest() {
		mapper.writeValue(new File(new File(System.getProperty('user.home')), '.tradeshiftexplorer'), model)
		
		ClientResponse res
		switch (model.method) {
			case 'GET':
				res = getResource().get(ClientResponse.class)
				break
			case 'POST':
	
				res = b.post(ClientResponse.class)
				break
			case 'DELETE':
				res = getResource().delete(ClientResponse.class)
				break
			case 'PUT':
				def b = getResource()
				if (model.contentType && model.body) {
					b = b.entity(model.body, model.contentType)
				}
				res = b.put(ClientResponse.class)
				break
			case 'HEAD':
				res = getResource().head()
				break
		}
		
		if (res.hasEntity()) {
			response.responseBody = res.getEntityInputStream().getText("UTF-8")
		} else {
			response.responseBody = ''
		}
		
		response.statusLine = "${res.clientResponseStatus.code} ${res.clientResponseStatus.reasonPhrase}"
		response.log = new String(out.toByteArray(), "UTF-8")
	
		tabPanel.selectedIndex = 1	
	}
	
	private Builder getResource() {
		OAuthParameters params = new OAuthParameters().signatureMethod("HMAC-SHA1").consumerKey(model.consumerKey).version()
		OAuthSecrets secrets = new OAuthSecrets().consumerSecret(model.consumerSecret)
		if (model.threelegged) {
			params = params.token(model.token)
			secrets = secrets.tokenSecret(model.tokenSecret)
		}
		OAuthClientFilter filter = new OAuthClientFilter(restClient.getProviders(), params, secrets)
		
		WebResource resource = restClient.resource(model.url)
		
		out = new ByteArrayOutputStream()
		PrintStream logstream = new PrintStream(out)
		
		resource.addFilter(new LoggingFilter(logstream))
		resource.addFilter(filter)
		def b = resource.header("X-Tradeshift-TenantId", model.companyAccountId).header("User-Agent", "TradeshiftRestExplorer/1.0").accept(model.accept)
		return b
	}

	
	public static void main(String[] args) {
		new RESTExplorer().start()
	}
}
