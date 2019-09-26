package com.zhongbenshuo.air.activity;

import android.content.Context;
import android.content.Intent;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.zhongbenshuo.air.R;
import com.zhongbenshuo.air.adapter.HistoryDataAdapter;
import com.zhongbenshuo.air.adapter.RealDataAdapter;
import com.zhongbenshuo.air.adapter.StationAdapter;
import com.zhongbenshuo.air.bean.Environment;
import com.zhongbenshuo.air.bean.EventMsg;
import com.zhongbenshuo.air.bean.OpenAndCloseDoorRecord;
import com.zhongbenshuo.air.bean.RealData;
import com.zhongbenshuo.air.bean.Result;
import com.zhongbenshuo.air.bean.Station;
import com.zhongbenshuo.air.constant.Constants;
import com.zhongbenshuo.air.constant.ErrorCode;
import com.zhongbenshuo.air.constant.NetWork;
import com.zhongbenshuo.air.glide.RoundedCornersTransformation;
import com.zhongbenshuo.air.network.ExceptionHandle;
import com.zhongbenshuo.air.network.NetClient;
import com.zhongbenshuo.air.network.NetworkObserver;
import com.zhongbenshuo.air.service.WebSocketService;
import com.zhongbenshuo.air.utils.GsonUtils;
import com.zhongbenshuo.air.utils.LogUtils;
import com.zhongbenshuo.air.utils.NetworkUtil;
import com.zhongbenshuo.air.utils.TimeUtils;
import com.zhongbenshuo.air.widget.ClockView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.zhongbenshuo.air.glide.RoundedCornersTransformation.CENTER_CROP;
import static com.zhongbenshuo.air.glide.RoundedCornersTransformation.CORNER_ALL;

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
    private ImageView ivUser;

    private static boolean flag = true;
    // 用于自动点击Item的定时任务
    private SyncTimeTask syncTimeTask;

    // 每个监测点停留时间
    private static final int WAIT_TIME_SECONDS = 10;

    // 定时任务执行时间
    private static volatile long seconds = 0;
    private static volatile int photoShowTime = 0;

    private MediaPlayer mediaPlayer;

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
        ivUser = findViewById(R.id.ivUser);

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 对于Android 8.0+
            AudioFocusRequest audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                    .setOnAudioFocusChangeListener(focusChangeListener).build();
            audioFocusRequest.acceptsDelayedFocusGain();
            audioManager.requestAudioFocus(audioFocusRequest);
        } else {
            // 小于Android 8.0
            int result = audioManager.requestAudioFocus(focusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                // could not get audio focus.
            }
        }

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
            // 通过站号过滤无效的数据（station为0的时候，表示这条数据在服务端解析异常）
            if (environment.getStation() > 0) {
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
        if (msg.getTag().equals(Constants.SHOW_USER_PHOTO)) {
            //接收到这个消息说明有人按门铃
            String url = msg.getMsg();
            if (!TextUtils.isEmpty(url)) {
                ivUser.setWillNotDraw(false);
                ivUser.setVisibility(View.VISIBLE);
                LogUtils.d(TAG, "展示照片：" + "http://" + NetWork.SERVER_HOST_MAIN + ":" + NetWork.SERVER_PORT_MAIN + "/" + url.replace("\\", "/"));
                RoundedCornersTransformation roundedCornersTransformation = new RoundedCornersTransformation(10, 0, CORNER_ALL, CENTER_CROP);
                RequestOptions options = new RequestOptions().dontAnimate().transform(roundedCornersTransformation);
                Glide.with(mContext).load("http://" + NetWork.SERVER_HOST_MAIN + ":" + NetWork.SERVER_PORT_MAIN + "/" + url.replace("\\", "/")).apply(options).into(ivUser);
            }
            // 播放门铃音乐
            mediaPlayer = MediaPlayer.create(mContext, R.raw.bell);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
            photoShowTime = 0;
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
            cvIlluminance.setValue(0, 2000, 0, 0);
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
                photoShowTime++;
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
            if (photoShowTime == 5) {
                mainActivity.ivUser.setWillNotDraw(true);
                mainActivity.ivUser.setVisibility(View.GONE);
                if (mainActivity.mediaPlayer != null) {
                    mainActivity.mediaPlayer.stop();
                    mainActivity.mediaPlayer.release();
                    mainActivity.mediaPlayer = null;
                }
                photoShowTime = 0;
            }
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }

    //焦点问题
    private AudioManager.OnAudioFocusChangeListener focusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                    // 长时间失去
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                    }
                    mediaPlayer.release();
                    mediaPlayer = null;
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    // 短时间失去
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    // 暂时失去 audio focus，但是允许持续播放音频(以很小的声音)，不需要完全停止播放。
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.setVolume(0.1f, 0.1f);
                    }
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    // 获得音频焦点
                    if (mediaPlayer == null) {
                        mediaPlayer = new MediaPlayer();
                    } else if (!mediaPlayer.isPlaying()) {
                        mediaPlayer.start();
                    }
                    mediaPlayer.setVolume(1.0f, 1.0f);
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 开关门
     *
     * @param status 1、开  2、关
     */
    private void openAndCloseDoorRecord(int status) {
        OpenAndCloseDoorRecord openAndCloseDoorRecord = new OpenAndCloseDoorRecord();
        openAndCloseDoorRecord.setUser_id(999999999);
        openAndCloseDoorRecord.setCreateTime(TimeUtils.getCurrentDateTime());
        openAndCloseDoorRecord.setStatus(status);

        Observable<Result> resultObservable = NetClient.getInstance(NetClient.BASE_URL_PROJECT, true).getZbsApi().openAndCloseDoorRecord(openAndCloseDoorRecord);
        resultObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new NetworkObserver<Result>(this) {

            @Override
            public void onSubscribe(Disposable d) {
                //接下来可以检查网络连接等操作
                if (!NetworkUtil.isNetworkAvailable(mContext)) {
                    showToast("当前网络不可用，请检查网络");
                }
            }

            @Override
            public void onError(ExceptionHandle.ResponseThrowable responseThrowable) {
                showToast(responseThrowable.message);
            }

            @Override
            public void onNext(Result result) {
                super.onNext(result);
                if (result.getCode() == ErrorCode.SUCCESS) {
                    if (status == 1) {
                        showToast("开门成功");
                    } else {
                        showToast("关门成功");
                    }
                } else if (result.getCode() == ErrorCode.FAIL) {
                    if (status == 1) {
                        showToast("开门失败");
                    } else {
                        showToast("关门失败");
                    }
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_DPAD_CENTER:
                //确定键enter
                LogUtils.d(TAG, "点击了确定键");
                // 开门
                openAndCloseDoorRecord(1);
                break;
            case KeyEvent.KEYCODE_BACK:
                //返回键
                //这里由于break会退出，所以我们自己要处理掉 不返回上一层
                LogUtils.d(TAG, "点击了返回键");
                return true;
            case KeyEvent.KEYCODE_SETTINGS:
                //设置键
                LogUtils.d(TAG, "点击了设置键");
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                //向下键
                /*    实际开发中有时候会触发两次，所以要判断一下按下时触发 ，松开按键时不触发
                 *    exp:KeyEvent.ACTION_UP
                 */
                LogUtils.d(TAG, "点击了下键");
                if (stationList.size() != 0) {
                    LogUtils.d(TAG, "跳转到下一个");
                    seconds = 0;
                    // 跳转到下一个监测点，如果是最后一个，则跳转到第一个
                    if (selectedStation == stationList.size() - 1) {
                        // 表示当前选中的是最后一个监测点
                        LogUtils.d(TAG, "当前在最后一个，跳转到第一个");
                        selectedStation = 0;
                    } else if (selectedStation < stationList.size() - 1) {
                        // 表示当前选中的不是最后一个
                        LogUtils.d(TAG, "当前不在最后一个，跳转到下一个");
                        selectedStation++;
                    }
                    // 平滑地将这个的item滚动到中间
                    rvPosition.smoothScrollToPosition(selectedStation);
                    stationAdapter.setSelectedPosition(selectedStation);
                    refreshPage(null);
                }
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                //向上键
                LogUtils.d(TAG, "点击了上键");
                if (stationList.size() != 0) {
                    LogUtils.d(TAG, "跳转到上一个");
                    seconds = 0;
                    // 跳转到上一个监测点，如果是第一个，则跳转到最后一个
                    if (selectedStation == 0) {
                        // 表示当前选中的是第一个监测点
                        LogUtils.d(TAG, "当前在第一一个，跳转到最后个");
                        selectedStation = stationList.size() - 1;
                    } else if (selectedStation > 0) {
                        // 表示当前选中的不是第一个
                        LogUtils.d(TAG, "当前不在第一个，跳转到上一个");
                        selectedStation--;
                    }
                    // 平滑地将这个的item滚动到中间
                    rvPosition.smoothScrollToPosition(selectedStation);
                    stationAdapter.setSelectedPosition(selectedStation);
                    refreshPage(null);
                }
                break;
            case KeyEvent.KEYCODE_0:
                //数字键0
                LogUtils.d(TAG, "点击了数字键0");
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                //向左键
                LogUtils.d(TAG, "点击了左键");
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                //向右键
                LogUtils.d(TAG, "点击了右键");
                break;
            case KeyEvent.KEYCODE_INFO:
                //info键
                LogUtils.d(TAG, "点击了Info键");
                break;
            case KeyEvent.KEYCODE_PAGE_DOWN:
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                //向下翻页键
                LogUtils.d(TAG, "点击了向下翻页键");
                break;
            case KeyEvent.KEYCODE_PAGE_UP:
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                //向上翻页键
                LogUtils.d(TAG, "点击了向上翻页键");
                break;
            case KeyEvent.KEYCODE_VOLUME_UP:
                //调大声音键
                LogUtils.d(TAG, "点击了音量增大键");
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                //降低声音键
                LogUtils.d(TAG, "点击了音量减小键");
                break;
            case KeyEvent.KEYCODE_VOLUME_MUTE:
                //禁用声音
                LogUtils.d(TAG, "点击了禁音键");
                break;
            default:
                break;
        }

        return super.onKeyDown(keyCode, event);

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
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }

}
