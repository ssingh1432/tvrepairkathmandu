from pathlib import Path

p = Path('app/src/main/java/com/tvrepairkathmandu/trkyoutube/MainActivity.java')
s = p.read_text()

def replace_required(old, new, label):
    global s
    if old not in s:
        raise SystemExit('Required source pattern missing: ' + label)
    s = s.replace(old, new)

replace_required('private static final String VERSION="0.4.0";', 'private static final String VERSION="0.5.1";', 'version')
replace_required('immersive();\n        showWelcome();', 'immersive();\n        if(apiKey().length()>0) showHome(); else showWelcome();', 'startup')

# Familiar viewer-facing branding. TRK is intentionally reserved for technician/settings screens.
s = s.replace('TextView logo=t("▶  TRK YouTube TV",sw()<700?28:34);', 'TextView logo=t("▶  YouTube",sw()<700?28:34);')
s = s.replace('root.addView(t("Native TV experience for old & new Android TVs",18));', 'root.addView(t("Watch videos on your TV",18));')
s = s.replace('TextView logo=t("▶ TRK",22);', 'TextView logo=t("▶ YouTube",22);')

# Welcome is viewer-simple: viewing/sign-in only.
s = s.replace('        Button setup=b("YOUTUBE SETUP");setup.setOnClickListener(v->showSetup());root.addView(setup,new LinearLayout.LayoutParams(dp(420),dp(62)));\n', '')
s = s.replace('        Button browser=b("WEB FALLBACK");browser.setOnClickListener(v->openWeb("https://www.youtube.com/"));root.addView(browser,new LinearLayout.LayoutParams(dp(420),dp(62)));\n', '')

# Configuration appears as technician function, not normal viewer branding.
s = s.replace('TextView h=t("Connect YouTube Data",22);', 'TextView h=t("YouTube setup required",22);')
s = s.replace('box.addView(t("Native Home and Search need a YouTube Data API key. Add it once in YouTube Setup. The key stays on this device.",16));', 'box.addView(t("One-time service configuration is required. Open TRK Technician Setup and save the key once.",16));')
s = s.replace('Button setup=b("OPEN YOUTUBE SETUP");', 'Button setup=b("OPEN TRK TECHNICIAN SETUP");')
s = s.replace('        Button fallback=b("USE WEB FALLBACK");fallback.setOnClickListener(v->openWeb("https://www.youtube.com/"));box.addView(fallback,new LinearLayout.LayoutParams(-1,dp(58)));\n', '')
s = s.replace('TextView h=t("YouTube Setup",30);', 'TextView h=t("TRK Technician Setup",30);')
s = s.replace('Button setup=b("YOUTUBE SETUP");setup.setOnClickListener(v->showSetup());r.addView(setup,new LinearLayout.LayoutParams(-1,dp(60)));', 'Button setup=b("TRK TECHNICIAN SETUP");setup.setOnClickListener(v->showSetup());r.addView(setup,new LinearLayout.LayoutParams(-1,dp(60)));')

# Preserve proven playback identity/referrer behavior.
s = s.replace("<head><meta name='viewport'", "<head><meta name='referrer' content='strict-origin-when-cross-origin'><meta name='viewport'")
s = s.replace("origin=https%3A%2F%2Fwww.youtube.com'", "origin=https%3A%2F%2Fcom.tvrepairkathmandu.trkyoutube&widget_referrer=https%3A%2F%2Fcom.tvrepairkathmandu.trkyoutube'")
s = s.replace('web.loadDataWithBaseURL("https://www.youtube.com/",html', 'web.loadDataWithBaseURL("https://com.tvrepairkathmandu.trkyoutube/",html')

# Preference + private-file recovery. This fixes the actual preference key used by the app: api_key.
replace_required(
    'private String apiKey(){return prefs.getString("api_key","").trim();}',
    '''private String apiKey(){
        String k=prefs.getString("api_key","").trim();
        if(k.length()>0){backupApiKey(k);return k;}
        try{
            java.io.File f=new java.io.File(getFilesDir(),".trk_yt_key");
            if(f.exists()){
                java.io.BufferedReader br=new java.io.BufferedReader(new java.io.FileReader(f));
                String x=br.readLine(); br.close();
                if(x!=null&&x.trim().length()>0){k=x.trim();prefs.edit().putString("api_key",k).apply();}
            }
        }catch(Exception ignored){}
        return k;
    }
    private void backupApiKey(String k){
        try{
            if(k==null||k.trim().length()==0)return;
            java.io.FileWriter w=new java.io.FileWriter(new java.io.File(getFilesDir(),".trk_yt_key"),false);
            w.write(k.trim());w.close();
        }catch(Exception ignored){}
    }''',
    'apiKey method')

replace_required(
    'prefs.edit().putString("api_key",key.getText().toString().trim()).putString("oauth_client_id",client.getText().toString().trim()).putString("region",reg.getText().toString().trim().toUpperCase(Locale.US)).apply();showHome();',
    'prefs.edit().putString("api_key",key.getText().toString().trim()).putString("oauth_client_id",client.getText().toString().trim()).putString("region",reg.getText().toString().trim().toUpperCase(Locale.US)).apply();backupApiKey(key.getText().toString().trim());showHome();',
    'setup save callback')

p.write_text(s)
print('v0.5.1 source patch completed')
