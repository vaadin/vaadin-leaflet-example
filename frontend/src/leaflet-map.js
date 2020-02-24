import {html, PolymerElement} from '@polymer/polymer/polymer-element.js'; // import Web Component helpers
import {ThemableMixin} from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin'; // import Vaadin theme helpers

import 'leaflet'; // import from NPM

/**
 * This is our Web Component wrapper for the Leaflet JS object.
 * 
 * It defines some styles, web component registration methods, and some custom API methods we can call from Java.
 * 
 * It extends ThemableMixin so that Vaadin styles can be applied easily (for instance in the margin style).
 */
class LeafletMap extends ThemableMixin(PolymerElement) {

	/**
	 * This method creates the DOM for our web component. We define some styles and 
	 * finally a single DIV element to hold our map.
	 * 
	 * This is standard Web Component API.
	 */
    static get template() {
        return html`
<style>
    :host {
        position: relative;
        display: block;
        flex: 1 1 0;
    }
    
    #map {
        width: 100%;
        height: 100%;
    }
    
    /* Override the default 400, 800, and 1000 z-indices so that a vaadin-dialog appears on top of the map */
   
    :host .leaflet-pane {
        z-index: 10;
    }
    :host .leaflet-control {
        z-index: 30;
    }
    :host .leaflet-top, :host .leaflet-bottom {
        z-index: 50;
    }
    
</style>

<!-- This is the div the map will be rendered in -->
<div id="map"></div>
`;
    }

    /**
     * Identity method for our web component, see also customElements.define() at the bottom of this file.
     */
    static get is() {
        return "leaflet-map"; // must match @Tag in the Java file
    }
    
    /**
     * Called automatically when this element is created
     */
    ready() {
        super.ready();
        
        this.map = L.map(this.$.map);

        this.markers = [];

        this.map.dragging.enable();
        this.map.scrollWheelZoom.enable();
        
        this.map.on('click', ev =>
            this._mapClicked(ev) // ev is an event object (MouseEvent in this case)
        );
    }
    

    /**
     * Adds a layer of map tiles to the map. Must be called at least once for anything to be visible.
     *
     * @param urlTemplate           The URL (see https://leafletjs.com/reference-1.5.0.html#tilelayer)
     * @param attribution           The attribution shown in the lower right corner of the map
     * @param options               Options as defined here https://leafletjs.com/reference-1.5.0.html#tilelayer
     */
    addTileLayer(urlTemplate, attribution, options) {
        let defaults = {
            attribution: attribution,
            maxZoom: 13
        };
        
        // add all given options to 'defaults' array
        for(var k in options) defaults[k]=options[k];
        
        let tileLayer = L.tileLayer(urlTemplate, defaults);
        tileLayer.addTo(this.map);
    }


    /**
     * Zooms and pans the map to fit the given bounds, with the highest zoom level possible.
     * 
     * Note that we don't need to provide 'minWidth' from the server, it's optional.
     *
     * @param bounds    The bounds to fit, as JSON array
     * @param minWidth  Optionally, the minimum width of the map
     */
    fitBounds(bounds, minWidth) {
        if (minWidth !== undefined) {
            this.style.minWidth = minWidth;
            // Set timeout to let the browser finish rendering the current request first
            setTimeout(() => {
                this.map.invalidateSize();
                this.map.fitBounds(bounds);
            }, 0);
        } else {
            this.map.fitBounds(bounds);
        }
    }

    /**
     * Adds a Marker to this map at the given coordinates and maps the id for future
     * click events.
     */
    addMarker(latitude, longitude, markerText, markerId) {
    	
    	// use default marker icons from Leaflet directly
		let iconUrl = "https://unpkg.com/leaflet@1.6.0/dist/images/marker-icon.png";
		let shadowUrl = "https://unpkg.com/leaflet@1.6.0/dist/images/marker-shadow.png";
		let myIcon = L.icon({
		    iconUrl: iconUrl,
		    iconAnchor: [25, 41],
		    popupAnchor: [-3, -76],
		    shadowUrl: shadowUrl,
		    shadowAnchor: [25, 40]
		});
		
		let opts = {
				title: markerText,
				icon: myIcon
			};
		
		// create marker object
    	let marker = L.marker([latitude,longitude],opts);
    	
    	// add click event handler
    	marker.id = markerId;
    	marker.on('click', () => this._markerClicked(marker.id));
    	
    	// add marker to map
    	marker.addTo(this.map);
    	
    	this.markers.push(marker);
    }
    
    /**
     * Removes all markers on the map.
     */
    removeAllMarkers() {
    	this.markers.forEach(marker => {
    		marker.remove();
    	});
    }

    /**
     * Internal method to dispatch an event to the server. We only send the marker id.
     */
    _markerClicked(id) {
        this.dispatchEvent(new CustomEvent('marker-click', {detail: {id: id}}));
    }
    
    /**
     * Internal method to dispatch an event to the server. We send the coordinates we get from the map.
     */
    _mapClicked(mouseEvent) {
        let latitude = mouseEvent.latlng.lat;
        let longitude = mouseEvent.latlng.lng;
        this.dispatchEvent(new CustomEvent('map-click', {detail: {lat:latitude, lng:longitude}}));
    }
}

customElements.define(LeafletMap.is, LeafletMap); // Registers this custom element with the browser
