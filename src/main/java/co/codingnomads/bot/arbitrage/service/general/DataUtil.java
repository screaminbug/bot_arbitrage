package co.codingnomads.bot.arbitrage.service.general;

import co.codingnomads.bot.arbitrage.model.ticker.TickerData;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Thomas Leruth on 12/12/17
 *
 * Class containing methods to work on data
 */

@Service
public class DataUtil {

    /**
     * Find the item within a list with the lowest ask (best buy)
     * @param list a list of TickerData for different exchanges
     * @return the item with the lowest Ask
     */
    public TickerData lowAskFinder(List<TickerData> list) {
        int lowIndex = 0;
        if (list.size() > 1) {
            for (int i = 1; i < list.size(); i++) {
                if (list.get(lowIndex).getAsk().compareTo(list.get(i).getAsk()) > 0) {
                    lowIndex = i;
                }
            }
        }
        return list.get(lowIndex);
    }

    /**
     * Find the item within a list with the highest bid (best sell)
     * @param list a list of TickerData for different exchanges
     * @return the item with highest Ask
     */
    public TickerData highBidFinder(List<TickerData> list) {
        int highIndex = 0;
        if (list.size() > 1) {
            for (int i = 1; i < list.size(); i++) {
                if (list.get(highIndex).getBid().compareTo(list.get(i).getBid()) < 0) {
                    highIndex = i;
                }
            }
        }
        return list.get(highIndex);
    }
}
