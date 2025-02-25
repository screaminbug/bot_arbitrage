package co.codingnomads.bot.arbitrage.exchange;

import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.bitfinex.v1.BitfinexExchange;

/**
 * Created by Thomas Leruth on 12/16/17
 *
 * Extension of the specs for Bitfinex
 */
public class BitfinexSpecs extends ExchangeSpecs {

    public BitfinexSpecs(String apiKey, String secretKey) {
        super(apiKey, secretKey);
        if (null != apiKey && null != secretKey) {
            setTradingMode(true);
        }
    }

    public BitfinexSpecs() {
        super();
    }

    public BitfinexSpecs(boolean isSimulation) {
        super(isSimulation);
    }

    @Override
    public ExchangeSpecification getSetupExchange() {
        ExchangeSpecification exSpec = new BitfinexExchange().getDefaultExchangeSpecification();
        if (super.getTradingMode()) {
            exSpec.setApiKey(super.getApiKey());
            exSpec.setSecretKey(super.getSecretKey());
        }
        return exSpec;
    }

}
