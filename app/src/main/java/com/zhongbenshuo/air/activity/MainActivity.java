package com.zhongbenshuo.air.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;

import com.zhongbenshuo.air.R;
import com.zhongbenshuo.air.adapter.HistoryDataAdapter;
import com.zhongbenshuo.air.adapter.RealDataAdapter;
import com.zhongbenshuo.air.adapter.StationAdapter;
import com.zhongbenshuo.air.bean.Environment;
import com.zhongbenshuo.air.bean.EventMsg;
import com.zhongbenshuo.air.bean.RealData;
import com.zhongbenshuo.air.bean.Station;
import com.zhongbenshuo.air.constant.Constants;
import com.zhongbenshuo.air.service.WebSocketService;
import com.zhongbenshuo.air.utils.GsonUtils;
import com.zhongbenshuo.air.utils.LogUtils;
import com.zhongbenshuo.air.widget.ClockView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 主页面，环境监测页面
 * Created at 2019/9/24 18:21
 *
 * @author LiYuliang
 * @version 1.0
 */

public class MainActivity extends BaseActivity {

    private Context mContext;
    private RecyclerView rvPosition;
    private SparseArray<List<Environment>> environmentListMap;
    private List<Station> stationList;
    private List<Environment> environmentList;
    private List<RealData> realDataList;
    private StationAdapter stationAdapter;
    private HistoryDataAdapter historyDataAdapter;
    private RealDataAdapter realDataAdapter;
    private int selectedStation = 0;
    private ClockView cvIlluminance;

    private static boolean flag = true;
    // 用于自动点击Item的定时任务
    private SyncTimeTask syncTimeTask;

    // 每个监测点停留时间
    private static final int WAIT_TIME_SECONDS = 10;

    // 定时任务执行时间
    private static volatile long seconds = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        environmentListMap = new SparseArray<>();

        rvPosition = findViewById(R.id.rvPosition);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvPosition.setLayoutManager(linearLayoutManager);
        stationList = new ArrayList<>();
        stationAdapter = new StationAdapter(this, stationList, selectedStation);
        stationAdapter.setOnItemClickListener(onItemClickListener);
        rvPosition.setAdapter(stationAdapter);

        RecyclerView rvHistory = findViewById(R.id.rvHistory);
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(mContext);
        linearLayoutManager2.setOrientation(LinearLayoutManager.VERTICAL);
        rvHistory.setLayoutManager(linearLayoutManager2);
        environmentList = new ArrayList<>();
        historyDataAdapter = new HistoryDataAdapter(this, environmentList);
        rvHistory.setAdapter(historyDataAdapter);

        RecyclerView rvRealTime = findViewById(R.id.rvRealTime);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext, 3);
        rvRealTime.setLayoutManager(gridLayoutManager);
        realDataList = new ArrayList<>();
        realDataAdapter = new RealDataAdapter(this, realDataList);
        rvRealTime.setAdapter(realDataAdapter);

        cvIlluminance = findViewById(R.id.cvIlluminance);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        initWebSocketService();

        syncTimeTask = new SyncTimeTask(this);
        syncTimeTask.execute();
    }

    private StationAdapter.OnItemClickListener onItemClickListener = position -> {
        selectedStation = position;
        seconds = 0;
        // 平滑地将点击的item滚动到中间
        rvPosition.smoothScrollToPosition(position);
        stationAdapter.setSelectedPosition(position);
        refreshPage(null);
    };

    /**
     * 收到EventBus发来的消息并处理
     *
     * @param msg 消息对象
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void receiveMessage(EventMsg msg) {
        if (msg.getTag().equals(Constants.CONNECT_OPEN_WEBSOCKET)) {
            //接收到这个消息说明连接成功
            LogUtils.d(TAG, "WebSocket连接成功");
        }
        if (msg.getTag().equals(Constants.CONNECT_CLOSE_WEBSOCKET)) {
            //接收到这个消息说明连接失败或者中断了
            LogUtils.d(TAG, "WebSocket关闭");
        }
        if (msg.getTag().equals(Constants.CONNECT_FAIL_WEBSOCKET)) {
            //接收到这个消息说明连接出错了
            LogUtils.d(TAG, "WebSocket连接错误");
        }
        if (msg.getTag().equals(Constants.SHOW_TOAST_WEBSOCKET)) {
            //接收到这个消息说明是非Websocket对象的数据
            LogUtils.d(TAG, msg.getMsg());
        }
        if (msg.getTag().equals(Constants.SHOW_DATA_WEBSOCKET)) {
            //接收到这个消息说明需要展示数据
            Environment environment = GsonUtils.parseJSON(msg.getMsg(), Environment.class);
            if (environmentListMap.get(environment.getStation()) == null) {
                List<Environment> environments = new ArrayList<>();
                environments.add(0, environment);
                environmentListMap.put(environment.getStation(), environments);
            } else {
                environmentListMap.get(environment.getStation()).add(0, environment);
            }
            // 最多保留4条
            if (environmentListMap.get(environment.getStation()).size() > 4) {
                environmentListMap.get(environment.getStation()).remove(0);
            }
            // 刷新页面
            refreshPage(new Station(environment.getStation(), environment.getStation_name(), environment.isState()));
        }
    }

    /**
     * 刷新页面
     *
     * @param station 环境监测站对象
     */
    private void refreshPage(Station station) {
        if (station != null && !stationList.contains(station)) {
            stationList.add(station);
        }
        // 刷新站点列表
        Collections.sort(stationList);
        stationAdapter.notifyDataSetChanged();

        // 刷新历史数据列表
        environmentList.clear();
        environmentList.addAll(environmentListMap.get(stationList.get(selectedStation).getStationId()));
        historyDataAdapter.notifyDataSetChanged();

        // 刷新实时数据页面
        if (environmentListMap.get(stationList.get(selectedStation).getStationId()).size() > 0) {
            realDataList.clear();
            Environment environment = environmentListMap.get(stationList.get(selectedStation).getStationId()).get(0);
            realDataList.add(new RealData("温度", RealData.DATA_TYPE.TYPE_TEMP, environment.getTemperature(), "℃"));
            realDataList.add(new RealData("湿度", RealData.DATA_TYPE.TYPE_HUMIDITY, environment.getHumidity(), "%"));
            realDataList.add(new RealData("PM2.5", RealData.DATA_TYPE.TYPE_PM25, environment.getPm25(), "μg/m³"));
            realDataList.add(new RealData("PM10", RealData.DATA_TYPE.TYPE_PM10, environment.getPm10(), "μg/m³"));
            realDataList.add(new RealData("甲醛", RealData.DATA_TYPE.TYPE_HCHO, environment.getFormaldehyde(), "mg/m³"));
            realDataList.add(new RealData("二氧化碳", RealData.DATA_TYPE.TYPE_CO2, environment.getCarbonDioxide(), "ppm"));
            realDataAdapter.notifyDataSetChanged();
            cvIlluminance.setTitle("光照度");
            cvIlluminance.setCompleteDegree(environment.getIlluminance(), "lux");
            cvIlluminance.setColor(mContext.getResources().getColor(R.color.value_low), mContext.getResources().getColor(R.color.value_low), mContext.getResources().getColor(R.color.value_low));
            cvIlluminance.setValue(0, 1000, 0, 0);
            cvIlluminance.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 初始化并绑定WebSocketService
     */
    private void initWebSocketService() {
        Intent intent = new Intent(mContext, WebSocketService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    // 无限循环的定时任务
    private static class SyncTimeTask extends AsyncTask<Void, Void, Void> {

        private WeakReference<MainActivity> mainActivityWeakReference;

        private SyncTimeTask(MainActivity activity) {
            mainActivityWeakReference = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            while (flag) {
                if (isCancelled()) {
                    break;
                }
                publishProgress();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                seconds++;
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate();
            if (isCancelled()) {
                return;
            }
            MainActivity mainActivity = mainActivityWeakReference.get();
            LogUtils.d(mainActivity.TAG, "当前seconds为：" + seconds);
            if (mainActivity.stationList.size() != 0 && seconds % WAIT_TIME_SECONDS == 0) {
                LogUtils.d(mainActivity.TAG, "跳转到下一个");
                // 跳转到下一个监测点，如果是最后一个，则跳转到第一个
                if (mainActivity.selectedStation == mainActivity.stationList.size() - 1) {
                    // 表示当前选中的是最后一个监测点
                    LogUtils.d(mainActivity.TAG, "当前在最后一个，跳转到第一个");
                    mainActivity.selectedStation = 0;
                } else if (mainActivity.selectedStation < mainActivity.stationList.size() - 1) {
                    // 表示当前选中的不是最后一个
                    LogUtils.d(mainActivity.TAG, "当前不在最后一个，跳转到下一个");
                    mainActivity.selectedStation++;
                }
                // 平滑地将这个的item滚动到中间
                mainActivity.rvPosition.smoothScrollToPosition(mainActivity.selectedStation);
                mainActivity.stationAdapter.setSelectedPosition(mainActivity.selectedStation);
                mainActivity.refreshPage(null);
            }
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }

    @Override
    protected void onDestroy() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        flag = false;
        if (syncTimeTask != null) {
            syncTimeTask.cancel(true);
            syncTimeTask = null;
        }
        super.onDestroy();
    }

}
