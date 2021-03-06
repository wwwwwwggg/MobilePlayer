package com.wjc.last_mobilephone.fragment;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cjj.MaterialRefreshLayout;
import com.cjj.MaterialRefreshListener;
import com.google.gson.Gson;
import com.wjc.last_mobilephone.R;
import com.wjc.last_mobilephone.adapter.RecyclerViewFragmentAdapter;
import com.wjc.last_mobilephone.domain.ListViewBean;
import com.wjc.last_mobilephone.utils.CacheUtils;
import com.wjc.last_mobilephone.utils.Constants;
import com.wjc.last_mobilephone.utils.LogUtil;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.List;

import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayerManager;

/**
 * 作用：网络音频
 */
public class RecyclerViewFragment extends BaseFragment {


    private RecyclerView recyclerview;
    private ProgressBar progressbar;
    private TextView tv_nomedia;
    private MaterialRefreshLayout refresh;

    private List<ListViewBean.ListBean> datas;

    private RecyclerViewFragmentAdapter myAdapter;

    @Override
    public View initView() {
        Log.e("TAG", "网络视频视图（页面）初始化了...");
        View view = View.inflate(mContext, R.layout.fragment_recyclerview_audio, null);
        recyclerview = (RecyclerView) view.findViewById(R.id.recyclerview);
        progressbar = (ProgressBar) view.findViewById(R.id.progressbar);
        tv_nomedia = (TextView) view.findViewById(R.id.tv_nomedia);
        refresh = (MaterialRefreshLayout) view.findViewById(R.id.refresh);

        refresh.setMaterialRefreshListener(new MaterialRefreshListener() {
            /**
             * 下拉刷新
             * @param materialRefreshLayout
             */
            @Override
            public void onRefresh(final MaterialRefreshLayout materialRefreshLayout) {

                getDataFromNet();

            }


            /**
             * 加载更多
             * @param materialRefreshLayout
             */
            @Override
            public void onRefreshLoadMore(final MaterialRefreshLayout materialRefreshLayout) {
                getMoreDataFromNet();
            }
        });


        return view;
    }

    private void getMoreDataFromNet() {
        RequestParams reques = new RequestParams(Constants.NET_AUDIO_URL);
        x.http().get(reques, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {

                LogUtil.e("onSuccess==" + result);
                processMoreData(result);
                refresh.finishRefreshLoadMore();

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                LogUtil.e("onError==" + ex.getMessage());
            }

            @Override
            public void onCancelled(CancelledException cex) {
                LogUtil.e("onCancelled==" + cex.getMessage());
            }

            @Override
            public void onFinished() {
                LogUtil.e("onFinished==");
            }
        });

    }

    @Override
    public void initData() {
        super.initData();
        Log.e("TAG", "网络视频数据初始化了...");

        String saveJson = CacheUtils.getString(mContext, Constants.NET_AUDIO_URL);
        if(!TextUtils.isEmpty(saveJson)){
            processData(saveJson);
        }

        getDataFromNet();
    }

    private void getDataFromNet() {
        RequestParams reques = new RequestParams(Constants.NET_AUDIO_URL);
        x.http().get(reques, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {

                CacheUtils.putString(mContext,Constants.NET_AUDIO_URL,result);
                LogUtil.e("onSuccess==" + result);
                processData(result);
                refresh.finishRefresh();
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                LogUtil.e("onError==" + ex.getMessage());
            }

            @Override
            public void onCancelled(CancelledException cex) {
                LogUtil.e("onCancelled==" + cex.getMessage());
            }

            @Override
            public void onFinished() {
                LogUtil.e("onFinished==");
            }
        });

    }

    /**
     * 解析和绑定数据
     *
     * @param json
     */
    private void processData(String json) {

        datas = parsedJson(json);

        if(datas != null && datas.size() >0){
            //有视频
            tv_nomedia.setVisibility(View.GONE);
            //设置适配器
            myAdapter = new RecyclerViewFragmentAdapter(mContext,datas);
            recyclerview.setAdapter(myAdapter);
            recyclerview.setLayoutManager(new LinearLayoutManager(mContext,LinearLayoutManager.VERTICAL,false));
            recyclerview.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
                @Override
                public void onChildViewAttachedToWindow(View view) {

                }

                @Override
                public void onChildViewDetachedFromWindow(View view) {
                    if (JCVideoPlayerManager.listener() != null) {
                        JCVideoPlayer videoPlayer = (JCVideoPlayer) JCVideoPlayerManager.listener();
                        if (((ViewGroup) view).indexOfChild(videoPlayer) != -1 && videoPlayer.currentState == JCVideoPlayer.CURRENT_STATE_PLAYING) {
                            JCVideoPlayer.releaseAllVideos();
                        }
                    }
                }
            });


        }else{
            //没有视频
            tv_nomedia.setVisibility(View.VISIBLE);
        }

        progressbar.setVisibility(View.GONE);

    }

    /**
     * 解析和绑定数据
     *
     * @param json
     */
    private void processMoreData(String json) {

        datas.addAll(parsedJson(json));
        myAdapter.setData(datas);
        myAdapter.notifyDataSetChanged();

    }


    /**
     * 手动解析json数据
     * @param json
     * @return
     */
    private List<ListViewBean.ListBean>  parsedJson(String json) {

        ListViewBean listViewBean = new Gson().fromJson(json,ListViewBean.class);
        return listViewBean.getList();
    }
}
