package com.example.myapplication;

import android.os.Bundle;
import android.os.Handler;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myapplication.cyRecyclerView.CyRecyclerView;

public class MainActivity extends AppCompatActivity {

    TextView Empty;
    CyRecyclerView cyRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout frameLayout = new FrameLayout(this);
        ViewGroup.LayoutParams vl = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addContentView(frameLayout, vl);
        {
            cyRecyclerView = new CyRecyclerView(this);
            cyRecyclerView.setAdapter(new MyAdapter(this));
            cyRecyclerView.setHeaderView(new TextHeader(this), true);
            cyRecyclerView.setFooterView(new TextFooter(this), true);
            cyRecyclerView.setLoadingListener(new CyRecyclerView.LoadingListener() {
                @Override
                public void onRefresh() {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            cyRecyclerView.refreshComplete();
                        }
                    }, 3000);
                }

                @Override
                public void onLoadMore() {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            cyRecyclerView.loadingMoreComplete();
                            cyRecyclerView.setNoMore();
                        }
                    }, 3000);
                }
            });
            cyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            vl = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            frameLayout.addView(cyRecyclerView, vl);

            Empty = new TextView(this);
            Empty.setText("空页面");
            Empty.setBackgroundColor(0xffffffff);
            vl = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            frameLayout.addView(Empty, vl);
            cyRecyclerView.setEmpty(Empty);
        }
    }
}
