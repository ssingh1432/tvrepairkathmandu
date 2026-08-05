package com.tvrepairkathmandu.trkyoutube;

import android.app.*;
import android.os.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.content.*;
import android.net.*;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.webkit.*;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.net.*;
import java.util.*;
import com.google.zxing.*;
import com.google.zxing.common.BitMatrix;

public class MainActivity extends Activity {
    private static final String VERSION="0.4.0";
    private static final String PREFS="trk_youtube";
    private SharedPreferences prefs;
    private WebView web;
    private EditText search;
    private LinearLayout content;
    private boolean webOpen=false;
    private int dp(float n){return (int)(n*getResources().getDisplayMetrics().density+0.5f);} 
    private int sw(){return getResources().getConfiguration().screenWidthDp;}

    static class VideoItem {
        String id,title,channel,thumb;
        VideoItem(String i,String t,String c,String th){id=i;title=t;channel=c;thumb=th;}
    }

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        prefs=getSharedPreferences(PREFS,MODE_PRIVATE);
        immersive();
        showWelcome();
    }

    private void immersive(){getWindow().getDecorView().setSystemUiVisibility(5894);}
    private TextView t(String s,int z){TextView v=new TextView(this);v.setText(s);v.setTextColor(Color.WHITE);v.setTextSize(z);v.setPadding(dp(10),dp(6),dp(10),dp(6));return v;}
    private GradientDrawable bg(int c,int r){GradientDrawable g=new GradientDrawable();g.setColor(c);g.setCornerRadius(dp(r));return g;}
    private Button b(String s){Button v=new Button(this);v.setText(s);v.setTextColor(Color.WHITE);v.setTextSize(sw()<700?14:16);v.setAllCaps(false);v.setSingleLine(true);v.setEllipsize(android.text.TextUtils.TruncateAt.END);v.setFocusable(true);v.setBackgroundResource(R.drawable.focus_bg);return v;}

    private void showWelcome(){
        destroyWeb(); webOpen=false;
        ScrollView sc=new ScrollView(this);sc.setFillViewport(true);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setGravity(Gravity.CENTER_HORIZONTAL);
        int p=dp(sw()<700?24:54);root.setPadding(p,dp(28),p,dp(28));root.setBackgroundColor(Color.rgb(10,10,10));sc.addView(root);
        TextView logo=t("▶  TRK YouTube TV",sw()<700?28:34);logo.setTypeface(null,Typeface.BOLD);root.addView(logo);
        root.addView(t("Native TV experience for old & new Android TVs",18));
        root.addView(t("v"+VERSION+"  •  Android 4.4+",14));
        root.addView(new Space(this),new LinearLayout.LayoutParams(1,dp(22)));
        Button guest=b("WATCH AS GUEST");guest.setOnClickListener(v->showHome());root.addView(guest,new LinearLayout.LayoutParams(dp(420),dp(62)));
        Button signin=b("SIGN IN WITH PHONE / QR");signin.setOnClickListener(v->showSignIn());root.addView(signin,new LinearLayout.LayoutParams(dp(420),dp(62)));
        Button setup=b("YOUTUBE SETUP");setup.setOnClickListener(v->showSetup());root.addView(setup,new LinearLayout.LayoutParams(dp(420),dp(62)));
        Button browser=b("WEB FALLBACK");browser.setOnClickListener(v->openWeb("https://www.youtube.com/"));root.addView(browser,new LinearLayout.LayoutParams(dp(420),dp(62)));
        setContentView(sc);guest.requestFocus();
    }

    private void showHome(){
        destroyWeb();webOpen=false;
        LinearLayout shell=new LinearLayout(this);shell.setOrientation(LinearLayout.HORIZONTAL);shell.setPadding(dp(12),dp(12),dp(12),dp(12));shell.setBackgroundColor(Color.rgb(8,8,8));
        int navW=sw()<700?dp(170):dp(220);
        LinearLayout nav=new LinearLayout(this);nav.setOrientation(LinearLayout.VERTICAL);nav.setPadding(dp(2),dp(4),dp(8),dp(4));
        TextView logo=t("▶ TRK",22);logo.setTypeface(null,Typeface.BOLD);nav.addView(logo);
        addNav(nav,"⌂  Home",v->showHome());
        addNav(nav,"⌕  Search",v->{if(search!=null)search.requestFocus();});
        addNav(nav,"🔥  Explore",v->loadCategory("Trending",""));
        addNav(nav,"▣  Subscriptions",v->showSubscriptions());
        addNav(nav,"◷  History",v->showLocalHistory());
        addNav(nav,"★  Favorites",v->showFavorites());
        addNav(nav,"☺  Account",v->showAccount());
        addNav(nav,"⚙  Settings",v->showSettings());
        shell.addView(nav,new LinearLayout.LayoutParams(navW,-1));

        ScrollView sc=new ScrollView(this);content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);content.setPadding(dp(14),dp(2),dp(8),dp(30));sc.addView(content);shell.addView(sc,new LinearLayout.LayoutParams(0,-1,1));
        LinearLayout top=new LinearLayout(this);
        search=new EditText(this);search.setSingleLine();search.setHint("Search YouTube");search.setHintTextColor(Color.LTGRAY);search.setTextColor(Color.WHITE);search.setTextSize(17);search.setImeOptions(EditorInfo.IME_ACTION_SEARCH);search.setBackground(bg(Color.rgb(38,38,38),22));search.setOnEditorActionListener((v,a,e)->{doNativeSearch();return true;});
        top.addView(search,new LinearLayout.LayoutParams(0,dp(54),1));
        Button go=b("SEARCH");go.setOnClickListener(v->doNativeSearch());top.addView(go,new LinearLayout.LayoutParams(dp(110),dp(54)));content.addView(top);
        TextView h=t("Home",sw()<700?24:29);h.setTypeface(null,Typeface.BOLD);content.addView(h);
        setContentView(shell);nav.getChildAt(1).requestFocus();

        if(apiKey().length()==0){showApiNeededInline();}
        else loadPopularInto(content,"Popular now");
    }

    private void addNav(LinearLayout n,String s,View.OnClickListener l){Button x=b(s);x.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);x.setOnClickListener(l);n.addView(x,new LinearLayout.LayoutParams(-1,dp(52)));}

    private void showApiNeededInline(){
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(18),dp(18),dp(18),dp(18));box.setBackground(bg(Color.rgb(28,28,28),16));
        TextView h=t("Connect YouTube Data",22);h.setTypeface(null,Typeface.BOLD);box.addView(h);
        box.addView(t("Native Home and Search need a YouTube Data API key. Add it once in YouTube Setup. The key stays on this device.",16));
        Button setup=b("OPEN YOUTUBE SETUP");setup.setOnClickListener(v->showSetup());box.addView(setup,new LinearLayout.LayoutParams(-1,dp(58)));
        Button fallback=b("USE WEB FALLBACK");fallback.setOnClickListener(v->openWeb("https://www.youtube.com/"));box.addView(fallback,new LinearLayout.LayoutParams(-1,dp(58)));
        content.addView(box,new LinearLayout.LayoutParams(-1,-2));
        setup.requestFocus();
    }

    private void loadPopularInto(final LinearLayout target,final String heading){
        final TextView loading=t("Loading real YouTube videos…",17);target.addView(loading);
        new AsyncTask<Void,Void,Object>(){
            protected Object doInBackground(Void...v){try{return fetchPopular();}catch(Exception e){return e;}}
            protected void onPostExecute(Object o){target.removeView(loading);if(o instanceof Exception){showDataError(target,(Exception)o);return;}addVideoRows(target,heading,(List<VideoItem>)o);}
        }.execute();
    }

    private List<VideoItem> fetchPopular() throws Exception{
        String url="https://www.googleapis.com/youtube/v3/videos?part=snippet&chart=mostPopular&maxResults=20&regionCode="+Uri.encode(region())+"&key="+Uri.encode(apiKey());
        JSONObject root=new JSONObject(http(url,null));JSONArray arr=root.getJSONArray("items");List<VideoItem> out=new ArrayList<>();
        for(int i=0;i<arr.length();i++){JSONObject x=arr.getJSONObject(i),s=x.getJSONObject("snippet");String id=x.getString("id");JSONObject thumbs=s.getJSONObject("thumbnails");String th=thumbUrl(thumbs);out.add(new VideoItem(id,s.optString("title"),s.optString("channelTitle"),th));}
        return out;
    }

    private List<VideoItem> fetchSearch(String q) throws Exception{
        String url="https://www.googleapis.com/youtube/v3/search?part=snippet&type=video&maxResults=20&q="+Uri.encode(q)+"&key="+Uri.encode(apiKey());
        JSONObject root=new JSONObject(http(url,null));JSONArray arr=root.getJSONArray("items");List<VideoItem> out=new ArrayList<>();
        for(int i=0;i<arr.length();i++){JSONObject x=arr.getJSONObject(i),s=x.getJSONObject("snippet");String id=x.getJSONObject("id").optString("videoId");if(id.length()==0)continue;String th=thumbUrl(s.getJSONObject("thumbnails"));out.add(new VideoItem(id,decode(s.optString("title")),decode(s.optString("channelTitle")),th));}
        return out;
    }

    private String thumbUrl(JSONObject o){JSONObject q=o.optJSONObject("medium");if(q==null)q=o.optJSONObject("high");if(q==null)q=o.optJSONObject("default");return q==null?"":q.optString("url");}
    private String decode(String s){return s.replace("&amp;","&").replace("&quot;","\"").replace("&#39;","'");}

    private String http(String u,String bearer) throws Exception{
        HttpURLConnection c=(HttpURLConnection)new URL(u).openConnection();c.setConnectTimeout(12000);c.setReadTimeout(15000);c.setRequestProperty("Accept","application/json");if(bearer!=null)c.setRequestProperty("Authorization","Bearer "+bearer);int code=c.getResponseCode();InputStream in=code>=200&&code<300?c.getInputStream():c.getErrorStream();BufferedReader r=new BufferedReader(new InputStreamReader(in,"UTF-8"));StringBuilder b=new StringBuilder();String line;while((line=r.readLine())!=null)b.append(line);r.close();if(code<200||code>=300)throw new IOException("YouTube API HTTP "+code+": "+b.toString());return b.toString();
    }

    private void addVideoRows(LinearLayout target,String heading,List<VideoItem> list){
        TextView hh=t(heading,21);hh.setTypeface(null,Typeface.BOLD);target.addView(hh);
        for(int start=0;start<list.size();start+=5){HorizontalScrollView hs=new HorizontalScrollView(this);hs.setHorizontalScrollBarEnabled(false);LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);for(int i=start;i<Math.min(start+5,list.size());i++)row.addView(videoCard(list.get(i)));hs.addView(row);target.addView(hs,new LinearLayout.LayoutParams(-1,dp(205)));}
    }

    private View videoCard(final VideoItem item){
        int cw=dp(sw()<700?190:225);LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(5),dp(5),dp(5),dp(5));
        ImageView im=new ImageView(this);im.setScaleType(ImageView.ScaleType.CENTER_CROP);im.setBackground(bg(Color.rgb(35,35,35),10));im.setFocusable(true);im.setOnClickListener(v->openPlayer(item));card.addView(im,new LinearLayout.LayoutParams(cw,dp(120)));loadImage(item.thumb,im);
        TextView title=t(item.title,15);title.setMaxLines(2);title.setEllipsize(android.text.TextUtils.TruncateAt.END);title.setFocusable(true);title.setOnClickListener(v->openPlayer(item));card.addView(title,new LinearLayout.LayoutParams(cw,dp(48)));
        TextView ch=t(item.channel,12);ch.setTextColor(Color.LTGRAY);ch.setSingleLine(true);ch.setEllipsize(android.text.TextUtils.TruncateAt.END);card.addView(ch,new LinearLayout.LayoutParams(cw,dp(30)));
        return card;
    }

    private void loadImage(final String url,final ImageView im){if(url==null||url.length()==0)return;new AsyncTask<Void,Void,Bitmap>(){protected Bitmap doInBackground(Void...v){try{HttpURLConnection c=(HttpURLConnection)new URL(url).openConnection();c.setConnectTimeout(8000);c.setReadTimeout(8000);return BitmapFactory.decodeStream(c.getInputStream());}catch(Exception e){return null;}}protected void onPostExecute(Bitmap b){if(b!=null)im.setImageBitmap(b);}}.execute();}

    private void doNativeSearch(){
        if(search==null)return;String q=search.getText().toString().trim();if(q.length()==0)return;if(apiKey().length()==0){showSetup();Toast.makeText(this,"Add a YouTube API key first",Toast.LENGTH_LONG).show();return;}showSearchResults(q);
    }

    private void showSearchResults(final String q){
        destroyWeb();webOpen=false;LinearLayout r=page();LinearLayout bar=new LinearLayout(this);Button back=b("← HOME");back.setOnClickListener(v->showHome());bar.addView(back,new LinearLayout.LayoutParams(dp(120),dp(52)));TextView h=t("Search: "+q,24);h.setTypeface(null,Typeface.BOLD);bar.addView(h,new LinearLayout.LayoutParams(0,dp(52),1));r.addView(bar);
        final TextView loading=t("Searching YouTube…",18);r.addView(loading);
        new AsyncTask<Void,Void,Object>(){protected Object doInBackground(Void...v){try{return fetchSearch(q);}catch(Exception e){return e;}}protected void onPostExecute(Object o){r.removeView(loading);if(o instanceof Exception){showDataError(r,(Exception)o);return;}List<VideoItem> x=(List<VideoItem>)o;if(x.size()==0)r.addView(t("No videos found.",18));else addVideoRows(r,"Results",x);}}.execute();back.requestFocus();
    }

    private void showDataError(LinearLayout target,Exception e){
        String m=e.getMessage()==null?e.toString():e.getMessage();TextView h=t("Could not load YouTube data",20);h.setTypeface(null,Typeface.BOLD);target.addView(h);target.addView(t(m,14));Button setup=b("CHECK API KEY / SETUP");setup.setOnClickListener(v->showSetup());target.addView(setup,new LinearLayout.LayoutParams(-1,dp(58)));Button web=b("WEB FALLBACK");web.setOnClickListener(v->openWeb("https://www.youtube.com/"));target.addView(web,new LinearLayout.LayoutParams(-1,dp(58)));
    }

    private void loadCategory(String title,String q){if(q.length()==0){showHome();return;}showSearchResults(q);}

    private void openPlayer(final VideoItem item){
        addHistory(item);destroyWeb();webOpen=true;setupWeb();LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.VERTICAL);r.setBackgroundColor(Color.BLACK);
        LinearLayout bar=new LinearLayout(this);Button home=b("⌂ HOME");home.setOnClickListener(v->showHome());bar.addView(home,new LinearLayout.LayoutParams(dp(120),dp(50)));Button fav=b(isFavorite(item.id)?"★ SAVED":"☆ SAVE");fav.setOnClickListener(v->{toggleFavorite(item);((Button)v).setText(isFavorite(item.id)?"★ SAVED":"☆ SAVE");});bar.addView(fav,new LinearLayout.LayoutParams(dp(120),dp(50)));TextView tx=t(item.title,15);tx.setSingleLine(true);tx.setEllipsize(android.text.TextUtils.TruncateAt.END);bar.addView(tx,new LinearLayout.LayoutParams(0,dp(50),1));r.addView(bar);r.addView(web,new LinearLayout.LayoutParams(-1,0,1));setContentView(r);
        String html="<!doctype html><html><head><meta name='viewport' content='width=device-width,initial-scale=1,maximum-scale=1'><style>html,body,#p{width:100%;height:100%;margin:0;background:#000;overflow:hidden}</style></head><body><iframe id='p' src='https://www.youtube.com/embed/"+item.id+"?autoplay=1&playsinline=1&rel=0&enablejsapi=1&origin=https%3A%2F%2Fwww.youtube.com' frameborder='0' allow='autoplay;encrypted-media;picture-in-picture;fullscreen' allowfullscreen></iframe></body></html>";
        web.loadDataWithBaseURL("https://www.youtube.com/",html,"text/html","UTF-8",null);web.requestFocus();
    }

    private void setupWeb(){destroyWeb();web=new WebView(this);WebSettings s=web.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setDatabaseEnabled(true);s.setMediaPlaybackRequiresUserGesture(false);s.setLoadWithOverviewMode(true);s.setUseWideViewPort(true);if(Build.VERSION.SDK_INT>=21)CookieManager.getInstance().setAcceptThirdPartyCookies(web,true);web.setBackgroundColor(Color.BLACK);web.setWebChromeClient(new WebChromeClient());web.setWebViewClient(new WebViewClient());}

    private void openWeb(String url){webOpen=true;setupWeb();LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.VERTICAL);r.setBackgroundColor(Color.BLACK);LinearLayout bar=new LinearLayout(this);Button home=b("⌂ HOME");home.setOnClickListener(v->showHome());bar.addView(home,new LinearLayout.LayoutParams(dp(125),dp(50)));Button back=b("← BACK");back.setOnClickListener(v->{if(web.canGoBack())web.goBack();else showHome();});bar.addView(back,new LinearLayout.LayoutParams(dp(110),dp(50)));bar.addView(t("Web fallback",16),new LinearLayout.LayoutParams(0,dp(50),1));r.addView(bar);r.addView(web,new LinearLayout.LayoutParams(-1,0,1));setContentView(r);web.loadUrl(url);}

    private void showSetup(){
        destroyWeb();webOpen=false;LinearLayout r=page();TextView h=t("YouTube Setup",30);h.setTypeface(null,Typeface.BOLD);r.addView(h);r.addView(t("For native Home/Search, paste a YouTube Data API v3 key. For real Google TV sign-in later, paste a TV/Limited Input OAuth client ID. These values are stored only in this app's private settings on the device.",16));
        EditText key=new EditText(this);key.setHint("YouTube Data API key");key.setText(apiKey());key.setTextColor(Color.WHITE);key.setHintTextColor(Color.LTGRAY);key.setSingleLine(true);key.setBackground(bg(Color.rgb(38,38,38),16));r.addView(key,new LinearLayout.LayoutParams(-1,dp(58)));
        EditText client=new EditText(this);client.setHint("TV OAuth client ID (optional for now)");client.setText(clientId());client.setTextColor(Color.WHITE);client.setHintTextColor(Color.LTGRAY);client.setSingleLine(true);client.setBackground(bg(Color.rgb(38,38,38),16));r.addView(client,new LinearLayout.LayoutParams(-1,dp(58)));
        EditText reg=new EditText(this);reg.setHint("Region code, e.g. NP");reg.setText(region());reg.setTextColor(Color.WHITE);reg.setHintTextColor(Color.LTGRAY);reg.setSingleLine(true);reg.setBackground(bg(Color.rgb(38,38,38),16));r.addView(reg,new LinearLayout.LayoutParams(-1,dp(58)));
        Button save=b("SAVE & TEST NATIVE HOME");save.setOnClickListener(v->{prefs.edit().putString("api_key",key.getText().toString().trim()).putString("oauth_client_id",client.getText().toString().trim()).putString("region",reg.getText().toString().trim().toUpperCase(Locale.US)).apply();showHome();});r.addView(save,new LinearLayout.LayoutParams(-1,dp(60)));
        Button clear=b("CLEAR CREDENTIALS");clear.setOnClickListener(v->{prefs.edit().remove("api_key").remove("oauth_client_id").apply();key.setText("");client.setText("");Toast.makeText(this,"Credentials cleared",Toast.LENGTH_SHORT).show();});r.addView(clear,new LinearLayout.LayoutParams(-1,dp(60)));
        Button back=b("← BACK");back.setOnClickListener(v->showHome());r.addView(back,new LinearLayout.LayoutParams(-1,dp(60)));save.requestFocus();
    }

    private String apiKey(){return prefs.getString("api_key","").trim();}
    private String clientId(){return prefs.getString("oauth_client_id","").trim();}
    private String region(){String x=prefs.getString("region","NP").trim().toUpperCase(Locale.US);return x.length()==2?x:"NP";}

    private void showSignIn(){
        destroyWeb();webOpen=false;LinearLayout r=page();TextView h=t("Sign in with your phone",28);h.setTypeface(null,Typeface.BOLD);r.addView(h);
        if(clientId().length()==0){r.addView(t("A Google TV/Limited Input OAuth client ID has not been configured yet. Add it in YouTube Setup. Until then you can use Guest mode with native Home/Search.",17));ImageView qr=new ImageView(this);qr.setImageBitmap(qr("https://www.google.com/device",190));r.addView(qr,new LinearLayout.LayoutParams(dp(205),dp(205)));r.addView(t("google.com/device",19));Button setup=b("OPEN YOUTUBE SETUP");setup.setOnClickListener(v->showSetup());r.addView(setup,new LinearLayout.LayoutParams(-1,dp(58)));Button guest=b("CONTINUE AS GUEST");guest.setOnClickListener(v->showHome());r.addView(guest,new LinearLayout.LayoutParams(-1,dp(58)));setup.requestFocus();return;}
        r.addView(t("OAuth client ID is configured. Device authorization wiring is the next account milestone; native guest Home/Search already work independently.",17));ImageView qr=new ImageView(this);qr.setImageBitmap(qr("https://www.google.com/device",190));r.addView(qr,new LinearLayout.LayoutParams(dp(205),dp(205)));r.addView(t("google.com/device",19));Button back=b("← HOME");back.setOnClickListener(v->showHome());r.addView(back,new LinearLayout.LayoutParams(-1,dp(58)));back.requestFocus();
    }

    private Bitmap qr(String data,int sizeDp){int size=dp(sizeDp);try{BitMatrix m=new MultiFormatWriter().encode(data,BarcodeFormat.QR_CODE,size,size);Bitmap bm=Bitmap.createBitmap(size,size,Bitmap.Config.RGB_565);for(int y=0;y<size;y++)for(int x=0;x<size;x++)bm.setPixel(x,y,m.get(x,y)?Color.BLACK:Color.WHITE);return bm;}catch(Exception e){return Bitmap.createBitmap(1,1,Bitmap.Config.RGB_565);}}

    private LinearLayout page(){ScrollView s=new ScrollView(this);s.setFillViewport(true);LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.VERTICAL);int p=dp(sw()<700?28:54);r.setPadding(p,dp(24),p,dp(24));r.setBackgroundColor(Color.rgb(10,10,10));s.addView(r);setContentView(s);return r;}

    private void showAccount(){LinearLayout r=page();TextView h=t("Account",30);h.setTypeface(null,Typeface.BOLD);r.addView(h);r.addView(t(clientId().length()>0?"TV OAuth client configured":"Guest mode",18));Button sign=b("SIGN IN WITH PHONE / QR");sign.setOnClickListener(v->showSignIn());r.addView(sign,new LinearLayout.LayoutParams(-1,dp(60)));Button setup=b("YOUTUBE SETUP");setup.setOnClickListener(v->showSetup());r.addView(setup,new LinearLayout.LayoutParams(-1,dp(60)));Button back=b("← HOME");back.setOnClickListener(v->showHome());r.addView(back,new LinearLayout.LayoutParams(-1,dp(60)));sign.requestFocus();}

    private void showSubscriptions(){LinearLayout r=page();TextView h=t("Subscriptions",30);h.setTypeface(null,Typeface.BOLD);r.addView(h);r.addView(t("Subscriptions require Google account authorization. Guest Home/Search remain fully native.",17));Button sign=b("SIGN IN / ACCOUNT");sign.setOnClickListener(v->showSignIn());r.addView(sign,new LinearLayout.LayoutParams(-1,dp(60)));Button back=b("← HOME");back.setOnClickListener(v->showHome());r.addView(back,new LinearLayout.LayoutParams(-1,dp(60)));sign.requestFocus();}

    private void addHistory(VideoItem v){List<VideoItem> l=readItems("history");for(int i=l.size()-1;i>=0;i--)if(l.get(i).id.equals(v.id))l.remove(i);l.add(0,v);while(l.size()>30)l.remove(l.size()-1);writeItems("history",l);}
    private boolean isFavorite(String id){for(VideoItem x:readItems("favorites"))if(x.id.equals(id))return true;return false;}
    private void toggleFavorite(VideoItem v){List<VideoItem> l=readItems("favorites");for(int i=0;i<l.size();i++)if(l.get(i).id.equals(v.id)){l.remove(i);writeItems("favorites",l);return;}l.add(0,v);writeItems("favorites",l);}
    private List<VideoItem> readItems(String key){List<VideoItem> out=new ArrayList<>();try{JSONArray a=new JSONArray(prefs.getString(key,"[]"));for(int i=0;i<a.length();i++){JSONObject o=a.getJSONObject(i);out.add(new VideoItem(o.optString("id"),o.optString("title"),o.optString("channel"),o.optString("thumb")));}}catch(Exception e){}return out;}
    private void writeItems(String key,List<VideoItem> l){JSONArray a=new JSONArray();try{for(VideoItem x:l){JSONObject o=new JSONObject();o.put("id",x.id);o.put("title",x.title);o.put("channel",x.channel);o.put("thumb",x.thumb);a.put(o);}}catch(Exception e){}prefs.edit().putString(key,a.toString()).apply();}

    private void showLocalHistory(){showLocalList("Watch history","history");}
    private void showFavorites(){showLocalList("Favorites","favorites");}
    private void showLocalList(String heading,String key){LinearLayout r=page();TextView h=t(heading,30);h.setTypeface(null,Typeface.BOLD);r.addView(h);List<VideoItem> list=readItems(key);if(list.size()==0)r.addView(t("Nothing here yet.",17));else addVideoRows(r,heading,list);Button clear=b("CLEAR "+heading.toUpperCase(Locale.US));clear.setOnClickListener(v->{prefs.edit().remove(key).apply();showLocalList(heading,key);});r.addView(clear,new LinearLayout.LayoutParams(-1,dp(58)));Button back=b("← HOME");back.setOnClickListener(v->showHome());r.addView(back,new LinearLayout.LayoutParams(-1,dp(58)));back.requestFocus();}

    private void showSettings(){LinearLayout r=page();TextView h=t("Settings",30);h.setTypeface(null,Typeface.BOLD);r.addView(h);r.addView(t("TRK YouTube TV v"+VERSION+"\nAndroid "+Build.VERSION.RELEASE+" / API "+Build.VERSION.SDK_INT+"\nModel: "+Build.MANUFACTURER+" "+Build.MODEL+"\nNative API: "+(apiKey().length()>0?"CONFIGURED":"NOT CONFIGURED")+"\nRegion: "+region()+"\nMinimum Android: 4.4 / API 19",17));Button setup=b("YOUTUBE SETUP");setup.setOnClickListener(v->showSetup());r.addView(setup,new LinearLayout.LayoutParams(-1,dp(60)));Button cookies=b("CLEAR YOUTUBE PLAYER COOKIES");cookies.setOnClickListener(v->{CookieManager.getInstance().removeAllCookie();CookieManager.getInstance().removeSessionCookie();Toast.makeText(this,"Cookies cleared",Toast.LENGTH_SHORT).show();});r.addView(cookies,new LinearLayout.LayoutParams(-1,dp(60)));Button welcome=b("PROFILE / START SCREEN");welcome.setOnClickListener(v->showWelcome());r.addView(welcome,new LinearLayout.LayoutParams(-1,dp(60)));Button back=b("← HOME");back.setOnClickListener(v->showHome());r.addView(back,new LinearLayout.LayoutParams(-1,dp(60)));setup.requestFocus();}

    private void destroyWeb(){if(web!=null){try{web.stopLoading();web.loadUrl("about:blank");web.removeAllViews();web.destroy();}catch(Throwable e){}web=null;}}
    @Override public void onBackPressed(){if(webOpen&&web!=null&&web.canGoBack()){web.goBack();return;}showHome();}
    @Override protected void onDestroy(){destroyWeb();super.onDestroy();}
}
