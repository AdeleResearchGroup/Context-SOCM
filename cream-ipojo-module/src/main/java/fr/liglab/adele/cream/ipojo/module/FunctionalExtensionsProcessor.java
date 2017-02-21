package fr.liglab.adele.cream.ipojo.module;

import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtension;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtensions;

/**
 * Created by aygalinc on 15/01/16.
 */
public class FunctionalExtensionsProcessor extends AbstractFunctionalExtensionProcessor<FunctionalExtensions> {

	public FunctionalExtensionsProcessor(ClassLoader classReferenceLoader) {
		super(FunctionalExtensions.class,classReferenceLoader);
	}

	@Override
	public void process(FunctionalExtensions annotation) {
		for (FunctionalExtension functionalExtension : annotation.value()){
			buildSubBehaviorElement(functionalExtension);
		}
	}

}
