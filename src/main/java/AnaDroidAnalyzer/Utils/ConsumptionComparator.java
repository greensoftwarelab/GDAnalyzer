package AnaDroidAnalyzer.Utils;

import java.util.Comparator;

/**
 * Created by rrua on 02/05/17.
 */
public class ConsumptionComparator implements Comparator<Consumption> {

    @Override
    public int compare(Consumption consumption, Consumption t1) {

        return Integer.compare(consumption.applicationState.first,t1.applicationState.first);
    }

}
