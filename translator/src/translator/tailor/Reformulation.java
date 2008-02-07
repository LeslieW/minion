package translator.tailor;

//import java.sql.Timestamp;
import translator.normaliser.NormalisedModel;

public interface Reformulation {

	/**
	 * Enhance the give normalised model by a reformulation and 
	 * return the enhanced model.
	 * 
	 * @param model
	 * @return
	 * @throws TailorException
	 */
	public NormalisedModel enhance(NormalisedModel model) throws TailorException;
	
	/**
	 * print any kind of statistics concerning the reformulation
	 */
	public String printStatistics();

	
	/**
	 * Return the amount of time used for the reformulation
	 * @return the amount of time used for the reformulation
	 */
	public long getReformulationTime();
	
}
