package com.zq.dfrqfirst;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.data.Geodatabase;
import com.esri.arcgisruntime.data.GeodatabaseFeatureTable;
import com.esri.arcgisruntime.data.TileCache;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.ArcGISTiledLayer;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.LayerList;
import com.esri.arcgisruntime.mapping.view.BackgroundGrid;
import com.esri.arcgisruntime.mapping.view.Callout;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.mapping.view.MapView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final int REQUEST_CODE = 1;
    private BackgroundGrid mBackgroundGrid;
    //sdcard 目录名称
    private static File extStorDir;
    // 离线文件文件路径
    private static String extSDCardDirName_tpk, extSDCardDirName_vtpk, extSDCardDirName_geodatabase, extSDCardDirName_mmpk;
    // 离线文件文件名
    private static String filename_tpk,filename_vtpk, filename_geodatabase, filename_mmpk;
    // 离线文件完整路径
    private static String tpkFilePath, vtpkFilePath, geodatabaseFilePath, mmpkFilePath;

    // 离线文件后缀名
    private static final String TPK_FILE_EXTENSION = ".tpk";
    //private static final String VTPK_FILE_EXTENSION =".vtpk";
    private static final String GEODATABASE_FILE_EXTENSION =".geodatabase";
    //private static final String MMPK_FILE_EXTENSION = ".mmpk";
    Envelope mEnvelope;
    DrawerLayout mDrawerLayout;
    NavigationView mNavigationView;
    ImageButton ibLocation,ibLengthMeasure,ibAreaMeasure,ibZoomIn,ibZoomOut,ibLayerManager,ibSearch;
    ListView mListView;
    MapView mMapView;
    LinearLayout mBottomMenu;
    LocationDisplay locationDisplay;
    Toolbar mToolbar;
    private Callout mCallout;
    private static final String sTag = "Gesture";

    private Basemap mainBasemap,secondBasemap;
    private ArcGISMap mainArcGISMap,secondArcGISMap;
    double mScale;
    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.mainContainer);
        mNavigationView = (NavigationView)findViewById(R.id.nav_right_view);
        mMapView = (MapView) findViewById(R.id.mainMapView);
        mBottomMenu = (LinearLayout)findViewById(R.id.main_bottom_menu);
        //Toobar的搜索 ！！！！
        mToolbar = (Toolbar) findViewById(R.id.mainHeaderToolBar);
        //mToolbar.setLogo(R.drawable.logo_dongfeng);
        setSupportActionBar(mToolbar);
        mToolbar.setOnMenuItemClickListener(onMenuItemClick);
        ArcGISRuntimeEnvironment.setLicense("runtimelite,1000,rud4660843194,none,7RE60RFLTHELXH46C249");
        createFilePath();
        askPermission();
//        SpatialReference sr =  mMapView.getSpatialReference();
//        Point point=locationDisplay.getMapLocation();
//        Log.i("sss=",point.toString());
//        LocationDataSource.Location location=locationDisplay.getLocation();
//        Point point2=location.getPosition();
//        Log.i("sss=",point2.toString());
        //double mscale=  mMapView.getMapScale();
        ibLocation =(ImageButton)findViewById(R.id.mainLocationBT);
        ibLocation.setOnClickListener(this);
        ibZoomIn = (ImageButton)findViewById(R.id.mainZoomInBT);
        ibZoomIn.setOnClickListener(this);
        ibZoomOut=(ImageButton)findViewById(R.id.mainZoomOutBT);
        ibZoomOut.setOnClickListener(this);
        ibLayerManager = (ImageButton) findViewById(R.id.mainOpenRightBT );
        ibLayerManager.setOnClickListener(this);
        ibSearch = (ImageButton)findViewById(R.id.mainSearchBT);
        ibSearch.setOnClickListener(this);
        mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {

            @Override
            public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
               // Log.d(sTag, "onSingleTapConfirmed: " + motionEvent.toString());

                // get the point that was clicked and convert it to a point in map coordinates
                android.graphics.Point screenPoint = new android.graphics.Point(Math.round(motionEvent.getX()),
                        Math.round(motionEvent.getY()));
                // create a map point from screen point
                Point mapPoint = mMapView.screenToLocation(screenPoint);
                // convert to WGS84 for lat/lon format
                Point wgs84Point = (Point) GeometryEngine.project(mapPoint, SpatialReferences.getWgs84());
                // create a textview for the callout
                TextView calloutContent = new TextView(getApplicationContext());
                calloutContent.setTextColor(Color.BLACK);
                //calloutContent.setSingleLine();
                // format coordinates to 4 decimal places
                calloutContent.setText(R.string.operations_layer_label);
                // get callout, set content and show
                mCallout = mMapView.getCallout();
                mCallout.setLocation(mapPoint);
                mCallout.setContent(calloutContent);
                mCallout.show();

                // center on tapped point
                mMapView.setViewpointCenterAsync(mapPoint);

                return true;
            }
        });

    }
    private void createFilePath() {
        // 获取 sdcard 资源名称
        extStorDir = Environment.getExternalStorageDirectory();
        /// 获取目录
        extSDCardDirName_tpk = this.getResources().getString(R.string.config_data_sdcard_offline_dir_tpk);
        //extSDCardDirName_vtpk = this.getResources().getString(R.string.config_data_sdcard_offline_dir_vtpk);
        extSDCardDirName_geodatabase = this.getResources().getString(R.string.config_data_sdcard_offline_dir_geodatabase);
        //extSDCardDirName_mmpk = this.getResources().getString(R.string.config_data_sdcard_offline_dir_mmpk);
        /// 获取离线地图包文件名
        // 瓦片地图包文件名
        filename_tpk = this.getResources().getString(R.string.config_tpk_name);
        // 矢量地图包文件名
        //filename_vtpk = this.getResources().getString(R.string.config_vtpk_name);
        // 地图包文件名
        filename_geodatabase = this.getResources().getString(R.string.config_geodatabase_name);
        // 移动地图包文件名
        //filename_mmpk = this.getResources().getString(R.string.config_mmpk_name);
        // 创建移动地图包文件的完整路径
        tpkFilePath = createTilePackageFilePath();
       // vtpkFilePath = createVectorMapPackageFilePath();
        geodatabaseFilePath = createGeopackageFilePath();
       // mmpkFilePath = createMobileMapPackageFilePath();
    }
    /**
     * 创建地图切片包文件位置和名称结构-tpk
     */
    private static String createTilePackageFilePath() {
        return extStorDir.getAbsolutePath() + File.separator + extSDCardDirName_tpk + File.separator + filename_tpk
                + TPK_FILE_EXTENSION;
    }

    /**
     * 创建geodatabase文件位置和名称结构-geodatabase
     */
    private static String createGeopackageFilePath() {
        return extStorDir.getAbsolutePath() + File.separator + extSDCardDirName_geodatabase + File.separator + filename_geodatabase
                + GEODATABASE_FILE_EXTENSION;
    }
    private void askPermission() {
        //将所需申请的权限添加到List集合中
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        //判断权限列表是否为空，若不为空，则向用户申请权限，否则则直接执行操作
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, REQUEST_CODE);
        } else {
            //TODO
            loadTilePackageMap();
            //loadMapPackage();
            //loadVectorPackage();
            loadGeodatabase();
            //loadMapPackage2();
            //loadMapPackageBasemap();
        }
    }
    /**
     * 将瓦片地图包作为底图加载到MapView中
     */
    private void loadTilePackageMap(){
        //隐藏背景格网
        mBackgroundGrid = new BackgroundGrid();
        mBackgroundGrid.setColor(0xffffffff);
        mBackgroundGrid.setGridLineColor(0xffffffff);
        mBackgroundGrid.setGridLineWidth(0);
        mMapView.setBackgroundGrid(mBackgroundGrid);
        //加载地图
        TileCache mainTileCache = new TileCache(tpkFilePath);
        ArcGISTiledLayer mainArcGISTiledLayer = new ArcGISTiledLayer(mainTileCache);
        mainBasemap = new Basemap(mainArcGISTiledLayer);
        mainArcGISMap = new ArcGISMap(mainBasemap);
        mMapView.setMap(mainArcGISMap);
//        mScale = 0.0;
//        mScale=mMapView.getMapScale();
//        mScale*=0.5;
//        mMapView.setViewpointScaleAsync(mScale);
    }

    /**
     * 将geodatabase包作为操作图层加载到 MapView 中
     */
    private void loadGeodatabase(){
        final Geodatabase mainGeodatabase = new Geodatabase(geodatabaseFilePath);
        mainGeodatabase.loadAsync();
        mainGeodatabase.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                List<GeodatabaseFeatureTable> resultsGFT = mainGeodatabase.getGeodatabaseFeatureTables();
                int valueCount = resultsGFT.size();
                /*for (int i = valueCount - 1; i > -1; i--) {
                    FeatureLayer valueFL = new FeatureLayer(resultsGFT.get(i));
                    valueFL.setVisible(true);
                    LayerList mainLayerList = mainArcGISMap.getOperationalLayers();
                    mainLayerList.add(valueFL);
                }*/
                FeatureLayer valueFL = new FeatureLayer(resultsGFT.get(3));
                valueFL.setVisible(true);
                LayerList mainLayerList = mainArcGISMap.getOperationalLayers();
                mainLayerList.add(valueFL);
            }
        });
    }
    @Override
    public void onClick(View v) {
        double mScale = mMapView.getMapScale();
        switch (v.getId()) {
            case R.id.mainLocationBT:
                locationDisplay = mMapView.getLocationDisplay();
                locationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.RECENTER);
                locationDisplay.startAsync();
                break;
            case R.id.mainOpenRightBT:
                mDrawerLayout.openDrawer(mNavigationView);
                break;
            case R.id.mainZoomInBT:

                mMapView.setViewpointScaleAsync(mScale*0.5);
                break;
            case R.id.mainZoomOutBT:
                mMapView.setViewpointScaleAsync(mScale*2);
                break;
            case R.id.mainSearchBT:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                final AlertDialog dialog = builder.create();

                View view = View.inflate(this, R.layout.place_search, null);
                dialog.setView(view);// 将自定义的布局文件设置给dialog
                dialog.show();
                break;
//            case R.id.mainMapView:
//
//                break;




        }
    }
    private Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            String msg = "";
            switch (menuItem.getItemId()) {
                case R.id.action_search:
                    msg += "Click edit";
                    break;
                case R.id.action_settings:
                    msg += "Click setting";
                    break;
            }
            return true;
        }
    };


    @Override
    protected void onPause() {
        super.onPause();
        mMapView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.dispose();
    }
}
