package com.simon.table.views;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.simon.table.R;

import java.util.ArrayList;
import java.util.List;


/**
 *  柱状图
 * @author Administrator
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ColumnarChart extends View{
	public static  String tag="com.example.testchart";
	/**
	 * 控件的宽度和高度
	 */
	private int viewHeight;
	private int viewWidth;
	/**
	 *屏幕宽度的一半 
	 */
	private int widthMiddle;

	/**
	 * 柱状图的宽度
	 */
	private int columnarWidth=20;

	/**
	 * 左右边距
	 */
     private int leftMargin, rightMargin, topMargin,bottomMargin;

	 /**
	  * 坐标线的宽度
	  */
	 private int columnarCoordinatesLineWidth;
	/**
	 * 坐标线的色值
	 */
	int columnarXcoordinatesTextColor=Color.parseColor("#b8c0cb");

	/**
	 * x坐标的文字的大小
	 */
	int columnarXcoordinatesTextSize;

	/**
	 * X轴上方和y轴右侧指示文字的大小
	 */
	int columnarXIndicateTextSize;

	/**
	 * X轴上方和y轴右侧指示文字的颜色
	 */
	int columnarXIndicateTextColor;
	 /**
	 * x轴圆点的半径
	 */
	private float indicateAverageCircleradius;

	int columnarSpace;
	private int columnarNormalColor=Color.parseColor("#A6D3E3");
	private int columnarSelectColor=Color.parseColor("#f1453b");
	/**
	 * 文字的偏移
	 */
	int textOffset;
	/**
	 * y轴左右偏移
	 */
	int yOffset;

	/**
	 * 提示框内文字的大小
	 */
	int promptTextSize;

	/**
	 * 用户触点的文字
	 */
	private int downX;
	private int downY;
	private boolean IsChoiseColumnar;

	/**
	 * 箭头的偏移
	 */
	private int arrowOffset;
	/**
	 * 弹窗偏移
	 */
	private int upOffset;

	/**
	 * 阴影的半径
	 */
	private int shadowRadius;

	/**
	 * 柱子最高点
	 */
	public int columnarMaxPositon;
	/**
	 * 柱子起点
	 */
	public int xBaseLine;

	/**
	 * 画布对象
	 */
	Canvas mCanvas;

	/**
	 * 画布工具
	 */
	private Paint barPaint, linePaint, textPaint,pointPaint;
	/**
	 * 组装的柱状图对象的集合
	 */
	List<RectObject> rectlList=new ArrayList<>();

	public ColumnarChart(Context context)
	{
		this(context, null);
	}

	public ColumnarChart(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	public ColumnarChart(Context context, AttributeSet attrs,int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ColumnarChart, defStyleAttr, 0);
		int index = array.getIndexCount();
		for (int i = 0; i < index; i++)
		{
			int attr = array.getIndex(i);
			switch (attr)
			{
				case R.styleable.ColumnarChart_columnarCoordinatesLineWidth:
					// 这里将以px为单位,默认值为2px;
					columnarCoordinatesLineWidth = array.getDimensionPixelSize(attr,
							(int) TypedValue.applyDimension(
									TypedValue.COMPLEX_UNIT_PX, 2,
									getResources().getDisplayMetrics()));
					break;
				case R.styleable.ColumnarChart_columnarXcoordinatesTextSize:
					columnarXcoordinatesTextSize = array.getDimensionPixelSize(attr,
							(int) TypedValue.applyDimension(
									TypedValue.COMPLEX_UNIT_PX, 2,
									getResources().getDisplayMetrics()));
					break;
				case R.styleable.ColumnarChart_columnarXcoordinatesTextColor:
					columnarXcoordinatesTextColor = array.getColor(attr, Color.WHITE);
					break;

				case R.styleable.ColumnarChart_columnarXIndicateTextSize:
					columnarXIndicateTextSize = array.getDimensionPixelSize(attr,
							(int) TypedValue.applyDimension(
									TypedValue.COMPLEX_UNIT_PX, 2,
									getResources().getDisplayMetrics()));
					break;
				case R.styleable.ColumnarChart_columnarXIndicateTextColor:
					columnarXIndicateTextColor = array.getColor(attr, Color.WHITE);
					break;
				case R.styleable.ColumnarChart_indicateAverageCircleradius:
					indicateAverageCircleradius = array.getDimensionPixelSize(attr,
							(int) TypedValue.applyDimension(
									TypedValue.COMPLEX_UNIT_PX, 2,
									getResources().getDisplayMetrics()));
					break;
				case R.styleable.ColumnarChart_columnarNormalColor:
					columnarNormalColor  = array.getColor(attr, Color.WHITE);
					break;
				case R.styleable.ColumnarChart_columnarSelectColor:
					columnarSelectColor  = array.getColor(attr, Color.WHITE);
					break;

				case R.styleable.ColumnarChart_columnarSpace:
					columnarSpace = array.getDimensionPixelSize(attr,
							(int) TypedValue.applyDimension(
									TypedValue.COMPLEX_UNIT_PX, 2,
									getResources().getDisplayMetrics()));
					break;
			}
		}
		// 记得释放资源
		array.recycle();
		init(context);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		 setMeasuredDimension(viewWidth,viewHeight); 
	}


	private void init(Context context) {
		//初始相对参数
		Resources resources =context.getResources();
		DisplayMetrics metric = context.getResources().getDisplayMetrics();
		viewWidth = metric.widthPixels;
		viewHeight = metric.heightPixels/3;
		widthMiddle=viewWidth/2;
		 //左边距
        leftMargin =  (int) resources.getDimension(R.dimen.space_value_x4);
        rightMargin = (int) resources.getDimension(R.dimen.space_value_x4);
        topMargin = (int) resources.getDimension(R.dimen.space_value_x4);
        bottomMargin = viewHeight/100*14;

        linePaint = new Paint();
        barPaint = new Paint();
        textPaint=new Paint();
        pointPaint=new Paint();
        textOffset=(int) resources.getDimension(R.dimen.space_value_x5);
        yOffset= (int) resources.getDimension(R.dimen.space_value_x20);
        upOffset= (int) resources.getDimension(R.dimen.space_value_x6);
        arrowOffset=(int) resources.getDimension(R.dimen.space_value_x4);
        promptTextSize=(int) resources.getDimension(R.dimen.space_value_x12);
        shadowRadius=(int) resources.getDimension(R.dimen.space_value_x3);

        columnarMaxPositon=topMargin+yOffset;

        xBaseLine=viewHeight-bottomMargin;
	}

	/**
	 * 设置参数
	 */
	public void setParamsDefult(float[] defaultRects,float userAtgroup,float maxGroup){
		calculatePillarWidth(defaultRects.length);
		//先清空集合
		rectlList.clear();
		  //组装参数
		int yMaxLength=viewHeight-topMargin-bottomMargin-yOffset;
		int columnarHeight=0;
		//初始化负半轴柱子
		int rectlength=(defaultRects.length/2-1);
		for (int i =rectlength; i >=0; i--) {
	     		columnarHeight=(int) (defaultRects[i]/maxGroup*yMaxLength);
			    //负的公式为： （middle-i*space-i*w, h,middle+-*space+space/2+(i-1)w,v-b）;
				int rectLeft=widthMiddle-(Math.abs(i-rectlength)+1)*columnarSpace-(Math.abs(i-rectlength)+1)*columnarWidth;
				RectObject rectObject=null;
				if(columnarHeight==0){
					rectObject=new RectObject(rectLeft, viewHeight-bottomMargin, rectLeft+columnarWidth, viewHeight-bottomMargin);
				}else{
					rectObject=new RectObject(rectLeft, viewHeight-bottomMargin-columnarHeight, rectLeft+columnarWidth, viewHeight-bottomMargin);
				}
				//设置柱子的位置 1-9
				 if(defaultRects[i]==userAtgroup){
					 rectObject.setUserAtGroup(true);
					 rectObject.setBackgroupColor(columnarSelectColor);
				  }else{
					  rectObject.setUserAtGroup(false);
					  rectObject.setBackgroupColor(columnarNormalColor);
				  }
				 //设置提示语句
				 rectlList.add(rectObject);
			}
			//初始正半轴的柱子
		    for (int i = 0; i <rectlength+1; i++) {
		      //计算柱子的高度
		      columnarHeight=(int) (defaultRects[i+rectlength+1]/maxGroup*yMaxLength);
			  int rectLeft=widthMiddle+(i+1)*columnarSpace+(i)*columnarWidth;
				RectObject rectObject=null;
				if(columnarHeight==0){
					rectObject=new RectObject(rectLeft, viewHeight-bottomMargin, rectLeft+columnarWidth, viewHeight-bottomMargin);
				}else{
					rectObject=new RectObject(rectLeft, viewHeight-bottomMargin-columnarHeight, rectLeft+columnarWidth, viewHeight-bottomMargin);
				}
				//设置柱子的位置 10-18
			  if(defaultRects[i+rectlength+1]==userAtgroup){
				  rectObject.setUserAtGroup(true);
				  rectObject.setBackgroupColor(columnarSelectColor);
			  }else{
				  rectObject.setBackgroupColor(columnarNormalColor);
				  rectObject.setUserAtGroup(false);
			  }
			  rectlList.add(rectObject);
			 }
	       startAnimator();
	}

    public void startAnimator() {
		ValueAnimator animator=ValueAnimator.ofInt(columnarMaxPositon,viewHeight);
		final  int baseLine=viewHeight;
		animator.setStartDelay(200);
		animator.setDuration(1000);
		animator.setInterpolator(new LinearInterpolator());
		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				xBaseLine= baseLine-((int) animation.getAnimatedValue()-columnarCoordinatesLineWidth);
				invalidate();
			}
		});
		animator.start();
    }

	public void setxBaseLine(int xBaseLine) {
		this.xBaseLine = xBaseLine;
	}


	/**
	 * 计算柱状的宽度
	 */
	private void calculatePillarWidth(int length) {
		//柱子宽度
		columnarWidth=(viewWidth-leftMargin-rightMargin-2*yOffset-length*columnarSpace)/length;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		this.mCanvas=canvas;
		super.onDraw(canvas);
		drawXAndYDialogText(canvas);
		drawLineXandY(canvas);
		drawColumnar(canvas);
		drawPointAndText(canvas);
	}

	/**
	 * 坐标轴绘制完成
	 * 绘制x轴和y轴
	 * 绘制箭头
	 */
	private void drawLineXandY(Canvas canvas) {
		linePaint.setStrokeWidth(columnarCoordinatesLineWidth);
        linePaint.setColor(columnarXcoordinatesTextColor);
		  //绘制x轴
        canvas.drawLine(leftMargin,viewHeight-bottomMargin,viewWidth-rightMargin,viewHeight-bottomMargin,linePaint);
        //绘制x轴的箭头
        //计算3个点
        int upPointx=viewWidth-rightMargin-arrowOffset;
        int upPointy=viewHeight-bottomMargin-arrowOffset;
        int downPointx=viewWidth-rightMargin-arrowOffset;
        int downPointy=viewHeight-bottomMargin+arrowOffset;
        int endX=viewWidth-rightMargin;
        int endY=viewHeight-bottomMargin;
        Path path=new Path();
        path.moveTo(upPointx, upPointy);
        path.lineTo(endX, endY);
        path.lineTo(downPointx,downPointy);
        linePaint.setStyle(Style.STROKE);
		canvas.drawPath(path,linePaint);
        //y轴
		linePaint.setStyle(Style.FILL);
        canvas.drawLine(widthMiddle,viewHeight-bottomMargin,widthMiddle,topMargin,linePaint);
        //绘制y轴的箭头
        //计算3个点
        int leftPointx=widthMiddle-arrowOffset;
        int leftPointy=topMargin+arrowOffset;
        int rightPointx=widthMiddle+arrowOffset;
        int rightPointy=topMargin+arrowOffset;
        int startX=widthMiddle;
        int startY=topMargin;
        Path ypath=new Path();
        ypath.moveTo(leftPointx, leftPointy);
        ypath.lineTo(startX, startY);
        ypath.lineTo(rightPointx,rightPointy);
        linePaint.setStyle(Style.STROKE);
		canvas.drawPath(ypath,linePaint);
	}
	
	/**
	 * 绘制x轴和y轴的提示
	 * 试试path，绘制文字
	 * @param canvas
	 */
	private void drawXAndYDialogText(Canvas canvas) {
		textPaint.reset();
		textPaint.setTextAlign(Align.LEFT);
		textPaint.setAntiAlias(true);
		textPaint.setColor(Color.parseColor("#999999"));
		textPaint.setTextSize(columnarXIndicateTextSize);
		String dialogYStr="人数";
		String dialogXStr1="收";
		String dialogXStr2="益";
		String dialogXStr3="比";
		Rect bounds = new Rect();  
		//绘制Y轴提示
		//测量text的矩阵范围，进行调整
//		textPaint.getTextBounds(dialogYStr, 0, dialogYStr.length(), bounds);
		float y_textX=widthMiddle+textOffset;
		float y_textY=topMargin+textOffset+arrowOffset;
		canvas.drawText(dialogYStr,y_textX, y_textY,textPaint);
		//绘制X轴提示
		//测量text的矩阵范围，进行调整
		textPaint.setTextAlign(Align.CENTER);
		textPaint.getTextBounds(dialogXStr1, 0,dialogXStr1.length(), bounds);
		float x_textX=viewWidth-rightMargin-bounds.width()/2;
		float x_textY=viewHeight-bottomMargin-textOffset-3*bounds.height();
		canvas.drawText(dialogXStr1,x_textX, x_textY,textPaint);
		canvas.drawText(dialogXStr2,x_textX, x_textY+bounds.height()+textOffset/2,textPaint);
		canvas.drawText(dialogXStr3,x_textX, x_textY+2*bounds.height()+textOffset/2,textPaint);
	}
	
	/**
	 * 绘制柱状图
	 * 首先确定x轴和y轴的最大值
	 * @param canvas
	 */
	private void drawColumnar(Canvas canvas) {
	   for (int i = 0; i <rectlList.size(); i++) {
		 barPaint.reset();
     	 RectObject rectObject = rectlList.get(i);
     	 //需要绘制的高度的位置
     	 RectF r;
     	//如果绘制的高度，没有超过当前矩形对象的高度，就绘制逐渐向上绘制
     	 if(xBaseLine>rectObject.top){
     		  r=new RectF(rectObject.left,xBaseLine,rectObject.right,rectObject.bottom);
     	 }else if(rectObject.top==xBaseLine){
     		 	//如果为零绘制默认高度
     		  r=new RectF(rectObject.left,rectObject.top,rectObject.right,rectObject.bottom);
     	 }else{
     		 //如果绘制的高度，超过或者等于当前矩形的宽度，绘制当前矩形的实际高度
     		 r=new RectF(rectObject.left,rectObject.top,rectObject.right,rectObject.bottom);
     	 }
		 barPaint.setColor(rectObject.getBackgroupColor());
		 canvas.drawRect(r,barPaint);
      }
    //绘制选中的状态
	  for (int i = 0; i <rectlList.size(); i++) {
		  barPaint.reset();
		 RectObject rectObject = rectlList.get(i);
		 if(rectObject.isChoised){
			 //阴影部分的矩形框
			 drawshadowRect(canvas, rectObject);
	     // 绘制提示框
			 drawDialogPrompt(canvas, rectObject);
		 }
	  }
	}

	/**
	 * 绘制选中状态的阴影
	 * @param canvas
	 * @param rectObject
	 */
	@SuppressLint("NewApi")
	private void drawshadowRect(Canvas canvas, RectObject rectObject) {
		 barPaint.setStyle(Style.FILL);
		 barPaint.setAntiAlias(true);
		 barPaint.setColor(rectObject.getBackgroupColor());
		 setLayerType(LAYER_TYPE_SOFTWARE, null);
		 barPaint.setShadowLayer(shadowRadius, 0, 0, Color.parseColor("#666666"));
		 RectF bigrR=new RectF(rectObject.left-columnarSpace,rectObject.top-columnarSpace,rectObject.right+columnarSpace,rectObject.bottom);
		 canvas.drawRect(bigrR,barPaint);
	}

	/**
	 * hh绘制提示框
	 * @param canvas
	 * @param rectObject
	 */
	@SuppressLint("NewApi")
	private void drawDialogPrompt(Canvas canvas, RectObject rectObject) {
		 barPaint.reset();
		 barPaint.setAntiAlias(true);
		 barPaint.setStyle(Style.FILL);
		 barPaint.setStrokeWidth(columnarCoordinatesLineWidth);
		 //提示框的颜色
		 barPaint.setColor(Color.parseColor("#ABBDBF"));
		 barPaint.setAlpha(220);
		 TextPaint textPaint=new TextPaint();
		 textPaint.setAntiAlias(true);  
		 textPaint.setTextSize(promptTextSize);
		 //文字颜色
		 textPaint.setColor(Color.parseColor("#ffffff"));
		 String judgeDialogText = judgeDialogText(rectObject);
		 //矩形框的宽度
		 int DialogPromptwidth=columnarWidth*10;
		 StaticLayout staticLayout = new StaticLayout(judgeDialogText,  textPaint, DialogPromptwidth, Alignment.ALIGN_NORMAL, 1, 0, false);
		 //矩形框的高度
		 int DialogPromptHeight=staticLayout.getHeight();
		 //计算矩形框的位置
		 if((rectObject.top-DialogPromptHeight-4*upOffset)<0){
			 drawOverHeightDialog(canvas, rectObject, DialogPromptwidth,staticLayout, DialogPromptHeight,upOffset);
		 }else{
			 drawNormalDialog(canvas, rectObject, DialogPromptwidth, staticLayout,DialogPromptHeight);
		 }
	}
	
	/**
	 * 绘制超出的弹窗
	 * @param canvas
	 * @param rectObject
	 * @param DialogPromptwidth
	 * @param staticLayout
	 * @param DialogPromptHeight
	 * @param textOffset
	 */
	private void drawOverHeightDialog(Canvas canvas, RectObject rectObject,int DialogPromptwidth, StaticLayout staticLayout,int DialogPromptHeight, float textOffset) {
		RectF dialogPrompt;
		if(rectObject.right<widthMiddle){
			 //左边
			 dialogPrompt=new RectF(rectObject.right,topMargin/4,rectObject.right+DialogPromptwidth+2*textOffset,topMargin/4+DialogPromptHeight+2*textOffset);
			 //绘制圆角矩形
			 canvas.drawRoundRect(dialogPrompt, shadowRadius, shadowRadius, barPaint);   
			 //矩形的下边线，矩形的宽度
			 float startX=rectObject.right;
			 float startY=topMargin/4+DialogPromptHeight;
			 
			 float stopX=rectObject.right+2*textOffset;
			 float stopY=topMargin/4+DialogPromptHeight+2*textOffset;
			//箭头点
			 float xPoint=rectObject.left+columnarWidth/2;
    	     float yPoint=topMargin/4+DialogPromptHeight+2*textOffset+textOffset;
    	     //提示框的左下角
    	     float x=rectObject.right+shadowRadius;
    	     float y=topMargin/4+DialogPromptHeight+2*textOffset-shadowRadius;
			 drawArrowPath(canvas, startX, startY, stopX, stopY, xPoint, yPoint,x,y);
			 canvas.save();
			 //使用画布平移一段距离
			 canvas.translate(rectObject.right+textOffset,topMargin/4+textOffset);
			 //绘制提示文字
			 staticLayout.draw(canvas);
			 canvas.restore();
		 }else{
			 //右边
			 dialogPrompt=new RectF(rectObject.left-DialogPromptwidth-2*textOffset,topMargin/4,rectObject.left,topMargin/4+DialogPromptHeight+2*textOffset);
			 //绘制圆角矩形
			 canvas.drawRoundRect(dialogPrompt, shadowRadius, shadowRadius, barPaint); 
			 //矩形的下边线，矩形的宽度
			 float startX=rectObject.left;
			 float startY=topMargin/4+DialogPromptHeight;
			 
			 float stopX=rectObject.left-2*textOffset;
			 float stopY=topMargin/4+DialogPromptHeight+2*textOffset;
			 //箭头点
			  float xPoint=rectObject.left+columnarWidth/2;
		     float yPoint=topMargin/4+DialogPromptHeight+2*textOffset+textOffset;
		     
		     float x=rectObject.left-shadowRadius;
		     float y=topMargin/4+DialogPromptHeight+2*textOffset-shadowRadius;
			 drawArrowPath(canvas, startX, startY, stopX, stopY, xPoint, yPoint,x,y);
			 canvas.save();
			  //使用画布平移一段距离
		     canvas.translate(rectObject.left-DialogPromptwidth-textOffset,topMargin/4+textOffset);
			 //绘制提示文字
			 staticLayout.draw(canvas);
			 canvas.restore();
		 }
	}
	
	/**
	 * 绘制正常的柱子
	 * @param canvas
	 * @param rectObject
	 * @param DialogPromptwidth
	 * @param staticLayout
	 * @param DialogPromptHeight
	 */
	private void drawNormalDialog(Canvas canvas, RectObject rectObject,
			int DialogPromptwidth, StaticLayout staticLayout,
			int DialogPromptHeight) {
		RectF dialogPrompt=null;
		 //文字内部偏移
		 //柱子向上偏移
		if(rectObject.right<widthMiddle){
			 dialogPrompt=new RectF(rectObject.right,rectObject.top-DialogPromptHeight-4*upOffset,rectObject.right+DialogPromptwidth+upOffset,rectObject.top-2*upOffset);
			 //绘制圆角矩形
			 canvas.drawRoundRect(dialogPrompt, shadowRadius, shadowRadius, barPaint); 
			 //矩形的下边线，矩形的宽度
			 float startX=rectObject.right;
			 float startY=rectObject.top-4*upOffset;
			 
			 float stopX=rectObject.right+2*upOffset;
			 float stopY=rectObject.top-2*upOffset;
			//箭头点
			 float xPoint=rectObject.left+columnarWidth/2;
    	     float yPoint=rectObject.top-upOffset;
    	     //提示框的左下角
    	     float x=rectObject.right+shadowRadius;
    	     float y=rectObject.top-2*upOffset-shadowRadius;
			 drawArrowPath(canvas, startX, startY, stopX, stopY, xPoint, yPoint,x,y);
			 canvas.save();
			 //使用画布平移一段距离
			 canvas.translate(rectObject.right+upOffset,rectObject.top-DialogPromptHeight-3*upOffset);
			 //绘制提示文字
			 staticLayout.draw(canvas);
			 canvas.restore();
		 }else{
			 //右边
			 dialogPrompt=new RectF(rectObject.left-DialogPromptwidth-2*upOffset,rectObject.top-DialogPromptHeight-4*upOffset,rectObject.left,rectObject.top-2*upOffset);
			 //绘制圆角矩形
			 canvas.drawRoundRect(dialogPrompt,shadowRadius,shadowRadius, barPaint); 
			 //矩形的下边线，矩形的宽度
			 float oneX=rectObject.left;
			 float oneY=rectObject.top-4*upOffset;
			 float twoX=rectObject.left-2*upOffset;
			 float twoY=rectObject.top-2*upOffset;
			 //箭头点
			 float xPoint=rectObject.left+columnarWidth/2;
		     float yPoint=rectObject.top-upOffset;
		     //提示框的右下角
		     float x=rectObject.left-shadowRadius;
		     float y=rectObject.top-2*upOffset-shadowRadius;
			 drawArrowPath(canvas, oneX, oneY,twoX, twoY, xPoint, yPoint,x,y);
			 canvas.save();
			  //使用画布平移一段距离
		     canvas.translate(rectObject.left-DialogPromptwidth-upOffset,rectObject.top-DialogPromptHeight-3*upOffset);
			 //绘制提示文字
			 staticLayout.draw(canvas);
			 canvas.restore();
		 }
	}
	
	/**
  	 * 绘制箭头   参数为箭头的点
	 * @param canvas
	 * @param startX
	 * @param startY
	 * @param stopX
	 * @param stopY
	 * @param xPoint
	 * @param yPoint
	 */
	private void drawArrowPath(Canvas canvas, float startX, float startY,float stopX, float stopY, float xPoint, float yPoint,float x, float y) {
		 Path arrowsPath=new Path();
		 arrowsPath.moveTo(startX, startY);
		 arrowsPath.lineTo(x, y);
		 arrowsPath.lineTo(stopX, stopY);
		 arrowsPath.lineTo(xPoint, yPoint);
		 arrowsPath.close();
		 linePaint.reset();
		 linePaint.setStyle(Style.FILL);
		 linePaint.setColor(Color.parseColor("#AEC3C6"));
		 linePaint.setAlpha(220);
		 //绘制箭头
		 canvas.drawPath(arrowsPath, linePaint);
	}
	
	/**
	 * 判断需要显示的文字
	 */
	private String judgeDialogText(RectObject rectObject) {
		//用户所在
		String dialogStr="testDialog";
		return dialogStr;
	}

	/**
	 * 绘制x轴 指引点
	 * @param canvas
	 */
	private void drawPointAndText(Canvas canvas) {
		if(rectlList!=null&&rectlList.size()>=18){
			textPaint.reset();
			textPaint.setTextAlign(Align.CENTER);
			textPaint.setAntiAlias(true);
			pointPaint.setColor(Color.parseColor("#f1453b"));
			textPaint.setColor(Color.parseColor("#f1453b"));
			textPaint.setTextSize(columnarXcoordinatesTextSize);
			int[] rightPoints=new int[]{12,16};
			String[]  rightTexts=new String[]{"+8%","+16%"};
			//绘制正x轴的点
			for (int i = 0; i < rightPoints.length; i++) {
				 RectObject rectObject = rectlList.get(rightPoints[i]);
				 int pointX=rectObject.right+columnarSpace/2;
				 int pointY=rectObject.bottom;
					//绘制指示点
				 canvas.drawCircle(pointX, pointY, indicateAverageCircleradius, pointPaint);
				 //绘制x轴的指数的线
				 int textX=rectObject.right+columnarSpace/2;
				 int textY=viewHeight-bottomMargin/2;
				canvas.drawText(rightTexts[i],textX, textY,textPaint);
		   }

			//绘制负x轴的点
			pointPaint.setColor(Color.parseColor("#38c4a9"));
			textPaint.setColor(Color.parseColor("#38c4a9"));
			int[] leftPoints=new int[]{8,4};
			String[]  leftTexts=new String[]{"-16%","-8%"};
			for (int i = 0; i < leftPoints.length; i++) {
					RectObject rectObject = rectlList.get(leftPoints[i]);
					int pointX=rectObject.right+columnarSpace/2;
					int pointY=rectObject.bottom;
					//绘制指示点
				 canvas.drawCircle(pointX, pointY, indicateAverageCircleradius, pointPaint);
				 //绘制x轴的指数的线
				 int textX=rectObject.right+columnarSpace/2;
				 int textY=viewHeight-bottomMargin/2;
				 canvas.drawText(leftTexts[i],textX, textY,textPaint);
		  }
		}
	  //绘制中心点	
		textPaint.setColor(Color.parseColor("#000000"));
		canvas.drawText("0",widthMiddle, viewHeight-bottomMargin/2,textPaint);
	}
	
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			downX=(int) event.getX();
			downY=(int) event.getY();
			checkIsClickColumnar();
			if(IsChoiseColumnar){
				IsChoiseColumnar=false;
			}
			//重绘
			//也就是说，每次点击柱子视图都会进行重绘操作
			invalidate();
			break;
		}
		return true;
	}
	
	/**
	 * 判断是否是点击选中了矩形图框
	 * ------------点击事件完成
	 */
    private void checkIsClickColumnar() {
    	//判断是否选中一个
    	for (int i = 0; i < rectlList.size(); i++) {
    		RectObject rectObject=rectlList.get(i);
    		int left=rectObject.left;
    		int right=rectObject.right;
    		//添加x轴宽度的可点区域
    		int top=rectObject.top;
    		int bottom=rectObject.bottom;
    		rectObject.setChoised(false);
    		if((left<downX&&downX<right)&&(downY<bottom&&downY>top)){
    			//标识为选中
    			rectObject.setChoised(true);
//    			Log.i(tag, "第"+i+"个");
    			IsChoiseColumnar=true;
    		}
    	}
//		Log.i(tag, "IsChoiseColumnar="+IsChoiseColumnar);
    	//如果点击中没有选中矩形框，将所有的框全部设置为未选中
    	resetAllTableState();
	}

    /**
     * 重置所有视图的状态
     */
	public  void resetAllTableState() {
		if(!IsChoiseColumnar){
	    	for (int i = 0; i < rectlList.size(); i++) {
	    		RectObject rectObject=rectlList.get(i);
	    		rectObject.setChoised(false);
	    	}
    	}
	}
    
	/**
	 * 矩形对象
	 * 指示所在对象的区域，是否选中，背景颜色，提示文字等
	 * @author Administrator
	 */
	class RectObject{
		int left;
		int right;
		int top;
		int bottom;
		/**
		 * 是否选中
		 */
		boolean isChoised;
		/**
		 * 是否用户所在组
		 */
		boolean isUserAtGroup;
		/**
		 * 背景颜色
		 */
		int backgroupColor;

		public RectObject(int left,  int top,int right, int bottom) {
			super();
			this.left = left;
			this.right = right;
			this.top = top;
			this.bottom = bottom;
		}
		public boolean isChoised() {
			return isChoised;
		}
		public void setChoised(boolean isChoised) {
			this.isChoised = isChoised;
		}
		public void setUserAtGroup(boolean isUserAtGroup) {
			this.isUserAtGroup = isUserAtGroup;
		}
		public int getBackgroupColor() {
			return backgroupColor;
		}
		public void setBackgroupColor(int backgroupColor) {
			this.backgroupColor = backgroupColor;
		}
	}

	
}
