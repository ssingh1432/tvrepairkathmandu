package com.tvrepairkathmandu.trkyoutube;

import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Gravity;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private static final String VERSION="0.2";
    private static final String PREFS="trk_youtube";
    private LinearLayout root;
    private WebView webView;
    private EditText input;
    private TextView status;
    private boolean webOpen=false, diagnosticsOpen=false;
    private SharedPreferences prefs;
    private String currentVideoId="";

    @Override public void onCreate(Bundle b){
        super.onCreate(b); prefs=getSharedPreferences(PREFS,MODE_PRIVATE);
        immersive(); showHome();
    }
    private void immersive(){ getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY); }
    private TextView text(String s,int size){ TextView v=new TextView(this);v.setText(s);v.setTextColor(Color.WHITE);v.setTextSize(size);v.setPadding(18,10,18,10);return v; }
    private Button button(String s){ Button b=new Button(this);b.setText(s);b.setTextColor(Color.WHITE);b.setTextSize(17);b.setBackgroundResource(R.drawable.focus_bg);b.setFocusable(true);b.setAllCaps(false);b.setPadding(18,8,18,8);return b; }
    private void addButton(LinearLayout box,String label,View.OnClickListener l){ Button b=button(label);b.setOnClickListener(l);box.addView(b,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,64)); }
    private LinearLayout page(){ ScrollView sc=new ScrollView(this);LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(34,20,34,30);box.setBackgroundColor(Color.rgb(15,15,15));sc.addView(box);setContentView(sc);root=box;return box; }
    private void heading(LinearLayout box,String subtitle){ TextView h=text("TRK YouTube TV",29);h.setTypeface(Typeface.DEFAULT,Typeface.BOLD);box.addView(h);box.addView(text(subtitle,15)); }

    private void showHome(){
        destroyWeb(); webOpen=false;diagnosticsOpen=false;LinearLayout box=page();heading(box,"Android 4.4+ universal TV build  •  v"+VERSION);
        TextView welcome=text("YouTube for old & new Android TVs",20);welcome.setTypeface(Typeface.DEFAULT,Typeface.BOLD);box.addView(welcome);
        input=new EditText(this);input.setHint("Search YouTube or paste video URL / ID");input.setTextColor(Color.WHITE);input.setHintTextColor(Color.LTGRAY);input.setSingleLine(true);input.setTextSize(17);input.setFocusable(true);box.addView(input,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,62));
        addButton(box,"🔎  SEARCH YOUTUBE",v->search());
        addButton(box,"▶  PLAY URL / VIDEO ID",v->playInput());
        addButton(box,"🏠  YOUTUBE HOME",v->openYouTube("https://www.youtube.com/"));
        addButton(box,"🔥  EXPLORE / TRENDING",v->openYouTube("https://www.youtube.com/feed/trending"));
        addButton(box,"📺  SUBSCRIPTIONS",v->openYouTube("https://www.youtube.com/feed/subscriptions"));
        addButton(box,"📚  YOUTUBE LIBRARY",v->openYouTube("https://www.youtube.com/feed/you"));
        addButton(box,"🕘  TRK WATCH HISTORY",v->showHistory());
        addButton(box,"⭐  TRK FAVORITES",v->showFavorites());
        addButton(box,"⚙  SETTINGS",v->showSettings());
        addButton(box,"🛠  DEVICE DIAGNOSTICS",v->showDiagnostics());
        status=text("Ready • D-pad + OK • BACK returns to TRK home",14);box.addView(status);
        if(box.getChildCount()>3)box.getChildAt(3).requestFocus();
    }

    private void search(){ String q=input==null?"":input.getText().toString().trim();if(q.length()==0){Toast.makeText(this,"Type something to search",Toast.LENGTH_SHORT).show();return;}String id=extractId(q);if(id!=null){openPlayer(id);return;}prefs.edit().putString("last_search",q).apply();openYouTube("https://www.youtube.com/results?search_query="+Uri.encode(q)); }
    private void playInput(){ String s=input==null?"":input.getText().toString().trim();String id=extractId(s);if(id==null){Toast.makeText(this,"Paste a YouTube URL or 11-character video ID",Toast.LENGTH_LONG).show();return;}openPlayer(id); }
    private String extractId(String s){ if(s==null)return null;if(s.matches("[A-Za-z0-9_-]{11}"))return s;String[] marks={"youtu.be/","v=","/embed/","/shorts/","/live/"};for(String m:marks){int p=s.indexOf(m);if(p>=0){String x=s.substring(p+m.length());for(char c:new char[]{'&','?','#','/'}){int e=x.indexOf(c);if(e>=0)x=x.substring(0,e);}if(x.matches("[A-Za-z0-9_-]{11}"))return x;}}return null; }

    private void setupWeb(){
        destroyWeb();webView=new WebView(this);WebSettings w=webView.getSettings();w.setJavaScriptEnabled(true);w.setDomStorageEnabled(true);w.setDatabaseEnabled(true);w.setMediaPlaybackRequiresUserGesture(false);w.setLoadWithOverviewMode(true);w.setUseWideViewPort(true);w.setBuiltInZoomControls(false);w.setDisplayZoomControls(false);w.setSupportZoom(false);w.setJavaScriptCanOpenWindowsAutomatically(true);
        if(Build.VERSION.SDK_INT>=21){CookieManager.getInstance().setAcceptThirdPartyCookies(webView,true);w.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);}
        String ua=prefs.getBoolean("desktop_mode",false)?"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 Chrome/120 Safari/537.36":"Mozilla/5.0 (Linux; Android "+Build.VERSION.RELEASE+"; "+Build.MODEL+") AppleWebKit/537.36 Mobile Safari/537.36";w.setUserAgentString(ua);
        webView.setBackgroundColor(Color.BLACK);webView.setWebChromeClient(new WebChromeClient());webView.setWebViewClient(new WebViewClient(){
            @Override public boolean shouldOverrideUrlLoading(WebView view,String url){return handleUrl(view,url);}
            @Override public boolean shouldOverrideUrlLoading(WebView view,WebResourceRequest req){return handleUrl(view,req.getUrl().toString());}
        });
    }
    private boolean handleUrl(WebView view,String url){ String id=extractId(url);if(id!=null&&(url.contains("watch")||url.contains("youtu.be")||url.contains("shorts")||url.contains("live"))){openPlayer(id);return true;}return false; }
    private void openYouTube(String url){webOpen=true;diagnosticsOpen=false;setupWeb();LinearLayout frame=new LinearLayout(this);frame.setOrientation(LinearLayout.VERTICAL);frame.setBackgroundColor(Color.BLACK);LinearLayout bar=new LinearLayout(this);bar.setGravity(Gravity.CENTER_VERTICAL);Button home=button("⌂ TRK");home.setOnClickListener(v->showHome());bar.addView(home,new LinearLayout.LayoutParams(120,54));Button back=button("←");back.setOnClickListener(v->{if(webView!=null&&webView.canGoBack())webView.goBack();else showHome();});bar.addView(back,new LinearLayout.LayoutParams(80,54));TextView t=text("YouTube",17);bar.addView(t,new LinearLayout.LayoutParams(0,54,1));frame.addView(bar);frame.addView(webView,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0,1));setContentView(frame);webView.loadUrl(url);webView.requestFocus(); }

    private void openPlayer(String id){
        currentVideoId=id;addDelimited("history",id);webOpen=true;diagnosticsOpen=false;setupWeb();LinearLayout frame=new LinearLayout(this);frame.setOrientation(LinearLayout.VERTICAL);frame.setBackgroundColor(Color.BLACK);
        LinearLayout bar=new LinearLayout(this);Button home=button("⌂ HOME");home.setOnClickListener(v->showHome());bar.addView(home,new LinearLayout.LayoutParams(130,54));Button fav=button(isIn("favorites",id)?"★ SAVED":"☆ FAVORITE");fav.setOnClickListener(v->{toggleDelimited("favorites",id);((Button)v).setText(isIn("favorites",id)?"★ SAVED":"☆ FAVORITE");});bar.addView(fav,new LinearLayout.LayoutParams(170,54));TextView tip=text("OK/Play: YouTube controls  •  BACK: previous",14);bar.addView(tip,new LinearLayout.LayoutParams(0,54,1));frame.addView(bar);frame.addView(webView,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0,1));setContentView(frame);
        String html="<!doctype html><html><head><meta name='viewport' content='width=device-width,initial-scale=1,maximum-scale=1'><style>html,body,#p{width:100%;height:100%;margin:0;background:#000;overflow:hidden}</style></head><body><iframe id='p' src='https://www.youtube.com/embed/"+id+"?autoplay=1&playsinline=1&rel=0&enablejsapi=1&origin=https%3A%2F%2Fwww.youtube.com' frameborder='0' allow='autoplay;encrypted-media;picture-in-picture;fullscreen' allowfullscreen></iframe></body></html>";
        webView.loadDataWithBaseURL("https://www.youtube.com/",html,"text/html","UTF-8",null);webView.requestFocus();
    }

    private void showHistory(){ showIdList("TRK WATCH HISTORY","history",false); }
    private void showFavorites(){ showIdList("TRK FAVORITES","favorites",true); }
    private void showIdList(String title,String key,boolean removable){ destroyWeb();webOpen=false;LinearLayout box=page();heading(box,title);List<String> ids=getDelimited(key);if(ids.size()==0)box.addView(text("Nothing here yet. Videos you play/save will appear here.",17));for(String id:ids){Button b=button("▶  "+id);b.setOnClickListener(v->openPlayer(id));box.addView(b,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,62));}if(ids.size()>0){addButton(box,"🗑  CLEAR "+title,v->{prefs.edit().remove(key).apply();showIdList(title,key,removable);});}addButton(box,"← BACK",v->showHome()); }
    private List<String> getDelimited(String key){String s=prefs.getString(key,"");List<String> out=new ArrayList<>();if(s.length()>0)for(String x:s.split(","))if(x.length()>0)out.add(x);return out;}
    private boolean isIn(String key,String id){return getDelimited(key).contains(id);}
    private void addDelimited(String key,String id){List<String> l=getDelimited(key);l.remove(id);l.add(0,id);while(l.size()>30)l.remove(l.size()-1);prefs.edit().putString(key,join(l)).apply();}
    private void toggleDelimited(String key,String id){List<String> l=getDelimited(key);if(l.contains(id))l.remove(id);else l.add(0,id);prefs.edit().putString(key,join(l)).apply();}
    private String join(List<String> l){StringBuilder b=new StringBuilder();for(String x:l){if(b.length()>0)b.append(',');b.append(x);}return b.toString();}

    private void showSettings(){destroyWeb();webOpen=false;LinearLayout box=page();heading(box,"SETTINGS");boolean desk=prefs.getBoolean("desktop_mode",false);addButton(box,(desk?"✓ ":"")+"Desktop Web Compatibility Mode",v->{prefs.edit().putBoolean("desktop_mode",!prefs.getBoolean("desktop_mode",false)).apply();showSettings();});addButton(box,"Clear YouTube cookies",v->{CookieManager.getInstance().removeAllCookie();CookieManager.getInstance().removeSessionCookie();Toast.makeText(this,"Cookies cleared",Toast.LENGTH_SHORT).show();});addButton(box,"Clear TRK history",v->{prefs.edit().remove("history").apply();Toast.makeText(this,"History cleared",Toast.LENGTH_SHORT).show();});addButton(box,"About TRK YouTube TV",v->Toast.makeText(this,"TRK YouTube TV v"+VERSION+" • Android API 19+",Toast.LENGTH_LONG).show());addButton(box,"← BACK",v->showHome()); }

    private void showDiagnostics(){
        destroyWeb();webOpen=false;diagnosticsOpen=true;LinearLayout box=page();heading(box,"DEVICE DIAGNOSTICS");String abi=Build.VERSION.SDK_INT>=21?Build.SUPPORTED_ABIS[0]:Build.CPU_ABI;String web="System/legacy WebView";if(Build.VERSION.SDK_INT>=26){try{android.content.pm.PackageInfo p=WebView.getCurrentWebViewPackage();if(p!=null)web=p.packageName+" "+p.versionName;}catch(Throwable ignored){}}
        ConnectivityManager cm=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);NetworkInfo ni=cm==null?null:cm.getActiveNetworkInfo();String d=String.format(Locale.US,"Android: %s\nSDK/API: %d\nManufacturer: %s\nModel: %s\nBoard: %s\nCPU ABI: %s\nWebView: %s\nNetwork: %s\nDesktop mode: %s\n\nPackage: com.tvrepairkathmandu.trkyoutube\nApp version: %s\nMinimum API: 19 (Android 4.4)\n\nREMOTE TEST\nPress remote keys. Last key appears below.",Build.VERSION.RELEASE,Build.VERSION.SDK_INT,Build.MANUFACTURER,Build.MODEL,Build.BOARD,abi,web,(ni!=null&&ni.isConnected())?"CONNECTED":"DISCONNECTED",prefs.getBoolean("desktop_mode",false)?"ON":"OFF",VERSION);box.addView(text(d,17));status=text("Last key: none",19);box.addView(status);addButton(box,"TEST YOUTUBE CONNECTION",v->openYouTube("https://www.youtube.com/"));addButton(box,"← BACK",v->showHome());
    }

    private void destroyWeb(){if(webView!=null){try{webView.stopLoading();webView.loadUrl("about:blank");webView.clearHistory();webView.removeAllViews();webView.destroy();}catch(Throwable ignored){}webView=null;}}
    @Override public boolean dispatchKeyEvent(KeyEvent e){if(e.getAction()==KeyEvent.ACTION_DOWN&&diagnosticsOpen&&status!=null)status.setText("Last key: "+KeyEvent.keyCodeToString(e.getKeyCode())+" ("+e.getKeyCode()+")");return super.dispatchKeyEvent(e);}
    @Override public void onBackPressed(){if(webOpen&&webView!=null&&webView.canGoBack()){webView.goBack();return;}showHome();}
    @Override protected void onDestroy(){destroyWeb();super.onDestroy();}
}
