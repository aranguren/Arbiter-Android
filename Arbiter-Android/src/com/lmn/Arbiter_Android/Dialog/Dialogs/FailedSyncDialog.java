package com.lmn.Arbiter_Android.Dialog.Dialogs;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.ConnectivityListeners.ConnectivityListener;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogFragment;
import com.lmn.Arbiter_Android.Dialog.ProgressDialog.SyncProgressDialog;
import com.lmn.Arbiter_Android.Map.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class FailedSyncDialog extends ArbiterDialogFragment {
	public static final String TAG = "FailedSyncDialog";
	
	protected String title;
	protected String ok;
	protected String cancel;
	protected int layout;
	protected OnClickListener validatingClickListener = null;
	
	private String[] failedVectorUploads;
	private String[] failedVectorDownloads;
	private JSONObject failedMediaUploads;
	private String[] failedMediaDownloads;
	private Map.CordovaMap cordovaMap;
	private ConnectivityListener connectivityListener;
	
	public FailedSyncDialog(){}
	
	public static FailedSyncDialog newInstance(
			String[] failedVectorUploads, String[] failedVectorDownloads,
			JSONObject failedMediaUploads, String[] failedMediaDownloads,
			ConnectivityListener connectivityListener){
		
		final FailedSyncDialog dialog = new FailedSyncDialog();
		
		dialog.failedVectorUploads = failedVectorUploads;
		dialog.failedVectorDownloads = failedVectorDownloads;
		dialog.failedMediaUploads = failedMediaUploads;
		dialog.failedMediaDownloads = failedMediaDownloads;
		
		dialog.connectivityListener = connectivityListener;
		
		return dialog;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Activity activity = getActivity();
		
		setTitle(activity.getResources().getString(R.string.failed_to_sync));
		setOk(activity.getResources().getString(R.string.retry));
		setCancel(activity.getResources().getString(R.string.close));
		setLayout(R.layout.failed_sync);
		
		setValidatingClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				onPositiveClick();
			}
		});
		
		setRetainInstance(true);
	}
	
	@Override
	public void onStart(){
		super.onStart();
		
		try{
			
			this.cordovaMap = (Map.CordovaMap) this.getActivity();
		}catch(ClassCastException e){
			e.printStackTrace();
		}
	}
	
	private void populateView(View view){
		
		String failed = null;
		
		TextView errorsVectorUpload = (TextView) view.findViewById(R.id.vector_upload_errors);
		TextView errorsVectorUploadLabel = (TextView) view.findViewById(R.id.vector_upload_errors_label);
		
		TextView errorsVectorDownload = (TextView) view.findViewById(R.id.vector_download_errors);
		TextView errorsVectorDownloadLabel = (TextView) view.findViewById(R.id.vector_download_errors_label);
		
		TextView errorsMediaUpload = (TextView) view.findViewById(R.id.media_upload_errors);
		TextView errorsMediaUploadLabel = (TextView) view.findViewById(R.id.media_upload_errors_label);
		
		TextView errorsMediaDownload = (TextView) view.findViewById(R.id.media_download_errors);
		TextView errorsMediaDownloadLabel = (TextView) view.findViewById(R.id.media_download_errors_label);
	
		if(this.isFailed(this.failedVectorUploads)){
			
			failed = buildErrorString(failedVectorUploads);
			
			errorsVectorUpload.setText(failed);
		}else{
			errorsVectorUpload.setVisibility(View.GONE);
			errorsVectorUploadLabel.setVisibility(View.GONE);
		}
		
		if(this.isFailed(this.failedVectorDownloads)){
			failed = buildErrorString(failedVectorDownloads);
			
			errorsVectorDownload.setText(failed);
		}else{
			errorsVectorDownload.setVisibility(View.GONE);
			errorsVectorDownloadLabel.setVisibility(View.GONE);
		}
		
		if(this.isFailedMediaUploads(this.failedMediaUploads)){
			failed = buildErrorString(failedMediaUploads);
			
			errorsMediaUpload.setText(failed);
		}else{
			errorsMediaUpload.setVisibility(View.GONE);
			errorsMediaUploadLabel.setVisibility(View.GONE);
		}

		if(this.isFailed(this.failedMediaDownloads)){
			failed = buildErrorString(failedMediaDownloads);
			
			errorsMediaDownload.setText(failed);
		}else{
			errorsMediaDownload.setVisibility(View.GONE);
			errorsMediaDownloadLabel.setVisibility(View.GONE);
		}
	}
	
	private String buildErrorString(String[] failed){
		String results = "";
		
		if(failed == null){
			return results;
		}
		
		for(int i = 0; i < failed.length; i++){
			results += failed[i] + "\n";
		}
		
		return results;
	}
	
	private String buildErrorString(JSONObject failed){
		String results = "";
		String key = null;
		JSONArray mediaForLayer = null;
		
		if(failed == null){
			return null;
		}
		
		Iterator<?> iterator = failed.keys();
		
		try{
			while(iterator.hasNext()){
				key = (String) iterator.next();
				
				mediaForLayer = failed.getJSONArray(key);
				
				for(int i = 0, count = mediaForLayer.length(); i < count; i++){
					Log.w("FailedSyncDialog", "FailedSyncDialog mediaForLayer + " + mediaForLayer.getString(i));
					results += mediaForLayer.getString(i) + "\n";
				}
			}
		}catch(JSONException e){
			e.printStackTrace();
		}
		
		return results;
	}
	
	public boolean isFailedMediaUploads(JSONObject failed){
		
		if(failed == null){
			return false;
		}
		
		Iterator<?> keys = failed.keys();
		
		JSONArray jsonArray = null;
		
		String key = null;
		
		try{
			
			while(keys.hasNext()){
				
				key = (String) keys.next();
				
				jsonArray = failed.getJSONArray(key);
				
				if(jsonArray.length() > 0){
					return true;
				}
			}
		}catch(JSONException e){
			e.printStackTrace();
		}
		
		return false;
	}
	
	public boolean isFailed(String[] failed){
		if(failed != null && failed.length > 0){
			return true;
		}
		
		return false;
	}

	@Override
	public void beforeCreateDialog(View view) {
		
		populateView(view);
	}

	@Override
	public void onPositiveClick() {
		
		if(this.connectivityListener != null){
			
			final Activity activity = getActivity();
			
			if(this.connectivityListener.isConnected()){
				
				activity.runOnUiThread(new Runnable(){
					@Override
					public void run(){
						SyncProgressDialog.show(activity);
						
						Map.getMap().sync(cordovaMap.getWebView());
						
						dismiss();
					}
				});
			}else{
				
				activity.runOnUiThread(new Runnable(){
					@Override
					public void run(){
						
						AlertDialog.Builder builder = new AlertDialog.Builder(activity);
						
						builder.setTitle(R.string.no_network);
						
						builder.setMessage(R.string.check_network_connection);
						
						builder.setPositiveButton(R.string.close, null);
						
						builder.create().show();
					}
				});
			}
		}
	}

	@Override
	public void onNegativeClick() {
		// TODO Auto-generated method stub
		
	}
}
