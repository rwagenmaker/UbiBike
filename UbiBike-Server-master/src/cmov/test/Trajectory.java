package cmov.test;

import java.util.ArrayList;
import java.util.List;

public class Trajectory {

	private List<String> listLatitudes;
	private List<String> listLongitudes;
	
	public Trajectory(List<String> listLatitudes, List<String> listLongitudes){
		this.listLatitudes = listLatitudes;
		this.listLongitudes = listLongitudes;
	}
	
	public List<String> getLatitudes(){
		return this.listLatitudes;
	}
	
	public List<String> getLongitudes(){
		return this.listLongitudes;
	}
}
