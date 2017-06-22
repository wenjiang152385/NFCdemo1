package com.senter.demo.hf;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Locale;

import com.senter.demo.hf.commons.nfc.NFCBaseActivity;
import com.senter.demo.hf.commons.nfc.NFCs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

/**
 * 写入活动
 * 
 * @author neo
 *
 */
public class WriterActivity extends NFCBaseActivity {

	private static final byte DEFAULT_URI_TYPE = 0x00;

	private static final int WHAT_SET_FOCUSABLE = 0x01;
	private static final int WHAT_SHOW_ALERTDIALOG = 0x02;
	private static final int WHAT_SHOW_PROGRESSDIALOG = 0x03;
	private static final int WHAT_SHOW_TAG_INFOS = 0x04;
	private static final int WHAT_WRITE_WORKER = 0x05;

	private RadioGroup typeRadioGroup;
	private TextView byteStatusTextView;
	private LinearLayout uriLayout, mimeLayout;
	private EditText uriEditText, typeEditText, asciiEditText, bytesEditText;
	private CheckBox editCheckBox;

	private MyHandler handler;

	private Dialog dialog;
	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_writer);

		typeRadioGroup = (RadioGroup) findViewById(R.id.writer_radio_group);
		byteStatusTextView = (TextView) findViewById(R.id.writer_bytes_status);

		uriLayout = (LinearLayout) findViewById(R.id.writer_uri_layout);
		uriEditText = (EditText) findViewById(R.id.writer_uri_edit);

		mimeLayout = (LinearLayout) findViewById(R.id.writer_mime_layout);
		typeEditText = (EditText) findViewById(R.id.writer_mime_type);
		asciiEditText = (EditText) findViewById(R.id.writer_mime_ascii);
		bytesEditText = (EditText) findViewById(R.id.writer_mime_bytes);
		editCheckBox = (CheckBox) findViewById(R.id.writer_edit_bytes);

		asciiEditText.addTextChangedListener(ascii4byte);

		typeRadioGroup
				.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(RadioGroup group, int id) {
						switch (id) {
						case R.id.writer_radio_uri:
							uriLayout.setVisibility(View.VISIBLE);
							mimeLayout.setVisibility(View.GONE);
							break;

						case R.id.writer_radio_mime:
							uriLayout.setVisibility(View.GONE);
							mimeLayout.setVisibility(View.VISIBLE);
							break;
						}
					}
				});

		editCheckBox
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						asciiEditText.setFocusable(!isChecked);
						asciiEditText.setFocusableInTouchMode(!isChecked);
						bytesEditText.setFocusable(isChecked);
						bytesEditText.setFocusableInTouchMode(isChecked);
					}
				});

		handler = new MyHandler(WriterActivity.this);

		progressDialog = new ProgressDialog(WriterActivity.this,
				ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
		progressDialog.setMessage(getString(R.string.msg_writing_please_wait));

		handler.sendEmptyMessageDelayed(WHAT_SET_FOCUSABLE, 10);

	}

	private static class MyHandler extends Handler {

		private WriterActivity activity;

		public MyHandler(WriterActivity activity) {
			this.activity = activity;
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case WHAT_SET_FOCUSABLE:
				activity.uriEditText.setFocusable(true);
				activity.uriEditText.setFocusableInTouchMode(true);

				activity.typeEditText.setFocusable(true);
				activity.typeEditText.setFocusableInTouchMode(true);

				boolean isChecked = activity.editCheckBox.isChecked();
				activity.asciiEditText.setFocusable(!isChecked);
				activity.asciiEditText.setFocusableInTouchMode(!isChecked);
				activity.bytesEditText.setFocusable(isChecked);
				activity.bytesEditText.setFocusableInTouchMode(isChecked);

				activity.byteStatusTextView.setVisibility(View.GONE);
				break;

			case WHAT_SHOW_ALERTDIALOG:
				if (null != activity.dialog && activity.dialog.isShowing()) {
					activity.dialog.dismiss();
					activity.dialog = null;
				}

				if (null != activity.progressDialog
						&& activity.progressDialog.isShowing()) {
					activity.progressDialog.hide();
				}

				activity.dialog = new AlertDialog.Builder(activity,
						AlertDialog.THEME_DEVICE_DEFAULT_DARK)
						.setTitle(msg.arg1)
						.setMessage(msg.arg2)
						.setPositiveButton(R.string.confirm,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// [Neo] Empty

									}
								}).create();
				activity.dialog.show();
				break;

			case WHAT_SHOW_PROGRESSDIALOG:
				activity.progressDialog.show();
				break;

			case WHAT_SHOW_TAG_INFOS:
				if (null != activity.dialog && activity.dialog.isShowing()) {
					activity.dialog.dismiss();
				}

				if (null != activity.progressDialog
						&& activity.progressDialog.isShowing()) {
					activity.progressDialog.hide();
				}

				activity.dialog = new AlertDialog.Builder(activity,
						AlertDialog.THEME_DEVICE_DEFAULT_DARK)
						.setTitle(R.string.tag_info)
						.setMessage(msg.obj.toString())
						.setPositiveButton(R.string.confirm,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// [Neo] Empty

									}
								}).create();
				activity.dialog.show();
				break;

			case WHAT_WRITE_WORKER:
				activity.new WriteWorker().start();
				break;

			default:
				break;
			}
			super.handleMessage(msg);
		}

	}

	public void onWrite(View view) {
		handler.sendEmptyMessage(WHAT_SHOW_PROGRESSDIALOG);
		handler.sendEmptyMessageDelayed(WHAT_WRITE_WORKER, 255);
	}

	public void onShowAdvance(View view) {
		// [Neo] TODO
	}

	public void onBackup(View view) {
		// [Neo] TODO
	}

	public void onBack(View view) {
		finish();
	}

	@Override
	protected void onDestroy() {
		if (null != dialog && dialog.isShowing()) {
			dialog.dismiss();
			dialog = null;
		}

		if (null != progressDialog) {
			progressDialog.dismiss();
			progressDialog = null;
		}

		super.onDestroy();

		NFCs.clear();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		if (isTagDiscovered) {
			if (null != dialog && dialog.isShowing()) {
				dialog.dismiss();
				dialog = null;
			}

			dialog = new AlertDialog.Builder(WriterActivity.this,
					AlertDialog.THEME_DEVICE_DEFAULT_DARK)
					.setTitle(R.string.tips)
					.setMessage(R.string.msg_got_tag_in_writer)
					.setPositiveButton(R.string.confirm,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									new Thread() {
										@Override
										public void run() {
											StringBuilder sBuilder = new StringBuilder(
													1024);
											if (null != techMap) {
												if (techMap.containsKey("uid")) {
													sBuilder.append(
															getString(R.string.uid))
															.append(techMap
																	.get("uid")
																	.replace(
																			":",
																			" "))
															.append("\n");
												}
											}
											if (null != msgsList) {
												HashMap<String, String> msgMap = msgsList
														.get(0);
												if (msgMap.containsKey("uri")) {
													sBuilder.append(
															getString(R.string.type))
															.append(msgMap
																	.get("type-ascii"))
															.append("\n")
															.append(getString(R.string.readable_value))
															.append(msgMap
																	.get("uri"))
															.append("\n");
												} else if (msgMap
														.containsKey("mime")) {
													sBuilder.append(
															getString(R.string.type))
															.append(msgMap
																	.get("type-ascii"))
															.append("\n")
															.append(getString(R.string.mime))
															.append(msgMap
																	.get("mime"))
															.append("\n");
													if (msgMap
															.get("mime")
															.contains(
																	"text/plain")) {
														sBuilder.append(
																getString(R.string.encoding))
																.append(msgMap
																		.get("encoding"))
																.append(", ")
																.append(getString(R.string.lang))
																.append(msgMap
																		.get("lang"))
																.append("\n")
																.append(getString(R.string.readable_value))
																.append(msgMap
																		.get("text"))
																.append("\n");
													} else {
														sBuilder.append(
																getString(R.string.raw_bytes))
																.append("\n")
																.append(msgMap
																		.get("payload-byte"))
																.append("\n");
													}
												}
											}

											sBuilder.deleteCharAt(sBuilder
													.length() - 1);
											handler.sendMessage(handler
													.obtainMessage(
															WHAT_SHOW_TAG_INFOS,
															0, 0,
															sBuilder.toString()));
										}
									}.start();
								}
							})
					.setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// [Neo] Empty

								}
							}).create();

			dialog.show();
		}
	}

	private TextWatcher ascii4byte = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			try {
				bytesEditText.setText(NFCs.b2str(asciiEditText.getText()
						.toString().getBytes("UTF-8"), true));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// [Neo] Empty
		}

		@Override
		public void afterTextChanged(Editable s) {
			// [Neo] Empty
		}
	};

	private class WriteWorker extends Thread {
		@Override
		public void run() {
			NdefMessage msg = null;

			if (NFCs.isTagConnecting()) {
				switch (typeRadioGroup.getCheckedRadioButtonId()) {
				case R.id.writer_radio_uri:
					String uri = uriEditText.getText().toString();
					if (0 == uri.length()) {
						handler.sendMessage(handler.obtainMessage(
								WHAT_SHOW_ALERTDIALOG, R.string.error,
								R.string.msg_write_without_uri_content));
					} else {
						uri = uri.trim();
						try {
							new URI(uri);
							msg = NFCs.genMsg(NFCs.genWellKnownURIRecord(
									DEFAULT_URI_TYPE, uri));
						} catch (URISyntaxException e) {
							e.printStackTrace();
							handler.sendMessage(handler.obtainMessage(
									WHAT_SHOW_ALERTDIALOG, R.string.error,
									R.string.msg_write_failed_with_bad_uri));
						}
					}
					break;

				case R.id.writer_radio_mime:
					String mimeType = typeEditText.getText().toString().trim();
					String mimeAscii = asciiEditText.getText().toString()
							.trim();
					String mimeBytes = bytesEditText.getText().toString()
							.trim();

					if (0 == mimeType.length()) {
						handler.sendMessage(handler.obtainMessage(
								WHAT_SHOW_ALERTDIALOG, R.string.error,
								R.string.msg_write_without_mime_type));
					} else {
						if (editCheckBox.isChecked()) {
							if (0 == mimeBytes.length()) {
								handler.sendMessage(handler.obtainMessage(
										WHAT_SHOW_ALERTDIALOG, R.string.error,
										R.string.msg_write_without_mime_bytes));
							} else {
								if (getString(R.string.plain_text).equals(
										typeEditText.getText().toString())) {
									msg = NFCs.genMsg(NFCs
											.genWellKnownTextRecord(
													NFCs.str2b(mimeBytes),
													Locale.CHINESE, true));
								} else {
									msg = NFCs.genMIMEMsg(mimeType,
											NFCs.str2b(mimeBytes));
								}
							}
						} else {
							if (0 == mimeAscii.length()) {
								handler.sendMessage(handler.obtainMessage(
										WHAT_SHOW_ALERTDIALOG, R.string.error,
										R.string.msg_write_without_mime_ascii));
							} else {
								if (getString(R.string.plain_text).equals(
										typeEditText.getText().toString())) {
									try {
										msg = NFCs
												.genMsg(NFCs
														.genWellKnownTextRecord(
																mimeAscii
																		.getBytes("UTF-8"),
																Locale.CHINESE,
																true));
									} catch (UnsupportedEncodingException e) {
										e.printStackTrace();
										handler.sendMessage(handler
												.obtainMessage(
														WHAT_SHOW_ALERTDIALOG,
														R.string.error,
														R.string.msg_bad_encoding));
									}
								} else {
									try {
										msg = NFCs.genMIMEMsg(mimeType,
												mimeAscii.getBytes("UTF-8"));
									} catch (UnsupportedEncodingException e) {
										e.printStackTrace();
										handler.sendMessage(handler
												.obtainMessage(
														WHAT_SHOW_ALERTDIALOG,
														R.string.error,
														R.string.msg_bad_encoding));
									}
								}
							}
						}
					}
					break;
				}

				if (null != msg) {
					if (NFCs.write(msg)) {
						handler.sendMessage(handler.obtainMessage(
								WHAT_SHOW_ALERTDIALOG, R.string.tips,
								R.string.msg_write_success));
					} else {
						handler.sendMessage(handler.obtainMessage(
								WHAT_SHOW_ALERTDIALOG, R.string.error,
								R.string.msg_write_failed));
					}
				}

			} else {
				handler.sendMessage(handler.obtainMessage(
						WHAT_SHOW_ALERTDIALOG, R.string.error,
						R.string.msg_no_tag_scanned));
			}
		}
	}

}
