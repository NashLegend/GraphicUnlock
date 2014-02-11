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

	private RelativeLayout relativeLayout;// �����ڷžŸ�Բ��
	private ImageView view;// �������ƽ���·��
	private Path path;// ������·��
	private Paint paint;
	private Canvas canvas;
	private Dot[] array = new Dot[9];// Բ�ε�����
	private Dot lastDot;// ��һ�������ĵ�
	private Bitmap bitmap;// �����õ�bitmap
	private boolean drawing = false;// �Ƿ����ڻ�ͼ
	private int radius = 0;// Բ�ΰ뾶

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// ��������
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		// ����ʾ������
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// ȫ��
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);
		relativeLayout = (RelativeLayout) findViewById(R.id.rela);
		view = (ImageView) findViewById(R.id.view);
		view.setOnTouchListener(this);
		drawDots();
	}

	/**
	 * ���þŸ�Բ�� ���Ÿ�Բ������Ļ�о��з��ã�ÿ��Ļ������֮һ���Ϊһ�񣬺����Ÿ�������ÿ��Բ�������Ļ��ȵ�1/6
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
				// �½��뾶Ϊradius��Բ��
				Dot dot = new Dot(this, radius);
				array[i * 3 + j] = dot;
				relativeLayout.addView(dot, params);
			}
		}
	}

	/**
	 * ���pointF�Ƿ���ĳ��Բ�η�Χ��
	 * 
	 * @param point
	 *            Ҫ���ĵ�
	 * @return ���ȷʵ��ĳ��Բ�η�Χ�ڣ��򷵻ظ�Բ�Σ���֮����null
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
	 * Ҫ���Ƶ���Ŀ��ͼƬ�ϵĴ����¼� ��������view.invalidate()�����Ǳ���ģ���û��һ������
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			// ����ֻ����µĵ��Ƿ���ĳ��Բ���ڣ���������Դ�Բ��Ϊ��㿪ʼ����ͼ��
			PointF point = new PointF(event.getRawX(), event.getRawY());
			Dot dot = hitValidDot(point);
			if (dot != null) {
				// ��ʼ���� ��ʵ����Ҫ���Ƶ�bitmap canvas paint �ͻ��Ƶ�·��path
				bitmap = Bitmap.createBitmap(getWindowWidth(),
						getWindowHeight(), Config.ARGB_8888);
				canvas = new Canvas(bitmap);
				paint = new Paint();
				path = new Path();
				// ��ȡ��Բ�����ĵ��λ��
				RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) dot
						.getLayoutParams();
				PointF startPoint = new PointF(params.leftMargin + radius,
						params.topMargin + radius);
				// ��loasDot��ֵ��dot������dot����Ϊ����״̬
				lastDot = dot;
				lastDot.drawPassed();
				// ��Բ�ε����ĵ�����Ϊ·������� ������Ҫ����·������ɫ�Ŀ��
				path.moveTo(startPoint.x, startPoint.y);
				paint.setARGB(255, 0, 0, 255);
				paint.setStrokeWidth(8);
				paint.setStyle(Style.STROKE);
				// ���Ƶ���Ļ
				view.setImageBitmap(bitmap);
				// ���Ϊ���ڻ�ͼ��
				drawing = true;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (drawing) {
				// �����ͼƬ ���򿴵�����ÿ�λ��Ƶĵ���Ч��
				clear();
				// ͬMotionEvent.ACTION_DOWN��һ�� ����Ƿ񾭹���ĳһ��
				PointF point2 = new PointF(event.getRawX(), event.getRawY());
				Dot dot2 = hitValidDot(point2);
				if (dot2 != null) {
					// ������ʱ������֮����ܻ��е������㣬�����������Ϊ�Ǿ���״̬���򽫴˵�����Ϊ����״̬
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
				// ���Ƴ����������е��·��
				canvas.drawPath(path, paint);

				// ���Ƴ���һ���㵽��ָ������λ�õ�·��
				RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) lastDot
						.getLayoutParams();
				canvas.drawLine(params.leftMargin + radius, params.topMargin
						+ radius, event.getX(), event.getY(), paint);
				view.invalidate();
			}
			break;
		case MotionEvent.ACTION_UP:
			if (drawing) {
				// ��ָ̧�����ղ����»������о����ĵ��·���������ͻ������һ���㵽��ָ������λ�õ�·����
				clear();
				canvas.drawPath(path, paint);
				view.invalidate();
				// ������ϣ�������״̬��Ϊfalse
				drawing = false;

				// �����ֺ����ã������������Ϊ�˲������ù���
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
	 * ��������Ϊ��ʼ״̬
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
	 * �������֮���Ƿ񾭹������㣬������򷵻ص����㣬���򷵻�null
	 */
	protected Dot checkDotBetween(Dot dot1, Dot dot2) {
		int[] loc1 = { 0, 0 };
		int[] loc2 = { 0, 0 };
		dot1.getLocationOnScreen(loc1);
		dot2.getLocationOnScreen(loc2);
		// ����֮����е�
		PointF pointF = new PointF((loc1[0] + loc2[0]) / 2 + radius,
				(loc1[1] + loc2[1]) / 2 + radius);
		return hitValidDot(pointF);
	}

	/**
	 * ��ջ���
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
	 * @return ��Ļ���
	 */
	public int getScreenWidth() {
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		return metrics.widthPixels;
	}

	/**
	 * @return ��Ļ�߶�
	 */
	public int getScreenHeight() {
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		return metrics.heightPixels;
	}

	/**
	 * @return ���ش������ݵĿ�ȣ�������֪ͨ���ı���������ʵ��getScreenWidth()һ��
	 */
	public int getWindowWidth() {
		return getWindow().findViewById(Window.ID_ANDROID_CONTENT).getWidth();
	}

	/**
	 * @return ���ش������ݵĸ߶ȣ�������֪ͨ���ı�������������������ȫ����������getScreenHeight()���ص���ʵ��һ�µ�
	 */
	public int getWindowHeight() {
		return getWindow().findViewById(Window.ID_ANDROID_CONTENT).getHeight();
	}

	/**
	 * Բ��
	 */
	public class Dot extends ImageView {

		private int dotradius = 0;// Բ�ΰ뾶
		private boolean passed = false;// �Ƿ񾭹���״̬

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
		 * ����δ����ʱ��״̬
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
		 * ���ƾ���ʱ��״̬
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
