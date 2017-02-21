package fr.liglab.adele.cream.ipojo.module;

import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtension;

/**
 * Created by aygalinc on 15/01/16.
 */
public class FunctionalExtensionProcessor extends AbstractFunctionalExtensionProcessor<FunctionalExtension> {


	public FunctionalExtensionProcessor(ClassLoader classReferenceLoader) {
		super(FunctionalExtension.class,classReferenceLoader);
	}

	@Override
	public void process(FunctionalExtension annotation) {
		buildSubBehaviorElement(annotation);
	}

}
