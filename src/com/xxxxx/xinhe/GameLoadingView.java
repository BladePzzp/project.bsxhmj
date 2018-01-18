package com.xxxxx.xinhe;

import com.xxxxx.mj.tools.ConstVar;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class GameLoadingView extends FrameLayout {
	
	//LoadingView
	private View loadingView;
	//游戏Logo
    private ImageView imgLogoView;
    //游戏加载图片
    private ImageView imgPreLoadView;
    //游戏LogoFoot
    private ImageView imgLogoFootView;

    /**
     * 游戏下载进度条 上线前请替换渠道自定制进度条
     * 
     * @param context
     */
    public GameLoadingView(Context context) {
        super(context);
        this.createView(context);
    }
    
    private void createView(Context context) {
		Resources res = this.getResources();
	    int top,left;
	    loadingView = View.inflate(context, R.layout.loading_view, null);
	    imgLogoView = (ImageView)loadingView.findViewById(R.id.ld_logo);
	    imgPreLoadView = (ImageView)loadingView.findViewById(R.id.ld_preload);
	    imgLogoFootView = (ImageView)loadingView.findViewById(R.id.ld_logofoot);
	    
	    //设置imgLogoView的位置
    	RelativeLayout.LayoutParams lp_imgLogoView = (RelativeLayout.LayoutParams)imgLogoView.getLayoutParams();
		top = (int)(ConstVar.yZoom * res.getDimension(R.dimen.logo_top));
    	left = (int)(ConstVar.xZoom * res.getDimension(R.dimen.logo_left));
    	lp_imgLogoView.width = (int)(ConstVar.xZoom * res.getDimension(R.dimen.logo_width));
    	lp_imgLogoView.height = (int)(ConstVar.yZoom * res.getDimension(R.dimen.logo_height));
    	lp_imgLogoView.setMargins(left, top, 0, 0);
    	lp_imgLogoView = null;
  		//设置imgPreLoadView的位置
    	RelativeLayout.LayoutParams lp_imgPreLoadView = (RelativeLayout.LayoutParams)imgPreLoadView.getLayoutParams();
		top = (int)(ConstVar.yZoom * res.getDimension(R.dimen.preload_top));
    	left = (int)(ConstVar.xZoom * res.getDimension(R.dimen.preload_left));
    	lp_imgPreLoadView.width = (int)(ConstVar.xZoom * res.getDimension(R.dimen.preload_width));
    	lp_imgPreLoadView.height = (int)(ConstVar.yZoom * res.getDimension(R.dimen.preload_height));
    	lp_imgPreLoadView.setMargins(left, top, 0, 0);
    	lp_imgPreLoadView = null;
    	
    	//设置imgLogoFootView的位置
    	RelativeLayout.LayoutParams lp_imgLogoFootView = (RelativeLayout.LayoutParams)imgLogoFootView.getLayoutParams();
    	lp_imgLogoFootView.width = (int)(ConstVar.xZoom * res.getDimension(R.dimen.logofoot_width));
    	lp_imgLogoFootView.height = (int)(ConstVar.yZoom * res.getDimension(R.dimen.logofoot_height));
    	lp_imgLogoFootView = null;
    	
    	
    	
    	//设置透明渐变度动画
    	final AlphaAnimation alphaAni = new AlphaAnimation(1, 0);
    	alphaAni.setDuration(1250);
    	alphaAni.setRepeatMode(AlphaAnimation.REVERSE);
    	alphaAni.setRepeatCount(AlphaAnimation.INFINITE);
//    	alphaAni.setStartTime(AlphaAnimation.START_ON_FIRST_FRAME);
    	imgPreLoadView.setAnimation(alphaAni);
    	//将relativeLayout添加到View中
        this.addView(loadingView);
    }

  //必须保留一下方法，来接受游戏包加载进度的信息。
    //必须保留
    public void onProgress(float progress) {
    	//接受到进度信息，设置信息显示。
//        bar.setProgress((int) progress);
    }

    //必须保留。
    public void onGameZipUpdateProgress(float percent) {
    	//接受到进度信息，设置信息显示。
//        bar.setProgress((int) percent);
    }

    //必须保留。
    public void onGameZipUpdateError() {
    	//接受到的错误信息
    }

    //必须保留。
    public void onGameZipUpdateSuccess() {
    	//接受到成功信息
    }
    //////////////////////////////////////////
}
