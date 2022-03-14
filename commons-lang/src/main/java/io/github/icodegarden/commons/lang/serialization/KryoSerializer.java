package io.github.icodegarden.commons.lang.serialization;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationHandler;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.DefaultSerializers;
import com.esotericsoftware.kryo.serializers.JavaSerializer;

import de.javakaffee.kryoserializers.ArraysAsListSerializer;
import de.javakaffee.kryoserializers.BitSetSerializer;
import de.javakaffee.kryoserializers.GregorianCalendarSerializer;
import de.javakaffee.kryoserializers.JdkProxySerializer;
import de.javakaffee.kryoserializers.RegexSerializer;
import de.javakaffee.kryoserializers.SynchronizedCollectionsSerializer;
import de.javakaffee.kryoserializers.URISerializer;
import de.javakaffee.kryoserializers.UUIDSerializer;
import de.javakaffee.kryoserializers.UnmodifiableCollectionsSerializer;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class KryoSerializer implements Serializer<Object> {

	private static AbstractKryoFactory kryoFactory = new ThreadLocalKryoFactory();

	@Override
	public byte[] serialize(Object obj) throws SerializationException {
		Kryo kryo = kryoFactory.getKryo();
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(256);

		try (Output output = new Output(byteArrayOutputStream);) {
//			kryo.writeObject(output, obj);
			kryo.writeClassAndObject(output, obj);
			output.flush();
			return byteArrayOutputStream.toByteArray();
		} catch (KryoException e) {
			throw new SerializationException("Error when serializing object to byte[]", e);
		}
	}

	abstract static class AbstractKryoFactory {

		private final Set<Class> registrations = new LinkedHashSet<Class>();

		private boolean registrationRequired;

		private volatile boolean kryoCreated;

		public AbstractKryoFactory() {

		}

		public void registerClass(Class clazz) {

			if (kryoCreated) {
				throw new IllegalStateException("Can't register class after creating kryo instance");
			}
			registrations.add(clazz);
		}

		public Kryo create() {
			if (!kryoCreated) {
				kryoCreated = true;
			}

//	        Kryo kryo = new CompatibleKryo();
			Kryo kryo = new Kryo();

//	        kryo.setReferences(false);
			kryo.setRegistrationRequired(registrationRequired);

			kryo.addDefaultSerializer(Throwable.class, new JavaSerializer());
			kryo.register(Arrays.asList("").getClass(), new ArraysAsListSerializer());
			kryo.register(GregorianCalendar.class, new GregorianCalendarSerializer());
			kryo.register(InvocationHandler.class, new JdkProxySerializer());
			kryo.register(BigDecimal.class, new DefaultSerializers.BigDecimalSerializer());
			kryo.register(BigInteger.class, new DefaultSerializers.BigIntegerSerializer());
			kryo.register(Pattern.class, new RegexSerializer());
			kryo.register(BitSet.class, new BitSetSerializer());
			kryo.register(URI.class, new URISerializer());
			kryo.register(UUID.class, new UUIDSerializer());
			UnmodifiableCollectionsSerializer.registerSerializers(kryo);
			SynchronizedCollectionsSerializer.registerSerializers(kryo);

			kryo.register(HashMap.class);
			kryo.register(ArrayList.class);
			kryo.register(LinkedList.class);
			kryo.register(HashSet.class);
			kryo.register(TreeSet.class);
			kryo.register(Hashtable.class);
			kryo.register(Date.class);
			kryo.register(Calendar.class);
			kryo.register(ConcurrentHashMap.class);
			kryo.register(SimpleDateFormat.class);
			kryo.register(GregorianCalendar.class);
			kryo.register(Vector.class);
			kryo.register(BitSet.class);
			kryo.register(StringBuffer.class);
			kryo.register(StringBuilder.class);
			kryo.register(Object.class);
			kryo.register(Object[].class);
			kryo.register(String[].class);
			kryo.register(byte[].class);
			kryo.register(char[].class);
			kryo.register(int[].class);
			kryo.register(float[].class);
			kryo.register(double[].class);
			kryo.register(double[].class);

			for (Class clazz : registrations) {// 外部的
				kryo.register(clazz);
			}

//	        SerializableClassRegistry.getRegisteredClasses().forEach((clazz, ser) -> {
//	            if (ser == null) {
//	                kryo.register(clazz);
//	            } else {
//	                kryo.register(clazz, (Serializer) ser);
//	            }
//	        });

			return kryo;
		}

		public void setRegistrationRequired(boolean registrationRequired) {
			this.registrationRequired = registrationRequired;
		}

		public abstract void returnKryo(Kryo kryo);

		public abstract Kryo getKryo();
	}

	static class ThreadLocalKryoFactory extends AbstractKryoFactory {

		private final ThreadLocal<Kryo> holder = new ThreadLocal<Kryo>() {
			@Override
			protected Kryo initialValue() {
				return create();
			}
		};

		@Override
		public void returnKryo(Kryo kryo) {
		}

		@Override
		public Kryo getKryo() {
			return holder.get();
		}
	}

}