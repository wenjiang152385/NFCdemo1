package com.senter.demo.hf.commons.nfc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;

/**
 * NFC 工具类
 * 
 * @author neo
 * 
 */
public class NFCs {

	private static int id = 0;
	private static Tag lastTag;
	private static HashMap<String, String> techMap;
	private static ArrayList<HashMap<String, String>> msgsList;

	private static final String[] TECHES = new String[] {
			IsoDep.class.getName(), MifareClassic.class.getName(),
			MifareUltralight.class.getName(), Ndef.class.getName(),
			NfcA.class.getName(), NfcB.class.getName(), NfcF.class.getName(),
			NfcV.class.getName(), };

	/**
	 * 判断是否是 NFC 意图
	 * 
	 * @param intent
	 * @return
	 */
	public static boolean isNFC(Intent intent) {
		String action = intent.getAction();
		boolean result = false;

		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)
				|| NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
				|| NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
			result = true;
		}

		return result;
	}

	/**
	 * 清空静态变量
	 * 
	 */
	public static void clear() {
		lastTag = null;

		if (null != techMap) {
			techMap.clear();
			techMap = null;
		}

		if (null != msgsList) {
			msgsList.clear();
			msgsList = null;
		}
	}

	/**
	 * 获取上一次技术细节
	 * 
	 * @return
	 */
	public static HashMap<String, String> getLastTechMap() {
		if (false == isMapSyncList()) {
			techMap = null;
		}
		return techMap;
	}

	/**
	 * 获取上一次消息内容
	 * 
	 * @return
	 */
	public static ArrayList<HashMap<String, String>> getLastMsgsList() {
		if (false == isMapSyncList()) {
			msgsList = null;
		}
		return msgsList;
	}

	/**
	 * 判断消息与技术信息是否同步
	 * 
	 * @return
	 */
	private static boolean isMapSyncList() {
		boolean result = false;
		if (null != techMap && null != msgsList) {
			int id = Integer.parseInt(techMap.get("id"));
			for (int i = 0; i < msgsList.size(); i++) {
				if (id != Integer.parseInt(msgsList.get(i).get("id"))) {
					result = false;
					break;
				} else {
					result = true;
				}
			}
		}
		return result;
	}

	/**
	 * 判断当前的卡片是否处于连接状态
	 * 
	 * @return
	 */
	public static boolean isTagConnecting() {
		boolean result = false;
		if (null != lastTag) {
			String[] techList = lastTag.getTechList();
			for (int i = 0; i < techList.length; i++) {
				for (int j = 0; j < TECHES.length; j++) {
					if (techList[i].equals(TECHES[j])) {
						switch (j) {
						case 0:
							IsoDep dep = IsoDep.get(lastTag);
							try {
								dep.connect();
								result = dep.isConnected();
								dep.close();
							} catch (IOException e) {
								if (null != dep) {
									try {
										dep.close();
									} catch (IOException e1) {
										e1.printStackTrace();
									}
								}
								e.printStackTrace();
							}
							break;

						case 1:
							MifareClassic mifareTag = MifareClassic
									.get(lastTag);
							try {
								mifareTag.connect();
								result = mifareTag.isConnected();
								mifareTag.close();
							} catch (IOException e) {
								if (null != mifareTag) {
									try {
										mifareTag.close();
									} catch (IOException e1) {
										e1.printStackTrace();
									}
								}
								e.printStackTrace();
							}
							break;

						case 2:
							MifareUltralight mifareUTag = MifareUltralight
									.get(lastTag);
							try {
								mifareUTag.connect();
								result = mifareUTag.isConnected();
								mifareUTag.close();
							} catch (IOException e) {
								try {
									mifareUTag.close();
								} catch (IOException e1) {
									e1.printStackTrace();
								}
								e.printStackTrace();
							}
							break;

						case 3:
							Ndef defTag = Ndef.get(lastTag);
							try {
								defTag.connect();
								result = defTag.isConnected();
								defTag.close();
							} catch (IOException e) {
								try {
									defTag.close();
								} catch (IOException e1) {
									e1.printStackTrace();
								}
								e.printStackTrace();
							}
							break;

						case 4:
							NfcA aTag = NfcA.get(lastTag);
							try {
								aTag.connect();
								result = aTag.isConnected();
								aTag.close();
							} catch (IOException e) {
								try {
									aTag.close();
								} catch (IOException e1) {
									e1.printStackTrace();
								}
								e.printStackTrace();
							}
							break;

						case 5:
							NfcB bTag = NfcB.get(lastTag);
							try {
								bTag.connect();
								result = bTag.isConnected();
								bTag.close();
							} catch (IOException e) {
								try {
									bTag.close();
								} catch (IOException e1) {
									e1.printStackTrace();
								}
								e.printStackTrace();
							}
							break;

						case 6:
							NfcF fTag = NfcF.get(lastTag);
							try {
								fTag.connect();
								result = fTag.isConnected();
								fTag.close();
							} catch (IOException e) {
								try {
									fTag.close();
								} catch (IOException e1) {
									e1.printStackTrace();
								}
								e.printStackTrace();
							}
							break;

						case 7:
							NfcV vTag = NfcV.get(lastTag);
							try {
								vTag.connect();
								result = vTag.isConnected();
								vTag.close();
							} catch (IOException e) {
								try {
									vTag.close();
								} catch (IOException e1) {
									e1.printStackTrace();
								}
								e.printStackTrace();
							}
							break;
						}

						break;
					}
				}

				if (false != result) {
					break;
				}
			}
		}

		return result;
	}

	/**
	 * 生成技术信息
	 * 
	 * @param tag
	 * @return
	 */
	public static HashMap<String, String> dumpTag(Tag tag) {
		lastTag = null;
		techMap = null;
		lastTag = tag;

		id++;
		techMap = new HashMap<String, String>();
		techMap.put("id", String.valueOf(id));
		techMap.put("uid", b2str(tag.getId()));
		techMap.put("uid-dec", b2no(tag.getId()));
		techMap.put("date", String.valueOf(System.currentTimeMillis() / 1000));

		StringBuilder sBuilder = new StringBuilder(100);
		String[] techList = tag.getTechList();

		for (int i = 0; i < techList.length; i++) {
			for (int j = 0; j < TECHES.length; j++) {
				if (techList[i].equals(TECHES[j])) {
					switch (j) {
					case 0:
						IsoDep dep = IsoDep.get(tag);
						techMap.put("HistoricalBytes",
								b2str(dep.getHistoricalBytes()));
						techMap.put("isExtendedLengthSupported", String
								.valueOf(dep.isExtendedLengthApduSupported()));
						techMap.put("IsoDepMaxTransceiveLength",
								String.valueOf(dep.getMaxTransceiveLength()));
						techMap.put("IsoDepTimeout",
								String.valueOf(dep.getTimeout()));
						break;

					case 1:
						MifareClassic mifareTag = MifareClassic.get(tag);
						techMap.put("size", String.valueOf(mifareTag.getSize()));
						techMap.put("sector",
								String.valueOf(mifareTag.getSectorCount()));
						techMap.put("block",
								String.valueOf(mifareTag.getBlockCount()));
						techMap.put("MifareClassicMaxTransceiveLength", String
								.valueOf(mifareTag.getMaxTransceiveLength()));
						techMap.put("MifareClassicTimeout",
								String.valueOf(mifareTag.getTimeout()));
						techMap.put("MifareClassicType",
								getMCType(mifareTag.getType()));
						break;

					case 2:
						MifareUltralight mifareUTag = MifareUltralight.get(tag);
						techMap.put("MifareUltralightMaxTransceiveLength",
								String.valueOf(mifareUTag
										.getMaxTransceiveLength()));
						techMap.put("MifareUltralightTimeout",
								String.valueOf(mifareUTag.getTimeout()));
						techMap.put("MifareUltralightType",
								getMUType(mifareUTag.getType()));
						break;

					case 3:
						Ndef defTag = Ndef.get(tag);
						techMap.put("canMakeReadOnly",
								String.valueOf(defTag.canMakeReadOnly()));
						techMap.put("MaxSize",
								String.valueOf(defTag.getMaxSize()));
						techMap.put("type", defTag.getType());
						techMap.put("isWritable",
								String.valueOf(defTag.isWritable()));
						break;

					case 4:
						NfcA aTag = NfcA.get(tag);
						techMap.put("ATQA", b2str(aTag.getAtqa()));
						techMap.put(
								"SAK",
								b2str(new byte[] { (byte) (aTag.getSak() & 0xFF) }));
						techMap.put("NfcAMaxTransceiveLength",
								String.valueOf(aTag.getMaxTransceiveLength()));
						techMap.put("NfcATimeout",
								String.valueOf(aTag.getTimeout()));
						break;

					case 5:
						NfcB bTag = NfcB.get(tag);
						techMap.put("ApplicationData",
								b2str(bTag.getApplicationData()));
						techMap.put("ProtocolInfo",
								b2str(bTag.getProtocolInfo()));
						techMap.put("NfcBMaxTransceiveLength",
								String.valueOf(bTag.getMaxTransceiveLength()));
						break;

					case 6:
						NfcF fTag = NfcF.get(tag);
						techMap.put("Manufacturer",
								b2str(fTag.getManufacturer()));
						techMap.put("SystemCode", b2str(fTag.getSystemCode()));
						techMap.put("NfcFMaxTransceiveLength",
								String.valueOf(fTag.getMaxTransceiveLength()));
						techMap.put("NfcFTimeout",
								String.valueOf(fTag.getTimeout()));
						break;

					case 7:
						NfcV vTag = NfcV.get(tag);
						techMap.put("DSF_ID",
								b2str(new byte[] { vTag.getDsfId() }));
						techMap.put("ResponseFlags",
								b2str(new byte[] { vTag.getResponseFlags() }));
						techMap.put("NfcVMaxTransceiveLength",
								String.valueOf(vTag.getMaxTransceiveLength()));
						break;
					}

					sBuilder.append(
							TECHES[j].substring("android.nfc.tech.".length()))
							.append("/");
					break;
				}
			}
		}

		techMap.put("tech",
				sBuilder.toString().substring(0, sBuilder.length() - 1));
		return techMap;
	}

	/**
	 * 生成消息内容
	 * 
	 * @param msgs
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static ArrayList<HashMap<String, String>> dumpMsgs(NdefMessage[] msgs) {
		msgsList = null;
		msgsList = new ArrayList<HashMap<String, String>>();

		if (null != msgs && msgs.length != 0) {
			HashMap<String, String> map = null;
			for (int i = 0; i < msgs.length; i++) {
				for (final NdefRecord record : msgs[i].getRecords()) {
					map = new HashMap<String, String>();
					map.put("id", String.valueOf(id));
					map.put("type-byte", b2str(record.getType()));
					map.put("type-ascii", new String(record.getType()));
					map.put("describe-contents",
							String.valueOf(record.describeContents()));
					map.put("tnf", String.valueOf(record.getTnf()));
					map.put("payload-byte", b2str(record.getPayload()));
					map.put("raw", b2str(record.toByteArray()));
					map.put("raw-size",
							String.valueOf(record.toByteArray().length));

					if (null != record.toMimeType()) {
						String mime = record.toMimeType();
						map.put("mime", mime);
						if (mime.contains("text/plain")) {
							int gap = 0;
							String charsetName = "UTF-8";
							byte[] bytes = record.getPayload();
							if (NdefRecord.TNF_WELL_KNOWN == record.getTnf()) {
								gap = bytes[0] & 127;
								map.put("lang", new String(bytes, 1, gap,
										Charset.forName(charsetName)));
								gap++;

								if (1 << 7 == (bytes[0] | 1 << 7)) {
									charsetName = "UTF-16";
								}
								map.put("encoding", charsetName);
							}
							map.put("text", new String(bytes, gap, bytes.length
									- gap, Charset.forName(charsetName)));
						} else if (mime.contains("x")) {
							// [Neo] TODO 解析其他特定 MIME
						}
					}

					if (null != record.toUri()) {
						map.put("uri", record.toUri().toString());
					}

					msgsList.add(map);
				}
			}
		}

		return msgsList;
	}

	/**
	 * 写入消息
	 * 
	 * @param message
	 * @return
	 */
	public static boolean write(NdefMessage message) {
		boolean result = false;
		Ndef ndef = Ndef.get(lastTag);
		if (null != ndef) {
			try {
				ndef.connect();
				if (ndef.isConnected() && ndef.isWritable()
						&& ndef.getMaxSize() > message.toByteArray().length) {
					ndef.writeNdefMessage(message);
					result = true;
				}
				ndef.close();
			} catch (Exception e) {
				try {
					ndef.close();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
			}
		} else {
			NdefFormatable formatable = NdefFormatable.get(lastTag);
			if (null != formatable) {
				try {
					formatable.connect();
					formatable.format(message);
					result = true;
					formatable.close();
				} catch (Exception e) {
					try {
						formatable.close();
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	public static NdefMessage genMIMEMsg(String mimeType, byte[] mimeData) {
		return new NdefMessage(new NdefRecord[] { NdefRecord.createMime(
				mimeType, mimeData) });
	}

	public static NdefMessage genURIMsg(String uriString) {
		return new NdefMessage(
				new NdefRecord[] { NdefRecord.createUri(uriString) });
	}

	public static NdefMessage genAppRecordMsg(String packageName) {
		return new NdefMessage(
				new NdefRecord[] { NdefRecord
						.createApplicationRecord(packageName) });
	}

	public static NdefMessage genMsg(NdefRecord record) {
		return new NdefMessage(new NdefRecord[] { record });
	}

	/**
	 * 生成标准文本记录
	 * 
	 * @param textBytes
	 * @param locale
	 * @param isUTF8
	 * @return
	 */
	public static NdefRecord genWellKnownTextRecord(byte[] textBytes,
			Locale locale, boolean isUTF8) {
		int utfBit = isUTF8 ? 0 : (1 << 7);
		byte[] langBytes = locale.getLanguage().getBytes();
		byte[] data = new byte[1 + langBytes.length + textBytes.length];
		data[0] = (byte) (utfBit + langBytes.length);
		System.arraycopy(langBytes, 0, data, 1, langBytes.length);
		System.arraycopy(textBytes, 0, data, 1 + langBytes.length,
				textBytes.length);
		return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT,
				new byte[0], data);
	}

	/**
	 * 生成标准 URI 记录
	 * 
	 * @param uriType
	 * @param uriString
	 * @return
	 * @throws URISyntaxException
	 */
	public static NdefRecord genWellKnownURIRecord(byte uriType,
			String uriString) throws URISyntaxException {
		byte[] uriField = encodeURI(uriString).getBytes();
		byte[] payload = new byte[uriField.length + 1];
		if (uriType > 32 || uriType < 0) {
			uriType = 0;
		}

		payload[0] = uriType;
		System.arraycopy(uriField, 0, payload, 1, uriField.length);
		return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_URI,
				new byte[0], payload);
	}

	/**
	 * 特定的 URL 字符串转字节数组
	 * 
	 * @param input
	 * @return
	 */
	public static byte[] str2b(String input) {
		String dummy = null;
		ByteArrayOutputStream output = new ByteArrayOutputStream(
				(int) (input.length() / 2));
		Matcher matcher = grep("(\\d+)?(\\D+)?", input);
		while (matcher.find()) {
			dummy = matcher.group(1);
			if (null != dummy) {
				try {
					output.write((byte) Integer.parseInt(dummy));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return output.toByteArray();
	}

	/**
	 * 通用字节数组转字符串
	 * 
	 * @param delim
	 * @param bytes
	 * @param lengthPerLine
	 * @param isDec
	 * @return
	 */
	public static String b2str(String delim, byte[] bytes, int lengthPerLine,
			boolean isDec) {
		StringBuilder result = null;

		if (null == delim || 0 == delim.length()) {
			delim = ":";
		}

		if (lengthPerLine > 0) {
			result = new StringBuilder((int) ((6 * bytes.length + 10)
					* bytes.length / lengthPerLine));
		} else {
			result = new StringBuilder(4 * bytes.length);
		}

		for (int i = 0; i < bytes.length; i++) {
			int b = bytes[i] & 0xFF;

			if (b < 0x10 && false == isDec) {
				result.append('0');
			}

			if (false == isDec) {
				result.append(Integer.toHexString(b));
			} else {
				result.append(b);
			}

			if (lengthPerLine > 0) {
				if (0 == (i + 1) % lengthPerLine) {
					result.append("\n");
				} else {
					if (0 == (i + 1) % 8) {
						result.append(" ");
					} else {
						result.append(delim);
					}
				}
			} else {
				if (0 == (i + 1) % 8 && -1 != lengthPerLine) {
					result.append(" ");
				} else {
					result.append(delim);
				}
			}
		}

		if (result.length() > 0) {
			result.deleteCharAt(result.length() - 1);
		}

		return result.toString().toUpperCase(Locale.ENGLISH);
	}

	/**
	 * 字节数组转字符串，十进制
	 * 
	 * @param bytes
	 * @param isDec
	 * @return
	 */
	public static String b2str(byte[] bytes, boolean isDec) {
		return b2str("-", bytes, -1, true);
	}

	/**
	 * 常规字节数组转字符串，8 个字节换一行
	 * 
	 * @param bytes
	 * @return
	 */
	public static String b2str(byte[] bytes) {
		return b2str(":", bytes, 8, false);
	}

	/**
	 * 字节数组转十进制大整数
	 * 
	 * @param bytes
	 * @return
	 */
	public static String b2no(byte[] bytes) {
		String no = null;
		if (bytes.length > 7) {
			// [Neo] more speed
			byte[] result = new byte[bytes.length + 1];
			result[0] = 0x0;
			for (int i = 1; i <= bytes.length; i++) {
				result[i] = bytes[bytes.length - i];
			}
			no = new BigInteger(result).toString();
		} else {
			long result = 0;
			for (int i = bytes.length - 1; i >= 0; i--) {
				result += bytes[i] & 0xFF;
				if (i != 0) {
					result <<= 8;
				}
			}
			no = String.valueOf(result);
		}
		return no;
	}

	private static String getMCType(int type) {
		String typeString = "bad type";
		switch (type) {
		case MifareClassic.TYPE_CLASSIC:
			typeString = "Classic";

		case MifareClassic.TYPE_PLUS:
			typeString = "Plus";

		case MifareClassic.TYPE_PRO:
			typeString = "Pro";

		case MifareClassic.TYPE_UNKNOWN:
			typeString = "unknown";

		}

		return typeString;
	}

	private static String getMUType(int type) {
		String typeString = "bad type";
		switch (type) {
		case MifareUltralight.TYPE_ULTRALIGHT:
			typeString = "Ultralight";

		case MifareUltralight.TYPE_ULTRALIGHT_C:
			typeString = "Ultralight C";

		case MifareUltralight.TYPE_UNKNOWN:
			typeString = "unkown";

		}

		return typeString;
	}

	/**
	 * 简单的正则
	 * 
	 * @param reg
	 *            正则模式字符串
	 * @param content
	 *            待匹配的内容
	 * @return 匹配结果集对象
	 */
	public static Matcher grep(String reg, String content) {
		return Pattern.compile(reg).matcher(content);
	}

	/**
	 * 转义非 ASCII 字符的 URI
	 * 
	 * @param uri
	 *            源内容
	 * @return 转义后的结果
	 */
	public static String encodeURI(String uri) {
		Matcher matcher = grep("([\\x21-\\x7E]+)?([^\\x21-\\x7E]+)?", uri);
		String dummy = null;
		StringBuilder sBuilder = new StringBuilder(uri.length() * 4);
		while (matcher.find()) {
			if (2 == matcher.groupCount()) {
				dummy = matcher.group(1);
				if (null != dummy) {
					sBuilder.append(dummy);
				}
				dummy = matcher.group(2);
				if (null != dummy) {
					try {
						sBuilder.append(URLEncoder.encode(dummy, "UTF-8"));
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
			}
		}

		return sBuilder.toString();
	}

}
