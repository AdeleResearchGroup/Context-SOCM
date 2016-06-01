package fr.liglab.adele.cream.ipojo.module;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

/**
 * Utility class to extract the type argument of a generic type field declaration.
 * 
 * This extractor can handle generic classes with several type parameter, but the
 * actual type arguments must be classes.
 *
 */
class TypeArgumentExtractor extends SignatureVisitor {

	private boolean 		visitingParameterizedClass 	= false;
	private boolean 		visitingTypeArgument		= false;
	private List<String> 	typeArguments 				= new ArrayList<>(); 
	

	public TypeArgumentExtractor(String signature) {
		super(Opcodes.ASM5);
		new SignatureReader(signature).acceptType(this);
	}
	
	public List<String> getTypeArguments() {
		return typeArguments;
	}

	@Override
	public SignatureVisitor visitTypeArgument(char wildcard) {
		visitingTypeArgument = true;
		return super.visitTypeArgument(wildcard);
	}
	
	@Override
	public void visitClassType(String name) {
		if (visitingTypeArgument) {
			typeArguments.add(Type.getObjectType(name).getClassName());
		}
		else {
			visitingParameterizedClass 	= true;
			visitingTypeArgument		= false;
		}
	}

	@Override
	public void visitEnd() {
		if (visitingTypeArgument) {
			visitingTypeArgument 		= false;
		}
		else if (visitingParameterizedClass) {
			visitingParameterizedClass	= false;
		}
	}
	
	
}