package com.zq.dfrq01;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.esri.arcgisruntime.data.Geodatabase;
import com.esri.arcgisruntime.data.GeodatabaseFeatureTable;
import com.esri.arcgisruntime.data.TileCache;
import com.esri.arcgisruntime.data.VectorTileCache;
import com.esri.arcgisruntime.layers.ArcGISTiledLayer;
import com.esri.arcgisruntime.layers.ArcGISVectorTiledLayer;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.LayerList;
import com.esri.arcgisruntime.mapping.MobileMapPackage;
import com.esri.arcgisruntime.mapping.view.BackgroundGrid;
import com.esri.arcgisruntime.mapping.view.MapView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 1;
    // 离线文件后缀名
    private static final String TPK_FILE_EXTENSION = ".tpk";
    private static final String VTPK_FILE_EXTENSION =".vtpk";
    private static final String GEODATABASE_FILE_EXTENSION =".geodatabase";
    private static final String MMPK_FILE_EXTENSION = ".mmpk";
    //sdcard 目录名称
    private static File extStorDir;

    // 离线文件文件路径
    private static String extSDCardDirName_tpk, extSDCardDirName_vtpk, extSDCardDirName_geodatabase, extSDCardDirName_mmpk;
    // 离线文件文件名
    private static String filename_tpk,filename_vtpk, filename_geodatabase, filename_mmpk;
    // 离线文件完整路径
    private static String tpkFilePath, vtpkFilePath, geodatabaseFilePath, mmpkFilePath;
    private MapView mMapView;
    // mmpk离线地图包对象
    private MobileMapPackage mMobileMapPackage;
    private BackgroundGrid mBackgroundGrid;
    // 图层列表
    //private LayerList mLayerList;

    private Basemap mainBasemap,secondBasemap;
    private ArcGISMap mainArcGISMap,secondArcGISMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createFilePath();
        mMapView = findViewById(R.id.mMapView);
        askPermission();
    }

    private void createFilePath() {
        // 获取 sdcard 资源名称
        extStorDir = Environment.getExternalStorageDirectory();
        /// 获取目录
        extSDCardDirName_tpk = this.getResources().getString(R.string.config_data_sdcard_offline_dir_tpk);
        extSDCardDirName_vtpk = this.getResources().getString(R.string.config_data_sdcard_offline_dir_vtpk);
        extSDCardDirName_geodatabase = this.getResources().getString(R.string.config_data_sdcard_offline_dir_geodatabase);
        extSDCardDirName_mmpk = this.getResources().getString(R.string.config_data_sdcard_offline_dir_mmpk);
        /// 获取离线地图包文件名
        // 瓦片地图包文件名
        filename_tpk = this.getResources().getString(R.string.config_tpk_name);
        // 矢量地图包文件名
        filename_vtpk = this.getResources().getString(R.string.config_vtpk_name);
        // 地图包文件名
        filename_geodatabase = this.getResources().getString(R.string.config_geodatabase_name);
        // 移动地图包文件名
        filename_mmpk = this.getResources().getString(R.string.config_mmpk_name);
        // 创建移动地图包文件的完整路径
        tpkFilePath = createTilePackageFilePath();
        vtpkFilePath = createVectorMapPackageFilePath();
        geodatabaseFilePath = createGeopackageFilePath();
        mmpkFilePath = createMobileMapPackageFilePath();
    }

    /**
     * 创建地图切片包文件位置和名称结构-tpk
     */
    private static String createTilePackageFilePath() {
        return extStorDir.getAbsolutePath() + File.separator + extSDCardDirName_tpk + File.separator + filename_tpk
                + TPK_FILE_EXTENSION;
    }
    /**
     * 创建矢量地图包文件位置和名称结构-vtpk
     */
    private static String createVectorMapPackageFilePath() {
        return extStorDir.getAbsolutePath() + File.separator + extSDCardDirName_vtpk + File.separator + filename_vtpk
                + VTPK_FILE_EXTENSION;
    }
    /**
     * 创建geodatabase文件位置和名称结构-geodatabase
     */
    private static String createGeopackageFilePath() {
        return extStorDir.getAbsolutePath() + File.separator + extSDCardDirName_geodatabase + File.separator + filename_geodatabase
                + GEODATABASE_FILE_EXTENSION;
    }
    /**
     * 创建移动地图包文件位置和名称结构-mmpk
     */
    private static String createMobileMapPackageFilePath() {
        return extStorDir.getAbsolutePath() + File.separator + extSDCardDirName_mmpk + File.separator + filename_mmpk
                + MMPK_FILE_EXTENSION;
    }

    /**
     * 向用户请求权限
     */
    private void askPermission() {
        //将所需申请的权限添加到List集合中
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
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
            //loadGeodatabase();
            loadMapPackage2();
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
    }
    /**
     * 将移动地图包作为底图图层加载到 MapView 中
     */
    private void loadMapPackageBasemap(){

        mMobileMapPackage = new MobileMapPackage(mmpkFilePath);
        mMobileMapPackage.loadAsync();
        mMobileMapPackage.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                LoadStatus mainLoadStatus = mMobileMapPackage.getLoadStatus();
                if (mainLoadStatus == LoadStatus.LOADED) {
                    List<ArcGISMap> mainArcGISMapL = mMobileMapPackage.getMaps();
                    ArcGISMap mainArcGISMap = mainArcGISMapL.get(0);
                    //Basemap mainBasemap = mainArcGISMap.getBasemap();
                    LayerList mainMMPKLL = mainArcGISMap.getOperationalLayers();
                    int akb = mainMMPKLL.size();
                    mMapView.setMap(mainArcGISMap);
                }
            }
        });
    }
    /**
     * 将矢量地图包作为底图加载到MapView中
     */
    private void loadVectorPackage(){
        VectorTileCache mainVectorTileChahe = new VectorTileCache(vtpkFilePath);
        ArcGISVectorTiledLayer mainArcGISVectorTiledLayer = new ArcGISVectorTiledLayer(mainVectorTileChahe);
        secondBasemap = new Basemap(mainArcGISVectorTiledLayer);
        secondArcGISMap = new ArcGISMap(secondBasemap);
        mMapView.setMap(secondArcGISMap);
        /*
        mainMapImageLayer = new ArcGISMapImageLayer(mainArcGISMapImageLayerURL);
        mainMapImageLayer.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                mainSublayerList = mainMapImageLayer.getSublayers();
                ArcGISMapImageSublayer mainMapImageSublayer = (ArcGISMapImageSublayer) mainSublayerList.get(0);
            }
        });
        mainMapImageLayer.setOpacity(0.5f);
        mainArcGISMap.getOperationalLayers().add(mainMapImageLayer);*/

    }

    /**
     * 将移动地图包作为操作图层加载到 MapView 中
     * 只显示出一层
     */
    private void loadMapPackage() {
        //加载mmpk
        mMobileMapPackage = new MobileMapPackage(mmpkFilePath);
        mMobileMapPackage.loadAsync();
        mMobileMapPackage.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                LoadStatus mainLoadStatus = mMobileMapPackage.getLoadStatus();
                if (mainLoadStatus == LoadStatus.LOADED) {

                    List<ArcGISMap> mArcGISMapList = mMobileMapPackage.getMaps();
                    ArcGISMap mainArcGISMapMMPK = mArcGISMapList.get(0);
                    //Basemap mainBasemapMMPK = mainArcGISMapMMPK.getBasemap();
                    LayerList mainMMPKLL = mainArcGISMapMMPK.getOperationalLayers();
                    LayerList mLayerList = mainArcGISMap.getOperationalLayers();
                    FeatureLayer mainFeatureLayer = (FeatureLayer) mainMMPKLL.get(0);
                    // 不透明性
                    mainFeatureLayer.setOpacity(0.8f);
                    mainArcGISMapMMPK.getOperationalLayers().remove(0);
                    mLayerList.add(mainFeatureLayer);
                }
            }
        });
    }
    /**
     * 将移动地图包作为操作图层加载到 MapView 中
     * 崩溃！！！
     */
    private void loadMapPackage2() {
        //加载mmpk
        mMobileMapPackage = new MobileMapPackage(mmpkFilePath);
        mMobileMapPackage.loadAsync();
        mMobileMapPackage.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                LoadStatus mainLoadStatus = mMobileMapPackage.getLoadStatus();
                if (mainLoadStatus == LoadStatus.LOADED) {
                    // ArcGISMap类型的列表存储mmpk地图包中的地图
                    List<ArcGISMap> mArcGISMapList = mMobileMapPackage.getMaps();

                    // 从列表中取出
                    ArcGISMap mainArcGISMapMMPK = mArcGISMapList.get(0);
                    LayerList mainMMPKLL = mainArcGISMapMMPK.getOperationalLayers();

                    //
                    int valueCount = mainMMPKLL.size();
                    for(int i = 0 ; i<valueCount; i++ ) {
                        mainArcGISMapMMPK.getOperationalLayers().remove(0);
                        FeatureLayer mainFeatureLayer = (FeatureLayer) mainMMPKLL.get(i);
                        mainFeatureLayer.setVisible(true);
                        LayerList mLayerList = mainArcGISMap.getOperationalLayers();
                        // 不透明性
                        // mainFeatureLayer.setOpacity(0.8f);
                        //崩溃
                        mLayerList.add(mainFeatureLayer);
                    }

                }
            }
        });
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
                for (int i = valueCount - 1; i > -1; i--) {
                    FeatureLayer valueFL = new FeatureLayer(resultsGFT.get(i));
                    valueFL.setVisible(true);
                    LayerList mainLayerList = mainArcGISMap.getOperationalLayers();
                    mainLayerList.add(valueFL);
                }
            }
        });
    }
}
