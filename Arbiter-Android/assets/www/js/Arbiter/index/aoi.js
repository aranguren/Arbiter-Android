var app = (function() {

	var waitFuncs = [];
	var ArbiterInitialized = false;
	
	/**
	 * On device ready
	 */
	var onDeviceReady = function() {
		Arbiter.Init(function() {
			
			// Get the file system for use in TileUtil.js
			Arbiter.FileSystem.setFileSystem(function(){
				
				// Make sure the directories for storing tiles exists
				Arbiter.FileSystem.ensureTileDirectoryExists(function(){
				
					// Get the AOI to check to see if it's been set
					Arbiter.PreferencesHelper.get(Arbiter.SHOULD_ZOOM_TO_AOI, this, function(shouldZoomToAOI){
						
						Arbiter.Cordova.Project.getSavedBounds(function(savedBounds, savedZoom){
							
							var bounds = null;
							
							if(savedBounds !== null && savedBounds !== undefined 
									&& savedZoom !== null 
									&& savedZoom !== undefined){
								
								bounds = savedBounds.split(',');
								
								Arbiter.Map.zoomToExtent(bounds[0], 
										bounds[1], bounds[2], 
										bounds[3], savedZoom);
							}else if(shouldZoomToAOI){
								Arbiter.Cordova.Project.zoomToAOI(null, function(e){
									console.log("Error initialing Arbiter while getting aoi");
								});
							}else{
								Arbiter.Cordova.Project.zoomToDefault();
							}
							
							Arbiter.Cordova.OOM_Workaround
							.registerMapListeners();
						
							Arbiter.setTileUtil(
								new Arbiter.TileUtil(
									Arbiter.ApplicationDbHelper.getDatabase(),
									Arbiter.ProjectDbHelper.getProjectDatabase(),
									Arbiter.Map.getMap(),
									Arbiter.FileSystem.getFileSystem()
								)
							);
							
							Arbiter.Layers.removeAllLayers();
						
							Arbiter.Layers.addDefaultLayer(true);
						
							for ( var i = 0; i < waitFuncs.length; i++) {
								waitFuncs[i].call();
							}

							ArbiterInitialized = true;
						
						}, function(e){
							console.log("Error initializing Arbiter while getting saved bounds", e);
						});
					}, function(e){
						console.log("Error initializing Arbiter while getting "
								+ Arbiter.SHOULD_ZOOM_TO_AOI, e);
					});
				}, function(e){
					console.log("Error making sure tile directory exists", e);
				});
			}, function(e){
				console.log("Error getting file system", e);
			});
		});
	};
	
	/**
	 * Bind event listeners
	 */
	var bindEvents = function() {
		document.addEventListener('deviceready', onDeviceReady, false);
	};
	
	/**
	 * Initialize the app
	 */
	var Init = function() {
		bindEvents();
	};
	
	Init();
	
	return {
		waitForArbiterInit : function(func) {
			if (!ArbiterInitialized) {
				waitFuncs.push(func);
			} else {
				func.call();
			}
		}
	};
})();