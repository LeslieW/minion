package translator.conjureEssenceSpecification;

public class Annotation implements EssenceGlobals  {
	
	int restriction_mode;
	Symmetry symmetry;
	Channel channel;
	QAnnotation q_annotation;
	
	public Annotation(Symmetry s){
		restriction_mode = SYMMETRIE;
		symmetry = s;
	}
	
	public Annotation(Channel c){
		restriction_mode = CHANNEL;
		channel = c;
	}
	
	public Annotation(QAnnotation q){
		restriction_mode = QANNOTATION;
		q_annotation = q;
	}

	public String toString(){
		
		return "";
	}
}
