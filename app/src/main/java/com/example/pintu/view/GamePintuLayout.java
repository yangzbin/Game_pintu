package com.example.pintu.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.pintu.R;
import com.example.pintu.utils.ImagePiece;
import com.example.pintu.utils.ImageSplitterUtil;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.LogRecord;

/**
 * Created by Administrator on 2017/2/15.
 */

public class GamePintuLayout extends RelativeLayout implements View.OnClickListener {

    private int mColumn = 3;//默认3*3；
    private int mPadding;//容器内边距
    private int mMagin = 3;//每张小图之间的间隙（横，纵）dp
    private ImageView[] mGamePintuItems;
    private int mItemWidth;
    private Bitmap mBitmap;//游戏图片
    private List<ImagePiece> mItemBitmaps;
    private boolean once;//操作一次
    private int mWidth;//游戏面板宽度

    private boolean isGameSuccess;
    private boolean isGameOver;
    private int level = 1;
    //回调借口
    public interface GamePintuListener{
        void nextLevel(int nextLevel);
        void timeChanged(int currentTime);
        void gameOver();
    }
    public GamePintuListener mListener;

    /**
     * 设置接口回调
     * @param mListener
     */
    public void setOnGamePintuListener(GamePintuListener mListener) {
        this.mListener = mListener;
    }

    //用 handler更新 ui
    private static final int TIME_CHANGED = 0x110;
    private static final int NEXT_LEVEL = 0x111;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case TIME_CHANGED://时间改变
                    if(isGameSuccess||isGameOver||isPause){//游戏成功或者结束或者暂停 停止计时
                        return;
                    }
                    if(mListener!=null){
                        mListener.timeChanged(mTime);
                    }
                    if (mTime == 0){
                        isGameOver = true;//游戏结束
                        mListener.gameOver();
                        return;
                    }
                    mTime--;//游戏过程中开始计时
                    mHandler.sendEmptyMessageDelayed(TIME_CHANGED,1000);//延迟1s向主界面发送时间信息
                    break;
                case NEXT_LEVEL://下一关
                    level++;
                    if(mListener!=null){
                        mListener.nextLevel(level);
                    }else {
                        nextLevel();
                    }
                    break;
            }
        }
    };

    /**
     * 游戏是否设定时间限制
     */
    private boolean isTimeEnabled = false;
    private int mTime;

    public void setTimeEnabled(boolean timeEnabled) {
        isTimeEnabled = timeEnabled;
    }

    public GamePintuLayout(Context context) {
        this(context, null);
    }

    public GamePintuLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GamePintuLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //取测量高度和宽度的最小值，游戏面板为正方形
        mWidth = Math.min(getMeasuredWidth(), getMeasuredHeight());
        if (!once) {
            //进行切图
            initBitmap();
            //设置Imageview(item)的宽高等属性
            initItem();
            //判断是否开启时间
            checkTimeEnable();
            once = true;
        }
        setMeasuredDimension(mWidth, mWidth);
    }

    private void initView() {
        //将xp转换为dp
        mMagin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, getResources().getDisplayMetrics());
        mPadding = min(getPaddingLeft(), getPaddingRight(), getPaddingTop(), getPaddingBottom());
    }

    /**
     * 判断是否开启时间
     */
    private void checkTimeEnable() {
        if(isTimeEnabled){
            //如果开启时间 根据关卡计算时间长短
            countTimeBaseLevel();
            mHandler.sendEmptyMessage(TIME_CHANGED);
        }
    }

    private void countTimeBaseLevel() {
        mTime = (int)Math.pow(2,level)*60; //2*level*60s 难度指数增长;
    }

    /**
     * 获取多个参数的最小值
     *
     * @return
     */
    private int min(int... params) {//可变参数
        int min = params[0];
        for (int param : params) {
            if (param < min) {
                min = param;
            }
        }
        return min;
    }

    /**
     * 进行切图
     */
    private void initBitmap() {
        if (mBitmap == null) {
            mBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.zjl);
        }
        mItemBitmaps = ImageSplitterUtil.splitImage(mBitmap, mColumn);
        //将图片碎片顺序打乱 使用sort实现乱序
        Collections.sort(mItemBitmaps, new Comparator<ImagePiece>() {
            @Override
            public int compare(ImagePiece a, ImagePiece b) {
                return Math.random() > 0.5 ? 1 : -1;//让a和b比较结果随机
            }
        });
    }

    /**
     * 设置Imageview(item)的宽高等属性
     */
    private void initItem() {
        mItemWidth = (mWidth - mPadding * 2 - mWidth * (mColumn - 1)) / mColumn;
        mGamePintuItems = new ImageView[mColumn * mColumn];
        //生成item 设置规则
        for (int i = 0; i < mGamePintuItems.length; i++) {
            ImageView item = new ImageView(getContext());
            item.setOnClickListener(this);

            item.setImageBitmap(mItemBitmaps.get(i).getBitmap());
            mGamePintuItems[i] = item;
            item.setId(i + 1);
            //tag存储碎片图片的index
            item.setTag(i + "_" + mItemBitmaps.get(i).getIndex());

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(mItemWidth, mItemWidth);

            //设置item横向边距 通过rightMargin,最后一列不设置
            if ((i + 1) % mColumn != 0) {//不是最后一列
                lp.rightMargin = mMagin;
            }
            //不是第一列
            if (i % mColumn != 0) {
                lp.addRule(RelativeLayout.RIGHT_OF, mGamePintuItems[i - 1].getId());
            }

            //设置item纵向边距 通过topMargin,第一行不设置
            if ((i + 1) > mColumn) {
                lp.topMargin = mMagin;
                lp.addRule(RelativeLayout.BELOW, mGamePintuItems[i - mColumn].getId());
            }
            addView(item, lp);
        }
    }

    private ImageView mFirst;//第一张点中的图片
    private ImageView mSecond;//第二张点中的图片

    @Override
    public void onClick(View v) {
        if(isAniming) //正在动画 不允许点击
            return;
        if (mFirst == v) {//如果两次点中同一个 取消
            mFirst.setColorFilter(null);
            mFirst = null;
            return;
        }
        if (mFirst == null) {//第一次点击
            mFirst = (ImageView) v;
            //设置选中颜色
            mFirst.setColorFilter(Color.parseColor("#55FF0000"));
        } else {
            mSecond = (ImageView) v;
            exchageView();//两个图片进行交换
        }
    }

    /**
     * 动画层
     */
    private RelativeLayout mAnimLayout;
    private boolean isAniming;
    /**
     * 交换item
     */
    private void exchageView() {
        mFirst.setColorFilter(null);//取消选中颜色

        setUpAnimLayout();
        //得到Bitmap，通过Tag得到
        final String firstTag = mFirst.getTag().toString();
        final String secondTag = mSecond.getTag().toString();

        ImageView first = new ImageView(getContext());
//        final Bitmap firstBitmap = mItemBitmaps.get(getImagIdByTag(mFirst.getTag().toString())).getBitmap();
//        first.setImageBitmap(firstBitmap);//给图层的第一个item设置bitmap
//        LayoutParams lp = new LayoutParams(mItemWidth,mItemWidth);
//        lp.leftMargin = mFirst.getLeft()-mPadding;
//        lp.topMargin = mFirst.getTop()-mPadding;
//        first.setLayoutParams(lp);
//        mAnimLayout.addView(first);

        ImageView second = new ImageView(getContext());
//        final Bitmap secondBitmap = mItemBitmaps.get(getImagIdByTag(mSecond.getTag().toString())).getBitmap();
//        second.setImageBitmap(secondBitmap);//给图层的第一个item设置bitmap
//        LayoutParams lp2 = new LayoutParams(mItemWidth,mItemWidth);
//        lp2.leftMargin = mSecond.getLeft()-mPadding;
//        lp2.topMargin = mSecond.getTop()-mPadding;
//        second.setLayoutParams(lp2);
//        mAnimLayout.addView(second);

        final Bitmap firstBitmap = setAnimLayoutByTag(first,firstTag, mFirst);

        final Bitmap secondBitmap = setAnimLayoutByTag(second,secondTag, mSecond);
        //设置动画
        TranslateAnimation anim = new TranslateAnimation(0, mSecond.getLeft() - mFirst.getLeft(),
                0, mSecond.getTop() - mFirst.getTop());
        anim.setDuration(300);//设置动画时间
        anim.setFillAfter(true);
        first.startAnimation(anim);

        TranslateAnimation animSecond = new TranslateAnimation(0, -mSecond.getLeft() + mFirst.getLeft(),
                0, -mSecond.getTop() + mFirst.getTop());
        animSecond.setDuration(300);//设置动画时间
        animSecond.setFillAfter(true);
        second.startAnimation(animSecond);

        //监听动画
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isAniming = true;
                mFirst.setVisibility(INVISIBLE);
                mSecond.setVisibility(INVISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {//动画结束将面板中的图片进行交换 显示
                mFirst.setImageBitmap(secondBitmap);
                mSecond.setImageBitmap(firstBitmap);

//                String firstTag = mFirst.getTag().toString();
//                String secondTag = mSecond.getTag().toString();
                mFirst.setTag(secondTag);
                mSecond.setTag(firstTag);

                mFirst.setVisibility(VISIBLE);
                mSecond.setVisibility(VISIBLE);

                mFirst = mSecond = null;
                mAnimLayout.removeAllViews();//将动画层移除
                //判断游戏是否成功
                checkSuccess();
                isAniming = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


    }

    /**
     * 判断拼图是否完成
     */
    private void checkSuccess() {
        boolean isSuccess = true;
        for(int i=0;i<mGamePintuItems.length;i++){
            ImageView img = mGamePintuItems[i];
            if(getImageIndexByTag(img.getTag().toString())!= i){//交换后的顺序和乱序前的顺序不一致
                isSuccess = false;
            }
        }
        if(isSuccess){//成功
            isGameSuccess = true;
            mHandler.removeMessages(TIME_CHANGED);
            Toast.makeText(getContext(),"Success,level up!!!",Toast.LENGTH_LONG).show();
            mHandler.sendEmptyMessage(NEXT_LEVEL);
            //此处用mHandler.obtainMessage() 无效
        }

    }

    /**
     * 设置动画层
     *
     * @param itemTag
     * @param item
     * @return
     */
    private Bitmap setAnimLayoutByTag(ImageView img,String itemTag, ImageView item) {
        ImageView imageView = img;
        Bitmap bitmap = mItemBitmaps.get(getImagIdByTag(itemTag)).getBitmap();
        imageView.setImageBitmap(bitmap);//给图层的item设置bitmap
        LayoutParams lp = new LayoutParams(mItemWidth, mItemWidth);
        lp.leftMargin = item.getLeft() - mPadding;
        lp.topMargin = item.getTop() - mPadding;
        imageView.setLayoutParams(lp);
        mAnimLayout.addView(imageView);
        return bitmap;
    }

    /**
     * 根据tag获取图片id
     *
     * @param tag
     * @return
     */
    public int getImagIdByTag(String tag) {
        String[] params = tag.split("_");
        return Integer.parseInt(params[0]);
    }

    /**
     * 根据tag获取图片index
     *
     * @param tag
     * @return
     */
    public int getImageIndexByTag(String tag) {
        String[] params = tag.split("_");
        return Integer.parseInt(params[1]);
    }

    /**
     * 构造动画层
     */
    private void setUpAnimLayout() {
        if (mAnimLayout == null) {
            mAnimLayout = new RelativeLayout(getContext());
            addView(mAnimLayout);//将动画层加入到游戏面板中
        }
    }

    /**
     * 下一关
     */
    public void nextLevel() {
        this.removeAllViews();
        mAnimLayout = null;
        mColumn++;//3*3变成4*4
        isGameSuccess = false;
        //下一关重新计算时间
        checkTimeEnable();
        //重新生成游戏面板
        initBitmap();
        initItem();

    }
    /**
     * 重来
     */
    public void restart(){
        isGameOver = false;
        mColumn--;//让面板回到当前游戏面板
        nextLevel();
    }

    private boolean isPause;//是否暂停
    /**
     * 暂停
     */
    public void pause(){
        isPause = true;//暂停
        mHandler.removeMessages(TIME_CHANGED);//停止计时
    }
    /**
     * 恢复
     */
    public void resume(){
        if(isPause){//如果暂停 则取消暂停
            isPause = false;
            mHandler.sendEmptyMessage(TIME_CHANGED);//让时间接着变化
        }
    }
}
