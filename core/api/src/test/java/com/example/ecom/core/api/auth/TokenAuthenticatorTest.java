package com.example.ecom.core.api.auth;
import com.example.ecom.core.data.*;
import org.junit.Test;
import static org.junit.Assert.*;
import okhttp3.*;
import okhttp3.mockwebserver.*;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
public class TokenAuthenticatorTest {
 static class Memory implements TokenStore {
  volatile String access="old-access",refresh="old-refresh";
  public String getAccessToken(){return access;} public String getRefreshToken(){return refresh;}
  public User getUser(){return new User();} public void save(String a,String r,User u){access=a;refresh=r;}
  public void clear(){access=null;refresh=null;}
 }
 private AuthService service(MockWebServer server){return new Retrofit.Builder().baseUrl(server.url("/api/")).addConverterFactory(GsonConverterFactory.create()).build().create(AuthService.class);}
 private Response unauthorized(MockWebServer server){return new Response.Builder().request(new Request.Builder().url(server.url("/api/auth/me")).header("Authorization","Bearer old-access").build()).protocol(Protocol.HTTP_1_1).code(401).message("Expired").build();}
 @Test public void concurrent401RotatesOnceAndSendsBothTokens() throws Exception {
  try(MockWebServer server=new MockWebServer()){
   server.enqueue(new MockResponse().setBody("{\"success\":true,\"data\":{\"accessToken\":\"new-access\",\"refreshToken\":\"new-refresh\"}}"));server.start();
   Memory store=new Memory();TokenAuthenticator auth=new TokenAuthenticator(store,service(server),()->fail("Unexpected expiry"));
   Response failed=unauthorized(server);ExecutorService pool=Executors.newFixedThreadPool(8);
   try {List<Future<Request>> results=new ArrayList<>();CountDownLatch gate=new CountDownLatch(1);
    for(int i=0;i<8;i++)results.add(pool.submit(()->{gate.await();return auth.authenticate(null,failed);}));gate.countDown();
    for(Future<Request> f:results)assertEquals("Bearer new-access",f.get(5,TimeUnit.SECONDS).header("Authorization"));
    assertEquals(1,server.getRequestCount());String body=server.takeRequest().getBody().readUtf8();
    assertTrue(body.contains("\"accessToken\":\"old-access\""));assertTrue(body.contains("\"refreshToken\":\"old-refresh\""));assertEquals("new-refresh",store.refresh);
   }finally{pool.shutdownNow();}
  }
 }
 @Test public void refreshForbiddenClearsSessionAndNotifiesOnlyOnce() throws Exception {
  try(MockWebServer server=new MockWebServer()){
   server.enqueue(new MockResponse().setResponseCode(403).setBody("{\"success\":false,\"message\":\"Revoked\"}"));server.start();
   Memory store=new Memory();AtomicInteger expired=new AtomicInteger();TokenAuthenticator auth=new TokenAuthenticator(store,service(server),expired::incrementAndGet);
   assertNull(auth.authenticate(null,unauthorized(server)));assertNull(auth.authenticate(null,unauthorized(server)));
   assertNull(store.access);assertNull(store.refresh);assertEquals(1,expired.get());assertEquals(1,server.getRequestCount());
  }
 }
 @Test public void publicLogin401DoesNotRefresh() throws Exception {
  try(MockWebServer server=new MockWebServer()){server.start();Memory store=new Memory();TokenAuthenticator auth=new TokenAuthenticator(store,service(server),()->fail("Unexpected expiry"));
   Response r=unauthorized(server);r=r.newBuilder().request(r.request().newBuilder().url(server.url("/api/auth/login")).build()).build();
   assertNull(auth.authenticate(null,r));assertEquals(0,server.getRequestCount());assertEquals("old-access",store.access);
  }
 }
 @Test public void successFalseDoesNotKeepSession() throws Exception {
  try(MockWebServer server=new MockWebServer()){server.enqueue(new MockResponse().setBody("{\"success\":false,\"message\":\"Invalid session\"}"));server.start();Memory store=new Memory();
   assertNull(new TokenAuthenticator(store,service(server),()->{}).authenticate(null,unauthorized(server)));assertNull(store.access);
  }
 }
}
