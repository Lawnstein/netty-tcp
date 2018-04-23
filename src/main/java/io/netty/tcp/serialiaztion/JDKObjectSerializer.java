/**
 * netty-tcp.
 * Copyright (C) 1999-2017, All rights reserved.
 *
 * This program and the accompanying materials are under the terms of the Apache License Version 2.0.
 */
package io.netty.tcp.serialiaztion;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * JDK序列化.
 * 
 * @author Lawnstein.Chan
 * @version $Revision:$
 */
public class JDKObjectSerializer implements ObjectSerializable {

	/**
	 * 序列化
	 * 
	 * @param t
	 * @return
	 */
	public static byte[] serializing(Object t) {
		ObjectOutputStream out = null;
		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			out = new ObjectOutputStream(bout);
			out.writeObject(t);
			out.flush();
			byte[] target = bout.toByteArray();
			return target;
		} catch (IOException e) {
			throw new RuntimeException(
					"JDKMsgSerializer serialize " + (t == null ? "nvl" : t.getClass()) + " exception", e);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
				}
			}
		}
	}

	/**
	 * 反序列化
	 * 
	 * @param bytes
	 * @return
	 */
	public static <T> T deserializing(byte[] bytes) {
		try {
			ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes));
			return (T) in.readObject();
		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException("JDKMsgSerializer deserialize exception", e);
		}
	}

	public static void main(String[] args) {
		Map m = new HashMap();
		m.put("a", "a");
		m.put("b", "b");
		m.put("c", "c");
		System.out.println(m);
		byte[] out = JDKObjectSerializer.serializing(m);
		System.out.println("outBytes.size=" + out.length + ":" + new String(out));
		Object n = JDKObjectSerializer.deserializing(out);
		System.out.println(n);
	}

	@Override
	public byte[] serialize(Object t) {		
		return serializing(t);
	}

	@Override
	public <T> T deserialize(byte[] bytes) {		
		return deserializing(bytes);
	}
}
