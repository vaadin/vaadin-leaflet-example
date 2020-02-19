package org.vaadin.example.leaflet.ui;

import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.example.leaflet.data.MapLocation;
import org.vaadin.example.leaflet.data.MapLocationService;
import org.vaadin.example.leaflet.ui.LeafletMap.MapClickEvent;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Main (and only) view in this demo app.
 * <p>
 * Demonstrates how to use our custom {@link LeafletMap} component, in the same
 * way you'd use any built-in Vaadin component.
 *
 * @see LeafletMap
 */
@Route("")
@PageTitle("The Best Fishing Spots in the World!")
public class MainView extends VerticalLayout {

    private MapLocationService service;

    private LeafletMap map;

    @Autowired
    public MainView(MapLocationService service) {
        this.service = service;

        setSizeFull();

        H3 title = new H3("Welcome to the best fishing spots in the world!");
        add(title);

        Span subtitle = new Span("You can add a marker by clicking on the map.");
        add(subtitle);

        // Create the map and add it to this view
        map = new LeafletMap();
        map.setSizeFull();
        add(map);

        // Register for marker clicks
        map.addMarkerClickListener(e -> Notification.show("User clicked on the marker " + e.getMarker().getName()));

        // Register for clicks on the map itself
        map.addMapClickListener(this::mapClicked);

        // Add all known markers to the map
        map.addMarkersAndZoom(service.getAll());
    }

    /**
     * Called when the user clicks the map. Creates a {@link Dialog} for the user to
     * input further data and to save the data.
     */
    private void mapClicked(MapClickEvent event) {

        // Create a dialog for adding a marker. This is not part of the custom
        // component, just normal Vaadin stuff

        VerticalLayout popupLayout = new VerticalLayout();
        popupLayout.setMargin(false);

        Dialog popup = new Dialog(popupLayout);
        popup.open();

        Span coords = new Span(String.format("You selected the following coordinates: %f, %f", event.getLatitude(), event.getLongitude()));

        TextField markerName = new TextField("What is this spot called?");
        markerName.focus();

        Button saveMarker = new Button("Save", VaadinIcon.CHECK.create(), e -> {
            saveMarkerAndRefresh(markerName.getValue(), event.getLatitude(), event.getLongitude());
            popup.close();
        });
        saveMarker.addClickShortcut(Key.ENTER);
        saveMarker.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancel = new Button("Cancel", e -> popup.close());
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancel.getStyle().set("margin-left", "auto");

        HorizontalLayout buttons = new HorizontalLayout(saveMarker, cancel);
        buttons.setWidthFull();
        buttons.setMargin(false);

        popupLayout.add(coords, markerName, buttons);
    }

    /**
     * Save a new marker in the backend and add it to the map
     */
    private void saveMarkerAndRefresh(String name, double latitude, double longitude) {
        MapLocation spot = new MapLocation(latitude, longitude, name);
        service.addSpot(spot);
        map.addMarker(spot);
    }
}
