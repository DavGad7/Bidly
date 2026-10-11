package com.bidly.validator;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;

@FacesValidator("passwordStrengthValidator")
public class PasswordStrengthValidator implements Validator<String> {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 100;

    @Override
    public void validate(FacesContext context, UIComponent component, String value)
            throws ValidatorException {
        if (value == null || value.isBlank()) {
            return;
        }

        if (value.length() < MIN_LENGTH) {
            throw new ValidatorException(new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Password too short",
                    "Password must be at least " + MIN_LENGTH + " characters long."));
        }

        if (value.length() > MAX_LENGTH) {
            throw new ValidatorException(new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Password too long",
                    "Password must not exceed " + MAX_LENGTH + " characters."));
        }

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;

        for (char c : value.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUpper = true;
            } else if (Character.isLowerCase(c)) {
                hasLower = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }

        if (!hasUpper || !hasLower || !hasDigit) {
            throw new ValidatorException(new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Password too weak",
                    "Password must contain at least one uppercase letter, " +
                    "one lowercase letter, and one digit."));
        }
    }
}

