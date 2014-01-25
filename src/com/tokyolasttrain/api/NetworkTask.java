package com.tokyolasttrain.api;

import java.io.IOException;

import com.tokyolasttrain.TokyoLastTrain;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

abstract public class NetworkTask<Params, Progress, Result>
extends AsyncTask<Params, Progress, Result>
{

	public static interface OnCompleteListener<Result> {
		public void onComplete(Result result);
	}

	public static interface OnIOExceptionListener {
		public void onIOException(IOException exception);
	}

	public static interface OnExceptionListener {
		public void onException(Exception exception);
	}

	public static interface OnNetworkUnavailableListener {
		public void onNetworkException(NetworkErrorException exception);
	}

	private Exception exception;
	private IOException ioException;

	private boolean mIsComplete = false;
	public boolean isComplete() {
		return mIsComplete;
	}

	private boolean mIsAborted = false;
	public boolean isAborted() {
		return mIsAborted;
	}

	private OnCompleteListener<Result> completeListener;
	public void setOnCompleteListener(OnCompleteListener<Result> completeListener) {
		this.completeListener = completeListener;
	}

	private OnExceptionListener exceptionListener;
	public void setOnExceptionListener(OnExceptionListener l) {
		this.exceptionListener = l;
	}

	private OnExceptionListener genericExceptionListener;
	/**
	 * This listener gets called if any error happens. It's a convenience method to
	 * catch all the errors in 1 shot.
	 * @param l
	 */
	public void setOnGenericExceptionListener(OnExceptionListener l) {
		this.genericExceptionListener = l;
	}

	private OnIOExceptionListener ioExceptionListener;
	public void setOnIOExceptionListener(OnIOExceptionListener l) {
		this.ioExceptionListener = l;
	}

	private OnNetworkUnavailableListener networkUnavailableListener;
	public void setOnNetworkUnavailableListener(
			OnNetworkUnavailableListener networkUnavailableListener) {
		this.networkUnavailableListener = networkUnavailableListener;
	}

	public NetworkTask() {
		super();
	}

	@SuppressWarnings("unchecked")
	public void execute() {
		execute(null, null);
	}

	public void abort() {
		mIsAborted = true;
		cancel(true);
	}


	/**
	 * This is where we make the network call. We're not passing object here, so this method must get the params
	 * it needs from the class properties. Since this is thread be sure to make as volatile if needed.
	 *
	 * @return
	 * @throws OmwException
	 * @throws Exception
	 */
	abstract protected Result doNetworkAction() throws IOException;

	/**
	 * This method runs on the UI Thread.
	 * Use this hook for what happens when the doNetworkAction method returns successfully.
	 *
	 * @param result The result from doNetworkAction
	 */
	protected void onPostSuccess(Result result) { }
	protected void onPostFault(Exception e) { }

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		mIsComplete = false;
		mIsAborted = false;
		boolean hasNetworkConnection = hasInternetAccess(TokyoLastTrain.getAppContext());
		if (!hasNetworkConnection) {
			if (networkUnavailableListener != null) {
				networkUnavailableListener.onNetworkException(new NetworkErrorException("Internet connection unavailable"));
			}
			abort();
		}
	}

	/**
	 * Mostly likely you should not override this. It's not marked as final, but treat it like that.
	 */
	@Override
	protected Result doInBackground(Params... params) {
		if (isCancelled()) {
			return null;
		}
		try {
			return doNetworkAction();
		} catch (IOException e) {
			ioException = e;
			return null;
		} catch (Exception e) {
			exception = e;
			return null;
		}
	}

	/**
	 * Out logic to figure what kind of result we got.
	 */
	@Override
	protected void onPostExecute(Result result) {
		super.onPostExecute(result);
		mIsComplete= true;
		if (isCancelled() || isAborted()) {
			return;
		}

		if (ioException != null) {
			onPostFault(ioException);
			if (ioExceptionListener != null) ioExceptionListener.onIOException(ioException);
			if (genericExceptionListener != null) genericExceptionListener.onException(ioException);
		} else if (exception != null) {
			onPostFault(exception);
			if (exceptionListener != null) exceptionListener.onException(exception);
			if (genericExceptionListener != null) genericExceptionListener.onException(exception);
		}

		// SUCCESS!
		else {
			onPostSuccess(result);
			if (completeListener != null) {
				completeListener.onComplete(result);
			}
		}
	}

	public static boolean hasInternetAccess(Context context) {
		boolean hasInternet = false;
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
			hasInternet = true;
		}

		return hasInternet;
	}
}