package com.bidly.validator;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;
import java.math.BigDecimal;

@FacesValidator("minBidValidator")
public class MinBidValidator implements Validator<BigDecimal> {

    @Override
    public void validate(FacesContext context, UIComponent component, BigDecimal value)
            throws ValidatorException {
        if (value == null) {
            return;
        }

        Object attr = component.getAttributes().get("minimumBid");
        if (attr == null) {
            return;
        }

        BigDecimal min;
        if (attr instanceof BigDecimal) {
            min = (BigDecimal) attr;
        } else {
            min = new BigDecimal(attr.toString());
        }

        if (value.compareTo(min) < 0) {
            throw new ValidatorException(new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Bid must be at least $" + min.toPlainString(),
                    "The minimum allowed bid is $" + min.toPlainString() + "."));
        }
    }
}

