package com.tvrepairkathmandu.trkyoutube;

import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
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

public class MainActivity extends Activity {
    private LinearLayout root;
    private WebView webView;
    private EditText input;
    private TextView status;
    private boolean playerOpen = false;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        showHome();
    }

    private TextView text(String s, int size) {
        TextView v = new TextView(this); v.setText(s); v.setTextColor(Color.WHITE); v.setTextSize(size); v.setPadding(18,12,18,12); return v;
    }
    private Button button(String s) {
        Button b = new Button(this); b.setText(s); b.setTextColor(Color.WHITE); b.setTextSize(18); b.setBackgroundResource(com.tvrepairkathmandu.trkyoutube.R.drawable.focus_bg); b.setFocusable(true); b.setAllCaps(false); return b;
    }
    private void showHome() {
        playerOpen=false;
        ScrollView sc=new ScrollView(this); root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(36,24,36,24); root.setBackgroundColor(Color.rgb(16,16,16)); sc.addView(root); setContentView(sc);
        TextView title=text("TRK YouTube TV",30); title.setTypeface(Typeface.DEFAULT,Typeface.BOLD); root.addView(title);
        root.addView(text("Android 4.4+ TV compatibility build  •  v0.1",16));
        input=new EditText(this); input.setHint("Paste YouTube URL or 11-character video ID"); input.setTextColor(Color.WHITE); input.setHintTextColor(Color.LTGRAY); input.setSingleLine(true); input.setTextSize(18); input.setFocusable(true); root.addView(input,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,70));
        Button play=button("▶  PLAY VIDEO"); play.setOnClickListener(v->playInput()); root.addView(play);
        Button search=button("🔎  SEARCH YOUTUBE"); search.setOnClickListener(v->searchYouTube()); root.addView(search);
        Button diag=button("⚙  TRK DEVICE DIAGNOSTICS"); diag.setOnClickListener(v->showDiagnostics()); root.addView(diag);
        status=text("Ready. Use the TV remote D-pad and OK button.",16); root.addView(status);
        play.requestFocus();
    }
    private void playInput(){ String id=extractId(input.getText().toString().trim()); if(id==null){Toast.makeText(this,"Enter a valid YouTube URL/video ID",Toast.LENGTH_LONG).show();return;} openPlayer(id); }
    private String extractId(String s){ if(s.matches("[A-Za-z0-9_-]{11}"))return s; String[] marks={"youtu.be/","v=","/embed/","/shorts/"}; for(String m:marks){int p=s.indexOf(m);if(p>=0){String x=s.substring(p+m.length());int e=x.indexOf('&');if(e>=0)x=x.substring(0,e);e=x.indexOf('?');if(e>=0)x=x.substring(0,e);e=x.indexOf('/');if(e>=0)x=x.substring(0,e);if(x.matches("[A-Za-z0-9_-]{11}"))return x;}} return null; }
    private void setupWeb(){ webView=new WebView(this); WebSettings w=webView.getSettings(); w.setJavaScriptEnabled(true); w.setDomStorageEnabled(true); w.setMediaPlaybackRequiresUserGesture(false); w.setLoadWithOverviewMode(true); w.setUseWideViewPort(true); webView.setBackgroundColor(Color.BLACK); webView.setWebChromeClient(new WebChromeClient()); webView.setWebViewClient(new WebViewClient()); }
    private void openPlayer(String id){ playerOpen=true; setupWeb(); setContentView(webView); String html="<!doctype html><html><head><meta name='viewport' content='width=device-width,initial-scale=1'></head><body style='margin:0;background:#000;overflow:hidden'><iframe width='100%' height='100%' src='https://www.youtube.com/embed/"+id+"?autoplay=1&playsinline=1' title='YouTube video player' frameborder='0' allow='autoplay; encrypted-media; picture-in-picture' allowfullscreen></iframe></body></html>"; webView.loadDataWithBaseURL("https://www.youtube.com/",html,"text/html","UTF-8",null); }
    private void searchYouTube(){ String q=input.getText().toString().trim(); if(q.length()==0){Toast.makeText(this,"Type search words first",Toast.LENGTH_SHORT).show();return;} playerOpen=true; setupWeb(); setContentView(webView); webView.loadUrl("https://www.youtube.com/results?search_query="+android.net.Uri.encode(q)); }
    private void showDiagnostics(){
        ScrollView sc=new ScrollView(this); LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(36,24,36,24); box.setBackgroundColor(Color.rgb(16,16,16)); sc.addView(box); setContentView(sc);
        TextView h=text("TRK DEVICE DIAGNOSTICS",28); h.setTypeface(Typeface.DEFAULT,Typeface.BOLD); box.addView(h);
        String abi=Build.VERSION.SDK_INT>=21?Build.SUPPORTED_ABIS[0]:Build.CPU_ABI;
        String web="Unknown"; if(Build.VERSION.SDK_INT>=26){try{android.content.pm.PackageInfo p=WebView.getCurrentWebViewPackage(); if(p!=null)web=p.packageName+" "+p.versionName;}catch(Throwable ignored){}}
        ConnectivityManager cm=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE); NetworkInfo ni=cm==null?null:cm.getActiveNetworkInfo();
        String d=String.format(Locale.US,"Android: %s\nSDK/API: %d\nManufacturer: %s\nModel: %s\nBoard: %s\nCPU ABI: %s\nWebView: %s\nNetwork: %s\n\nPackage: com.tvrepairkathmandu.trkyoutube\nMinimum API: 19 (Android 4.4)\n\nREMOTE TEST\nPress remote keys. Last key will appear below.",Build.VERSION.RELEASE,Build.VERSION.SDK_INT,Build.MANUFACTURER,Build.MODEL,Build.BOARD,abi,web,(ni!=null&&ni.isConnected())?"CONNECTED":"DISCONNECTED");
        box.addView(text(d,18)); status=text("Last key: none",20); box.addView(status); Button back=button("← BACK TO HOME"); back.setOnClickListener(v->showHome()); box.addView(back); back.requestFocus();
    }
    @Override public boolean dispatchKeyEvent(KeyEvent e){ if(e.getAction()==KeyEvent.ACTION_DOWN && status!=null && !playerOpen){status.setText("Last key: "+KeyEvent.keyCodeToString(e.getKeyCode())+" ("+e.getKeyCode()+")");} return super.dispatchKeyEvent(e); }
    @Override public void onBackPressed(){ if(playerOpen){ if(webView!=null){webView.stopLoading();webView.destroy();webView=null;} showHome(); } else showHome(); }
    @Override protected void onDestroy(){ if(webView!=null)webView.destroy(); super.onDestroy(); }
}
