package com.senter.demo.hf;

import java.util.ArrayList;
import java.util.HashMap;

import com.senter.demo.hf.commons.MySimpleAdapter;
import com.senter.demo.hf.commons.nfc.NFCs;
import com.senter.demo.hf.commons.nfc.PrivateUtils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 查看记录活动
 * 
 * @author neo
 *
 */
public class ViewerActivity extends Activity {

	private static final int WHAT_INIT_DB = 0x01;
	private static final int WHAT_CLOSE_DB = 0x02;
	private static final int WHAT_QUERY_DETIALS = 0x03;
	private static final int WHAT_CLEAR_DATA = 0x04;
	private static final int WHAT_TAG_404 = 0x05;

	private static final int WHAT_UPDATE_LISTVIEW = 0x11;

	private static final String UID = "uid";
	private static final String TIME = "time";

	protected static boolean DEFAULT_INTENT_KEY_VALUE;

	private ArrayList<HashMap<String, String>> infosList;

	private ListView listView;
	private MySimpleAdapter adapter;

	private TextView tipsTextView;

	private MyHandler handler;

	private String scopeName;
	private int listViewSelected;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_viewer);

		listView = (ListView) findViewById(R.id.viewer_list);
		tipsTextView = (TextView) findViewById(R.id.viewer_tips);
		tipsTextView.setText(getString(R.string.msg_no_data_in_saved_table)
				+ "\n\n");

		DEFAULT_INTENT_KEY_VALUE = false;
		handler = new MyHandler(ViewerActivity.this);

		listView.setCacheColorHint(Color.TRANSPARENT);
		listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				listViewSelected = position;
				handler.sendEmptyMessage(WHAT_QUERY_DETIALS);
			}
		});

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (getIntent().getBooleanExtra(ReaderActivity.KEY_SHOW_TODAY,
				DEFAULT_INTENT_KEY_VALUE)) {
			scopeName = PrivateUtils.TABLE_TMP;
			setTitle(R.string.tmp_scan_data);
		} else {
			scopeName = PrivateUtils.TABLE_SAVED;
			setTitle(R.string.data_viewer);
		}

		handler.sendEmptyMessage(WHAT_INIT_DB);
	}

	@Override
	protected void onPause() {
		super.onPause();
		DEFAULT_INTENT_KEY_VALUE = false;
	}

	public void onClear(View view) {
		handler.sendEmptyMessage(WHAT_CLEAR_DATA);
	}

	public void onBack(View view) {
		handler.sendEmptyMessage(WHAT_CLOSE_DB);
		finish();
	}

	@Override
	public void onBackPressed() {
		handler.sendEmptyMessage(WHAT_CLOSE_DB);
		super.onBackPressed();
	}

	private static class MyHandler extends Handler {

		private ViewerActivity activity;

		public MyHandler(ViewerActivity activity) {
			this.activity = activity;
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case WHAT_INIT_DB:
				PrivateUtils.initDB(activity);
				activity.infosList = PrivateUtils.SQLS.select2list(
						"SELECT uid, time FROM " + activity.scopeName, null);
				activity.handler.sendEmptyMessage(WHAT_UPDATE_LISTVIEW);
				break;

			case WHAT_UPDATE_LISTVIEW:
				if (null != activity.infosList && activity.infosList.size() > 0) {
					if (null == activity.adapter) {
						activity.adapter = new MySimpleAdapter(activity,
								activity.infosList, R.layout.listview_viewer,
								new String[] { UID, TIME }, new int[] {
										R.id.list_uid, R.id.list_time });
						activity.listView.setAdapter(activity.adapter);
					} else {
						activity.adapter.update(activity.infosList);
					}
					// [Neo] Hey, important here
					activity.listView.setVisibility(View.VISIBLE);
					activity.tipsTextView.setVisibility(View.GONE);
				} else {
					activity.listView.setVisibility(View.GONE);
					activity.tipsTextView.setVisibility(View.VISIBLE);
				}
				break;

			case WHAT_CLOSE_DB:
				if (null != PrivateUtils.SQLS) {
					new Thread() {
						@Override
						public void run() {
							PrivateUtils.SQLS.close();
							PrivateUtils.SQLS = null;
						}
					}.start();
				}
				break;

			case WHAT_QUERY_DETIALS:
				new Thread() {
					@Override
					public void run() {
						PrivateUtils.initDB(activity);
						HashMap<String, String> map = activity.infosList
								.get(activity.listViewSelected);
						ArrayList<HashMap<String, String>> target = PrivateUtils.SQLS
								.select2list("SELECT content FROM "
										+ activity.scopeName + " WHERE uid = '"
										+ map.get("uid") + "' and time = '"
										+ map.get("time") + "'", null);
						if (null != target && target.size() == 1) {
							Intent intent = new Intent(activity,
									ReaderActivity.class);
							intent.putExtra(ReaderActivity.KEY_VIEW_DETAILS,
									true);
							intent.putExtra(ReaderActivity.KEY_DETAILS_CONTENT,
									target.get(0).get("content"));
							activity.startActivity(intent);
						} else {
							activity.handler.sendEmptyMessage(WHAT_TAG_404);
						}
						activity.handler.sendEmptyMessage(WHAT_CLOSE_DB);
					}
				}.start();
				break;

			case WHAT_CLEAR_DATA:
				new Thread() {
					@Override
					public void run() {
						if (null != activity.infosList) {
							activity.infosList.clear();
						}

						if (PrivateUtils.TABLE_SAVED.equals(activity.scopeName)) {
							PrivateUtils.SQLS.execSQL("DELETE FROM "
									+ activity.scopeName);
						} else {
							PrivateUtils.SQLS.execSQL("DELETE FROM "
									+ PrivateUtils.TABLE_TMP);
						}

						NFCs.clear();
					}
				}.start();
				activity.listView.setVisibility(View.GONE);
				activity.tipsTextView.setVisibility(View.VISIBLE);
				Toast.makeText(activity, R.string.msg_clear_done,
						Toast.LENGTH_LONG).show();
				break;

			case WHAT_TAG_404:
				// [Neo] TODO 小概率事件
				Toast.makeText(activity, R.string.msg_view_tag_404,
						Toast.LENGTH_LONG).show();
				activity.infosList.remove(activity.listViewSelected);

				if (activity.infosList.size() == 0) {
					activity.listView.setVisibility(View.GONE);
					activity.tipsTextView.setVisibility(View.VISIBLE);
				} else {
					activity.adapter.update(activity.infosList);
				}
				break;

			default:
				break;
			}

			super.handleMessage(msg);
		}
	}

}
