package com.seunghyun.evcar;

import android.app.*;import android.os.*;import android.webkit.*;import android.widget.*;import android.content.*;import java.io.*;import java.net.*;import java.util.concurrent.*;

public class MainActivity extends Activity {
    WebView web;
    static final String KEPCO="https://api.odcloud.kr/api/15104443/v1/uddi:02be31ea-daae-4f1c-8264-acd9c13e021d";
    static final String CHA="https://api.odcloud.kr/api/15151017/v1/uddi:478c0421-0e46-4127-8650-afbbb03e3cbd";
    @Override public void onCreate(Bundle b){super.onCreate(b); web=new WebView(this); WebSettings s=web.getSettings(); s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setAllowFileAccess(true);s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW); web.addJavascriptInterface(new Bridge(this),"Android"); web.setWebViewClient(new WebViewClient()); web.loadUrl("file:///android_asset/index.html"); setContentView(web);}
    public static class Bridge{
      Context c; Bridge(Context c){this.c=c;}
      @JavascriptInterface public void fetchOnline(String key,String type){
        if(key==null||key.trim().isEmpty()){send(type,"{\"ok\":false,\"error\":\"공공데이터포털 인증키가 필요합니다. 설정에서 입력하세요.\"}");return;}
        String base=type.equals("kepco")?KEPCO:CHA; String url=base+"?serviceKey="+URLEncoder.encode(key.trim(),java.nio.charset.StandardCharsets.UTF_8)+"&page=1&perPage=1000&returnType=JSON";
        ExecutorService ex=Executors.newSingleThreadExecutor(); ex.submit(()->{String out; try{out=http(url);}catch(Exception e){out="{\"ok\":false,\"error\":"+quote(e.toString())+"}";} final String f=out; ((Activity)c).runOnUiThread(()->send(type,"{\"ok\":true,\"data\":"+f+"}"));}); ex.shutdown();
      }
      void send(String type,String payload){String js="window.onOnlineData("+quote(type)+","+quote(payload)+")"; ((Activity)c).runOnUiThread(()->((MainActivity)c).web.evaluateJavascript(js,null));}
      static String http(String u)throws Exception{HttpURLConnection h=(HttpURLConnection)new URL(u).openConnection();h.setConnectTimeout(12000);h.setReadTimeout(20000);h.setRequestMethod("GET");h.setRequestProperty("Accept","application/json");int code=h.getResponseCode();InputStream in=code>=200&&code<300?h.getInputStream():h.getErrorStream();BufferedReader r=new BufferedReader(new InputStreamReader(in,"UTF-8"));StringBuilder b=new StringBuilder();String l;while((l=r.readLine())!=null)b.append(l);h.disconnect();if(code<200||code>=300)throw new IOException("HTTP "+code+" "+b);return b.toString();}
      static String quote(String s){return "\""+s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","")+"\"";}
    }
}
