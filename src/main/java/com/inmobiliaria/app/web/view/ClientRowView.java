package com.inmobiliaria.app.web.view;

import com.inmobiliaria.app.domain.ClientType;
import com.inmobiliaria.app.domain.ContactChannel;
import com.inmobiliaria.app.domain.InterestStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ClientRowView {

    private Long clientId;
    private ClientType clientType;
    private String fullName;
    private String generalNotes;

    private LocalDate lastContactDate;
    private String solviaCode;
    private ContactChannel channel;

    private InterestStatus status; // <-- NUEVO

    private List<String> phones = new ArrayList<>();
    private List<String> emails = new ArrayList<>();

    private String lastPropertyType;
    private String lastPropertyCode;
    private String lastPropertyAddress;
    private String lastPropertyMunicipality;

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public ClientType getClientType() { return clientType; }
    public void setClientType(ClientType clientType) { this.clientType = clientType; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getGeneralNotes() { return generalNotes; }
    public void setGeneralNotes(String generalNotes) { this.generalNotes = generalNotes; }

    public LocalDate getLastContactDate() { return lastContactDate; }
    public void setLastContactDate(LocalDate lastContactDate) { this.lastContactDate = lastContactDate; }

    public String getSolviaCode() { return solviaCode; }
    public void setSolviaCode(String solviaCode) { this.solviaCode = solviaCode; }

    public ContactChannel getChannel() { return channel; }
    public void setChannel(ContactChannel channel) { this.channel = channel; }

    public InterestStatus getStatus() { return status; }                 // <-- NUEVO
    public void setStatus(InterestStatus status) { this.status = status; } // <-- NUEVO

    public List<String> getPhones() { return phones; }
    public void setPhones(List<String> phones) { this.phones = (phones == null) ? new ArrayList<>() : phones; }

    public List<String> getEmails() { return emails; }
    public void setEmails(List<String> emails) { this.emails = (emails == null) ? new ArrayList<>() : emails; }

    public String getLastPropertyType() { return lastPropertyType; }
    public void setLastPropertyType(String lastPropertyType) { this.lastPropertyType = lastPropertyType; }

    public String getLastPropertyCode() { return lastPropertyCode; }
    public void setLastPropertyCode(String lastPropertyCode) { this.lastPropertyCode = lastPropertyCode; }

    public String getLastPropertyAddress() { return lastPropertyAddress; }
    public void setLastPropertyAddress(String lastPropertyAddress) { this.lastPropertyAddress = lastPropertyAddress; }

    public String getLastPropertyMunicipality() { return lastPropertyMunicipality; }
    public void setLastPropertyMunicipality(String lastPropertyMunicipality) { this.lastPropertyMunicipality = lastPropertyMunicipality; }
}
