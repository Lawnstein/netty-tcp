/**
 * netty-tcp.
 * Copyright (C) 1999-2017, All rights reserved.
 *
 * This program and the accompanying materials are under the terms of the Apache License Version 2.0.
 */
package io.netty.tcp.serialiaztion;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import io.netty.tcp.util.CommUtil;

/**
 * Kryo虚拟化.
 * 
 * @author Lawnstein.Chan
 * @version $Revision:$
 */
public class KryoObjectSerializer implements ObjectSerializable {
	public static final int MIN_BUFFSIZE = 1024 * 10; /* 10K */
	public static final int MID_BUFFSIZE = 1024 * 1024 * 10; /* 10M */
	public static final int MAX_BUFFSIZE = 1024 * 1024 * 1024; /* 1G */
	public static final int SHORT_BYTES_LENGTH = CommUtil.SHORT_BYTES_LENGTH;
	public static final int INT_BYTES_LENGTH = CommUtil.INT_BYTES_LENGTH;
	public static final int LONG_BYTES_LENGTH = CommUtil.LONG_BYTES_LENGTH;

	private static ThreadLocal<Kryo> threadLocalKryo = new ThreadLocal<Kryo>() {
		protected Kryo initialValue() {
			Kryo kryo = new Kryo();
			kryo.register(List.class);
			kryo.register(ArrayList.class);
			kryo.register(Map.class);
			kryo.register(HashMap.class);
			kryo.register(String.class);
			kryo.register(byte.class);
			kryo.register(short.class);
			kryo.register(Short.class);
			kryo.register(int.class);
			kryo.register(Integer.class);
			kryo.register(long.class);
			kryo.register(Long.class);
			kryo.register(float.class);
			kryo.register(Float.class);
			kryo.register(double.class);
			kryo.register(Double.class);
			kryo.register(BigDecimal.class);
			return kryo;
		};
	};

	public static Kryo getKryo() {
		return threadLocalKryo.get();
	}

	/**
	 * 序列化
	 * 
	 * @param t
	 * @return
	 */
	public static byte[] serializing(Object t) {
		Kryo kryo = getKryo();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Output output = new Output(MIN_BUFFSIZE, MAX_BUFFSIZE);
		output.setOutputStream(baos);
		kryo.writeClassAndObject(output, t);
		output.flush();
		output.close();
		byte[] b = baos.toByteArray();
		try {
			baos.flush();
			baos.close();
		} catch (IOException e) {
			throw new RuntimeException("close ByteArrayOutputStream on serialize IOException", e);
		}
		return b;
	}

	/**
	 * 反序列化
	 * 
	 * @param bytes
	 * @return
	 */
	public static <T> T deserializing(byte[] bytes) {
		Kryo kryo = getKryo();
		// Input input = new Input(MID_BUFFSIZE);
		Input input = new Input();
		input.setBuffer(bytes);
		T t = (T) kryo.readClassAndObject(input);
		input.close();
		return t;
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
