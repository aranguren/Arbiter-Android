package com.lmn.Arbiter_Android.ListAdapters;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.BaseClasses.BaseLayer;
import com.lmn.Arbiter_Android.ConnectivityListeners.ConnectivityListener;
import com.lmn.Arbiter_Android.Dialog.Dialogs.ChooseBaseLayer.ChooseBaselayerDialog;
import com.lmn.Arbiter_Android.GeometryEditor.GeometryEditor;
import com.lmn.Arbiter_Android.Map.Map.MapChangeListener;
import com.lmn.Arbiter_Android.OrderLayers.OrderLayersModel;

import android.app.AlertDialog;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class BaseLayerList extends CustomList<JSONArray, JSONObject> {

	private LayoutInflater inflater;
	private int itemLayout;
	private FragmentActivity activity;
	private Context context;
	private OrderLayersModel orderLayersModel;
	private MapChangeListener mapChangeListener;
    private ConnectivityListener connectivityListener;
    
	public BaseLayerList(ViewGroup viewGroup, FragmentActivity activity, int itemLayout, ConnectivityListener connectivityListener){
		super(viewGroup);
		
		this.activity = activity;
		this.context = activity.getApplicationContext();
		this.inflater =	LayoutInflater.from(this.context);
		this.itemLayout = itemLayout;
		this.connectivityListener = connectivityListener;
		
		try {
			mapChangeListener = (MapChangeListener) activity;
		} catch (ClassCastException e){
			throw new ClassCastException(activity.toString() 
					+ " must implement MapChangeListener");
		}
	}

	@Override
	public void setData(JSONArray layers){
		super.setData(layers);
	}
	
	@Override
	public int getCount() {
		JSONArray data = getData();
		
		return (data != null) ? data.length() : 0;
	}

	@Override
	public JSONObject getItem(int index) {
		JSONObject obj = null;
		
		if(getData() != null){
			try{
				obj = getData().getJSONObject(index);
			}catch(JSONException e){
				e.printStackTrace();
			}
		}
		
		return obj;
	}

	@Override
	public View getView(final int position) {
		
		View view = inflater.inflate(itemLayout, null);
		
		final BaseLayer layer = new BaseLayer(getItem(position));
		
		if(layer != null){
            TextView layerNameView = (TextView) view.findViewById(R.id.layerName);
            TextView serverNameView = (TextView) view.findViewById(R.id.serverName);
            
            if(layerNameView != null){
            	layerNameView.setText(layer.getName());
            }
            
            if(serverNameView != null){
            	serverNameView.setText(layer.getName());
            }
		
			view.setOnClickListener(new OnClickListener(){
	
				@Override
				public void onClick(View v) {
					
					activity.getSupportFragmentManager();
					
					String title = activity.getResources().getString(R.string.choose_baselayer);
					String ok = activity.getResources().getString(android.R.string.ok);
					String cancel = activity.getResources().getString(android.R.string.cancel);
					
					ChooseBaselayerDialog dialog = ChooseBaselayerDialog.newInstance(title,
							ok, cancel, R.layout.choose_baselayer_dialog, false, layer, connectivityListener);
					dialog.show(activity.getSupportFragmentManager(), ChooseBaselayerDialog.TAG);
				}
			});
		}
		
		return view;
	}
	
	// Return true if not editing
    private boolean makeSureNotEditing(){
    	
		int editMode = mapChangeListener.getMapChangeHelper().getEditMode();
		
		if(editMode == GeometryEditor.Mode.OFF || editMode == GeometryEditor.Mode.SELECT){
			return true;
		}
			
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		
		builder.setTitle(R.string.finish_editing_title);
		builder.setMessage(R.string.finish_editing_message);
		builder.setIcon(R.drawable.icon);
		builder.setPositiveButton(android.R.string.ok, null);
		
		builder.create().show();
		
		return false;
    }
	
	public void setItemLayout(int itemLayout){
		this.itemLayout = itemLayout;
	}
	
	public OrderLayersModel getOrderLayersModel(){
		return this.orderLayersModel;
	}
}
