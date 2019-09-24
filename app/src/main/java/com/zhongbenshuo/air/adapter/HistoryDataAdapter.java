package com.zhongbenshuo.air.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zhongbenshuo.air.R;
import com.zhongbenshuo.air.bean.Environment;
import com.zhongbenshuo.air.utils.TimeUtils;

import java.util.List;

/**
 * 环境监测点列表适配器
 * Created at 2019/9/24 16:08
 *
 * @author LiYuliang
 * @version 1.0
 */

public class HistoryDataAdapter extends RecyclerView.Adapter<HistoryDataAdapter.ListViewHolder> {

    private Context mContext;
    private List<Environment> list;

    public HistoryDataAdapter(Context context, List<Environment> lv) {
        mContext = context;
        list = lv;
    }

    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_environment, viewGroup, false);
        ListViewHolder listViewHolder = new ListViewHolder(view);
        listViewHolder.tvTime = view.findViewById(R.id.tvTime);
        listViewHolder.tvTemperature = view.findViewById(R.id.tvTemperature);
        listViewHolder.tvHumidity = view.findViewById(R.id.tvHumidity);
        listViewHolder.tvPM25 = view.findViewById(R.id.tvPM25);
        listViewHolder.tvPM10 = view.findViewById(R.id.tvPM10);
        listViewHolder.tvFormaldehyde = view.findViewById(R.id.tvFormaldehyde);
        listViewHolder.tvCarbonDioxide = view.findViewById(R.id.tvCarbonDioxide);
        return listViewHolder;
    }

    @Override
    public void onBindViewHolder(ListViewHolder holder, int position) {
        Environment environment = list.get(position);
        holder.tvTime.setText(TimeUtils.normalFormat(environment.getCreateTime(), "yyyy-MM-dd HH:mm:ss", "HH:mm:ss"));
        holder.tvTemperature.setText(environment.getTemperature() + "℃");
        holder.tvHumidity.setText(environment.getHumidity() + "%");
        holder.tvPM25.setText(environment.getPm25() + "μg/m³");
        holder.tvPM10.setText(environment.getPm10() + "μg/m³");
        holder.tvFormaldehyde.setText(environment.getFormaldehyde() + "mg/m³");
        holder.tvCarbonDioxide.setText(environment.getCarbonDioxide() + "ppm");
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ListViewHolder extends RecyclerView.ViewHolder {

        private TextView tvTime, tvTemperature, tvHumidity, tvPM25, tvPM10, tvFormaldehyde, tvCarbonDioxide;

        private ListViewHolder(View itemView) {
            super(itemView);
        }
    }

}
