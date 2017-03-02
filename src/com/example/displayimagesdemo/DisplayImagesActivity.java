package com.example.displayimagesdemo;

import java.io.IOException;
import java.io.InputStream;
import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

public class DisplayImagesActivity extends Activity {
	private AssetManager assets = null;
	private String[] images = null;
	private int currentImg = 0;
	private ImageView image;
	private Button btnStart;
	private Button btnStop;
	// 定义一个负责更新图片的Handler
	private Handler handler = null;
	private Thread thread = null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏
		setContentView(R.layout.activity_display_images);
		//初始化视图
		onInitView();
		//获取assets下图片
		images = getImages();
		//displayAssets();
	}

	private void onInitView(){
		image = (ImageView) findViewById(R.id.image);
		btnStart = (Button) findViewById(R.id.btnStart);
		btnStart.setOnClickListener(listener);

		btnStop = (Button) findViewById(R.id.btnStop);
		btnStop.setOnClickListener(listener);
		
		handler = new Handler() {
			public void handleMessage(android.os.Message msg) {
				// 表明消息是该程序发出的
				if (msg.what == 0x110) {
					// 展示下一张图片
					dispalyNextImage();
				}
			};
		};
	}
	
	private String[] getImages(){
		String[] tempImages = null;
		try {
			assets = getAssets();
			// 获取/assets/目录下所有文件
			if(null!=assets){
				tempImages = assets.list("");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			return tempImages;
		}
	}
	
	View.OnClickListener listener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (v == btnStart) {
				if(thread==null){
					thread = new Thread() {
						@Override
						public void run() {
							Thread curThread = Thread.currentThread();
							while (thread!=null && thread == curThread) {
								try {
									Thread.sleep(1000);
									Message msg = new Message();
									msg.what = 0x110;
									handler.sendMessage(msg);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						}
					};
					thread.start();
				}
			} else if (v == btnStop) {
				Thread temp = thread;
				thread = null;
				temp.interrupt();
			}
		}
	};

	// 展示assets内容
	private void displayAssets() {
		int length = images.length;
		String str = null;
		for (int i = 0; i < length; i++) {
			str = images[i];
			System.out.println(i + "=" + str);
		}
	}

	// 展示下一张图片
	private void dispalyNextImage() {
		// 如果发生数组越界
		if (currentImg >= images.length) {
			currentImg = 0;
		}
		//备注1
		// 找到下一个图片文件
		while (!images[currentImg].endsWith(".png")
				&& !images[currentImg].endsWith(".jpg")
				&& !images[currentImg].endsWith(".gif")) {
			currentImg++;
			// 如果已发生数组越界
			if (currentImg >= images.length) {
				currentImg = 0;
			}
		}

		InputStream assetFile = null;
		try {
			// 打开指定资源对应的输入流
			assetFile = assets.open(images[currentImg++]);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		BitmapDrawable bitmapDrawable = (BitmapDrawable) image.getDrawable();
		//备注2
		// 如果图片还未回收，先强制回收该图片
		if (bitmapDrawable != null && !bitmapDrawable.getBitmap().isRecycled()){
			bitmapDrawable.getBitmap().recycle();
		}
		// 改变ImageView显示的图片
		image.setImageBitmap(BitmapFactory.decodeStream(assetFile));
	}
}