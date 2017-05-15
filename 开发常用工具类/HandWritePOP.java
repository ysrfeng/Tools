package com.zeone.inspection.ui.pop;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.zeone.inspection.R;
import com.zeone.inspection.util.Toasttool;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 手写签名面板
 */
public class HandWritePOP extends Dialog {

	public static String signatureName = "signature.jpg";
	//是否签字 2014-05-05
	private boolean isSigned=false;

	/**手写完毕回调监听接口*/
	public interface OnHandWriteListener {

		/***
		 * 保存手写结果
		 * @param object
		 */
		public void onSaveHandWriteData(Object object);

	}

	static final int BACKGROUND_COLOR = Color.WHITE;
	static final int BRUSH_COLOR = Color.BLACK;
	private PaintView mView;
	/** The index of the current color to use. */
	int mColorIndex;
	private Context context;
	private LayoutParams p ;
	private OnHandWriteListener dialogListener;
	private String mtitle;

	/***
	 * 设置手写签名监听
	 * @param listener
	 */
	public void setOnHandWriteListener(OnHandWriteListener listener){
		this.dialogListener = listener;
	}

	public HandWritePOP(Context context) {
		super(context);
		this.context = context;
		String title = context.getString(R.string.sign_check);
		mtitle = title.replaceAll(":", "");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		requestWindowFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.pop_hand_write);

		p = getWindow().getAttributes();  //获取对话框当前的参数值

		if (context instanceof Activity) {
			DisplayMetrics dm = new DisplayMetrics();
			((Activity) context).getWindowManager().getDefaultDisplay()
					.getMetrics(dm);
			p.height = (int) (dm.heightPixels * 0.7);   //高度设置为屏幕的0.4
			p.width = (int) (dm.widthPixels * 0.95);    //宽度设置为屏幕的0.6
		}else{
			p.height = 300;
			p.width = 400;
		}


		getWindow().setAttributes(p);     //设置生效

		TextView tvTitle = (TextView)findViewById(R.id.tvTitle);
		tvTitle.setText(mtitle);

		mView = new PaintView(context);
		FrameLayout frameLayout = (FrameLayout) findViewById(R.id.tablet_view);
		frameLayout.addView(mView);
		mView.requestFocus();
		Button btnClear = (Button) findViewById(R.id.tablet_clear);
		btnClear.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mView.clear();
				isSigned=false;
			}
		});

		Button btnOk = (Button) findViewById(R.id.tablet_ok);
		btnOk.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					if(isSigned){
						if(dialogListener != null){
							dialogListener.onSaveHandWriteData(mView.getCachebBitmap());
						}
//						Toasttool.MyToast(context,"签字确认成功");
						HandWritePOP.this.dismiss();
					}else{
						Toasttool.MyToast(context,"请先签字确认");
					}


				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		Button btnCancel = (Button)findViewById(R.id.tablet_cancel);
		btnCancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				cancel();
			}
		});
	}


	/**处理手写程序的View*/
	class PaintView extends View {
		private Paint paint;
		private Canvas cacheCanvas;
		private Bitmap cachebBitmap;
		private Path path;// 记录触点的路径
		private float cur_x, cur_y;//记录当前坐标

		public Bitmap getCachebBitmap() {
			return cachebBitmap;
		}

		public PaintView(Context context) {
			super(context);
			init();
		}

		private void init(){
			paint = new Paint();
			paint.setAntiAlias(true);
			paint.setStrokeWidth(3);
			paint.setStyle(Paint.Style.STROKE);
			paint.setColor(Color.BLACK);
			path = new Path();
			cachebBitmap = Bitmap.createBitmap(p.width, (int)(p.height*0.8), Config.ARGB_8888);
			cacheCanvas = new Canvas(cachebBitmap);
			cacheCanvas.drawColor(Color.WHITE);
		}

		/**清空绘图*/
		public void clear() {
			if (cacheCanvas != null) {

				paint.setColor(BACKGROUND_COLOR);
				cacheCanvas.drawPaint(paint);
				paint.setColor(Color.BLACK);
				cacheCanvas.drawColor(Color.WHITE);
				invalidate();
			}
		}


		@Override
		protected void onDraw(Canvas canvas) {
			// canvas.drawColor(BRUSH_COLOR);
			canvas.drawBitmap(cachebBitmap, 0, 0, null);
			canvas.drawPath(path, paint);
		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {

			int curW = cachebBitmap != null ? cachebBitmap.getWidth() : 0;
			int curH = cachebBitmap != null ? cachebBitmap.getHeight() : 0;
			if (curW >= w && curH >= h) {
				return;
			}

			if (curW < w)
				curW = w;
			if (curH < h)
				curH = h;

			Bitmap newBitmap = Bitmap.createBitmap(curW, curH, Bitmap.Config.ARGB_8888);
			Canvas newCanvas = new Canvas();
			newCanvas.setBitmap(newBitmap);
			if (cachebBitmap != null) {
				newCanvas.drawBitmap(cachebBitmap, 0, 0, null);
			}
			cachebBitmap = newBitmap;
			cacheCanvas = newCanvas;
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {

			float x = event.getX();
			float y = event.getY();

			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN: {
					cur_x = x;
					cur_y = y;
					path.moveTo(cur_x, cur_y);
					break;
				}
				case MotionEvent.ACTION_MOVE: {
					path.quadTo(cur_x, cur_y, x, y);
					cur_x = x;
					cur_y = y;
					isSigned=true;
					break;
				}
				case MotionEvent.ACTION_UP: {
					cacheCanvas.drawPath(path, paint);
					path.reset();
					break;
				}
			}

			invalidate();

			return true;
		}
	}

	/**保存签名文件*/
	public String saveFile(String path) {
		ByteArrayOutputStream baos = null;
		String _path = null;
		try {

			//_path = path + System.currentTimeMillis() + ".jpg";
			_path = path + signatureName;//每个工程只能签名一次 所以文件相同
			baos = new ByteArrayOutputStream();
			mView.getCachebBitmap().compress(Bitmap.CompressFormat.JPEG, 100, baos);
			byte[] photoBytes = baos.toByteArray();
			if (photoBytes != null) {
				new FileOutputStream(new File(_path)).write(photoBytes);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (baos != null)
					baos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return _path;
	}

}
