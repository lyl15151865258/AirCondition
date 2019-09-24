package com.zhongbenshuo.air.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
        return listViewHolder;
    }

    @Override
    public void onBindViewHolder(ListViewHolder holder, int position) {
        RealData realData = list.get(position);
        holder.cvRealData.setCompleteDegree(realData.getValue());
        holder.cvRealData.setTitle(realData.getChartName());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ListViewHolder extends RecyclerView.ViewHolder {

        private ClockView cvRealData;

        private ListViewHolder(View itemView) {
            super(itemView);
        }
    }

}
