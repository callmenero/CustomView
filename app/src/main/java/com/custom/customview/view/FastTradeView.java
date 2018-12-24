package com.custom.customview.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;

import com.custom.customview.R;
import com.custom.customview.util.DensityUtils;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 点价下单交易控件，反编译自国富易星App
 */
public class FastTradeView extends View {
    private static final boolean DBG = true;
    private static final String TAG = "EsDrawTableView";
    public static final int TRADE_CLICK_COLUMN_TYPE_ASK_QTY = 3;
    public static final int TRADE_CLICK_COLUMN_TYPE_BID_QTY = 1;
    public static final int TRADE_CLICK_COLUMN_TYPE_CANCEL_BUY = 0;
    public static final int TRADE_CLICK_COLUMN_TYPE_CANCEL_SELL = 4;
    public static final int TRADE_CLICK_COLUMN_TYPE_LAST_PRICE = 2;
    public static final int TRADE_CLICK_REFRESH_TYPE_CHANGE_CONTRACT = 3;
    public static final int TRADE_CLICK_REFRESH_TYPE_CHANGE_TYPE = 2;
    public static final int TRADE_CLICK_REFRESH_TYPE_QUOTE = 1;
    public static final int TRADE_CLICK_REFRESH_TYPE_UI = 0;
    //每次滚动一个item的高度还是滚动偏移的距离
    private boolean isScrollDistance = false;
    private SimpleOnGestureListener gestureListener = new SimpleOnGestureListener() {
        public boolean onDown(MotionEvent e) {
            if (!mIsDoubleTap) {
                getClickRowCol(e.getX(), e.getY());
                refreshView(0);
                Log.d(FastTradeView.TAG, "onDown: ");
            }
            mIsDoubleTap = false;
            return true;
        }

        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            //distanceY取值手指向上滑动为正值，向下滑动为负值,累加每次滚动的总偏移量
            mOffsetY = mOffsetY - distanceY;
            if (!isScrollDistance) {
                if (Math.abs(mOffsetY) >= (mItemHeight / 2)) {
                    if (mOffsetY < 0) {
                        mOffsetY = -mItemHeight;
                    } else {
                        mOffsetY = mItemHeight;
                    }
                } else {
                    mIsScrollTap = true;
                    return true;
                }
            }
            Log.d(FastTradeView.TAG, "onScroll: " + "distanceY=" + distanceY + " mOffsetY=" + mOffsetY + " mItemHeight/2=" + mItemHeight);
            correctOffsetY();
            refreshView(0);
            mIsScrollTap = true;
            return true;
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.d(FastTradeView.TAG, "onFling: ");
            cancelClick();
            return true;
        }

        public boolean onSingleTapUp(MotionEvent e) {
            Log.d(FastTradeView.TAG, "onSingleTapUp: ");
            cancelClick();
            return super.onSingleTapUp(e);
        }

        public boolean onDoubleTap(MotionEvent e) {
            Log.d(FastTradeView.TAG, "onDoubleTap: ");
            mIsDoubleTap = true;
            cancelClick();
            return super.onDoubleTap(e);
        }

        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
        }

        public boolean onSingleTapConfirmed(MotionEvent e) {
            return super.onSingleTapConfirmed(e);
        }
    };
    private Paint mBackgroundPaint = new Paint(1);
    private Paint mBuyOrderItemBgPaint = new Paint(1);
    private Paint mCancelOrderItemBgPaint = new Paint(1);
    private Paint mCancelOrderItemClickBgPaint = new Paint(1);
    private int mClickCol = -1;
    private int mClickRow = -1;
    private int mColCount = 5;
    private GestureDetector mGestureDetector;
    private Paint mGridPaint = new Paint(1);
    private float mHeight;
    private boolean mIsDoubleTap = false;
    private boolean mIsScrollTap = false;
    private float mItemHeight = 0.0f;
    private float mPriceItemWidth = 0.0f;
    private double mLastPrice = 0.0d;
    private Paint mLastPriceBgPaint = new Paint(1);
    private TradeClickOrderViewListener mListener;
    //位于屏幕中间行位置
    private int mMidItemRow = -1;
    //偏移数量(每次滑动一个新的条目可见加1)，往上滑动为正值，往下滑动为负值
    private int mOffsetIndex = 0;
    private float mOffsetY = 0.0f;
    private Paint mOrderItemClickBgPaint = new Paint(1);
    private float mOtherItemWidth = 0.0f;
    private Paint mPositionTextPaint = new Paint(1);
    private double mPriceTick = 1;
    private Paint mSellOrderItemBgPaint = new Paint(1);
    //初始价格
    private double mStartValue = 0.0d;
    private Paint mTextPaint = new Paint(1);
    private int mUIFreshType = 0;
    private float mWidth;

    public interface TradeClickOrderViewListener {
        void onItemClickListener(double d, int i);
    }

    public void setListener(TradeClickOrderViewListener listener) {
        if (listener != null) {
            mListener = listener;
        }
    }

    public void refreshView(int type) {
        mLastPrice = 1659.2/*EsTradeClickOrderData.getInstance().getLastPrice()*/;
        mPriceTick = 1/*EsTradeClickOrderData.getInstance().getPriceTick()*/;
        mUIFreshType = type;
        if (type == 2) {
            //切换最新价永远处于屏幕中央属性刷新
            mOffsetY = 0.0f;
            mOffsetIndex = 0;
            getStartValue(mLastPrice);
        } else if (type == 1) {
            //通用刷新
            //判断是否有设置最新价永远处于屏幕中央
            if (true/*EsTradeClickOrderData.getInstance().isLastPriceCentered()*/) {
                mOffsetIndex = 0;
                mOffsetY = 0.0f;
                getStartValue(mLastPrice);
            }
        } else if (type == 3) {
            //合约切换刷新
            mOffsetIndex = 0;
            mOffsetY = 0.0f;
            getStartValue(mLastPrice);
        }
        postInvalidate();
    }

    public FastTradeView(Context context) {
        super(context);
        init();
    }

    public FastTradeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FastTradeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setThemeColor();
        setTextSize();
        mGestureDetector = new GestureDetector(getContext(), gestureListener);
        mItemHeight = DensityUtils.dip2px(getContext(),40);
        mPriceItemWidth = DensityUtils.dip2px(getContext(),60);
    }

    private void setTextSize() {
        mTextPaint.setTextSize(DensityUtils.dip2px(getContext(),15));
        mTextPaint.setTextAlign(Align.CENTER);
        mPositionTextPaint.setTextSize(DensityUtils.dip2px(getContext(),15));
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = (float) MeasureSpec.getSize(widthMeasureSpec);
        mHeight = (float) MeasureSpec.getSize(heightMeasureSpec);
        mOtherItemWidth = (mWidth - mPriceItemWidth) / ((float) (mColCount - 1));
        mMidItemRow = (int) Math.ceil((double) ((mHeight / mItemHeight) / 2.0f));
        Log.d(TAG, "onMeasure: " + "mMidItemRow=" + mMidItemRow);
        getStartValue(mLastPrice);
       /* Log.d(TAG, "onMeasure_mWidth:" + mWidth);
        Log.d(TAG, "onMeasure_mHeight:" + mHeight);
        Log.d(TAG, "onMeasure_mOtherItemWidth:" + mOtherItemWidth);
        Log.d(TAG, "onMeasure_mMidItemRow:" + mMidItemRow);*/
    }

    @Override
    @SuppressLint({"DrawAllocation"})
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d(TAG, "onDraw-------start");
        //画背景
        canvas.drawRect(0.0f, 0.0f, mWidth, mHeight, mBackgroundPaint);
        canvas.drawLine(0.0f, 0.0f, mWidth, 1.0f, mGridPaint);
        FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        float top = fontMetrics.top;
        float bottom = fontMetrics.bottom;
        correctOffsetY();
        if (/*EsTradeClickOrderData.getInstance().isLastPriceCentered()*/true && mUIFreshType != 0) {
            getStartValue(mLastPrice);
        }

        //画竖直方向四条分割线
        canvas.drawLine(mOtherItemWidth, 0.0f, mOtherItemWidth, mHeight, mGridPaint);
        canvas.drawLine(mOtherItemWidth * 2.0f, 0.0f, mOtherItemWidth * 2.0f, mHeight, mGridPaint);
        canvas.drawLine(mPriceItemWidth + (2.0f * mOtherItemWidth), 0.0f, mPriceItemWidth + (2.0f * mOtherItemWidth), mHeight, mGridPaint);
        canvas.drawLine(mPriceItemWidth + (3.0f * mOtherItemWidth), 0.0f, mPriceItemWidth + (3.0f * mOtherItemWidth), mHeight, mGridPaint);

        //如果是每次滚动一个则舍弃偏移量
        if (!isScrollDistance) {
            mOffsetY = 0;
        }

        //向下滑mOffsetY大于0
        int row = mOffsetY > 0.0f ? -1 : 0;
        while (row <= getItemCount()) {
            float y;
            if (row == -1) {
                y = -(mItemHeight - mOffsetY);
                Log.d(TAG, "-1row=" + row + " mOffsetY=" + mOffsetY + " y=" + y + " mItemHeight=" + mItemHeight);
            } else {
                y = mOffsetY + (((float) row) * mItemHeight);
                Log.d(TAG, "row=" + row + " mOffsetY=" + mOffsetY + " y=" + y + " mItemHeight=" + mItemHeight);

            }
            int baseLineY = (int) ((((mItemHeight - top) - bottom) / 2.0f) + y);
            //画水平方向每行分割线
            canvas.drawLine(0.0f, y, mWidth, y, mGridPaint);
            if (row == mMidItemRow - 1) {
                canvas.drawRect(0.0f, y, mWidth, (y + mItemHeight), mGridPaint);
                Log.d(TAG, "绘制中间");
            }
            double price = getPriceByRow(row);
            Log.d(TAG, "price= " + price);
            int col = 0;
            while (col < mColCount) {
                RectF rectF;
                if (col < 2) {
                    rectF = new RectF(((float) col) * mOtherItemWidth, y, (((float) col) * mOtherItemWidth) + mOtherItemWidth, mItemHeight + y);
                } else if (col == 2) {
                    rectF = new RectF(((float) col) * mOtherItemWidth, y, (((float) col) * mOtherItemWidth) + mPriceItemWidth, mItemHeight + y);
                } else {
                    rectF = new RectF((((float) (col - 1)) * mOtherItemWidth) + mPriceItemWidth, y, (((float) col) * mOtherItemWidth) + mPriceItemWidth, mItemHeight + y);
                }
                String qty = "";
                if (col == 0) {
//                    canvas.drawRect(rectF, mCancelOrderItemBgPaint);
                    qty = "100"/* EsTradeClickOrderData.getInstance().getParOrderQty(price, 'B')*/;
                } else if (col == 1) {
//                    canvas.drawRect(rectF, mBuyOrderItemBgPaint);
                    qty = "200"/*EsTradeClickOrderData.getInstance().getQuoteQty(price, 'B')*/;
                } else if (col == 3) {
//                    canvas.drawRect(rectF, mSellOrderItemBgPaint);
                    qty = "300"/*EsTradeClickOrderData.getInstance().getQuoteQty(price, 'S')*/;
                } else if (col == 4) {
//                    canvas.drawRect(rectF, mCancelOrderItemBgPaint);
                    qty = "400"/*EsTradeClickOrderData.getInstance().getParOrderQty(price, 'S')*/;
                } else if (col == 2) {
                    if (/*EsTradeClickOrderData.getInstance().isShowPrice()*/true) {
                        long position;
                        //绘制最新价条目背景
                        if (mLastPrice == price &&/* EsTradeClickOrderData.getInstance().isLastPrice()*/true) {
                            canvas.drawRect(rectF.left, rectF.top, rectF.right, rectF.bottom, mLastPriceBgPaint);
                        }
                        if (/*contract != null*/true) {
                            String str =/* EstarBaseApi.formatPrice(contract.getContractNo(), price)*/String.valueOf(price);
                            if (/*EsTradeClickOrderData.getInstance().isImpliedPrice(price)*/false) {
                                str = "*" + str;
                            }
                            canvas.drawText(str, rectF.centerX(), (float) baseLineY, mTextPaint);
                        }
                        BigInteger data = new BigInteger("158956")/*EsTradeClickOrderData.getInstance().getPositionByPrice(price, 'B')*/;
                        int positionBaseLineY = (int) (((rectF.top + DensityUtils.dip2px(getContext(),5)) + mPositionTextPaint.getFontMetrics().descent) - mPositionTextPaint.getFontMetrics().ascent);
                        if (data != null) {
                            position = (long) data.intValue();
                            if (position > 0) {
                                canvas.drawText(position + "", rectF.left + DensityUtils.dip2px(getContext(),5), (float) positionBaseLineY, mPositionTextPaint);
                            }
                        }
                        data = new BigInteger("585585");/*EsTradeClickOrderData.getInstance().getPositionByPrice(price, 'S');*/
                        if (data != null) {
                            position = (long) data.intValue();
                            if (position > 0) {
                                canvas.drawText(position + "", (rectF.right - DensityUtils.dip2px(getContext(),5)) - mPositionTextPaint.measureText(position + ""), (float) positionBaseLineY, mPositionTextPaint);
                            }
                        }
                    }
                    col++;
                }
                if (/*EsTradeClickOrderData.getInstance().isShowPrice()*/true) {
                    if (row == mClickRow && col == mClickCol) {
                        canvas.drawRect(rectF.left, rectF.top, rectF.right, rectF.bottom, mCancelOrderItemClickBgPaint);
                    }
                    canvas.drawText(qty, rectF.centerX(), (float) baseLineY, mTextPaint);
                }
                col++;
            }
            row++;
        }
        Log.d(TAG, "onDraw-------end");

    }

    /**
     * 获取初始价格,即可见范围第一条数据的价格
     *
     * @param lastPrice
     */
    private void getStartValue(double lastPrice) {
        mStartValue = (((double) (mMidItemRow - 1)) * mPriceTick) + lastPrice;
        Log.d(TAG, "getStartValue: " + mStartValue);
    }

    /**
     * 获取指定行数的价格
     *
     * @param row
     * @return
     */
    private double getPriceByRow(int row) {
        double value = new BigDecimal(String.valueOf(mStartValue)).subtract(new BigDecimal(((double) (mOffsetIndex + row)) * mPriceTick)).doubleValue();
        Log.d(TAG, "getPriceByRow: " + "value=" + value + " mStartValue=" + mStartValue + " mOffsetIndex=" + mOffsetIndex);
        return value;
    }

    private int getItemCount() {
        Log.d(TAG, "getItemCount: " + "mHeight=" + mHeight + " mItemHeight=" + mItemHeight + " itemCount=" + (((int) (mHeight / mItemHeight)) + 1));
        return ((int) (mHeight / mItemHeight)) + 1;
    }

    private void getClickRowCol(float x, float y) {
        mClickRow = getClickRow(x, y);
        mClickCol = getClickCol(x, y);
        Log.d(TAG, "getClickRowCol: " + "Row" + mClickRow + "Col" + mClickCol);
    }

    /**
     * 点击的行数
     *
     * @param x
     * @param y
     * @return
     */
    private int getClickRow(float x, float y) {
        float showHeight = mItemHeight + mOffsetY;
        if (y <= showHeight) {
            return 0;
        }
        return ((int) ((y - showHeight) / mItemHeight)) + 1;
    }

    /**
     * 点击的列数
     *
     * @param x
     * @param y
     * @return
     */
    private int getClickCol(float x, float y) {
        if (x > 0.0f && x <= mOtherItemWidth) {
            return 0;
        }
        if (x > mOtherItemWidth && x < mOtherItemWidth * 2.0f) {
            return 1;
        }
        if (x > mOtherItemWidth * 2.0f && x < (mOtherItemWidth * 2.0f) + mPriceItemWidth) {
            return 2;
        }
        if (x <= (mOtherItemWidth * 2.0f) + mPriceItemWidth || x >= (3.0f * mOtherItemWidth) + mPriceItemWidth) {
            return 4;
        }
        return 3;
    }

    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                if (!mIsDoubleTap && !mIsScrollTap &&/* EsTradeClickOrderData.getInstance().isShowPrice()*/true && mClickRow == getClickRow(event.getX(), event.getY()) && mClickCol == getClickCol(event.getX(), event.getY()) && mListener != null) {
                    mListener.onItemClickListener(getPriceByRow(mClickRow), mClickCol);
                }
                cancelClick();
                mIsScrollTap = false;
                mIsDoubleTap = false;
                if (!isScrollDistance) {
                    mOffsetY = 0;
                }
                Log.d(TAG, "onTouchEvent: actionUP");
                break;
        }
        mGestureDetector.onTouchEvent(event);
        return true;
    }

    private void cancelClick() {
        mClickRow = -1;
        mClickCol = -1;
        refreshView(0);
    }

    private void correctOffsetY() {
        //向下滑动时，滑动偏移到达一个条目高度时，计算距离画第一根线的偏移量
        if (mOffsetY >= mItemHeight) {
            mOffsetY -= mItemHeight;
            mOffsetIndex--;
            if (mClickRow != -1) {
                mClickRow++;
            }
            Log.d(TAG, "大于正");
            Log.d(FastTradeView.TAG, "correctOffsetY：" + "mOffsetIndex=" + mOffsetIndex + " mClickRow=" + mClickRow + " mOffsetY=" + mOffsetY);
        }
        if (mOffsetY <= (-mItemHeight)) {
            mOffsetY += mItemHeight;
            mOffsetIndex++;
            if (mClickRow != -1) {
                mClickRow--;
            }
            Log.d(TAG, "小于负");
            Log.d(FastTradeView.TAG, "correctOffsetY：" + "mOffsetIndex=" + mOffsetIndex + " mClickRow=" + mClickRow + " mOffsetY=" + mOffsetY);

        }
    }

    private void setThemeColor() {
        /*if (EsSPHelper.getTheme()) {
            mBackgroundPaint.setColor(ContextCompat.getColor(getContext(), R.color.viewBkColor));
            mTextPaint.setColor(ContextCompat.getColor(getContext(), R.color.mainTextColor));
            mPositionTextPaint.setColor(ContextCompat.getColor(getContext(), R.color.mainTextColor));
            mGridPaint.setColor(ContextCompat.getColor(getContext(), R.color.separatorColor));
            mGridPaint.setStrokeWidth(getResources().getDimension(R.dimen.y1));
            mCancelOrderItemBgPaint.setColor(ContextCompat.getColor(getContext(), R.color.es_trade_click_order_cell_bg_cancel_order_normal));
            mCancelOrderItemClickBgPaint.setColor(ContextCompat.getColor(getContext(), R.color.es_trade_click_order_cell_bg_cancel_order_click));
            mBuyOrderItemBgPaint.setColor(ContextCompat.getColor(getContext(), R.color.es_trade_click_order_cell_bg_bid_qty_normal));
            mSellOrderItemBgPaint.setColor(ContextCompat.getColor(getContext(), R.color.es_trade_click_order_cell_bg_ask_qty_normal));
            mOrderItemClickBgPaint.setColor(ContextCompat.getColor(getContext(), R.color.es_trade_click_order_cell_bg_bid_qty_click));
            mLastPriceBgPaint.setColor(ContextCompat.getColor(getContext(), R.color.es_trade_click_order_cell_bg_last_price));
            return;
        }*/
        mBackgroundPaint.setColor(ContextCompat.getColor(getContext(), R.color.colorDarkBlue));
        mTextPaint.setColor(ContextCompat.getColor(getContext(), R.color.colorWhite));
        mPositionTextPaint.setColor(ContextCompat.getColor(getContext(), R.color.colorWhite));
        mGridPaint.setColor(ContextCompat.getColor(getContext(), R.color.colorBlack));
        mGridPaint.setStrokeWidth(2);
        /*mCancelOrderItemBgPaint.setColor(ContextCompat.getColor(getContext(), R.color.color_block_night));
        mCancelOrderItemClickBgPaint.setColor(ContextCompat.getColor(getContext(), R.color.color_item_dark_background_night));
        mBuyOrderItemBgPaint.setColor(ContextCompat.getColor(getContext(),R.color.color_block_night));
        mSellOrderItemBgPaint.setColor(ContextCompat.getColor(getContext(), R.color.color_block_night));
        mOrderItemClickBgPaint.setColor(ContextCompat.getColor(getContext(), R.color.color_item_dark_background_night));
        mLastPriceBgPaint.setColor(ContextCompat.getColor(getContext(), R.color.color_green_night));*/
    }
}