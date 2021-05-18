package cn.piesat.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.*;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.*;
import org.apache.http.conn.HttpConnectionFactory;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultHttpResponseParser;
import org.apache.http.impl.conn.DefaultHttpResponseParserFactory;
import org.apache.http.impl.conn.ManagedHttpClientConnectionFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.io.DefaultHttpRequestWriterFactory;
import org.apache.http.io.HttpMessageParser;
import org.apache.http.io.HttpMessageParserFactory;
import org.apache.http.io.HttpMessageWriterFactory;
import org.apache.http.io.SessionInputBuffer;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicLineParser;
import org.apache.http.message.LineParser;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.CharArrayBuffer;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;
import java.util.Map;

@Component
public class ClientConfiguration {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    CloseableHttpClient httpclient;

    PoolingHttpClientConnectionManager connManager;

    RequestConfig defaultRequestConfig;

    public ClientConfiguration() {
        // Use custom message parser / writer to customize the way HTTP
        // messages are parsed from and written out to the data stream.
        HttpMessageParserFactory<HttpResponse> responseParserFactory = new DefaultHttpResponseParserFactory() {

            @Override
            public HttpMessageParser<HttpResponse> create(
                    SessionInputBuffer buffer, MessageConstraints constraints) {
                LineParser lineParser = new BasicLineParser() {

                    @Override
                    public Header parseHeader(final CharArrayBuffer buffer) {
                        try {
                            return super.parseHeader(buffer);
                        } catch (ParseException ex) {
                            return new BasicHeader(buffer.toString(), null);
                        }
                    }

                };
                return new DefaultHttpResponseParser(
                        buffer, lineParser, DefaultHttpResponseFactory.INSTANCE, constraints) {

                    @Override
                    protected boolean reject(final CharArrayBuffer line, int count) {
                        // try to ignore all garbage preceding a status line infinitely
                        return false;
                    }

                };
            }

        };
        HttpMessageWriterFactory<HttpRequest> requestWriterFactory = new DefaultHttpRequestWriterFactory();

        // Use a custom connection factory to customize the process of
        // initialization of outgoing HTTP connections. Beside standard connection
        // configuration parameters HTTP connection factory can define message
        // parser / writer routines to be employed by individual connections.
        HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection> connFactory = new ManagedHttpClientConnectionFactory(
                requestWriterFactory, responseParserFactory);

        // Client HTTP connection objects when fully initialized can be bound to
        // an arbitrary network socket. The process of network socket initialization,
        // its connection to a remote address and binding to a local one is controlled
        // by a connection socket factory.

        // SSL context for secure connections can be created either based on
        // system or application specific properties.
        SSLContext sslcontext = SSLContexts.createSystemDefault();

        // Create a registry of custom connection socket factories for supported
        // protocol schemes.
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", new SSLConnectionSocketFactory(sslcontext))
                .build();

        // Create a connection manager with custom configuration.
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(
                socketFactoryRegistry, connFactory);

        // Create socket configuration
        SocketConfig socketConfig = SocketConfig.custom()
                .setTcpNoDelay(true)
                .build();
        // Configure the connection manager to use socket configuration either
        // by default or for a specific host.
        connManager.setDefaultSocketConfig(socketConfig);
        // Validate connections after 1 sec of inactivity
        connManager.setValidateAfterInactivity(1000);

        // Create message constraints
        MessageConstraints messageConstraints = MessageConstraints.custom()
                .setMaxHeaderCount(200)
                .setMaxLineLength(2000)
                .build();
        // Create connection configuration
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setMalformedInputAction(CodingErrorAction.IGNORE)
                .setUnmappableInputAction(CodingErrorAction.IGNORE)
                .setCharset(Consts.UTF_8)
                .setMessageConstraints(messageConstraints)
                .build();
        // Configure the connection manager to use connection configuration either
        connManager.setDefaultConnectionConfig(connectionConfig);

        // Configure total max or per route limits for persistent connections
        // that can be kept in the pool or leased by the connection manager.
        connManager.setMaxTotal(100);
        connManager.setDefaultMaxPerRoute(10);
        this.connManager = connManager;

        // Create global request configuration
        this.defaultRequestConfig = RequestConfig.custom()
                .setSocketTimeout(200000)
                .setConnectTimeout(200000)
                .setConnectionRequestTimeout(20000)
                .setCookieSpec(CookieSpecs.DEFAULT)
                .setExpectContinueEnabled(true)
                .setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM, AuthSchemes.DIGEST))
                .setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC))
                .build();
        HttpRequestRetryHandler httpRequestRetryHandler = new HttpRequestRetryHandler() {
            @Override
            public boolean retryRequest(IOException e, int i, HttpContext httpContext) {
                return false;
            }
        };

        this.httpclient = HttpClients.custom()
                .setRetryHandler(httpRequestRetryHandler)
                .setConnectionManager(connManager)
                .build();
    }


    public JSONObject sendGet(String url, Map<String, Object> paramMap) {
        HttpGet httpget = null;
        CloseableHttpResponse response = null;
        try {
            CloseableHttpClient httpclient = this.httpclient;
            httpget = new HttpGet(url);
            httpget.setConfig(this.defaultRequestConfig);
            if (!CollectionUtils.isEmpty(paramMap)) {
                for (String key : paramMap.keySet()) {
                    httpget.addHeader(key, paramMap.get(key).toString());
                }
            }
            response = httpclient.execute(httpget, HttpClientContext.create());
            if (!StringUtils.isEmpty(response)){
                StatusLine statusLine = response.getStatusLine();
                if (!StringUtils.isEmpty(statusLine)){
                    int statusCode = statusLine.getStatusCode();
                    if (statusCode!=200){
                        return null;
                    }
                }
            }else {
                return null;
            }
            return JSONObject.parseObject(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            log.error(String.format("send post to url :%s exception %s",url,e));
            return null;
        } finally {
            if (response != null) {
                try {
                    response.getEntity().getContent().close();
                } catch (IOException e) {
                    log.error(String.format("send get to url :%s exception %s",url,e));
                    e.printStackTrace();
                }
            }
        }
    }

    public JSONObject sendPost(String url, String body) {
        HttpPost httppost = null;
        CloseableHttpResponse response = null;
        try {
            CloseableHttpClient httpclient = this.httpclient;
            httppost = new HttpPost(url);
            httppost.setConfig(this.defaultRequestConfig);
            httppost.addHeader("Content-Type", "application/json");
            httppost.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
            response = httpclient.execute(httppost, HttpClientContext.create());
            return JSON.parseObject(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            log.error(String.format("send post to url :%s exception %s",url,e));
            return null;
        } finally {
            if (response != null) {
                try {
                    response.getEntity().getContent().close();
                } catch (IOException e) {
                    log.error(String.format("send post to url :%s exception %s",url,e));
                }
            }
        }
    }

    public JSONObject sendPost(String url,Map<String,Object> paramMap,String body) {
        HttpPost httppost = null;
        CloseableHttpResponse response = null;
        try {
            CloseableHttpClient httpclient = this.httpclient;
            httppost = new HttpPost(url);
            httppost.setConfig(this.defaultRequestConfig);
            httppost.addHeader("Content-Type", "application/json");
            if (!CollectionUtils.isEmpty(paramMap)) {
                for (String key : paramMap.keySet()) {
                    httppost.addHeader(key, paramMap.get(key).toString());
                }
            }
            httppost.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
            response = httpclient.execute(httppost, HttpClientContext.create());
            return JSON.parseObject(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            log.error(String.format("send post to url :%s exception %s",url,e));
            return null;
        } finally {
            if (response != null) {
                try {
                    response.getEntity().getContent().close();
                } catch (IOException e) {
                    log.error(String.format("send post to url :%s exception %s",url,e));
                }
            }
        }
    }

}

