package co.codingnomads.bot.arbitrage.exchange;

import org.knowm.xchange.ExchangeSpecification;


/**
 * Created by Thomas Leruth on 12/16/17
 *
 * Abstract class to be implemeted for all set up exchanged (needed for Auth params)
 * The implementation should have an empty constructor (for a version without Auth params) and one fully loaded
 */
public abstract class ExchangeSpecs {

    private String apiKey;
    private String secretKey;
    private Boolean tradingMode = false;
    private Boolean isSimulated = false;


    protected ExchangeSpecs(String apiKey, String secretKey) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;
    }

    protected ExchangeSpecs(boolean isSimulation) {
        isSimulated = isSimulation;
    }

    protected ExchangeSpecs() {
    }

    protected String getApiKey() {
        return apiKey;
    }

    protected String getSecretKey() {
        return secretKey;
    }

    public Boolean getTradingMode() {
        return tradingMode;
    }

    protected void setTradingMode(Boolean tradingMode) {
        this.tradingMode = tradingMode;
    }

    public abstract ExchangeSpecification getSetupExchange();

    public boolean getSimulatedMode() {
        return isSimulated;
    }
}
