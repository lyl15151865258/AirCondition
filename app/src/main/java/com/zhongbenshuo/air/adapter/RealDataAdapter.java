package com.zhongbenshuo.air.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zhongbenshuo.air.R;
import com.zhongbenshuo.air.bean.RealData;
import com.zhongbenshuo.air.widget.ClockView;

import java.util.List;

/**
 * 实时数据图表
 * Created at 2019/9/24 19:36
 *
 * @author LiYuliang
 * @version 1.0
 */
public class RealDataAdapter extends RecyclerView.Adapter<RealDataAdapter.ListViewHolder> {

    private Context mContext;
    private List<RealData> list;

    public RealDataAdapter(Context context, List<RealData> lv) {
        mContext = context;
        list = lv;
    }

    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_real_data, viewGroup, false);
        ListViewHolder listViewHolder = new ListViewHolder(view);
        listViewHolder.cvRealData = view.findViewById(R.id.cvRealData);
        listViewHolder.tvStatus = view.findViewById(R.id.tvStatus);
        return listViewHolder;
    }

    @Override
    public void onBindViewHolder(ListViewHolder holder, int position) {
        RealData realData = list.get(position);
        holder.cvRealData.setCompleteDegree(realData.getValue(), realData.getUnit());
        switch (realData.getDataType()) {
            case TYPE_TEMP:
                // 温度
                if (realData.getValue() >= 15 && realData.getValue() <= 28) {
                    holder.tvStatus.setText("舒适");
                    holder.tvStatus.setBackgroundResource(R.drawable.background_normal);
                } else if (realData.getValue() < 15) {
                    holder.tvStatus.setText("寒冷");
                    holder.tvStatus.setBackgroundResource(R.drawable.background_abnormal);
                } else {
                    holder.tvStatus.setText("炎热");
                    holder.tvStatus.setBackgroundResource(R.drawable.background_abnormal);
                }
                break;
            case TYPE_HUMIDITY:
                // 湿度
                if (realData.getValue() >= 40 && realData.getValue() <= 60) {
                    holder.tvStatus.setText("舒适");
                    holder.tvStatus.setBackgroundResource(R.drawable.background_normal);
                } else if (realData.getValue() < 40) {
                    holder.tvStatus.setText("干燥");
                    holder.tvStatus.setBackgroundResource(R.drawable.background_abnormal);
                } else {
                    holder.tvStatus.setText("潮湿");
                    holder.tvStatus.setBackgroundResource(R.drawable.background_abnormal);
                }
                break;
            case TYPE_PM25:
                // PM2.5
                if (realData.getValue() <= 35) {
                    holder.tvStatus.setText("低");
                    holder.tvStatus.setBackgroundResource(R.drawable.background_normal);
                } else if (realData.getValue() > 35 && realData.getValue() <= 75) {
                    holder.tvStatus.setText("正常");
                    holder.tvStatus.setBackgroundResource(R.drawable.background_normal);
                } else if (realData.getValue() > 75 && realData.getValue() <= 115) {
                    holder.tvStatus.setText("轻度污染");
                    holder.tvStatus.setBackgroundResource(R.drawable.background_abnormal);
                } else if (realData.getValue() > 115 && realData.getValue() <= 150) {
                    holder.tvStatus.setText("中度污染");
                    holder.tvStatus.setBackgroundResource(R.drawable.background_abnormal);
                } else if (realData.getValue() > 150 && realData.getValue() <= 250) {
                    holder.tvStatus.setText("重度污染");
                    holder.tvStatus.setBackgroundResource(R.drawable.background_abnormal);
                } else {
                    holder.tvStatus.setText("严重污染");
                    holder.tvStatus.setBackgroundResource(R.drawable.background_abnormal);
                }
                break;
            case TYPE_PM10:
                // PM10
                if (realData.getValue() <= 50) {
                    holder.tvStatus.setText("低");
                    holder.tvStatus.setBackgroundResource(R.drawable.background_normal);
                } else if (realData.getValue() > 50 && realData.getValue() <= 150) {
                    holder.tvStatus.setText("正常");
                    holder.tvStatus.setBackgroundResource(R.drawable.background_normal);
                } else {
                    holder.tvStatus.setText("高");
                    holder.tvStatus.setBackgroundResource(R.drawable.background_abnormal);
                }
                break;
            case TYPE_HCHO:
                // HCHO
                if (realData.getValue() <= 0.08) {
                    holder.tvStatus.setText("正常");
                    holder.tvStatus.setBackgroundResource(R.drawable.background_normal);
                } else {
                    holder.tvStatus.setText("异常");
                    holder.tvStatus.setBackgroundResource(R.drawable.background_abnormal);
                }
                break;
            case TYPE_CO2:
                // CO2
                if (realData.getValue() <= 450) {
                    holder.tvStatus.setText("低");
                    holder.tvStatus.setBackgroundResource(R.drawable.background_normal);
                } else if (realData.getValue() > 450 && realData.getValue() <= 1000) {
                    holder.tvStatus.setText("正常");
                    holder.tvStatus.setBackgroundResource(R.drawable.background_normal);
                } else if (realData.getValue() > 1000 && realData.getValue() <= 2000) {
                    holder.tvStatus.setText("偏高");
                    holder.tvStatus.setBackgroundResource(R.drawable.background_abnormal);
                } else {
                    holder.tvStatus.setText("异常");
                    holder.tvStatus.setBackgroundResource(R.drawable.background_abnormal);
                }
                break;
            default:
                break;
        }
        holder.cvRealData.setTitle(realData.getChartName());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ListViewHolder extends RecyclerView.ViewHolder {

        private ClockView cvRealData;
        private TextView tvStatus;

        private ListViewHolder(View itemView) {
            super(itemView);
        }
    }

}
