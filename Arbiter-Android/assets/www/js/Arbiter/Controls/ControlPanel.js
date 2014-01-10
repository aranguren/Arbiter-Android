Arbiter.Controls.ControlPanel = (function(){
	var selectedFeature = null;
	
	var selectControl = null;
	
	var modifyControl = null;
	
	var insertControl = null;
	
	var deleteControl = new Arbiter.Controls.Delete();
	
	var insertedFeature = null;
	
	var originalGeometry = null;
	
	var saveOriginalGeometry = function(feature){
		if(feature === null 
				|| feature === undefined){
			return;
		}
		
		originalGeometry = feature.geometry.clone();
	};
	
	var _endInsertMode = function(){
		
		insertControl.deactivate();
		
		insertControl = null;
		
		selectControl.activate();
	};
	
	var _startInsertMode = function(layerId){
		
		selectControl.deactivate();
		
		var olLayer = Arbiter.Layers.getLayerById(
				layerId, Arbiter.Layers.type.WFS);
		
		var geometryType = Arbiter.Util.Geometry.getGeometryType(layerId);
			
		var context = Arbiter.Controls.ControlPanel;
		
		var schema = Arbiter.getLayerSchemas()[layerId];
		
		if(schema === null || schema === undefined){
			throw "Arbiter.Controls.ControlPanel _startInsertMode - "
				"could not get schema for layer id '" + layerId + "'";
		}
		
		insertControl = new Arbiter.Controls.Insert(olLayer,
				geometryType, function(feature){
			
			_endInsertMode();
			
			insertedFeature = feature;
			
			selectControl.select(feature);
		});
	};
	
	var getInsertedOrSelectedFeature = function(){
		var feature = null;
		
		if(selectedFeature !== null && selectedFeature !== undefined){
			feature = selectedFeature;
		}else if(insertedFeature !== null && insertedFeature !== undefined){
			feature = insertedFeature;
		}
		
		return feature;
	};
	
	var startModifyMode = function(){
		var feature = getInsertedOrSelectedFeature();
		
		if(feature === null){
			throw "Arbiter.Controls.ControlPanel.enterModifyMode() - No feature to modify!";
		}
		
		saveOriginalGeometry(feature);
		
		modifyControl = new Arbiter.Controls.Modify(
				feature.layer, feature);
		
		selectControl.deactivate();
		
		modifyControl.activate();
	};
	
	var endModifyMode = function(){
		var feature = getInsertedOrSelectedFeature();
		
		if(feature === null){
			throw "Arbiter.Controls.ControlPanel.exitModifyMode()"
				+ " feature should not be " + feature;
		}
		
		// Deactivate the modifyControl
		modifyControl.deactivate();
		
		modifyControl = null;
		
		// Reactivate the selectControl
		selectControl.activate();
		
		// Reselect the selectedFeature
		selectControl.select(feature);
	};
	
	var restoreOriginalGeometry = function(){
		
		if(originalGeometry === null || originalGeometry === undefined){
			throw "Arbiter.Controls.Select - couldn't restore original"
			+ " geometry because originalGeometry is " + originalGeometry;
		} 
		
		var feature = getInsertedOrSelectedFeature();
		
		if(feature === null){
			throw "Arbiter.Controls.Select - couldn't restore original"
			+ " geometry because selectedFeature is " + feature;
		}
		
		// Get the center of the geometry
		var centroid = originalGeometry.getCentroid();
		
		feature.move(new OpenLayers.LonLat(centroid.x, centroid.y));
	};
	
	var removeFeature = function(feature){
		var layer = feature.layer;
		
		layer.removeFeatures([feature]);
	};
	
	selectControl = new Arbiter.Controls.Select(function(feature){
		console.log("feature selected and selectedFeature = " + selectedFeature);
		// If the selectedFeature hasn't been
		// cleared out yet, then it means this was
		// the reselect of the feature in
		// endModifyMode()
		if(selectedFeature === null 
				|| selectedFeature === undefined){
			
			if(insertedFeature === null || insertedFeature === undefined){
				
				selectedFeature = feature;
				
				console.log("feature selected and inserted feature is null");
				Arbiter.Cordova.displayFeatureDialog(
						feature.layer.protocol.featureType,
						feature.metadata[Arbiter.FeatureTableHelper.ID],
						Arbiter.Util.getLayerId(feature.layer)
				);
			}else{
				console.log("feature selected and inserted feature isn't null");
				var layerId = Arbiter.Util.getLayerId(insertedFeature.layer);
				
				Arbiter.Cordova.doneInsertingFeature(layerId, insertedFeature);
			}
		}
	}, function(){
		
		// If the modifyControl is null,
		// make sure selectedFeature is
		// cleared out.
		if(modifyControl === null){
			selectedFeature = null;
			originalGeometry = null;
		}
	});
	
	var setFeatureId = function(olFeature, featureId){
		olFeature.metadata = {};
		
		olFeature.metadata[Arbiter.FeatureTableHelper.ID] = featureId; 
	};
	
	return {
		registerMapListeners: function(){
			selectControl.registerMapListeners();
		},
		
		enterModifyMode: function(){
			try{
				startModifyMode();
			}catch(e){
				console.log(e);
			}
		},
		
		exitModifyMode: function(){
			try{
				endModifyMode();
			}catch(e){
				console.log(e);
			}
		},
		
		unselect: function(){
			console.log("ControlPanel.unselect()");
			selectControl.unselect();
		},
		
		/**
		 * Restore the geometry to its original location,
		 * and exit modify mode.
		 */
		cancelEdit: function(){
			restoreOriginalGeometry();
			
			this.exitModifyMode();
		},
		
		/**
		 * Unselect the feature
		 */
		cancelSelection: function(){
			console.log("ControlPanel.cancelSelection()");
			if(insertedFeature === null || insertedFeature === undefined){
				restoreOriginalGeometry();
			}else{
				removeFeature(insertedFeature);
				
				insertedFeature = null;
			}
			
			this.unselect();
		},
		
		getSelectedFeature: function(){
			return selectedFeature;
		},
		
		getInsertedFeature: function(){
			return insertedFeature;
		},
		
		getOriginalGeometry: function(){
			return originalGeometry;
		},
		
		startInsertMode: function(layerId){
			_startInsertMode(layerId);
		},
		
		getInsertControl: function(){
			return insertControl;
		}
	};
})();