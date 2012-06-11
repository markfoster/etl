//package org.cqc;
// Generated 24-May-2012 12:01:54 by Hibernate Tools 3.2.2.GA


import java.util.Date;

/**
 * LocationRegulatedActivity generated by hbm2java
 */
public class Location_Regulated_Activity implements java.io.Serializable {

     private String providerId;
     private String locationId;
     private String regulatedActivityNumber;
     private Character actionCode;
     private Date lastUpdated;

    public Location_Regulated_Activity() {
    }
	
    public Location_Regulated_Activity(String providerId, String locationId, String regulatedActivity) {
       this.providerId = providerId;
       this.locationId = locationId;
       this.regulatedActivityNumber = regulatedActivityNumber;
    }

    public Location_Regulated_Activity(String providerId, String locationId, String regulatedActivity, Character actionCode, Date lastUpdated) {
       this.providerId = providerId;
       this.locationId = locationId;
       this.regulatedActivityNumber = regulatedActivityNumber;
       this.actionCode = actionCode;
       this.lastUpdated = lastUpdated;
    }
  
    public String getProviderId() {
        return this.providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getLocationId() {
        return this.locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getRegulatedActivityNumber() {
        return this.regulatedActivityNumber;
    }

    public void setRegulatedActivityNumber(String regulatedActivityNumber) {
        this.regulatedActivityNumber = regulatedActivityNumber;
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

