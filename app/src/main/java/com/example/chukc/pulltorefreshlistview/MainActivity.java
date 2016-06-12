package com.example.chukc.pulltorefreshlistview;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.example.chukc.pulltorefreshlistview.listview.PullableListView;
import com.example.chukc.pulltorefreshlistview.refresh.PullToRefreshLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private PullableListView listView;
    private PullToRefreshLayout ptrl;
    private MainAdapter adapter;
    private List<String> listText= new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        /*上拉加载由PullableListView1控制 下拉刷新由PullToRefreshLayout1控制*/
        ptrl = ((PullToRefreshLayout) findViewById(R.id.refresh_view));
        ptrl.setOnRefreshListener(new PullToRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(PullToRefreshLayout pullToRefreshLayout) {
                load(pullToRefreshLayout);
            }
        });
        listView = (PullableListView) findViewById(R.id.content_view);
        listView.setOnLoadListener(new PullableListView.OnLoadListener() {
            @Override
            public void onLoad(PullableListView pullableListView) {
                load(null);
            }
        });

        adapter = new MainAdapter(this,listText);
        listView.setAdapter(adapter);
    }

    private void load(final PullToRefreshLayout pullToRefreshLayout){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (pullToRefreshLayout != null) {
                    pullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
                }
                for (int i = 0; i < 10; i++) {
                    if(listText.size()<30)
                        listText.add("哈哈");
                }
                if(listText.size()<10){
                    listView.setFinishedFooter();
                    adapter.notifyDataSetChanged();
                }else{
                    if(listText.size()>=30){
                        listView.setFinishedFooter();
                        Toast.makeText(MainActivity.this,"没有更多数据",Toast.LENGTH_LONG).show();
                    }else{
                        adapter.notifyDataSetChanged();
                        listView.finishLoading();
                    }
                }
            }
        }, 5000);
    }
}
