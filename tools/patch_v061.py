from pathlib import Path
p=Path('app/src/main/java/com/tvrepairkathmandu/trkyoutube/MainActivity.java')
s=p.read_text()

def req(old,new,label):
    global s
    if old not in s: raise SystemExit('Required v0.6 source pattern missing: '+label)
    s=s.replace(old,new)

req('private static final String VERSION="0.6.0";','private static final String VERSION="0.6.1";','version')

# Always use a polished splash first. Never expose technician configuration as the launch screen.
req('if(apiKey().length()>0) showHome(); else showWelcome();','showLaunchSplash();','startup route')
insert='''
    private void showLaunchSplash(){
        FrameLayout root=new FrameLayout(this);root.setBackgroundColor(Color.BLACK);
        LinearLayout center=new LinearLayout(this);center.setOrientation(LinearLayout.VERTICAL);center.setGravity(Gravity.CENTER);
        TextView mark=t("▶",54);mark.setGravity(Gravity.CENTER);mark.setAlpha(0f);mark.setScaleX(.72f);mark.setScaleY(.72f);center.addView(mark,new LinearLayout.LayoutParams(dp(110),dp(90)));
        TextView name=t("YouTube",30);name.setTypeface(null,Typeface.BOLD);name.setGravity(Gravity.CENTER);name.setAlpha(0f);center.addView(name,new LinearLayout.LayoutParams(dp(260),dp(60)));
        root.addView(center,new FrameLayout.LayoutParams(-1,-1));setContentView(root);
        mark.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(420).start();
        name.animate().alpha(1f).setStartDelay(180).setDuration(360).start();
        root.postDelayed(()->showHome(),1050);
    }
'''
pos=s.find('    private void immersive()')
if pos<0: raise SystemExit('immersive insertion point missing')
s=s[:pos]+insert+'\n'+s[pos:]

# Cleaner rail: no large permanent button boxes; focus pill appears only when selected.
req('private void addNav(LinearLayout n,String s,View.OnClickListener l){Button x=b(s);x.setTextSize(22);x.setGravity(Gravity.CENTER);x.setOnClickListener(l);n.addView(x,new LinearLayout.LayoutParams(-1,dp(58)));}', '''private void addNav(LinearLayout n,String s,View.OnClickListener l){
        TextView x=t(s,22);x.setGravity(Gravity.CENTER);x.setFocusable(true);x.setClickable(true);x.setBackground(navFocusBg());x.setOnClickListener(l);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(54));lp.setMargins(dp(8),dp(3),dp(8),dp(3));n.addView(x,lp);
    }
    private android.graphics.drawable.StateListDrawable navFocusBg(){
        android.graphics.drawable.StateListDrawable d=new android.graphics.drawable.StateListDrawable();
        GradientDrawable f=bg(Color.rgb(46,46,46),16);GradientDrawable normal=bg(Color.TRANSPARENT,16);
        d.addState(new int[]{android.R.attr.state_focused},f);d.addState(new int[]{},normal);return d;
    }''','nav style')

# Slimmer shell and content spacing.
s=s.replace('shell.setPadding(dp(12),dp(12),dp(12),dp(12));','shell.setPadding(dp(8),dp(10),dp(10),dp(8));')
s=s.replace('int navW=dp(82);','int navW=dp(72);')
s=s.replace('content.setPadding(dp(14),dp(2),dp(8),dp(30));','content.setPadding(dp(12),dp(2),dp(6),dp(28));')
s=s.replace('search.setBackground(bg(Color.rgb(38,38,38),22));','search.setBackground(bg(Color.rgb(32,32,32),24));')

# Avoid duplicate Recommended heading: the page title is enough; content rows use useful labels.
s=s.replace('addSingleTvRow(target, heading.equals("Popular now")?"Recommended":heading, list, 0, Math.min(10,list.size()));','addSingleTvRow(target, heading.equals("Popular now")?"Top picks":heading, list, 0, Math.min(10,list.size()));')

# If configuration is missing, keep it subtle and inside content; no giant warning panel.
a=s.find('    private void showApiNeededInline(){')
b=s.find('\n    private void loadPopularInto',a)
if a<0 or b<0: raise SystemExit('api-needed method not found')
replacement='''    private void showApiNeededInline(){
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.HORIZONTAL);box.setGravity(Gravity.CENTER_VERTICAL);box.setPadding(dp(14),dp(10),dp(14),dp(10));
        TextView msg=t("YouTube data needs technician setup",14);msg.setTextColor(Color.LTGRAY);box.addView(msg,new LinearLayout.LayoutParams(0,dp(48),1));
        Button setup=b("SETUP");setup.setOnClickListener(v->showSetup());box.addView(setup,new LinearLayout.LayoutParams(dp(120),dp(48)));
        content.addView(box,new LinearLayout.LayoutParams(-1,-2));
    }
'''
s=s[:a]+replacement+s[b:]

# Keep technician branding confined to setup/settings.
s=s.replace('TextView logo=t("▶  YouTube",sw()<700?28:34);','TextView logo=t("▶  YouTube",sw()<700?28:34);')

p.write_text(s)
print('v0.6.1 persistence/startup/pro TV UI refinement applied')
