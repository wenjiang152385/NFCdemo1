package com.senter.demo.hf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import com.senter.demo.hf.commons.MySimpleAdapter;
import com.senter.demo.hf.commons.nfc.NFCBaseActivity;
import com.senter.demo.hf.commons.nfc.NFCs;
import com.senter.demo.hf.commons.nfc.PrivateUtils;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 读卡活动
 * 
 * @author neo
 * 
 */
public class ReaderActivity extends NFCBaseActivity {

	private static final String TITLE = "title";
	private static final String CONTENT = "content";

	protected static final String KEY_SHOW_TODAY = "showTodayTemp";
	protected static final String KEY_VIEW_DETAILS = "viewDetails";
	protected static final String KEY_DETAILS_CONTENT = "viewDetailsContent";

	private static final int WHAT_AFTER_NEW_SCAN = 0x01;
	private static final int WHAT_SAVE_AND_SELECT = 0x02;

	private ArrayList<HashMap<String, String>> readerList;

	private ListView listView;
	private MySimpleAdapter adapter;

	private TextView tipsTextView;

	private MyHandler handler;

	private String uid;
	private String date;
	private boolean isViewDetails;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reader);

		listView = (ListView) findViewById(R.id.reader_list);
		tipsTextView = (TextView) findViewById(R.id.reader_tips);
		tipsTextView.setText(getString(R.string.msg_no_tag_scanned) + "\n\n");

		handler = new MyHandler(ReaderActivity.this);
		readerList = new ArrayList<HashMap<String, String>>();

		Intent intent = getIntent();
		isViewDetails = intent.getBooleanExtra(KEY_VIEW_DETAILS, false);

		if (isViewDetails) {
			try {
				setTitle(R.string.tag_details);
				findViewById(R.id.reader_buttons_layout).setVisibility(View.GONE);
				restoreList(intent.getStringExtra(KEY_DETAILS_CONTENT));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (NFCs.isNFC(intent)) {
			parseNFC(intent);
			isTagDiscovered = true;
		} else {
//			if (null == techMap) {
//				techMap = NFCs.getLastTechMap();
//			}
//
//			if (null == msgsList) {
//				msgsList = NFCs.getLastMsgsList();
//			}

			if (null != techMap && null != msgsList) {
				isTagDiscovered = true;
			} else {
				isTagDiscovered = false;
			}
		}

		if (isTagDiscovered) {
			dump2list();
		}

		if (isTagDiscovered || false == isDBSupported) {//
			listView.setVisibility(View.VISIBLE);
			tipsTextView.setVisibility(View.GONE);
		} else {
			listView.setVisibility(View.GONE);
			tipsTextView.setVisibility(View.VISIBLE);
		}

		adapter = new MySimpleAdapter(ReaderActivity.this, readerList,
				R.layout.listview_reader, new String[] { TITLE, CONTENT },
				new int[] { R.id.list_title, R.id.list_content });

		listView.setAdapter(adapter);
		listView.setCacheColorHint(Color.TRANSPARENT);
		listView.setSelector(android.R.color.transparent);
		listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

		if (isTagDiscovered) {
			handler.sendEmptyMessage(WHAT_SAVE_AND_SELECT);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		isViewDetails = intent.getBooleanExtra(KEY_VIEW_DETAILS, false);

		if (isViewDetails) {
			try {
				restoreList(intent.getStringExtra(KEY_DETAILS_CONTENT));
				handler.sendEmptyMessage(WHAT_AFTER_NEW_SCAN);
				return;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		super.onNewIntent(intent);

		if (isTagDiscovered) {
			dump2list();
			handler.sendEmptyMessage(WHAT_AFTER_NEW_SCAN);
		}
	}

	@Override
	public boolean onKeyDown(	int keyCode, KeyEvent event)
	{
		if (keyCode==KeyEvent.KEYCODE_BACK)
		{
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	
	
	/**
	 * 显示临时内容
	 * 
	 * @param view
	 */
	public void onShowMore(View view) {
		Intent intent = new Intent(ReaderActivity.this, ViewerActivity.class);
		intent.putExtra(KEY_SHOW_TODAY, true);
		ViewerActivity.DEFAULT_INTENT_KEY_VALUE = true;
		startActivity(intent);
	}

	public void onShowAdvance(View view) {
		// [Neo] TODO
	}

	/**
	 * 保存
	 * 
	 * @param view
	 */
	public void onSave(View view) {
		if (false == isTagDiscovered) {
			Toast.makeText(ReaderActivity.this, R.string.msg_no_tag_scanned,
					Toast.LENGTH_LONG).show();
		} else {
			if (date.equals(PrivateUtils.LAST_SAVED_DATE)) {
				Toast.makeText(ReaderActivity.this,
						R.string.msg_data_has_been_saved, Toast.LENGTH_LONG)
						.show();
			} else {
				if (save(true)) {
					Toast.makeText(ReaderActivity.this,
							R.string.msg_tag_has_been_saved, Toast.LENGTH_LONG)
							.show();
				} else {
					Toast.makeText(ReaderActivity.this,
							R.string.msg_tag_has_not_been_saved,
							Toast.LENGTH_LONG).show();
				}
			}

		}
	}

	private static class MyHandler extends Handler {

		private ReaderActivity activity;

		public MyHandler(ReaderActivity activity) {
			this.activity = activity;
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case WHAT_AFTER_NEW_SCAN:
				if (activity.isViewDetails) {
					activity.setTitle(R.string.tag_details);
					activity.findViewById(R.id.reader_buttons_layout)
							.setVisibility(View.GONE);
				} else {
					activity.setTitle(R.string.tag_reader);
					activity.findViewById(R.id.reader_buttons_layout)
							.setVisibility(View.VISIBLE);
				}
				activity.listView.setVisibility(View.VISIBLE);
				activity.tipsTextView.setVisibility(View.GONE);
				activity.adapter.update(activity.readerList);
				activity.handler.sendEmptyMessage(WHAT_SAVE_AND_SELECT);
				break;

			case WHAT_SAVE_AND_SELECT:
				if (activity.isDBSupported
						&& false == activity.isViewDetails
						&& false == activity.date
								.equals(PrivateUtils.LAST_TMP_DATE)) {
					if (false == activity.save(false)) {
						// [Neo] TODO 一般不会来这儿吧
						Toast.makeText(activity, "save to tmp table failed",
								Toast.LENGTH_LONG).show();
					}
				}
				activity.listView.setSelection(0);
				break;

			default:
				break;
			}
			super.handleMessage(msg);
		}
	}

	/**
	 * 界面更新用
	 * 
	 */
	private void dump2list() {
		readerList.clear();
		isDBSupported = true;
		// [Neo] TODO 冒号换空格
		uid = techMap.get("uid").replace(":", " ");
		date = techMap.get("date");
		HashMap<String, String> msgMap = msgsList.get(0);

		boolean isFindOneAtLeast = false;
		HashMap<String, String> map = null;
		StringBuilder sBuilder = new StringBuilder(1024);

		// [Neo] 卡号、协议
		map = new HashMap<String, String>();
		map.put(TITLE, getString(R.string.protocol_info));
		isFindOneAtLeast = true;
		sBuilder.append(getString(R.string.uid)).append(uid).append("\n")
				.append(getString(R.string.uid_dec))
				.append(techMap.get("uid-dec")).append("\n");
		if (techMap.containsKey("ATQA")) {
			sBuilder.append(getString(R.string.atqa)).append("0x")
					.append(techMap.get("ATQA").replace(":", "")).append("\n");
		}
		if (techMap.containsKey("SAK")) {
			sBuilder.append(getString(R.string.sak)).append("0x")
					.append(techMap.get("SAK").replace(":", "")).append("\n");
		}
		if (techMap.containsKey("DSF_ID")) {
			sBuilder.append(getString(R.string.dsf_id)).append("0x")
					.append(techMap.get("DSF_ID")).append("\n");
		}
		if (techMap.containsKey("ProtocolInfo")) {
			sBuilder.append(getString(R.string.nfcb_procotol_info))
					.append(techMap.get("ProtocolInfo")).append("\n");
		}
		if (techMap.containsKey("HistoricalBytes")) {
			sBuilder.append(getString(R.string.historical_bytes))
					.append(techMap.get("HistoricalBytes")).append("\n");
		}
		sBuilder.deleteCharAt(sBuilder.length() - 1);
		map.put(CONTENT, sBuilder.toString());
		readerList.add(map);

		isFindOneAtLeast = false;
		map = new HashMap<String, String>();
		map.put(TITLE, getString(R.string.tech_info));
		sBuilder.delete(0, sBuilder.length());
		if (techMap.containsKey("tech")) {
			String tech = techMap.get("tech");
			if (tech.contains("IsoDep")) {
				sBuilder.append(getString(R.string.isodep_tech));
			}
			if (tech.contains("MifareClassic")) {
				sBuilder.append(getString(R.string.mifare_classic_tech));
			}
			if (tech.contains("MifareUltralight")) {
				sBuilder.append(getString(R.string.mifare_ultralight_tech));
			}
			if (tech.contains("NfcA")) {
				sBuilder.append(getString(R.string.nfca_tech));
			}
			if (tech.contains("NfcB")) {
				sBuilder.append(getString(R.string.nfcb_tech));
			}
			if (tech.contains("NfcF")) {
				sBuilder.append(getString(R.string.nfcf_tech));
			}
			if (tech.contains("NfcV")) {
				sBuilder.append(getString(R.string.nfcv_tech));
			}
			isFindOneAtLeast = true;
		}
		if (isFindOneAtLeast) {
			sBuilder.deleteCharAt(sBuilder.length() - 1);
			map.put(CONTENT, sBuilder.toString());
			readerList.add(map);
		}

		// [Neo] URI
		isFindOneAtLeast = false;
		map = new HashMap<String, String>();
		map.put(TITLE, getString(R.string.uri_record));
		sBuilder.delete(0, sBuilder.length());
		if (msgMap.containsKey("uri")) {
			sBuilder.append(getString(R.string.type))
					.append(msgMap.get("type-ascii")).append("\n")
					.append(getString(R.string.readable_value))
					.append(msgMap.get("uri")).append("\n");
			isFindOneAtLeast = true;
		}
		if (isFindOneAtLeast) {
			sBuilder.deleteCharAt(sBuilder.length() - 1);
			map.put(CONTENT, sBuilder.toString());
			readerList.add(map);
		}

		// [Neo] MIME
		isFindOneAtLeast = false;
		map = new HashMap<String, String>();
		sBuilder.delete(0, sBuilder.length());
		map.put(TITLE, getString(R.string.mime_record));
		if (msgMap.containsKey("mime")) {
			sBuilder.append(getString(R.string.type))
					.append(msgMap.get("type-ascii")).append("\n")
					.append(getString(R.string.mime))
					.append(msgMap.get("mime")).append("\n");
			if (msgMap.get("mime").contains("text/plain")) {
				sBuilder.append(getString(R.string.encoding))
						.append(msgMap.get("encoding")).append("\n")
						.append(getString(R.string.lang))
						.append(msgMap.get("lang")).append("\n")
						.append(getString(R.string.readable_value))
						.append(msgMap.get("text")).append("\n");
			} else {
				sBuilder.append(getString(R.string.raw_bytes)).append("\n")
						.append(msgMap.get("payload-byte")).append("\n");
			}

			isFindOneAtLeast = true;
		}
		if (isFindOneAtLeast) {
			sBuilder.deleteCharAt(sBuilder.length() - 1);
			map.put(CONTENT, sBuilder.toString());
			readerList.add(map);
		}

		// [Neo] 消息、权限
		isFindOneAtLeast = false;
		map = new HashMap<String, String>();
		map.put(TITLE, getString(R.string.set_info));
		sBuilder.delete(0, sBuilder.length());
		if (msgMap.containsKey("raw-size")) {
			if ("D5:00:00".equals(msgMap.get("raw"))) {
				sBuilder.append(getString(R.string.unreadable_or_unauthorized));
			} else {
				sBuilder.append(String.format(
						getString(R.string.current_size_formatter),
						msgMap.get("raw-size")));
			}
			isFindOneAtLeast = true;
		}
		if (techMap.containsKey("MaxSize")) {
			sBuilder.append(String.format(
					getString(R.string.maximum_size_formatter),
					techMap.get("MaxSize")));
			isFindOneAtLeast = true;
		}
		if (techMap.containsKey("isWritable")) {
			sBuilder.append(getString(R.string.data_access));
			if ("false".equals(techMap.get("isWritable"))) {
				sBuilder.append(getString(R.string.read_only));
			} else {
				sBuilder.append(getString(R.string.read_write));
			}
			sBuilder.append("\n");
			isFindOneAtLeast = true;
		}
		if (techMap.containsKey("canMakeReadOnly")) {
			sBuilder.append(getString(R.string.can_read_only)).append("\n");
			isFindOneAtLeast = true;
		}
		if (isFindOneAtLeast) {
			sBuilder.deleteCharAt(sBuilder.length() - 1);
			map.put(CONTENT, sBuilder.toString());
			readerList.add(map);
		}

		// [Neo] 尺寸
		isFindOneAtLeast = false;
		map = new HashMap<String, String>();
		map.put(TITLE, getString(R.string.size_info));
		sBuilder.delete(0, sBuilder.length());
		if (techMap.containsKey("size")) {
			int size = Integer.parseInt(techMap.get("size"));
			sBuilder.append(String.format(getString(R.string.size_formatter),
					size));
			isFindOneAtLeast = true;

			int block = -1;
			if (techMap.containsKey("block")) {
				try {
					block = Integer.parseInt(techMap.get("block"));
				} catch (Exception e) {
					// [Neo] Empty
				}
			}

			int sector = -1;
			if (techMap.containsKey("sector")) {
				try {
					sector = Integer.parseInt(techMap.get("sector"));
					if (sector > 0) {
						if (block > 0) {
							sBuilder.append(String.format(
									getString(R.string.sector_formatter),
									sector, (block / sector)));
						} else {
							sBuilder.append(String.format(
									getString(R.string.sector_only), sector));
						}
					}
				} catch (Exception e) {
					// [Neo] Empty
				}
			}

			if (block > 0) {
				sBuilder.append(String.format(
						getString(R.string.block_formatter), block,
						(size / block)));
			}

		}
		if (isFindOneAtLeast) {
			sBuilder.deleteCharAt(sBuilder.length() - 1);
			map.put(CONTENT, sBuilder.toString());
			readerList.add(map);
		}

	}

	/**
	 * 将当前数据序列化
	 * 
	 * @return
	 * @throws IOException
	 */
	private String dumpList() throws IOException {
		String dump = null;
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ObjectOutputStream outputStream = new ObjectOutputStream(output);
		outputStream.writeObject(readerList);
		dump = URLEncoder.encode(output.toString("ISO-8859-1"), "UTF-8");
		outputStream.close();
		output.close();
		return dump;
	}

	/**
	 * 恢复序列化后的数据
	 * 
	 * @param dummy
	 * @throws StreamCorruptedException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	private void restoreList(String dummy) throws StreamCorruptedException,
			IOException, ClassNotFoundException {
		ByteArrayInputStream input = new ByteArrayInputStream(URLDecoder
				.decode(dummy, "UTF-8").getBytes("ISO-8859-1"));
		ObjectInputStream inputStream = new ObjectInputStream(input);
		isDBSupported = false;
		readerList = (ArrayList<HashMap<String, String>>) inputStream
				.readObject();
		inputStream.close();
		input.close();
	}

	/**
	 * 保存到数据库
	 * 
	 * @param is2db
	 *            是否真正存储
	 * @return
	 */
	private boolean save(boolean is2db) {
		boolean result = false;
		String content = null;

		try {
			content = dumpList();

			if (null == PrivateUtils.SQLS) {
				PrivateUtils.initDB(ReaderActivity.this);
			}

			if (false == is2db) {
				result = PrivateUtils.SQLS
						.execSQL("INSERT INTO tmp(uid, time, content) VALUES ('"
								+ uid
								+ "', datetime('"
								+ date
								+ "', 'unixepoch', 'localtime'), '"
								+ content
								+ "')");
				if (result) {
					PrivateUtils.LAST_TMP_DATE = date;
				}
			} else {
				result = PrivateUtils.SQLS
						.execSQL("INSERT INTO saved(uid, time, content) VALUES ('"
								+ uid
								+ "', datetime('"
								+ date
								+ "', 'unixepoch', 'localtime'), '"
								+ content
								+ "')");
				if (result) {
					PrivateUtils.LAST_SAVED_DATE = date;
				}
			}

			PrivateUtils.SQLS.close();
			PrivateUtils.SQLS = null;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

}
