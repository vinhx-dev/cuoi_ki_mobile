package com.example.ecom.core.api;
import java.util.Map;
import java.io.ByteArrayOutputStream;
import okhttp3.*;
import com.example.ecom.core.api.auth.AuthNetwork;
public class HttpTransport implements ApiTransport {
 private final OkHttpClient client;
 public HttpTransport(android.content.Context context) {client=AuthNetwork.get(context).client;}
 @Override public RawResponse execute(String method,String path,Map<String,String> query,RequestBody body,String token) throws Exception {
  HttpUrl.Builder url=HttpUrl.get(ApiConfig.BASE_URL.replaceAll("/+$", "")+path).newBuilder();
  if(query!=null)for(Map.Entry<String,String> e:query.entrySet())if(e.getValue()!=null)url.addQueryParameter(e.getKey(),e.getValue());
  okhttp3.RequestBody payload=null;
  if(body!=null){ByteArrayOutputStream out=new ByteArrayOutputStream();body.writeTo(out);payload=okhttp3.RequestBody.create(MediaType.parse(body.contentType()),out.toByteArray());}
  if(payload==null&&(method.equals("POST")||method.equals("PUT")||method.equals("PATCH")))payload=okhttp3.RequestBody.create(null,new byte[0]);
  Request request=new Request.Builder().url(url.build()).method(method,payload).build();
  try(Response response=client.newCall(request).execute()){return new RawResponse(response.code(),response.body()!=null?response.body().string():"");}
 }
}
