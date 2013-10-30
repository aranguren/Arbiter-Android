package com.lmn.Arbiter_Android.ListAdapters;

import java.util.ArrayList;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.BaseClasses.Layer;
import com.lmn.Arbiter_Android.DatabaseHelpers.GlobalDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.CommandExecutor.CommandExecutor;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.LayersHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.ProjectsHelper;
import com.lmn.Arbiter_Android.Loaders.LayersListLoader;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

public class LayerListAdapter extends BaseAdapter{

	private LayerChangeListener layerChangeListener;
	
	public interface LayerChangeListener {
		public void onLayerDeleted(long layerId);
		
		public void onLayerVisibilityChanged(long layerId);
	}
	
	private ArrayList<Layer> items;
	private final LayoutInflater inflater;
	private int itemLayout;
	private final FragmentActivity activity;
	private final Context context;
	
	public LayerListAdapter(FragmentActivity activity, int itemLayout){
		
		this.context = activity.getApplicationContext();
		this.inflater = LayoutInflater.from(this.context);
		this.items = new ArrayList<Layer>();
		this.itemLayout = itemLayout;
		this.activity = activity;
		
		try {
			layerChangeListener = (LayerChangeListener) activity;
		} catch (ClassCastException e){
			throw new ClassCastException(activity.toString() 
					+ " must implement LayerChangeListener");
		}
	}
	
	private void addDefaultLayer(ArrayList<Layer> layers){
		if(layers != null){
			if(ArbiterProject.getArbiterProject().includeDefaultLayer()){
				layers.add(0, new Layer(-1, null, -1, null, null,
						context.getResources().getString(R.string.default_layer_name), null, null));
				layers.get(0).setIsDefaultLayer(true);
			}
		}
	}
	
	public void setData(ArrayList<Layer> data){
		items = data;
		
		addDefaultLayer(items);

		notifyDataSetChanged();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		View view = convertView;
		
		// Inflate the layout
		if(view == null){
			view = inflater.inflate(itemLayout, null);
		}
		
		final Layer listItem = getItem(position);
		
		if(listItem != null){
            TextView layerNameView = (TextView) view.findViewById(R.id.layerName);
            TextView serverNameView = (TextView) view.findViewById(R.id.serverName);
            ImageButton deleteButton = (ImageButton) view.findViewById(R.id.deleteLayer);
            
            final boolean isDefaultLayer = listItem.isDefaultLayer();
            
            if(layerNameView != null){
            	layerNameView.setText(listItem.getLayerTitle());
            }
            
            if(serverNameView != null){
            	serverNameView.setText((isDefaultLayer) ? context.getResources().
            			getString(R.string.default_layer_name) : listItem.getServerName());
            }
            
            if(deleteButton != null){
            	deleteButton.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						if(!isDefaultLayer){
							deleteLayer(new Layer(listItem));
						}else{
							deleteDefaultLayer();
						}
					}
            		
            	});
            }
		}
		
		return view;
	}
	
	private void deleteDefaultLayer(){
		GlobalDatabaseHelper helper = GlobalDatabaseHelper.getGlobalHelper(context);
		ProjectsHelper.getProjectsHelper().setIncludeDefaultLayer(helper.getWritableDatabase(), 
				context, ArbiterProject.getArbiterProject().getOpenProject(context), false, new Runnable(){
			@Override
			public void run(){
				ArbiterProject.getArbiterProject().setIncludeDefaultLayer(false);
				
				layerChangeListener.onLayerDeleted(-1);
				
				LocalBroadcastManager.getInstance(context).
					sendBroadcast(new Intent(LayersListLoader.LAYERS_LIST_UPDATED));
			}
		});
	}
	
	private void deleteLayer(final Layer layer){
		CommandExecutor.runProcess(new Runnable(){
			@Override
			public void run() {
				final long layerId = layer.getLayerId();
				GlobalDatabaseHelper helper = GlobalDatabaseHelper.getGlobalHelper(context);
				LayersHelper.getLayersHelper().delete(
						helper.getWritableDatabase(), context, layer, new Runnable(){

							@Override
							public void run() {
								activity.runOnUiThread(new Runnable(){

									@Override
									public void run() {
										layerChangeListener.onLayerDeleted(layerId);
									}
								});
								
							}
							
						});
			}
		});
	}
	
	@Override
	public int getCount() {
		if(items == null){
			return 0;
		}
		
		return items.size();
	}

	@Override
	public Layer getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public ArrayList<Layer> getLayers(){
		return items;
	}
}
