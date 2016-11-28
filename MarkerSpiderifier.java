package ir.Amir_P.MarkerSpiderifier;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, ClusterManager.OnClusterItemClickListener, ClusterManager.OnClusterClickListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnCameraIdleListener, GoogleMap.OnMapClickListener {
    GoogleMap Map;
    float focus = 5f;
    ClusterManager<mClusterItem> mClusterManager;
    List<Marker> markers = new ArrayList<>();
    List<Polyline> polylines = new ArrayList<>();
    IconGenerator mIcon;
    TextView markerTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mIcon = new IconGenerator(this);
        markerTxt = (TextView) getLayoutInflater().inflate(R.layout.marker, null);
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Map = googleMap;
        getMapReady();
    }
    
    @Override
    public boolean onClusterItemClick(ClusterItem clusterItem) {
        selectedLatLng = clusterItem.getPosition();
        showBottomSheet();
        return true;
    }
    
    @Override
    public boolean onClusterClick(Cluster cluster) {
        for (int i = 0; i < markers.size(); i++) {
            markers.get(i).remove();
            polylines.get(i).remove();
        }
        markers.clear();
        polylines.clear();
        double radians = (360 / cluster.getSize() - 1) * Math.PI / 180;
        LatLng clusterPosition = cluster.getPosition();
        Object[] objects = cluster.getItems().toArray();
        LatLng farLeft = Map.getProjection().getVisibleRegion().farLeft;
        double center = Map.getCameraPosition().target.latitude;
        double unit = Math.abs(farLeft.latitude - center) / 2;
        MarkerOptions markerOptions = new MarkerOptions();
        for (int i = 0; i < objects.length; i++) {
            PolylineOptions options = new PolylineOptions().width(5).color(getResources().getColor(R.color.colorPrimary));
            mClusterItem prevClusterItem = (mClusterItem) objects[i];
            markerTxtClinic.setText(" " + prevClusterItem.getTitle() + " ");
            mIconClinic.setContentView(markerTxtClinic);
            BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(mIconClinic.makeIcon());
            LatLng latLng = new LatLng(clusterPosition.latitude + (Math.cos(radians * i) * unit), clusterPosition.longitude + (Math.sin(radians * i) * unit));
            markerOptions.icon(bitmapDescriptor)
                    .position(latLng)
                    .title(prevClusterItem.latLng.latitude + ":" + prevClusterItem.latLng.longitude);
            options.add(latLng).add(clusterPosition);
            markers.add(Map.addMarker(markerOptions));
            polylines.add(Map.addPolyline(options));
        }
        return true;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (markers.contains(marker)) {
            String[] split = marker.getTitle().split(":");
            LatLng markersRealLocation = new LatLng(Double.parseDouble(split[0]), Double.parseDouble(split[1]));
        } else {
            mClusterManager.onMarkerClick(marker);
        }
        return true;
    }
    
    @Override
    public void onCameraIdle() {
        mClusterManager.onCameraIdle();
        if (Map.getCameraPosition().zoom != focus) {
            focus = Map.getCameraPosition().zoom;
            for (int i = 0; i < markers.size(); i++) {
                markers.get(i).remove();
                polylines.get(i).remove();
            }
            markers.clear();
            polylines.clear();
        }
    }
    
    public void getMapReady() {
        mClusterManager = new ClusterManager<>(this, Map);
        mClusterManager.setRenderer(new mClusterRenderer());
        mClusterManager.setOnClusterItemClickListener(this);
        mClusterManager.setOnClusterClickListener(this);
        Map.setOnCameraIdleListener(this);
        Map.setOnMarkerClickListener(this);
        Map.setOnMapClickListener(this);
        Map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(32.4279, 53.6880), focus));
    }
    
    @Override
    public void onMapClick(LatLng latLng) {
        for (int i = 0; i < markers.size(); i++) {
            markers.get(i).remove();
            polylines.get(i).remove();
        }
        markers.clear();
        polylines.clear();
    }
    
    class mClusterRenderer extends DefaultClusterRenderer<mClusterItem> {
        mClusterRenderer() {
            super(getApplicationContext(), Map, mClusterManager);
        }

        @Override
        protected void onBeforeClusterItemRendered(mClusterItem item, MarkerOptions markerOptions) {
            markerTxt.setText(item.getTitle());
            mIcon.setContentView(markerTxt);
            BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(mIcon.makeIcon());
            markerOptions.icon(bitmapDescriptor);
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<mClusterItem> cluster, MarkerOptions markerOptions) {
            if (cluster.getSize() > 9) {
                markerTxt.setText((cluster.getSize() / 10 * 10) + "+");
            } else {
                markerTxt.setText(cluster.getSize());
            }
            mIcon.setContentView(markerTxt);
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(mIcon.makeIcon()));
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            return cluster.getSize() > 1;
        }
    }

    class mClusterItem implements ClusterItem {
        private final LatLng latLng;
        private final String title;

        mClusterItem(LatLng latLng, String title) {
            this.latLng = latLng;
            this.title = title;
        }

        public String getTitle() {
            return title;
        }

        @Override
        public LatLng getPosition() {
            return latLng;
        }
    }
