//package org.cqc;
// Generated 24-May-2012 12:01:54 by Hibernate Tools 3.2.2.GA


import java.util.Date;

/**
 * Partner generated by hbm2java
 */
public class Partner  implements java.io.Serializable {

     private String providerId;
     private String partnerId;
     private String name;
     private Character actionCode;
     private Date lastUpdated;

    public Partner() {
    }
	
    public Partner(String providerId, String partnerId) {
       this.providerId = providerId;
       this.partnerId = partnerId;
    }

    public Partner(String providerId, String partnerId, String name, Character actionCode, Date lastUpdated) {
       this.providerId = providerId;
       this.partnerId = partnerId;
       this.name = name;
       this.actionCode = actionCode;
       this.lastUpdated = lastUpdated;
    }

    public String getProviderId() {
        return this.providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getPartnerId() {
        return this.partnerId;
    }

    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
    }
   
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    public Character getActionCode() {
        return this.actionCode;
    }
    
    public void setActionCode(Character actionCode) {
        this.actionCode = actionCode;
    }
    public Date getLastUpdated() {
        return this.lastUpdated;
    }
    
    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }




}

