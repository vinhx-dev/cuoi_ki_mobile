package com.example.ecom.core.data;
/** Small storage contract also allows deterministic refresh concurrency tests. */
public interface TokenStore {
 String getAccessToken(); String getRefreshToken(); User getUser();
 void save(String access,String refresh,User user); void clear();
}
