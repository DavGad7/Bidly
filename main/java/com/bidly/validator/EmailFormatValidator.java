package com.bidly.validator;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;
import java.util.regex.Pattern;

@FacesValidator("emailValidator")
public class EmailFormatValidator implements Validator<String> {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @Override
    public void validate(FacesContext context, UIComponent component, String value)
            throws ValidatorException {
        if (value == null || value.isBlank()) {
            return;
        }

        if (!EMAIL_PATTERN.matcher(value.trim()).matches()) {
            throw new ValidatorException(new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Invalid email format",
                    "Please enter a valid email address (e.g. user@example.com)."));
        }
    }
}

