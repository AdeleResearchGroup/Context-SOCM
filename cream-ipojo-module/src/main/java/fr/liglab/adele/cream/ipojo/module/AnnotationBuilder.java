package fr.liglab.adele.cream.ipojo.module;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * An annotation builder is a visitor that allows to create at compile time proxy object to represent
 * the annotation. This allows compile-time tools to use similar processing to handle annotations as
 * the one used at runtime by doing reflection.   
 *
 * The proxy is not however completely equivalent to a run time instance of the annotation. In particular,
 * it cannot be compared or used as a key in a map. 
 *
 * Notice, that the usage of a proxy implies that the compile-time tool will depend on the actual run-time
 * annotations, this is the main constraint for using this class.
 *
 */
public class AnnotationBuilder<A extends Annotation> extends AnnotationVisitor {

	/**
	 * The type of the annotation that we are loading
	 */
	private final Class<? extends A> 	annotationType;

	/**
	 * The type of the different elements of the annotation type
	 */
	private final Map<String,Class<?>>	types;

	/**
	 * The list of values read from the annotation (values are automatically casted on-the-fly to the appropriate
	 * type according to the signature of the annotation type elements)
	 */
	private final Map<String, Object> 	values;

	/**
	 * An optional processor that will be invoked as soon as the proxy is build
	 */
	private final Consumer<A>			consumer;

	/**
	 * An optional class loader that will be used to load referenced classes
	 */
	private final ClassLoader			loader;

	/**
	 * The proxy created to represent the annotation (wrapping the collected values)
	 */
	private A							proxy;


	public AnnotationBuilder(Class<? extends A> annotationType) {
		this(annotationType, (Consumer<A>)null);
	}

	public AnnotationBuilder(Class<? extends A> annotationType, Consumer<A> consumer) {
		this(annotationType, new ClassReferenceLoader(annotationType.getClassLoader()),consumer);
	}

	public AnnotationBuilder(Class<? extends A> annotationType, ClassLoader loader, Consumer<A> consumer) {

		super(Opcodes.ASM5);

		this.annotationType = annotationType;

		this.types			= new HashMap<>();
		this.values			= new HashMap<>();
        
        /*
         * Get the type of the element from the methods of the annotation type
         */
		for (Method method : annotationType.getDeclaredMethods()) {

			if (method.getParameterCount() != 0) {
				continue;
			}

			String name 		= method.getName();
			Class<?> type		= method.getReturnType();
			Object defaultValue = method.getDefaultValue();

			types.put(name,type);
			if (defaultValue != null) {
				values.put(name,defaultValue);
			}

		}

		this.consumer		= consumer;
		this.loader			= loader;
	}

	@Override
	public String toString() {
		return "Annotation proxy for "+annotationType.getName()+" "+values;
	}

	/**
	 * Set the value of the specified element to a value of the type defined in the annotation type
	 *
	 */
	private void setValue(String element, Object value) {
		values.put(element,cast(types.get(element),value));
	}

	/**
	 * Casts the element value returned by the visitor to the type defined in the annotation type
	 *
	 */
	private final Object cast(Class<?> type, Object value) {

		Object cast = value;

		if (Enum.class.isAssignableFrom(type))
			cast =  castEnumConstant(type, (String) value);

		if (Class.class.isAssignableFrom(type)) {
			cast = castClassReference((Type) value);
		}

		return cast;
	}

	private static final <E extends Enum<E>> E castEnumConstant(Class<?> clazz, String value) {
		@SuppressWarnings("unchecked") Class<E> enumerationType  = (Class<E>) clazz;
		return Enum.valueOf(enumerationType, value);
	}

	private final Class<?>castClassReference(Type value) {
		try {
			return loader.loadClass(value.getClassName());
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(e);
		}
	}


	@Override
	public void visit(String name, Object value) {
		setValue(name,value);
	}

	@Override
	public  void visitEnum(String name, String desc, String value) {
		setValue(name,value);
	}

	@Override
	public AnnotationVisitor visitAnnotation(String name, String desc) {
		return nestedAnnotation(name, types.get(name));
	}

	private <T extends Annotation> AnnotationBuilder<T> nestedAnnotation(String element, Class<?> clazz) {
		@SuppressWarnings("unchecked") Class<T> annotationClass = (Class<T>) clazz;
		return new AnnotationBuilder<>(annotationClass, this.loader, (T proxyNested) -> this.setValue(element,proxyNested));
	}

	@Override
	public AnnotationVisitor visitArray(String name) {
		return this.new ArrayCollector(name);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void visitEnd() {

		/*
		 * Create the proxy from the collected values 
		 */
		proxy = (A) Proxy.newProxyInstance(annotationType.getClassLoader(),
				new Class []{annotationType, Annotation.class},
				this.new Handler());

		if (consumer != null){
			consumer.accept(proxy);
		}
	}

	public A getAnnotation() {
		return proxy;
	}

	/**
	 * The proxy handler for the annotation instance. Method invocations on the annotation interface are
	 * interpreted as requests for the stored value of the builder.
	 *
	 */
	private class Handler implements InvocationHandler, Annotation {

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if ( values.containsKey(method.getName()) ) {
				return values.get( method.getName() );
			}

			return method.invoke(this,args);
		}

		@Override
		public Class<? extends Annotation> annotationType() {
			return annotationType;
		}

		@Override
		public boolean equals(Object obj) {
			throw new UnsupportedOperationException("This method is not available for compile-time annotation proxies");
		}

		@Override
		public int hashCode() {
			throw new UnsupportedOperationException("This method is not available for compile-time annotation proxies");
		}

		@Override
		public String toString() {
			return AnnotationBuilder.this.toString();
		}
	}

	/**
	 * This helper class collects the components of an array of classes or an array of nested annotations.
	 *
	 */
	private class ArrayCollector extends AnnotationVisitor {

		private final String 		element;
		private final List<Object>	value;

		public ArrayCollector(String element) {
			super(Opcodes.ASM5);

			this.element	= element;
			this.value 		= new ArrayList<>();
		}
		@Override
		public void visit(String name, Object component) {
			value.add(AnnotationBuilder.this.cast(AnnotationBuilder.this.types.get(element).getComponentType(),component));
		}

		@Override
		public AnnotationVisitor visitAnnotation(String name, String desc) {
			return nestedAnnotation(types.get(element).getComponentType());
		}

		private <T extends Annotation> AnnotationBuilder<T> nestedAnnotation( Class<?> clazz) {
			@SuppressWarnings("unchecked") Class<T> annotationClass = (Class<T>) clazz;
			return new AnnotationBuilder<>(annotationClass, AnnotationBuilder.this.loader, (T proxyNested) -> this.value.add(proxyNested));
		}

		@Override
		public void visitEnd() {
			AnnotationBuilder.this.setValue(element, value.toArray(array(types.get(element).getComponentType(), value.size())));
		}

		@SuppressWarnings("unchecked")
		private final <E> E[] array(Class<E> componentType, int size) {
			return (E[]) Array.newInstance(componentType, size);
		}

	}

	/**
	 * A class loader that can create empty classes that can be used to represent class references in annotations at compile-time
	 * without loading the actual class.
	 *
	 * This allow minimal reflection capabilities on annotations.
	 *
	 * @author vega
	 *
	 */
	public static class ClassReferenceLoader extends ClassLoader {

		public ClassReferenceLoader(ClassLoader parent) {
			super(parent);
		}

		@Override
		protected Class<?> findClass(String name) throws ClassNotFoundException {
			byte[] b = loadClassData(name);
			return defineClass(name, b, 0, b.length);
		}

		private static byte[] loadClassData(String name) {
			ClassWriter writer = new ClassWriter(0);
			writer.visit(Opcodes.V1_7, Opcodes.ACC_PUBLIC, name.replace('.', '/'), null, "java/lang/Object", null);
			writer.visitEnd();
			return writer.toByteArray();
		}

	}
}
