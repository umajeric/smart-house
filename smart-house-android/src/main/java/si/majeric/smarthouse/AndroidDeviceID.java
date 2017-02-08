package si.majeric.smarthouse;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import android.content.Context;
import android.os.Build;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;

/**
 * 
 * NOTE: "<uses-permission android:name="android.permission.READ_PHONE_STATE" />" required!<br/>
 * <br/>
 * See {@link http://stackoverflow.com/a/2853253/1035514} for more details:<br />
 * All devices tested returned a value for TelephonyManager.getDeviceId()<br/>
 * All GSM devices (all tested with a SIM) returned a value for TelephonyManager.getSimSerialNumber()<br/>
 * All CDMA devices returned null for getSimSerialNumber() (as expected)<br/>
 * All devices with a Google account added returned a value for ANDROID_ID<br/>
 * All CDMA devices returned the same value (or derivation of the same value) for both ANDROID_ID and TelephonyManager.getDeviceId() -- as long as a Google
 * account has been added during setup.<br/>
 * I did not yet have a chance to test GSM devices with no SIM, a GSM device with no Google account added, or any of the devices in airplane mode.<br/>
 * 
 * @author Uros Majeric
 * 
 */
public class AndroidDeviceID {

	public static String getDeviceID(Context context) {
		TelephonyManager TelephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		String deviceId = TelephonyMgr.getDeviceId();

		/* Decide whether we need to create a unique ID or not (if IMEI is null or blank) */
		if (deviceId == null || deviceId.trim().length() == 0) {
			String _m_szDevIDShort = "35"
					+ // we make this look like a valid IMEI
					Build.BOARD.length() % 10 + Build.BRAND.length() % 10 + Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 + Build.DISPLAY.length()
					% 10 + Build.HOST.length() % 10 + Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 + Build.MODEL.length() % 10
					+ Build.PRODUCT.length() % 10 + Build.TAGS.length() % 10 + Build.TYPE.length() % 10 + Build.USER.length() % 10;

			String _m_szAndroidID = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);

			/* use these 2 variables */
			deviceId = _m_szDevIDShort + (_m_szAndroidID != null ? _m_szAndroidID : "");
		}

		return buildSerialNumber(deviceId);
	}

	private static String buildSerialNumber(String deviceId) {
		MessageDigest m = null;
		try {
			m = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			return deviceId;
		}
		m.update(deviceId.getBytes(), 0, deviceId.length());
		byte p_md5Data[] = m.digest();

		String newDeviceId = new String();
		for (int i = 0; i < p_md5Data.length; i++) {
			int b = (0xFF & p_md5Data[i]);
			// if it is a single digit, make sure it have 0 in front (proper padding)
			if (b <= 0xF) {
				newDeviceId += "0";
			}
			// add number to string
			newDeviceId += Integer.toHexString(b);
		}
		newDeviceId = newDeviceId.toUpperCase(Locale.ENGLISH);

		return newDeviceId;
	}
}
