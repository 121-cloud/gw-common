package otocloud.gw.common;

import java.nio.charset.StandardCharsets;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

public class AdapterUtils {
	public static Buffer toBufferWithLength(JsonObject jsonObj) {
		byte[] datas = jsonObj.toString().getBytes(StandardCharsets.UTF_8);
		int length = datas.length;
		Buffer buffer = Buffer.buffer();
		byte[] lengthBytes = new byte[4];
		lengthBytes[3] = (byte) length;
		lengthBytes[2] = (byte) (length >>> 8);
		lengthBytes[1] = (byte) (length >>> 16);
		lengthBytes[0] = (byte) (length >>> 24);
		buffer.appendBytes(lengthBytes).appendBytes(datas);
		return buffer;
	}
}
