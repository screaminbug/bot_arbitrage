package co.codingnomads.bot.arbitrage.action.arbitrage;

import co.codingnomads.bot.arbitrage.action.arbitrage.selection.ArbitrageActionSelection;
import co.codingnomads.bot.arbitrage.model.ticker.TickerData;
import co.codingnomads.bot.arbitrage.model.email.Email;
import co.codingnomads.bot.arbitrage.exception.EmailLimitException;
import co.codingnomads.bot.arbitrage.service.email.EmailService;
import co.codingnomads.bot.arbitrage.service.general.MarginDiffCompare;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

/**
 * @author Kevin Neag
 */

/**
 * Email class than extends ArbitrageActionSelection class
 * Class contains and autowired EmailService objects and a new Email object
 * Class also has an email method witch uses the Amazon AWS SES API, in order to use
 * this api you must create an aws account at aws.amazon.com, very your email address
 * at SES services, and also obtain credentials to be saved in a .aws directory.
 *
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@Component("email")
@Scope(scopeName = "websocket", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ArbitrageEmailAction extends ArbitrageActionSelection {

    @Autowired
    private EmailService emailService;

    private Email email;

    public Email getEmail() {
        return email;
    }

    public void setEmail(Email email) {
        this.email = email;
    }

    /**
     * Email method that checks to see if the email is under the email rate limit before sending.
     * the method uses an email being taken in to call the methods buildHTMLBody, buildTEXTBody and setSubject method to
     * set custom messages based on the highbid, lowask and difference. Then sends the email using the Amazon SES api.
     * @param lowAsk    the lowest ask found (buy)
     * @param highBid   the highest bid found (sell)
     * @throws EmailLimitException
     */
    public void email(TickerData lowAsk, TickerData highBid) throws EmailLimitException {


        if (!emailService.underEmailLimit()) {
            throw new EmailLimitException("Email limit reached for the day, please try again tomorrow");
        }
        email.buildHTMLBody(lowAsk, highBid, getArbitrageMargin());
        email.buildTextBody(lowAsk, highBid, getArbitrageMargin());
        email.setSUBJECT("Arbitrage Update");
        try {

            AmazonSimpleEmailService client =
                    AmazonSimpleEmailServiceClientBuilder.standard()
                            // Replace US_EAST_1 with the AWS Region you're using for
                            // Amazon SES.
                            .withRegion(Regions.US_EAST_1).build();
            SendEmailRequest request = new SendEmailRequest()
                    .withDestination(
                            new com.amazonaws.services.simpleemail.model.Destination().withToAddresses(email.getTO()))
                    .withMessage(new Message()
                            .withBody(new Body()
                                    .withHtml(new Content()
                                            .withCharset("UTF-8").withData(email.HTMLBODY))
                                    .withText(new Content()
                                            .withCharset("UTF-8").withData(email.TEXTBODY)))
                            .withSubject(new Content()
                                    .withCharset("UTF-8").withData(email.getSUBJECT())))
                    .withSource(email.getFROM());
            client.sendEmail(request);
            System.out.println("=======================================================================================");
            System.out.println("=======================================================================================");
            System.out.println();
            System.out.println("Email sent!");
            System.out.println();
            System.out.println("=======================================================================================");
            System.out.println("=======================================================================================");
        } catch (Exception ex) {
            System.out.println("=======================================================================================");
            System.out.println("=======================================================================================");
            System.out.println();
            System.out.println("The email was not sent. Error message: "
                    + ex.getMessage());
            System.out.println("=======================================================================================");
            System.out.println("=======================================================================================");
            System.out.println();
        }
    }

}
