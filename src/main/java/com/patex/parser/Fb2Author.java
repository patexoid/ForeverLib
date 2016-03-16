package com.patex.parser;

/**
 * Created by alex on 15.03.2015.
 */
public class Fb2Author {

 private final String firstName;
    private final String middleName;
    private final String lastName;
    private final String homePage;
    private final String email;

    public Fb2Author(String firstName, String middleName, String lastName, String homePage, String email) {
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.homePage = homePage;
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getHomePage() {
        return homePage;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Fb2Author fb2Author = (Fb2Author) o;

        if (email != null ? !email.equals(fb2Author.email) : fb2Author.email != null) return false;
        if (firstName != null ? !firstName.equals(fb2Author.firstName) : fb2Author.firstName != null) return false;
        if (homePage != null ? !homePage.equals(fb2Author.homePage) : fb2Author.homePage != null) return false;
        if (lastName != null ? !lastName.equals(fb2Author.lastName) : fb2Author.lastName != null) return false;
        if (middleName != null ? !middleName.equals(fb2Author.middleName) : fb2Author.middleName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = firstName != null ? firstName.hashCode() : 0;
        result = 31 * result + (middleName != null ? middleName.hashCode() : 0);
        result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
        result = 31 * result + (homePage != null ? homePage.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        return result;
    }
}
