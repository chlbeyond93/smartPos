package com.rainbow.smartpos.tablemanage;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.rainbow.smartpos.MainScreenActivity;
import com.rainbow.smartpos.R;
import com.sanyipos.sdk.api.SanyiScalaRequests;
import com.sanyipos.sdk.api.inters.Request;

import java.util.List;

public class MergeFragment extends Fragment implements OnClickListener {
    private View view;

    public GridView mGridView;
    private TableManageAdapter mAdapter;
    public Bundle bundle;
    public MainScreenActivity activity;

    public Bundle getBundle() {
        return bundle;
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        view = inflater.inflate(R.layout.fragment_split_table, container, false);
        activity = (MainScreenActivity) getActivity();
        initView();
        return view;
    }

    private void initView() {
        // TODO Auto-generated method stub
        mGridView = (GridView) view.findViewById(R.id.gridViewTables);
        mAdapter = new TableManageAdapter(getActivity(), MainScreenActivity.CHANGE_TABLE_FRAGMENT);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO Auto-generated method stub
                if (mAdapter.tableShown.get(position).lock) {
                    Toast.makeText(activity,"操作失败，桌子被锁",Toast.LENGTH_LONG).show();

                    return;
                }
                mAdapter.choose(position);
                mAdapter.notifyDataSetChanged();

            }
        });
        TextView title = (TextView) view.findViewById(R.id.title);
        title.setText(getString(R.string.merge_table));
        view.findViewById(R.id.back).setOnClickListener(this);
        view.findViewById(R.id.sure_btn).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.back:
                activity.displayView(MainScreenActivity.TABLE_MANAGE_FRAGMENT, bundle);
                break;
            case R.id.sure_btn:
                final List<Long> seats = mAdapter.getChooseTables();
                if (seats.size() == 0) {
                    activity.displayView(MainScreenActivity.TABLE_MANAGE_FRAGMENT, bundle);
                    return;
                } else if (seats.size() < 2) {

                    Toast.makeText(activity,"单个桌子无法并台",Toast.LENGTH_LONG).show();
                    return;
                }

                SanyiScalaRequests.batchTableRequest(seats, new Request.ICallBack() {


                    @Override
                    public void onFail(String error) {
                        // TODO Auto-generated method stub
                        Toast.makeText(activity,error,Toast.LENGTH_LONG).show();

                    }

                    @Override
                    public void onSuccess(String status) {
                        // TODO Auto-generated method stub
                        activity.displayView(MainScreenActivity.TABLE_FRAGMENT, null);
                    }
                });
                break;
            default:
                break;
        }

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        // TODO Auto-generated method stub
        super.onHiddenChanged(hidden);
        if (!hidden) {
            if (mAdapter != null) {
                mAdapter.refreshData();
                mAdapter.clearSelect();
            }
        }
    }

    public void refresh() {
        if (mAdapter != null) {
            mAdapter.refreshData();
        }
    }
}
