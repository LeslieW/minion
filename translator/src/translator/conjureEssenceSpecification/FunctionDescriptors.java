package translator.conjureEssenceSpecification;

public class FunctionDescriptors implements EssenceGlobals {
	
	/**
	 * restriction_mode 1 : kind function only
	 * restriction_mode 2 : class function only
	 * restriction_mode 3 : kind and class functions
	 */
	int restriction_mode;
	KindFunction kindfunction;
	ClassFunction classfunction;
	
	public int getrestriction_mode(){
		return restriction_mode;
	}
	public void setrestriction_mode(int rm){
		restriction_mode = rm;
	}
	
	public KindFunction getkindfunction(){
		return kindfunction;
	}
	public ClassFunction getclassfunction(){
		return classfunction;
	}
	
	public void setkindfunction(KindFunction kf){
		kindfunction=kf;
	}
	public void setclassfunction(ClassFunction cf){
		classfunction=cf;
	}
	
	public FunctionDescriptors(KindFunction kf){
		restriction_mode = KIND_FUNCTION;
		kindfunction = kf;
	}
	public FunctionDescriptors(ClassFunction cf){
		restriction_mode = CLASS_FUNCTION;
		classfunction = cf;
	}
	public FunctionDescriptors(KindFunction kf, ClassFunction cf){
		restriction_mode = KIND_CLASS_FUNCTION;
		kindfunction = kf;
		classfunction = cf;
	}

	public String toString(){
		
		switch(restriction_mode){
		case KIND_FUNCTION : return kindfunction.toString();
		case CLASS_FUNCTION : return classfunction.toString();
		case KIND_CLASS_FUNCTION : return kindfunction.toString() + classfunction.toString();
		}		
		return "";
	}
}
