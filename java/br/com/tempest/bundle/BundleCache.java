package br.com.tempest.bundle;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonObject;

public class BundleCache {

	private static Map<Locale, Properties> bundle = new HashMap<Locale, Properties>();

	public static Properties getBundle(Locale locale) {
		if (bundle.get(locale) == null) {
			bundle.put(locale, loadBundle(locale));
		}
		return bundle.get(locale);
	}

	public static String getBundleMessage(Locale locale, String key) {
		Properties props = getBundle(locale);
		String ret = props.getProperty(key);
		return StringUtils.isEmpty(ret) ? "???" + key + "???" : ret;
	}

	public static JsonObject getBundleAsJson(Locale locale) {
		Properties props = getBundle(locale);

		JsonObject json = new JsonObject();
		for (Map.Entry<Object, Object> entry : props.entrySet()) {
			json.addProperty((String) entry.getKey(), (String) entry.getValue());
		}
		return json;
	}

	private static Properties loadBundle(Locale locale) {
		Properties msgProps = new Properties();
		boolean carregado = loadProperties(msgProps, "messages-" + locale.getLanguage() + "_" + locale.getCountry() + ".properties");
		if (!carregado) {
			loadProperties(msgProps, "messages-" + locale.getLanguage() + ".properties");
		}
		if (!carregado) {
			loadProperties(msgProps, "messages.properties");
		}
		return msgProps;
	}

	private static boolean loadProperties(Properties p, String name) {
		try {
			InputStream is = BundleCache.class.getClassLoader().getResourceAsStream(name);
			if (is == null) {
				return false;
			} else {
				p.load(is);
				return true;
			}
		} catch (IOException e) {
			return false;
		}
	}
}