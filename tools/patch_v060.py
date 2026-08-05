from pathlib import Path

p = Path('app/src/main/java/com/tvrepairkathmandu/trkyoutube/MainActivity.java')
s = p.read_text()

def req(old, new, label):
    global s
    if old not in s:
        raise SystemExit('Required source pattern missing: ' + label)
    s = s.replace(old, new)

# v0.5.1 proven foundation
req('private static final String VERSION="0.4.0";', 'private static final String VERSION="0.6.0";', 'version')
req('immersive();\n        showWelcome();', 'immersive();\n        if(apiKey().length()>0) showHome(); else showWelcome();', 'startup')
s=s.replace('TextView logo=t("▶  TRK YouTube TV",sw()<700?28:34);','TextView logo=t("▶  YouTube",sw()<700?28:34);')
s=s.replace('root.addView(t("Native TV experience for old & new Android TVs",18));','root.addView(t("Watch videos on your TV",18));')
s=s.replace('        Button setup=b("YOUTUBE SETUP");setup.setOnClickListener(v->showSetup());root.addView(setup,new LinearLayout.LayoutParams(dp(420),dp(62)));\n','')
s=s.replace('        Button browser=b("WEB FALLBACK");browser.setOnClickListener(v->openWeb("https://www.youtube.com/"));root.addView(browser,new LinearLayout.LayoutParams(dp(420),dp(62)));\n','')
s=s.replace('TextView h=t("Connect YouTube Data",22);','TextView h=t("YouTube setup required",22);')
s=s.replace('box.addView(t("Native Home and Search need a YouTube Data API key. Add it once in YouTube Setup. The key stays on this device.",16));','box.addView(t("One-time service configuration is required. Open TRK Technician Setup and save the key once.",16));')
s=s.replace('Button setup=b("OPEN YOUTUBE SETUP");','Button setup=b("OPEN TRK TECHNICIAN SETUP");')
s=s.replace('        Button fallback=b("USE WEB FALLBACK");fallback.setOnClickListener(v->openWeb("https://www.youtube.com/"));box.addView(fallback,new LinearLayout.LayoutParams(-1,dp(58)));\n','')
s=s.replace('TextView h=t("YouTube Setup",30);','TextView h=t("TRK Technician Setup",30);')
s=s.replace('Button setup=b("YOUTUBE SETUP");setup.setOnClickListener(v->showSetup());r.addView(setup,new LinearLayout.LayoutParams(-1,dp(60)));','Button setup=b("TRK TECHNICIAN SETUP");setup.setOnClickListener(v->showSetup());r.addView(setup,new LinearLayout.LayoutParams(-1,dp(60)));')

# Proven playback identity/referrer behavior: do not alter beyond this known-good fix.
s=s.replace("<head><meta name='viewport'", "<head><meta name='referrer' content='strict-origin-when-cross-origin'><meta name='viewport'")
s=s.replace("origin=https%3A%2F%2Fwww.youtube.com'", "origin=https%3A%2F%2Fcom.tvrepairkathmandu.trkyoutube&widget_referrer=https%3A%2F%2Fcom.tvrepairkathmandu.trkyoutube'")
s=s.replace('web.loadDataWithBaseURL("https://www.youtube.com/",html','web.loadDataWithBaseURL("https://com.tvrepairkathmandu.trkyoutube/",html')

# Reliable key persistence
req('private String apiKey(){return prefs.getString("api_key","").trim();}', '''private String apiKey(){
        String k=prefs.getString("api_key","").trim();
        if(k.length()>0){backupApiKey(k);return k;}
        try{java.io.File f=new java.io.File(getFilesDir(),".trk_yt_key");if(f.exists()){java.io.BufferedReader br=new java.io.BufferedReader(new java.io.FileReader(f));String x=br.readLine();br.close();if(x!=null&&x.trim().length()>0){k=x.trim();prefs.edit().putString("api_key",k).apply();}}}catch(Exception ignored){}
        return k;
    }
    private void backupApiKey(String k){try{if(k==null||k.trim().length()==0)return;java.io.FileWriter w=new java.io.FileWriter(new java.io.File(getFilesDir(),".trk_yt_key"),false);w.write(k.trim());w.close();}catch(Exception ignored){}}''', 'apiKey')
req('prefs.edit().putString("api_key",key.getText().toString().trim()).putString("oauth_client_id",client.getText().toString().trim()).putString("region",reg.getText().toString().trim().toUpperCase(Locale.US)).apply();showHome();','prefs.edit().putString("api_key",key.getText().toString().trim()).putString("oauth_client_id",client.getText().toString().trim()).putString("region",reg.getText().toString().trim().toUpperCase(Locale.US)).apply();backupApiKey(key.getText().toString().trim());showHome();','save')

# v0.6 professional Android-TV style shell
req('int navW=sw()<700?dp(170):dp(220);','int navW=dp(82);','compact nav width')
s=s.replace('TextView logo=t("▶ TRK",22);','TextView logo=t("▶",24);')
s=s.replace('addNav(nav,"⌂  Home",v->showHome());','addNav(nav,"⌂",v->showHome());')
s=s.replace('addNav(nav,"⌕  Search",v->{if(search!=null)search.requestFocus();});','addNav(nav,"⌕",v->{if(search!=null)search.requestFocus();});')
s=s.replace('addNav(nav,"🔥  Explore",v->loadCategory("Trending",""));','addNav(nav,"🔥",v->loadCategory("Trending",""));')
s=s.replace('addNav(nav,"▣  Subscriptions",v->showSubscriptions());','addNav(nav,"▣",v->showSubscriptions());')
s=s.replace('addNav(nav,"◷  History",v->showLocalHistory());','addNav(nav,"◷",v->showLocalHistory());')
s=s.replace('addNav(nav,"★  Favorites",v->showFavorites());','addNav(nav,"★",v->showFavorites());')
s=s.replace('addNav(nav,"☺  Account",v->showAccount());','addNav(nav,"☺",v->showAccount());')
s=s.replace('addNav(nav,"⚙  Settings",v->showSettings());','addNav(nav,"⚙",v->showSettings());')
s=s.replace('private void addNav(LinearLayout n,String s,View.OnClickListener l){Button x=b(s);x.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);x.setOnClickListener(l);n.addView(x,new LinearLayout.LayoutParams(-1,dp(52)));}','private void addNav(LinearLayout n,String s,View.OnClickListener l){Button x=b(s);x.setTextSize(22);x.setGravity(Gravity.CENTER);x.setOnClickListener(l);n.addView(x,new LinearLayout.LayoutParams(-1,dp(58)));}')

# Search bar + compact profile button
req('Button go=b("SEARCH");go.setOnClickListener(v->doNativeSearch());top.addView(go,new LinearLayout.LayoutParams(dp(110),dp(54)));content.addView(top);','Button go=b("⌕");go.setTextSize(22);go.setOnClickListener(v->doNativeSearch());top.addView(go,new LinearLayout.LayoutParams(dp(64),dp(54)));Button profile=b("☺");profile.setTextSize(20);profile.setOnClickListener(v->showAccount());top.addView(profile,new LinearLayout.LayoutParams(dp(64),dp(54)));content.addView(top);','top controls')

# Cleaner main title and larger content cards
s=s.replace('TextView h=t("Home",sw()<700?24:29);h.setTypeface(null,Typeface.BOLD);content.addView(h);','TextView h=t("Recommended",sw()<700?20:23);h.setTypeface(null,Typeface.BOLD);content.addView(h);')
req('int cw=dp(sw()<700?190:225);','int cw=dp(sw()<700?230:285);','card width')
s=s.replace('card.addView(im,new LinearLayout.LayoutParams(cw,dp(120)));','card.addView(im,new LinearLayout.LayoutParams(cw,dp(sw()<700?132:160)));')
s=s.replace('card.addView(title,new LinearLayout.LayoutParams(cw,dp(48)));','card.addView(title,new LinearLayout.LayoutParams(cw,dp(52)));')
s=s.replace('card.addView(ch,new LinearLayout.LayoutParams(cw,dp(30)));','card.addView(ch,new LinearLayout.LayoutParams(cw,dp(30)));')
s=s.replace('target.addView(hs,new LinearLayout.LayoutParams(-1,dp(205)));','target.addView(hs,new LinearLayout.LayoutParams(-1,dp(sw()<700?225:255)));')

# More professional row structure without extra quota requests: split current popular result set into two TV rows.
old='''private void addVideoRows(LinearLayout target,String heading,List<VideoItem> list){
        TextView hh=t(heading,21);hh.setTypeface(null,Typeface.BOLD);target.addView(hh);
        for(int start=0;start<list.size();start+=5){HorizontalScrollView hs=new HorizontalScrollView(this);hs.setHorizontalScrollBarEnabled(false);LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);for(int i=start;i<Math.min(start+5,list.size());i++)row.addView(videoCard(list.get(i)));hs.addView(row);target.addView(hs,new LinearLayout.LayoutParams(-1,dp(205)));}
    }'''
new='''private void addVideoRows(LinearLayout target,String heading,List<VideoItem> list){
        addSingleTvRow(target, heading.equals("Popular now")?"Recommended":heading, list, 0, Math.min(10,list.size()));
        if(list.size()>10)addSingleTvRow(target,"More to watch",list,10,list.size());
    }
    private void addSingleTvRow(LinearLayout target,String heading,List<VideoItem> list,int from,int to){
        TextView hh=t(heading,21);hh.setTypeface(null,Typeface.BOLD);target.addView(hh);
        HorizontalScrollView hs=new HorizontalScrollView(this);hs.setHorizontalScrollBarEnabled(false);LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);
        for(int i=from;i<to;i++)row.addView(videoCard(list.get(i)));
        hs.addView(row);target.addView(hs,new LinearLayout.LayoutParams(-1,dp(sw()<700?225:255)));
    }'''
if old in s:
    s=s.replace(old,new)
else:
    # The row-height replacement above may have already changed the exact text; replace method by boundaries.
    a=s.find('    private void addVideoRows('); b=s.find('\n    private View videoCard',a)
    if a<0 or b<0: raise SystemExit('Could not locate addVideoRows method')
    s=s[:a]+new+'\n'+s[b:]

p.write_text(s)
print('v0.6 professional TV UI patch completed')
