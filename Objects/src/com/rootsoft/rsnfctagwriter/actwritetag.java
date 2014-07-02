package com.rootsoft.rsnfctagwriter;

import anywheresoftware.b4a.B4AMenuItem;
import android.app.Activity;
import android.os.Bundle;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BALayout;
import anywheresoftware.b4a.B4AActivity;
import anywheresoftware.b4a.ObjectWrapper;
import anywheresoftware.b4a.objects.ActivityWrapper;
import java.lang.reflect.InvocationTargetException;
import anywheresoftware.b4a.B4AUncaughtException;
import anywheresoftware.b4a.debug.*;
import java.lang.ref.WeakReference;

public class actwritetag extends Activity implements B4AActivity{
	public static actwritetag mostCurrent;
	static boolean afterFirstLayout;
	static boolean isFirst = true;
    private static boolean processGlobalsRun = false;
	BALayout layout;
	public static BA processBA;
	BA activityBA;
    ActivityWrapper _activity;
    java.util.ArrayList<B4AMenuItem> menuItems;
	public static final boolean fullScreen = true;
	public static final boolean includeTitle = true;
    public static WeakReference<Activity> previousOne;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (isFirst) {
			processBA = new BA(this.getApplicationContext(), null, null, "com.rootsoft.rsnfctagwriter", "com.rootsoft.rsnfctagwriter.actwritetag");
			processBA.loadHtSubs(this.getClass());
	        float deviceScale = getApplicationContext().getResources().getDisplayMetrics().density;
	        BALayout.setDeviceScale(deviceScale);
            
		}
		else if (previousOne != null) {
			Activity p = previousOne.get();
			if (p != null && p != this) {
                BA.LogInfo("Killing previous instance (actwritetag).");
				p.finish();
			}
		}
		if (!includeTitle) {
        	this.getWindow().requestFeature(android.view.Window.FEATURE_NO_TITLE);
        }
        if (fullScreen) {
        	getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,   
        			android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
		mostCurrent = this;
        processBA.sharedProcessBA.activityBA = null;
		layout = new BALayout(this);
		setContentView(layout);
		afterFirstLayout = false;
		BA.handler.postDelayed(new WaitForLayout(), 5);

	}
	private static class WaitForLayout implements Runnable {
		public void run() {
			if (afterFirstLayout)
				return;
			if (mostCurrent == null)
				return;
            
			if (mostCurrent.layout.getWidth() == 0) {
				BA.handler.postDelayed(this, 5);
				return;
			}
			mostCurrent.layout.getLayoutParams().height = mostCurrent.layout.getHeight();
			mostCurrent.layout.getLayoutParams().width = mostCurrent.layout.getWidth();
			afterFirstLayout = true;
			mostCurrent.afterFirstLayout();
		}
	}
	private void afterFirstLayout() {
        if (this != mostCurrent)
			return;
		activityBA = new BA(this, layout, processBA, "com.rootsoft.rsnfctagwriter", "com.rootsoft.rsnfctagwriter.actwritetag");
        
        processBA.sharedProcessBA.activityBA = new java.lang.ref.WeakReference<BA>(activityBA);
        anywheresoftware.b4a.objects.ViewWrapper.lastId = 0;
        _activity = new ActivityWrapper(activityBA, "activity");
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (BA.isShellModeRuntimeCheck(processBA)) {
			if (isFirst)
				processBA.raiseEvent2(null, true, "SHELL", false);
			processBA.raiseEvent2(null, true, "CREATE", true, "com.rootsoft.rsnfctagwriter.actwritetag", processBA, activityBA, _activity, anywheresoftware.b4a.keywords.Common.Density, mostCurrent);
			_activity.reinitializeForShell(activityBA, "activity");
		}
        initializeProcessGlobals();		
        initializeGlobals();
        
        BA.LogInfo("** Activity (actwritetag) Create, isFirst = " + isFirst + " **");
        processBA.raiseEvent2(null, true, "activity_create", false, isFirst);
		isFirst = false;
		if (this != mostCurrent)
			return;
        processBA.setActivityPaused(false);
        BA.LogInfo("** Activity (actwritetag) Resume **");
        processBA.raiseEvent(null, "activity_resume");
        if (android.os.Build.VERSION.SDK_INT >= 11) {
			try {
				android.app.Activity.class.getMethod("invalidateOptionsMenu").invoke(this,(Object[]) null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	public void addMenuItem(B4AMenuItem item) {
		if (menuItems == null)
			menuItems = new java.util.ArrayList<B4AMenuItem>();
		menuItems.add(item);
	}
	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		super.onCreateOptionsMenu(menu);
		if (menuItems == null)
			return false;
		for (B4AMenuItem bmi : menuItems) {
			android.view.MenuItem mi = menu.add(bmi.title);
			if (bmi.drawable != null)
				mi.setIcon(bmi.drawable);
            if (android.os.Build.VERSION.SDK_INT >= 11) {
				try {
                    if (bmi.addToBar) {
				        android.view.MenuItem.class.getMethod("setShowAsAction", int.class).invoke(mi, 1);
                    }
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			mi.setOnMenuItemClickListener(new B4AMenuItemsClickListener(bmi.eventName.toLowerCase(BA.cul)));
		}
		return true;
	}
    public void onWindowFocusChanged(boolean hasFocus) {
       super.onWindowFocusChanged(hasFocus);
       if (processBA.subExists("activity_windowfocuschanged"))
           processBA.raiseEvent2(null, true, "activity_windowfocuschanged", false, hasFocus);
    }
	private class B4AMenuItemsClickListener implements android.view.MenuItem.OnMenuItemClickListener {
		private final String eventName;
		public B4AMenuItemsClickListener(String eventName) {
			this.eventName = eventName;
		}
		public boolean onMenuItemClick(android.view.MenuItem item) {
			processBA.raiseEvent(item.getTitle(), eventName + "_click");
			return true;
		}
	}
    public static Class<?> getObject() {
		return actwritetag.class;
	}
    private Boolean onKeySubExist = null;
    private Boolean onKeyUpSubExist = null;
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (onKeySubExist == null)
			onKeySubExist = processBA.subExists("activity_keypress");
		if (onKeySubExist) {
			if (keyCode == anywheresoftware.b4a.keywords.constants.KeyCodes.KEYCODE_BACK &&
					android.os.Build.VERSION.SDK_INT >= 18) {
				HandleKeyDelayed hk = new HandleKeyDelayed();
				hk.kc = keyCode;
				BA.handler.post(hk);
				return true;
			}
			else {
				boolean res = new HandleKeyDelayed().runDirectly(keyCode);
				if (res)
					return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	private class HandleKeyDelayed implements Runnable {
		int kc;
		public void run() {
			runDirectly(kc);
		}
		public boolean runDirectly(int keyCode) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keypress", false, keyCode);
			if (res == null || res == true) {
                return true;
            }
            else if (keyCode == anywheresoftware.b4a.keywords.constants.KeyCodes.KEYCODE_BACK) {
				finish();
				return true;
			}
            return false;
		}
		
	}
    @Override
	public boolean onKeyUp(int keyCode, android.view.KeyEvent event) {
		if (onKeyUpSubExist == null)
			onKeyUpSubExist = processBA.subExists("activity_keyup");
		if (onKeyUpSubExist) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keyup", false, keyCode);
			if (res == null || res == true)
				return true;
		}
		return super.onKeyUp(keyCode, event);
	}
	@Override
	public void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
		this.setIntent(intent);
	}
    @Override 
	public void onPause() {
		super.onPause();
        if (_activity == null) //workaround for emulator bug (Issue 2423)
            return;
		anywheresoftware.b4a.Msgbox.dismiss(true);
        BA.LogInfo("** Activity (actwritetag) Pause, UserClosed = " + activityBA.activity.isFinishing() + " **");
        processBA.raiseEvent2(_activity, true, "activity_pause", false, activityBA.activity.isFinishing());		
        processBA.setActivityPaused(true);
        mostCurrent = null;
        if (!activityBA.activity.isFinishing())
			previousOne = new WeakReference<Activity>(this);
        anywheresoftware.b4a.Msgbox.isDismissing = false;
	}

	@Override
	public void onDestroy() {
        super.onDestroy();
		previousOne = null;
	}
    @Override 
	public void onResume() {
		super.onResume();
        mostCurrent = this;
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (activityBA != null) { //will be null during activity create (which waits for AfterLayout).
        	ResumeMessage rm = new ResumeMessage(mostCurrent);
        	BA.handler.post(rm);
        }
	}
    private static class ResumeMessage implements Runnable {
    	private final WeakReference<Activity> activity;
    	public ResumeMessage(Activity activity) {
    		this.activity = new WeakReference<Activity>(activity);
    	}
		public void run() {
			if (mostCurrent == null || mostCurrent != activity.get())
				return;
			processBA.setActivityPaused(false);
            BA.LogInfo("** Activity (actwritetag) Resume **");
		    processBA.raiseEvent(mostCurrent._activity, "activity_resume", (Object[])null);
		}
    }
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
	      android.content.Intent data) {
		processBA.onActivityResult(requestCode, resultCode, data);
	}
	private static void initializeGlobals() {
		processBA.raiseEvent2(null, true, "globals", false, (Object[])null);
	}

public anywheresoftware.b4a.keywords.Common __c = null;
public static String _nfctype = "";
public com.rootsoft.rsnfctagwriter.RSNFCTagWriter _nfctagwriter = null;
public anywheresoftware.b4a.objects.EditTextWrapper _txttag = null;
public com.rootsoft.rsnfctagwriter.main _main = null;

public static void initializeProcessGlobals() {
             try {
                Class.forName(BA.applicationContext.getPackageName() + ".main").getMethod("initializeProcessGlobals").invoke(null, null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
}
public static String  _activity_create(boolean _firsttime) throws Exception{
 //BA.debugLineNum = 23;BA.debugLine="Sub Activity_Create(FirstTime As Boolean)";
 //BA.debugLineNum = 26;BA.debugLine="Activity.LoadLayout (\"frmMain\")";
mostCurrent._activity.LoadLayout("frmMain",mostCurrent.activityBA);
 //BA.debugLineNum = 27;BA.debugLine="NFCTagWriter.Initialize(\"NFCTagWriter\")";
mostCurrent._nfctagwriter.Initialize(mostCurrent.activityBA,"NFCTagWriter");
 //BA.debugLineNum = 29;BA.debugLine="End Sub";
return "";
}
public static String  _activity_pause(boolean _userclosed) throws Exception{
 //BA.debugLineNum = 49;BA.debugLine="Sub Activity_Pause (UserClosed As Boolean)";
 //BA.debugLineNum = 50;BA.debugLine="NFCTagWriter.DisableForegroundDispatch";
mostCurrent._nfctagwriter.DisableForegroundDispatch();
 //BA.debugLineNum = 51;BA.debugLine="End Sub";
return "";
}
public static String  _activity_resume() throws Exception{
anywheresoftware.b4a.objects.IntentWrapper _nfcintent = null;
com.rootsoft.rsnfctagwriter.RSNFCTag _tag = null;
 //BA.debugLineNum = 30;BA.debugLine="Sub Activity_Resume";
 //BA.debugLineNum = 32;BA.debugLine="NFCTagWriter.EnableForegroundDispatch";
mostCurrent._nfctagwriter.EnableForegroundDispatch();
 //BA.debugLineNum = 33;BA.debugLine="Dim nfcIntent As Intent";
_nfcintent = new anywheresoftware.b4a.objects.IntentWrapper();
 //BA.debugLineNum = 34;BA.debugLine="nfcIntent = Activity.GetStartingIntent";
_nfcintent = mostCurrent._activity.GetStartingIntent();
 //BA.debugLineNum = 35;BA.debugLine="If nfcIntent <> Null Then";
if (_nfcintent!= null) { 
 //BA.debugLineNum = 36;BA.debugLine="Dim Tag As RSNFCTag";
_tag = new com.rootsoft.rsnfctagwriter.RSNFCTag();
 //BA.debugLineNum = 37;BA.debugLine="Tag = NFCTagWriter.getTag(nfcIntent)";
_tag.setObject((android.nfc.Tag)(mostCurrent._nfctagwriter.getTag(_nfcintent)));
 //BA.debugLineNum = 38;BA.debugLine="If Tag.IsInitialized = True AND Tag <> Null Then";
if (_tag.IsInitialized()==anywheresoftware.b4a.keywords.Common.True && _tag!= null) { 
 //BA.debugLineNum = 39;BA.debugLine="ProgressDialogHide";
anywheresoftware.b4a.keywords.Common.ProgressDialogHide();
 //BA.debugLineNum = 40;BA.debugLine="If Write(Tag, txtTag.Text) = True Then";
if (_write(_tag,mostCurrent._txttag.getText())==anywheresoftware.b4a.keywords.Common.True) { 
 //BA.debugLineNum = 41;BA.debugLine="NFCTagWriter_Success";
_nfctagwriter_success();
 }else {
 //BA.debugLineNum = 43;BA.debugLine="NFCTagWriter_Error(LastException.Message)";
_nfctagwriter_error(anywheresoftware.b4a.keywords.Common.LastException(mostCurrent.activityBA).getMessage());
 };
 };
 };
 //BA.debugLineNum = 48;BA.debugLine="End Sub";
return "";
}
public static String  _btnwrite_click() throws Exception{
 //BA.debugLineNum = 53;BA.debugLine="Sub btnWrite_Click";
 //BA.debugLineNum = 55;BA.debugLine="nfcType = \"txt\"";
_nfctype = "txt";
 //BA.debugLineNum = 56;BA.debugLine="ProgressDialogShow(\"Hold tag against device.\")";
anywheresoftware.b4a.keywords.Common.ProgressDialogShow(mostCurrent.activityBA,"Hold tag against device.");
 //BA.debugLineNum = 57;BA.debugLine="End Sub";
return "";
}
public static com.rootsoft.rsnfctagwriter.RSNFCRecord  _createtextndef(String _text) throws Exception{
String _lang = "";
byte[] _textbytes = null;
byte[] _langbytes = null;
int _langlength = 0;
int _textlength = 0;
byte[] _payload = null;
com.rootsoft.rsnfctagwriter.RSNFCRecord _recordnfc = null;
byte[] _b = null;
 //BA.debugLineNum = 58;BA.debugLine="Sub createTextNDEF(Text As String) As RSNFCRecord";
 //BA.debugLineNum = 60;BA.debugLine="Dim lang As String = \"en\"";
_lang = "en";
 //BA.debugLineNum = 61;BA.debugLine="Dim textBytes() As Byte = Text.GetBytes(\"UTF8\")";
_textbytes = _text.getBytes("UTF8");
 //BA.debugLineNum = 62;BA.debugLine="Dim langBytes() As Byte = lang.GetBytes(\"US-ASCII\")";
_langbytes = _lang.getBytes("US-ASCII");
 //BA.debugLineNum = 63;BA.debugLine="Dim langLength As Int = langBytes.Length";
_langlength = _langbytes.length;
 //BA.debugLineNum = 64;BA.debugLine="Dim textLength As Int = textBytes.Length";
_textlength = _textbytes.length;
 //BA.debugLineNum = 66;BA.debugLine="Dim payload(1 + langLength + textLength) As Byte";
_payload = new byte[(int) (1+_langlength+_textlength)];
;
 //BA.debugLineNum = 67;BA.debugLine="payload(0) = langLength";
_payload[(int) (0)] = (byte) (_langlength);
 //BA.debugLineNum = 69;BA.debugLine="NFCTagWriter.ArrayCopy(langBytes, 0, payload, 1, langLength)";
mostCurrent._nfctagwriter.ArrayCopy((Object)(_langbytes),(int) (0),(Object)(_payload),(int) (1),_langlength);
 //BA.debugLineNum = 70;BA.debugLine="NFCTagWriter.ArrayCopy(textBytes, 0, payload, 1 + langLength, textLength)";
mostCurrent._nfctagwriter.ArrayCopy((Object)(_textbytes),(int) (0),(Object)(_payload),(int) (1+_langlength),_textlength);
 //BA.debugLineNum = 72;BA.debugLine="Dim recordNFC As RSNFCRecord";
_recordnfc = new com.rootsoft.rsnfctagwriter.RSNFCRecord();
 //BA.debugLineNum = 73;BA.debugLine="Dim b(0) As Byte";
_b = new byte[(int) (0)];
;
 //BA.debugLineNum = 74;BA.debugLine="recordNFC.Initialize(recordNFC.TNF_WELL_KNOWN, recordNFC.RTD_TEXT, b, payload)";
_recordnfc.Initialize(_recordnfc.TNF_WELL_KNOWN,_recordnfc.RTD_TEXT,_b,_payload);
 //BA.debugLineNum = 75;BA.debugLine="Return recordNFC";
if (true) return _recordnfc;
 //BA.debugLineNum = 77;BA.debugLine="End Sub";
return null;
}
public static com.rootsoft.rsnfctagwriter.RSNFCRecord  _createurindef(String _text) throws Exception{
byte[] _textbytes = null;
int _textlength = 0;
byte[] _payload = null;
com.rootsoft.rsnfctagwriter.RSNFCRecord _recordnfc = null;
byte[] _b = null;
 //BA.debugLineNum = 78;BA.debugLine="Sub createURINDEF (Text As String) As RSNFCRecord";
 //BA.debugLineNum = 80;BA.debugLine="Dim textBytes() As Byte = Text.GetBytes(\"UTF8\")";
_textbytes = _text.getBytes("UTF8");
 //BA.debugLineNum = 81;BA.debugLine="Dim textLength As Int = textBytes.Length";
_textlength = _textbytes.length;
 //BA.debugLineNum = 83;BA.debugLine="Dim payload(1 + textLength) As Byte";
_payload = new byte[(int) (1+_textlength)];
;
 //BA.debugLineNum = 90;BA.debugLine="payload (0) = 0x3";
_payload[(int) (0)] = (byte) (0x3);
 //BA.debugLineNum = 91;BA.debugLine="NFCTagWriter.ArrayCopy(textBytes, 0, payload, 1, textLength)";
mostCurrent._nfctagwriter.ArrayCopy((Object)(_textbytes),(int) (0),(Object)(_payload),(int) (1),_textlength);
 //BA.debugLineNum = 94;BA.debugLine="Dim recordNFC As RSNFCRecord";
_recordnfc = new com.rootsoft.rsnfctagwriter.RSNFCRecord();
 //BA.debugLineNum = 95;BA.debugLine="Dim b(0) As Byte";
_b = new byte[(int) (0)];
;
 //BA.debugLineNum = 96;BA.debugLine="recordNFC.Initialize(recordNFC.TNF_WELL_KNOWN, recordNFC.RTD_URI, b, payload)";
_recordnfc.Initialize(_recordnfc.TNF_WELL_KNOWN,_recordnfc.RTD_URI,_b,_payload);
 //BA.debugLineNum = 97;BA.debugLine="Return recordNFC";
if (true) return _recordnfc;
 //BA.debugLineNum = 99;BA.debugLine="End Sub";
return null;
}
public static String  _globals() throws Exception{
 //BA.debugLineNum = 11;BA.debugLine="Sub Globals";
 //BA.debugLineNum = 16;BA.debugLine="Dim NFCTagWriter As RSNFCTagWriter";
mostCurrent._nfctagwriter = new com.rootsoft.rsnfctagwriter.RSNFCTagWriter();
 //BA.debugLineNum = 20;BA.debugLine="Dim txtTag As EditText";
mostCurrent._txttag = new anywheresoftware.b4a.objects.EditTextWrapper();
 //BA.debugLineNum = 22;BA.debugLine="End Sub";
return "";
}
public static String  _nfctagwriter_error(String _message) throws Exception{
 //BA.debugLineNum = 161;BA.debugLine="Sub NFCTagWriter_Error (Message As String)";
 //BA.debugLineNum = 162;BA.debugLine="ToastMessageShow(\"Error writing tag: \" & Message, False)";
anywheresoftware.b4a.keywords.Common.ToastMessageShow("Error writing tag: "+_message,anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 164;BA.debugLine="End Sub";
return "";
}
public static String  _nfctagwriter_success() throws Exception{
 //BA.debugLineNum = 156;BA.debugLine="Sub NFCTagWriter_Success";
 //BA.debugLineNum = 157;BA.debugLine="ToastMessageShow(\"Tag has been written!\", False)";
anywheresoftware.b4a.keywords.Common.ToastMessageShow("Tag has been written!",anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 158;BA.debugLine="End Sub";
return "";
}
public static String  _process_globals() throws Exception{
 //BA.debugLineNum = 6;BA.debugLine="Sub Process_Globals";
 //BA.debugLineNum = 9;BA.debugLine="Dim nfcType As String";
_nfctype = "";
 //BA.debugLineNum = 10;BA.debugLine="End Sub";
return "";
}
public static boolean  _write(com.rootsoft.rsnfctagwriter.RSNFCTag _tag,String _text) throws Exception{
com.rootsoft.rsnfctagwriter.RSNFCMessage _message = null;
com.rootsoft.rsnfctagwriter.RSNFCNdef _ndef = null;
com.rootsoft.rsnfctagwriter.RSNFCNdefFormatable _ndefformatable = null;
 //BA.debugLineNum = 100;BA.debugLine="Sub Write (Tag As RSNFCTag, Text As String) As Boolean";
 //BA.debugLineNum = 102;BA.debugLine="Dim Message As RSNFCMessage";
_message = new com.rootsoft.rsnfctagwriter.RSNFCMessage();
 //BA.debugLineNum = 103;BA.debugLine="Dim Ndef As RSNFCNdef";
_ndef = new com.rootsoft.rsnfctagwriter.RSNFCNdef();
 //BA.debugLineNum = 105;BA.debugLine="Select Case nfcType";
switch (BA.switchObjectToInt(_nfctype,"txt","uri")) {
case 0:
 //BA.debugLineNum = 108;BA.debugLine="Message.Initialize(createTextNDEF(Text))";
_message.Initialize((android.nfc.NdefRecord)(_createtextndef(_text).getObject()));
 break;
case 1:
 //BA.debugLineNum = 111;BA.debugLine="Message.Initialize(createURINDEF(Text))";
_message.Initialize((android.nfc.NdefRecord)(_createurindef(_text).getObject()));
 break;
default:
 break;
}
;
 //BA.debugLineNum = 115;BA.debugLine="Ndef.Initialize(Tag)";
_ndef.Initialize((android.nfc.Tag)(_tag.getObject()));
 //BA.debugLineNum = 116;BA.debugLine="ProgressDialogHide";
anywheresoftware.b4a.keywords.Common.ProgressDialogHide();
 //BA.debugLineNum = 117;BA.debugLine="If Ndef.IsInitialized = True Then";
if (_ndef.IsInitialized()==anywheresoftware.b4a.keywords.Common.True) { 
 //BA.debugLineNum = 118;BA.debugLine="Try";
try { //BA.debugLineNum = 119;BA.debugLine="Ndef.Connect";
_ndef.Connect();
 //BA.debugLineNum = 120;BA.debugLine="Ndef.WriteNdefMessage(Message)";
_ndef.WriteNdefMessage((android.nfc.NdefMessage)(_message.getObject()));
 //BA.debugLineNum = 121;BA.debugLine="Ndef.Close";
_ndef.Close();
 //BA.debugLineNum = 122;BA.debugLine="Return True";
if (true) return anywheresoftware.b4a.keywords.Common.True;
 } 
       catch (Exception e80) {
			processBA.setLastException(e80); //BA.debugLineNum = 124;BA.debugLine="Log(LastException.Message)";
anywheresoftware.b4a.keywords.Common.Log(anywheresoftware.b4a.keywords.Common.LastException(mostCurrent.activityBA).getMessage());
 //BA.debugLineNum = 125;BA.debugLine="Return False";
if (true) return anywheresoftware.b4a.keywords.Common.False;
 };
 }else {
 //BA.debugLineNum = 128;BA.debugLine="Dim NdefFormatable As RSNFCNdefFormatable";
_ndefformatable = new com.rootsoft.rsnfctagwriter.RSNFCNdefFormatable();
 //BA.debugLineNum = 129;BA.debugLine="NdefFormatable.Initialize(Tag)";
_ndefformatable.Initialize((android.nfc.Tag)(_tag.getObject()));
 //BA.debugLineNum = 131;BA.debugLine="If NdefFormatable.IsInitialized = True Then";
if (_ndefformatable.IsInitialized()==anywheresoftware.b4a.keywords.Common.True) { 
 //BA.debugLineNum = 132;BA.debugLine="Try";
try { //BA.debugLineNum = 133;BA.debugLine="NdefFormatable.Connect";
_ndefformatable.Connect();
 //BA.debugLineNum = 134;BA.debugLine="NdefFormatable.Format(Message)";
_ndefformatable.Format((android.nfc.NdefMessage)(_message.getObject()));
 //BA.debugLineNum = 135;BA.debugLine="NdefFormatable.Close";
_ndefformatable.Close();
 //BA.debugLineNum = 136;BA.debugLine="Return True";
if (true) return anywheresoftware.b4a.keywords.Common.True;
 } 
       catch (Exception e93) {
			processBA.setLastException(e93); //BA.debugLineNum = 138;BA.debugLine="Log(LastException.Message)";
anywheresoftware.b4a.keywords.Common.Log(anywheresoftware.b4a.keywords.Common.LastException(mostCurrent.activityBA).getMessage());
 //BA.debugLineNum = 139;BA.debugLine="Return False";
if (true) return anywheresoftware.b4a.keywords.Common.False;
 };
 };
 };
 //BA.debugLineNum = 146;BA.debugLine="Return False";
if (true) return anywheresoftware.b4a.keywords.Common.False;
 //BA.debugLineNum = 148;BA.debugLine="End Sub";
return false;
}
}
