package fr.liglab.adele.cream.ipojo.module.test;

import org.apache.commons.lang3.AnnotationUtils;

import java.lang.annotation.Annotation;

/**
 * Created by aygalinc on 29/08/16.
 */
public abstract class AbstractAnnotationTest implements Annotation {
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Annotation){
            return AnnotationUtils.equals(this,(Annotation) obj);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return AnnotationUtils.hashCode(this);
    }

    @Override
    public String toString() {
        return AnnotationUtils.toString(this);
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return this.getClass();
    }
}
