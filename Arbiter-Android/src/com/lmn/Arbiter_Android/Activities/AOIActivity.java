package com.lmn.Arbiter_Android.Activities;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.cordova.Config;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.BaseClasses.Project;
import com.lmn.Arbiter_Android.CordovaPlugins.ArbiterCordova;
import com.lmn.Arbiter_Android.LoaderCallbacks.MapLoaderCallbacks;
import com.lmn.Arbiter_Android.Map.Map;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;

public class AOIActivity extends FragmentActivity implements CordovaInterface, Map.CordovaMap{
	private static final String TAG = "AOIActivity";
	
	// For CORDOVA
    private CordovaWebView cordovaWebView;
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    private ArbiterProject arbiterProject;
    private MapLoaderCallbacks mapLoaderCallbacks;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choose_aoi_dialog);
		
		Config.init(this);
		
		cordovaWebView = (CordovaWebView) findViewById(R.id.aoiWebView);
		
		Init();
		
        String url = "file:///android_asset/www/index.html";
        cordovaWebView.loadUrl(url, 5000);
	}
	
	private void Init(){
		arbiterProject = ArbiterProject.getArbiterProject();
		registerListeners();
	}
	
	private void resetSavedExtent(){
		arbiterProject.setSavedBounds(null);
		arbiterProject.setSavedZoomLevel(null);
	}
	
	private void registerListeners(){
		View cancel = (View) findViewById(R.id.cancelButton);
        
        final AOIActivity activity = this;
        
        cancel.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View v){
        		ArbiterProject arbiterProject = ArbiterProject.getArbiterProject();
        		
        		arbiterProject.isSettingAOI(false);
        		
        		Project newProject = arbiterProject.getNewProject();
        		
        		if(newProject != null && newProject.isBeingCreated()){
        			newProject.isBeingCreated(false);
        		}
        		
        		activity.finish();
        	}
        });
        
        View ok = (View) findViewById(R.id.okButton);
        
        ok.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View v){
        		cordovaWebView.loadUrl("javascript:app.setNewProjectsAOI()");
        	}
        });
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.aoi, menu);
		return true;
	}

	@Override
    protected void onPause() {
            super.onPause();
            Log.d(TAG, "onPause");
    }
    
    @Override 
    protected void onResume(){
    	super.onResume();
    	Log.d(TAG, "onResume");
    	
    	resetSavedExtent();
    }
    
    @Override
    protected void onDestroy(){
    	super.onDestroy();
    	if(this.cordovaWebView != null){
    		cordovaWebView.handleDestroy();
    	}
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) 
    {
        super.onConfigurationChanged(newConfig);
    }

    /**
     * Map.CordovaMap methods
     */
    
    @Override
    public CordovaWebView getWebView(){
    	return this.cordovaWebView;
    }
    
    /**
     * Cordova methods
     */
	@Override
	public Activity getActivity() {
		return this;
	}

	@Override
	public ExecutorService getThreadPool() {
		return threadPool;
	}

	@Override
	public Object onMessage(String message, Object obj) {
		Log.d(TAG, message);
		if(message.equals("onPageFinished")){
        	if(obj instanceof String){
        		if(((String) obj).equals(ArbiterCordova.cordovaUrl)){
        			if(this.mapLoaderCallbacks == null){
        				this.mapLoaderCallbacks = new MapLoaderCallbacks(this, cordovaWebView , R.id.loader_aoi_map);
        			}else{
        				this.mapLoaderCallbacks.loadMap();
        			}
        		}else if(((String) obj).equals("about:blank")){
        			this.cordovaWebView.loadUrl(ArbiterCordova.cordovaUrl);
        		}
        	}
        }
		
        return null;
	}
	
	@Override
	public void setActivityResultCallback(CordovaPlugin cordovaPlugin) {
		Log.d(TAG, "setActivityResultCallback is unimplemented");
		
	}

	@Override
	public void startActivityForResult(CordovaPlugin cordovaPlugin, Intent intent, int resultCode) {
		Log.d(TAG, "startActivityForResult is unimplemented");
		
	}
}
