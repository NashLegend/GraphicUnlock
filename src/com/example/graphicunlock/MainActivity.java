package com.example.graphicunlock;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class MainActivity extends Activity implements OnTouchListener {

	private RelativeLayout relativeLayout;// 用来摆放九个圆形
	private ImageView view;// 用来绘制解锁路径
	private Path path;// 划过的路径
	private Paint paint;
	private Canvas canvas;
	private Dot[] array = new Dot[9];// 圆形的数组
	private Dot lastDot;// 上一个经过的点
	private Bitmap bitmap;// 绘制用的bitmap
	private boolean drawing = false;// 是否正在画图
	private int radius = 0;// 圆形半径

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 锁定竖屏
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		// 不显示标题栏
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 全屏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);
		relativeLayout = (RelativeLayout) findViewById(R.id.rela);
		view = (ImageView) findViewById(R.id.view);
		view.setOnTouchListener(this);
		drawDots();
	}

	/**
	 * 放置九个圆形 将九个圆形在屏幕中居中放置，每屏幕的三分之一宽度为一格，横竖排各三个，每个圆宽度是屏幕宽度的1/6
	 */
	protected void drawDots() {
		int TopMars = (getScreenHeight() - getScreenWidth()) / 2;
		radius = getScreenWidth() / 12;
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
						radius * 2, radius * 2);
				params.leftMargin = (int) (radius * 4 * (j + 0.25));
				params.topMargin = (int) (TopMars + radius * 4 * (i + 0.25));
				// 新建半径为radius的圆形
				Dot dot = new Dot(this, radius);
				array[i * 3 + j] = dot;
				relativeLayout.addView(dot, params);
			}
		}
	}

	/**
	 * 检查pointF是否在某个圆形范围内
	 * 
	 * @param point
	 *            要检查的点
	 * @return 如果确实在某个圆形范围内，则返回该圆形，反之返回null
	 */
	private Dot hitValidDot(PointF point) {
		for (int i = 0; i < array.length; i++) {
			Dot dot = array[i];
			if (!dot.getPassed()) {
				int[] location = { 0, 0 };
				dot.getLocationOnScreen(location);
				if (Math.sqrt((point.x - location[0] - radius)
						* (point.x - location[0] - radius)
						+ (point.y - location[1] - radius)
						* (point.y - location[1] - radius)) < radius) {
					return dot;
				}
			}
		}
		return null;
	}

	/**
	 * 要绘制到的目标图片上的触摸事件 本方法里view.invalidate()并不是必须的，有没有一样……
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			// 检查手机按下的点是否在某个圆形内，如果是则以此圆形为起点开始绘制图形
			PointF point = new PointF(event.getRawX(), event.getRawY());
			Dot dot = hitValidDot(point);
			if (dot != null) {
				// 开始绘制 先实例化要绘制的bitmap canvas paint 和绘制的路径path
				bitmap = Bitmap.createBitmap(getWindowWidth(),
						getWindowHeight(), Config.ARGB_8888);
				canvas = new Canvas(bitmap);
				paint = new Paint();
				path = new Path();
				// 获取此圆形中心点的位置
				RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) dot
						.getLayoutParams();
				PointF startPoint = new PointF(params.leftMargin + radius,
						params.topMargin + radius);
				// 将loasDot赋值给dot，并将dot设置为经过状态
				lastDot = dot;
				lastDot.drawPassed();
				// 将圆形的中心点设置为路径的起点 并设置要绘制路径的颜色的宽度
				path.moveTo(startPoint.x, startPoint.y);
				paint.setARGB(255, 0, 0, 255);
				paint.setStrokeWidth(8);
				paint.setStyle(Style.STROKE);
				// 绘制到屏幕
				view.setImageBitmap(bitmap);
				// 标记为正在绘图中
				drawing = true;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (drawing) {
				// 先清空图片 否则看到的是每次绘制的叠加效果
				clear();
				// 同MotionEvent.ACTION_DOWN中一样 检查是否经过了某一点
				PointF point2 = new PointF(event.getRawX(), event.getRawY());
				Dot dot2 = hitValidDot(point2);
				if (dot2 != null) {
					// 不过有时候两点之间可能会有第三个点，如果第三个点为非经过状态，则将此点设置为经过状态
					Dot dotBetween = checkDotBetween(lastDot, dot2);
					if (dotBetween != null) {
						lastDot = dotBetween;
						lastDot.drawPassed();
						RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) dot2
								.getLayoutParams();
						path.lineTo(params.leftMargin + radius,
								params.topMargin + radius);
					}
					lastDot = dot2;
					lastDot.drawPassed();
					RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) dot2
							.getLayoutParams();
					path.lineTo(params.leftMargin + radius, params.topMargin
							+ radius);
				}
				// 绘制出经过的所有点的路径
				canvas.drawPath(path, paint);

				// 绘制出上一个点到手指触摸的位置的路径
				RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) lastDot
						.getLayoutParams();
				canvas.drawLine(params.leftMargin + radius, params.topMargin
						+ radius, event.getX(), event.getY(), paint);
				view.invalidate();
			}
			break;
		case MotionEvent.ACTION_UP:
			if (drawing) {
				// 手指抬起后，清空并重新绘制所有经过的点的路径，这样就会清除上一个点到手指触摸的位置的路径了
				clear();
				canvas.drawPath(path, paint);
				view.invalidate();
				// 绘制完毕，将绘制状态改为false
				drawing = false;

				// 三秒种后重置，放在这仅仅是为了测试重置功能
				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
						clearAllDrawing();
					}
				}, 3000);
			}
			break;
		default:
			break;
		}
		return true;
	}

	/**
	 * 重置所有为初始状态
	 */
	protected void clearAllDrawing() {
		clear();
		for (int i = 0; i < array.length; i++) {
			Dot dot = array[i];
			if (dot != null) {
				dot.drawNormal();
			}
		}
		drawing = false;
	}

	/**
	 * 查检两点之间是否经过第三点，如果是则返回第三点，否则返回null
	 */
	protected Dot checkDotBetween(Dot dot1, Dot dot2) {
		int[] loc1 = { 0, 0 };
		int[] loc2 = { 0, 0 };
		dot1.getLocationOnScreen(loc1);
		dot2.getLocationOnScreen(loc2);
		// 两点之间的中点
		PointF pointF = new PointF((loc1[0] + loc2[0]) / 2 + radius,
				(loc1[1] + loc2[1]) / 2 + radius);
		return hitValidDot(pointF);
	}

	/**
	 * 清空画面
	 */
	protected void clear() {
		if (canvas != null && paint != null) {
			paint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
			canvas.drawPaint(paint);
			paint.setXfermode(new PorterDuffXfermode(Mode.SRC));
			view.invalidate();
		}
	}

	/**
	 * @return 屏幕宽度
	 */
	public int getScreenWidth() {
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		return metrics.widthPixels;
	}

	/**
	 * @return 屏幕高度
	 */
	public int getScreenHeight() {
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		return metrics.heightPixels;
	}

	/**
	 * @return 返回窗口内容的宽度，不包括通知栏的标题栏，其实跟getScreenWidth()一样
	 */
	public int getWindowWidth() {
		return getWindow().findViewById(Window.ID_ANDROID_CONTENT).getWidth();
	}

	/**
	 * @return 返回窗口内容的高度，不包括通知栏的标题栏，但是在这里是全屏，所以与getScreenHeight()返回的其实是一致的
	 */
	public int getWindowHeight() {
		return getWindow().findViewById(Window.ID_ANDROID_CONTENT).getHeight();
	}

	/**
	 * 圆形
	 */
	public class Dot extends ImageView {

		private int dotradius = 0;// 圆形半径
		private boolean passed = false;// 是否经过的状态

		public Dot(Context context) {
			super(context);
		}

		public Dot(Context context, int rad) {
			super(context);
			dotradius = rad;
			setLayoutParams(new LayoutParams(dotradius * 2, dotradius * 2));
			drawNormal();
		}

		/**
		 * 绘制未经过时的状态
		 */
		public void drawNormal() {
			passed = false;
			Bitmap bm = Bitmap.createBitmap(dotradius * 2, dotradius * 2,
					Config.ARGB_8888);
			Paint paint = new Paint();
			Canvas canvas = new Canvas(bm);
			paint.setAntiAlias(true);
			paint.setARGB(255, 156, 156, 156);
			paint.setStyle(Style.STROKE);
			paint.setStrokeWidth(5);
			canvas.drawCircle(dotradius, dotradius,
					dotradius - paint.getStrokeWidth(), paint);
			paint.setStrokeWidth(1);
			paint.setStyle(Style.FILL_AND_STROKE);
			canvas.drawCircle(dotradius, dotradius, 3, paint);
			setImageBitmap(bm);
		}

		/**
		 * 绘制经过时的状态
		 */
		public void drawPassed() {
			passed = true;
			Bitmap bm = Bitmap.createBitmap(dotradius * 2, dotradius * 2,
					Config.ARGB_8888);
			Paint paint = new Paint();
			Canvas canvas = new Canvas(bm);
			paint.setAntiAlias(true);
			paint.setARGB(255, 0, 0, 255);
			paint.setStyle(Style.STROKE);
			paint.setStrokeWidth(5);
			canvas.drawCircle(dotradius, dotradius,
					dotradius - paint.getStrokeWidth(), paint);
			paint.setStyle(Style.FILL_AND_STROKE);
			canvas.drawCircle(dotradius, dotradius, dotradius / 3, paint);
			setImageBitmap(bm);
		}

		public boolean getPassed() {
			return passed;
		}
	}

}
