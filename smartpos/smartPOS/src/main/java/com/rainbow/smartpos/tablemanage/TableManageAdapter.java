package com.rainbow.smartpos.tablemanage;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rainbow.smartpos.MainScreenActivity;
import com.rainbow.smartpos.R;
import com.sanyipos.sdk.api.SanyiSDK;
import com.sanyipos.sdk.model.SeatEntity;
import com.sanyipos.sdk.model.rest.VirtualTable;

import java.util.ArrayList;
import java.util.List;

public class TableManageAdapter extends BaseAdapter {

    public static final int AVAILABLE = 1;
    public static final int SERVING = 2;
    public static final int PREPRINT = 4;
    public static final int MERGE = 8;
    public static final int SPLIT = 16;
    public static final int RESERVE = 32;
    public static final int OVERTIME = 64;
    public static final int ALL = 0xffffffff;

    private Context mContext;

    public List<SeatEntity> tableShown = SanyiSDK.rest.operationData.shopTables;
    final LayoutInflater inflater;
    private List<Integer> selectpos = new ArrayList<Integer>();
    private int currentPosition = -1;
    private int flag = -1;

    public String[] colors = {"#8e6102", "#007a13", "#a4220c", "#9221be", "#7f5fbe"};

    public TableManageAdapter(Context c, int flag) {
        mContext = c;
        this.flag = flag;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        setTableGroup();
    }

    @Override
    public int getCount() {
        return tableShown.size();
    }

    public SeatEntity getSelectTable() {
        if (-1 == currentPosition) {
            return null;
        }
        return tableShown.get(currentPosition);
    }

    public void setSelectPosition(int position) {
        currentPosition = position;
    }

    public void choose(int sPosition) {
        if (selectpos.contains(sPosition)) {
            for (int i = 0; i < selectpos.size(); i++) {
                int position = selectpos.get(i);
                if (position == sPosition) {
                    selectpos.remove(i);
                }
            }
        } else {
            selectpos.add(sPosition);
        }
    }

    public List<Long> getChooseTables() {
        List<Long> chooseSeats = new ArrayList<Long>();
        for (int pos : selectpos) {
            chooseSeats.add(tableShown.get(pos).seat);
        }
        return chooseSeats;
    }

    public void clearSelect() {
        currentPosition = -1;
        selectpos.removeAll(selectpos);
    }

    public int getSelectPosition() {
        return currentPosition;
    }

    public List<SeatEntity> getAllData() {
        return tableShown;
    }

    @Override
    public void notifyDataSetChanged() {
        refreshData();
    }

    public void refreshData() {
        setTableGroup();
        super.notifyDataSetChanged();
    }

    static class ViewHolder {
        public TextView style;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SeatEntity table = tableShown.get(position);
        // ImageView i;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.choose_table_item, parent, false);
        }
        TextView tableName = (TextView) convertView.findViewById(R.id.tableName);
        TextView tablePersonCount = (TextView) convertView.findViewById(R.id.tablePersonCount);
        tableName.setTextColor(Color.parseColor("#ffffff"));
        tablePersonCount.setTextColor(Color.parseColor("#ffffff"));
        ImageView imageViewLock = (ImageView) convertView.findViewById(R.id.imageViewLock);
        tableName.setText(table.tableName);
        tablePersonCount.setText(table.personCount + "人");
        if (table.lock) {
            imageViewLock.setVisibility(View.VISIBLE);
        } else {
            imageViewLock.setVisibility(View.GONE);
        }
        if (table.state == AVAILABLE) { // table is available
            tableName.setTextColor(Color.parseColor("#000000"));
            tablePersonCount.setTextColor(Color.parseColor("#000000"));
            convertView.setBackgroundResource(R.drawable.change_table_background_nor);
        } else {
            if (-1 == table.seat) {
                tablePersonCount.setVisibility(View.GONE);
            }
            convertView.setBackgroundResource(R.drawable.table_background_ser);
            if ((table.state & MERGE) == MERGE) {
                if (table.order.tag != null) {
                    convertView.setBackgroundResource(R.drawable.table_background_combine);
                    tableName.setText(table.tableName + "(" + table.order.tag + ")");
                }
            }
            if ((table.state & PREPRINT) == PREPRINT) {
                convertView.setBackgroundResource(R.drawable.table_background_pre);
            }
        }
        if (currentPosition == position) {
            convertView.setBackgroundResource(R.drawable.order_op_dialog_gird_item_bg_single);
            tableName.setTextColor(Color.parseColor("#000000"));
            tablePersonCount.setTextColor(Color.parseColor("#000000"));
        }
        for (int i = 0; i < selectpos.size(); i++) {
            if (position == selectpos.get(i)) {
                convertView.setBackgroundResource(R.drawable.order_op_dialog_gird_item_bg_single);
                tableName.setTextColor(Color.parseColor("#000000"));
                tablePersonCount.setTextColor(Color.parseColor("#000000"));
            }
        }

        return convertView;
    }

    public String getColor(String tag) {
        String color = colors[0];
        for (int i = 0; i < SanyiSDK.rest.virtualTables.size(); i++) {
            VirtualTable virtualTable = SanyiSDK.rest.virtualTables.get(i);
            if (tag.equals(virtualTable.tag)) {
                color = colors[i % (colors.length)];
            }
        }
        return color;
    }

    /**
     * @param
     */
    public void setTableGroup() {
        List<SeatEntity> targetTable = new ArrayList<SeatEntity>();
        switch (flag) {
            case MainScreenActivity.SPLIT_TABLE_FRAGMENT:
                for (int i = 0; i < SanyiSDK.rest.operationData.shopTables.size(); ++i) {
                    SeatEntity obj = SanyiSDK.rest.operationData.shopTables.get(i);
                    if (obj == null) {
                        continue;
                    }
                    if (((obj.state & SERVING) == SERVING) | ((obj.state & PREPRINT) == PREPRINT)) {
                        targetTable.add(obj);
                    }
                }
                break;
            case MainScreenActivity.CANCEL_TABLE_FRAGMENT:
                for (int i = 0; i < SanyiSDK.rest.operationData.shopTables.size(); ++i) {
                    SeatEntity obj = SanyiSDK.rest.operationData.shopTables.get(i);
                    if (obj == null) {
                        continue;
                    }
                    if (((obj.state & SERVING) == SERVING) | ((obj.state & PREPRINT) == PREPRINT)) {
                        if (obj.order.amount <= 0) {
                            targetTable.add(obj);
                        }

                    }
                }
                break;
            case MainScreenActivity.UNSPLIT_TABLE_FRAGMENT:
                for (int i = 0; i < SanyiSDK.rest.operationData.shopTables.size(); ++i) {
                    SeatEntity obj = SanyiSDK.rest.operationData.shopTables.get(i);
                    if (obj == null) {
                        continue;
                    }
                    if ((obj.state & AVAILABLE) == AVAILABLE) {
                        if (obj.sysSeat > 0) {
                            targetTable.add(obj);
                        }
                    }
                }
                break;
            case MainScreenActivity.CHANGE_TABLE_FRAGMENT:
                for (int i = 0; i < SanyiSDK.rest.operationData.shopTables.size(); ++i) {
                    SeatEntity obj = SanyiSDK.rest.operationData.shopTables.get(i);
                    if (obj == null) {
                        continue;
                    }
                    if (obj.state != AVAILABLE) {
                        targetTable.add(obj);
                    }
                }
                break;
            case AVAILABLE:
                for (int i = 0; i < SanyiSDK.rest.operationData.shopTables.size(); ++i) {
                    SeatEntity obj = SanyiSDK.rest.operationData.shopTables.get(i);
                    if (obj == null) {
                        continue;
                    }
                    if ((obj.state & AVAILABLE) == AVAILABLE) {
                        targetTable.add(obj);
                    }
                }
                break;

            default:
                break;
        }

        tableShown = targetTable;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return tableShown.get(position);
    }
}
