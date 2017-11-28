package com.ubi.excel;


import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class Misc {
		
		public static final char[] ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
				.toCharArray();
		
		public static final synchronized String uuid() {
			return base58Uuid();
		}
		public static String base58Uuid() {
	        UUID uuid = UUID.randomUUID();
	        return base58Uuid(uuid);
	    }

	    protected static String base58Uuid(UUID uuid) {

	        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
	        bb.putLong(uuid.getMostSignificantBits());
	        bb.putLong(uuid.getLeastSignificantBits());

	        return Misc.encode(bb.array());
	    }
	    
	    /**
		 * Encodes the given bytes in base58. No checksum is appended.
		 */
		public static String encode(byte[] input) {
			if (input.length == 0) {
				return "";
			}
			input = copyOfRange(input, 0, input.length);
			// Count leading zeroes.
			int zeroCount = 0;
			while (zeroCount < input.length && input[zeroCount] == 0) {
				++zeroCount;
			}
			// The actual encoding.
			byte[] temp = new byte[input.length * 2];
			int j = temp.length;

			int startAt = zeroCount;
			while (startAt < input.length) {
				byte mod = divmod58(input, startAt);
				if (input[startAt] == 0) {
					++startAt;
				}
				temp[--j] = (byte) ALPHABET[mod];
			}

			// Strip extra '1' if there are some after decoding.
			while (j < temp.length && temp[j] == ALPHABET[0]) {
				++j;
			}
			// Add as many leading '1' as there were leading zeros.
			while (--zeroCount >= 0) {
				temp[--j] = (byte) ALPHABET[0];
			}

			byte[] output = copyOfRange(temp, j, temp.length);
			try {
				return new String(output, "US-ASCII");
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e); // Cannot happen.
			}
		}
		
		private static byte[] copyOfRange(byte[] source, int from, int to) {
			byte[] range = new byte[to - from];
			System.arraycopy(source, from, range, 0, range.length);

			return range;
		}
		//
		// number -> number / 58, returns number % 58
		//
		private static byte divmod58(byte[] number, int startAt) {
			int remainder = 0;
			for (int i = startAt; i < number.length; i++) {
				int digit256 = (int) number[i] & 0xFF;
				int temp = remainder * 256 + digit256;

				number[i] = (byte) (temp / 58);

				remainder = temp % 58;
			}

			return (byte) remainder;
		}
		

		/**
		 * 判断日期
		 * @param date
		 * @param format
		 * @return
		 */
		public final static String getDate(Date date, String format) {
			if (date == null) {
				return null;
			}
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			return sdf.format(date);
		}
		//判断是不是今天
		  public static boolean isNowDay(Date date) {
		        //当前时间
		        Date now = new Date();
		        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
		        //获取今天的日期
		        String nowDay = sf.format(now);
		         
		         
		        //对比的时间
		        String day = sf.format(date);
		         
		        return day.equals(nowDay);
		         
		         
		         
		    }
		
	}

