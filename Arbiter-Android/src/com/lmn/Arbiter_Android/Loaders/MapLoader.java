package com.lmn.Arbiter_Android.Loaders;

import android.content.Context;
import android.content.IntentFilter;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.LocalBroadcastManager;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.BaseClasses.Layer;
import com.lmn.Arbiter_Android.BroadcastReceivers.ProjectBroadcastReceiver;
//import com.lmn.Arbiter_Android.DatabaseHelpers.DbHelpers;
import com.lmn.Arbiter_Android.DatabaseHelpers.GlobalDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.LayersHelper;

public class MapLoader extends AsyncTaskLoader<Layer[]> {
	public static final String PROJECT_LIST_UPDATED = "PROJECT_LIST_UPDATED";
	
	private ProjectBroadcastReceiver<Layer[]> loaderBroadcastReceiver = null;
	private Layer[] layers;
	private GlobalDatabaseHelper globalDbHelper = null;
	private long projectId;
	
	public MapLoader(Context context) {
		super(context);
		this.projectId = ArbiterProject.getArbiterProject().getOpenProject(context);
		globalDbHelper = GlobalDatabaseHelper.getGlobalHelper(context);
	}

	@Override
	public Layer[] loadInBackground() {
		Layer[] layers = LayersHelper.getLayersHelper().
				getAll(globalDbHelper.getWritableDatabase(), projectId);
		
		return layers;
	}
	
	/**
     * Called when there is new data to deliver to the client.  The
     * super class will take care of delivering it; the implementation
     * here just adds a little more logic.
     */
    @Override public void deliverResult(Layer[] _layers) {
        if (isReset()) {
            // An async query came in while the loader is stopped.  We
            // don't need the result.
            if (layers != null) {
          //      onReleaseResources(cursor);
            }
        }
        
        Layer[] oldLayers = _layers;
        layers = _layers;

        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(layers);
        }

        // At this point we can release the resources associated with
        // 'oldApps' if needed; now that the new result is delivered we
        // know that it is no longer in use.
        if (oldLayers != null) {
            onReleaseResources(oldLayers);
        }
    }
    
    /**
     * Handles a request to start the Loader.
     */
    @Override protected void onStartLoading() {
        if (layers != null) {
            // If we currently have a result available, deliver it
            // immediately.
            deliverResult(layers);
        }

        // Start watching for changes in the app data.
        if (loaderBroadcastReceiver == null) {
        	loaderBroadcastReceiver = new ProjectBroadcastReceiver<Layer[]>(this);
        	LocalBroadcastManager.getInstance(getContext()).
        		registerReceiver(loaderBroadcastReceiver, new IntentFilter(PROJECT_LIST_UPDATED));
        }

        if (takeContentChanged() || layers == null) {
            // If the data has changed since the last time it was loaded
            // or is not currently available, start a load.
            forceLoad();
        }
    }

    /**
     * Handles a request to stop the Loader.
     */
    @Override protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    /**
     * Handles a request to cancel a load.
     */
    @Override public void onCanceled(Layer[] _projects) {
        super.onCanceled(_projects);

        // At this point we can release the resources associated with 'apps'
        // if needed.
        onReleaseResources(_projects);
    }

    /**
     * Handles a request to completely reset the Loader.
     */
    @Override protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        // At this point we can release the resources associated with 'apps'
        // if needed.
        if (layers != null) {
            onReleaseResources(layers);
            layers = null;
        }

        // Stop monitoring for changes.
        if (loaderBroadcastReceiver != null) {
        	LocalBroadcastManager.getInstance(getContext()).
        		unregisterReceiver(loaderBroadcastReceiver);
            loaderBroadcastReceiver = null;
        }
    }

    /**
     * Helper function to take care of releasing resources associated
     * with an actively loaded data set.
     */
    protected void onReleaseResources(Layer[] _projects) {
        // For a simple List<> there is nothing to do.  For something
        // like a Cursor, we would close it here.
    	
    }
}
