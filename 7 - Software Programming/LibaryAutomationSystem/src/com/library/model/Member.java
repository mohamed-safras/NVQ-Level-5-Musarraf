package com.library.model;

import java.time.LocalDate;

/**
 * Represents a row of the Members table (see Database Systems unit schema).
 */
public class Member {
    private int memberId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate membershipDate;
    private MembershipStatus status;

    public Member(int memberId, String firstName, String lastName, String email,
                  String phone, LocalDate membershipDate, MembershipStatus status) {
        this.memberId = memberId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.membershipDate = membershipDate;
        this.status = status;
    }

    public int getMemberId() { return memberId; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public LocalDate getMembershipDate() { return membershipDate; }
    public MembershipStatus getStatus() { return status; }
    public void setStatus(MembershipStatus status) { this.status = status; }

    public String getFullName() { return firstName + " " + lastName; }

    @Override
    public String toString() {
        return String.format("[%d] %-20s %-25s %-12s %-10s",
                memberId, getFullName(), email, phone, status);
    }
}
