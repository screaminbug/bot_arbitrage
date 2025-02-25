package co.codingnomads.bot.arbitrage.exchange;

import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.bittrex.BittrexExchange;

/**
 * Created by Thomas Leruth on 12/16/17
 *
 * Extension of the specs for Bittrex
 */
public class BittrexSpecs extends ExchangeSpecs  {

    public BittrexSpecs(String apiKey, String secretKey) {
        super(apiKey, secretKey);
        if (null != apiKey && null != secretKey) {
            setTradingMode(true);
        }
    }

    public BittrexSpecs() {
        super();
    }

    public BittrexSpecs(boolean isSimulation) {
        super(isSimulation);
    }

    @Override
    public ExchangeSpecification getSetupExchange() {
        ExchangeSpecification exSpec = new BittrexExchange().getDefaultExchangeSpecification();
        if (super.getTradingMode()) {
            exSpec.setApiKey(super.getApiKey());
            exSpec.setSecretKey(super.getSecretKey());
        }
        return exSpec;
    }
}