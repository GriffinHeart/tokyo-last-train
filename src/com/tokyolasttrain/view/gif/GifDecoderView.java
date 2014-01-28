package com.tokyolasttrain.view.gif;

import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.widget.ImageView;

public class GifDecoderView extends ImageView
{
	private GifDecoder _gifDecoder;
	private Bitmap _tmpBitmap;
	private boolean _isPlaying = false;
	
	final Handler _handler = new Handler();
	final Runnable _updateResults = new Runnable()
	{
		public void run()
		{
			if (_tmpBitmap != null && !_tmpBitmap.isRecycled())
			{
				GifDecoderView.this.setImageBitmap(_tmpBitmap);
			}
		}
	};

	public GifDecoderView(Context context, InputStream stream)
	{
		super(context);
		playGif(stream);
	}

	private void playGif(InputStream stream)
	{
		_gifDecoder = new GifDecoder();
		_gifDecoder.read(stream);

		_isPlaying = true;

		new Thread(new Runnable()
		{
			public void run()
			{
				final int n = _gifDecoder.getFrameCount();
				final int ntimes = _gifDecoder.getLoopCount();
				int repetitionCounter = 0;
				do
				{
					for (int i = 0; i < n; i++)
					{
						_tmpBitmap = _gifDecoder.getFrame(i);
						int t = _gifDecoder.getDelay(i);
						_handler.post(_updateResults);
						try
						{
							Thread.sleep(t);
						}
						catch (InterruptedException e)
						{
							e.printStackTrace();
						}
					}
					if(ntimes != 0)
					{
						repetitionCounter ++;
					}
				}
				while (_isPlaying && (repetitionCounter <= ntimes));
			}
		}).start();
	}

	public void stopRendering()
	{
		_isPlaying = true;
	}
}