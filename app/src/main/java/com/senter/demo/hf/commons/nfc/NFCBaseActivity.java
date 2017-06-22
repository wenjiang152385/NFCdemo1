package com.senter.demo.hf.commons.nfc;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Parcelable;

/**
 * NFC 基础类
 * 
 * @author neo
 *
 */
@SuppressLint("Registered")
public class NFCBaseActivity extends Activity {

	protected NfcAdapter adapter;
	protected boolean isDBSupported;
	protected boolean isTagDiscovered;

	protected HashMap<String, String> techMap;
	protected ArrayList<HashMap<String, String>> msgsList;

	private PendingIntent pendingIntent;
	private IntentFilter[] intentFilters;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		isDBSupported = true;
		adapter = NfcAdapter.getDefaultAdapter(this);
		pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		intentFilters = new IntentFilter[] {};
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (null != adapter) {
			adapter.disableForegroundDispatch(this);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (null != adapter) {
			adapter.enableForegroundDispatch(this, pendingIntent,
					intentFilters, null);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		isTagDiscovered = NFCs.isNFC(intent);
		if (isTagDiscovered) {
			parseNFC(intent);
		}
	}

	/**
	 * 解析 NFC 意图
	 * 
	 * @param intent
	 */
	protected void parseNFC(Intent intent) {
		// [Neo] 给用户通知
		notifyUser();
		
		techMap = NFCs.dumpTag((Tag) intent
				.getParcelableExtra(NfcAdapter.EXTRA_TAG));

		NdefMessage[] msgs = null;
		Parcelable[] rawMsgs = intent
				.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

		if (rawMsgs != null) {
			msgs = new NdefMessage[rawMsgs.length];
			for (int i = 0; i < rawMsgs.length; i++) {
				msgs[i] = (NdefMessage) rawMsgs[i];
			}
		} else {
			msgs = new NdefMessage[] { new NdefMessage(
					new NdefRecord[] { new NdefRecord(NdefRecord.TNF_UNKNOWN,
							new byte[0], new byte[0], new byte[0]) }) };
		}

		msgsList = NFCs.dumpMsgs(msgs);
	}

	private void notifyUser() {
		try {
			new MakeSomeNoise(getAssets().openFd("notify.mp3")
					.getFileDescriptor()).start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static class MakeSomeNoise extends Thread {

		private FileDescriptor fileDescriptor;

		public MakeSomeNoise(FileDescriptor fileDescriptor) {
			this.fileDescriptor = fileDescriptor;
		}

		@Override
		public void run() {
			try {
				MediaPlayer player = new MediaPlayer();
				player.setDataSource(fileDescriptor);
				player.prepare();
				player.setOnCompletionListener(new OnCompletionListener() {
					
					@Override
					public void onCompletion(MediaPlayer mp) {
						mp.reset();
						mp.release();
					}
				});
				player.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

}
