package csironi.ggp.course.experiments.tournaments;

import java.util.List;

public class ExternalGamerAvailabilityManager {

	/**
	 * Pairs (host-port) on which the external gamer associated with this class is active and
	 * currently free (i.e. not playing any game).
	 */
	private List<String> freeAddresses;

	/**
	 *
	 * @param addresses All pairs (host-port) on which the external gamer associated with this class is active.
	 */
	public ExternalGamerAvailabilityManager(List<String> addresses) {

		this.freeAddresses = addresses;

	}

	public synchronized String getFreeAddress(){

		if(this.freeAddresses.size() == 0){
			return null;
		}

		return this.freeAddresses.remove(0);

	}

	public synchronized void freeAddressInUse(String toFree){

		this.freeAddresses.add(toFree);

	}

}
